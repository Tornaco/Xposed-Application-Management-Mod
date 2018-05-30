package github.tornaco.xposedmoduletest.xposed.service.pm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import github.tornaco.xposedmoduletest.compat.os.XAppOpsManager;
import github.tornaco.xposedmoduletest.util.OSUtil;
import github.tornaco.xposedmoduletest.util.Singleton1;
import github.tornaco.xposedmoduletest.xposed.repo.RepoProxy;
import github.tornaco.xposedmoduletest.xposed.service.AppResource;
import github.tornaco.xposedmoduletest.xposed.service.ErrorCatchRunnable;
import github.tornaco.xposedmoduletest.xposed.service.PMRuleCheck;
import github.tornaco.xposedmoduletest.xposed.service.notification.UniqueIdFactory;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Created by Tornaco on 2018/5/8 11:24.
 * God bless no bug!
 */
public class PackageInstallerManager {

    private static final String NOTIFICATION_CHANNEL_ID_PM = "dev.tornaco.notification.channel.id.X-APM-PM";

    // Make it longer for dev.
    static final long PACKAGE_INSTALL_VERIFY_TIMEOUT_MILLS = (24 * 1000);
    static final long PACKAGE_INSTALL_VERIFY_TIMEOUT_S = PACKAGE_INSTALL_VERIFY_TIMEOUT_MILLS / 1000;

    @Getter
    private Context context;

    private final Map<String, String> SOURCE_APK_PATH_MAP = new HashMap<>();

    private PackageInstallerManager(Context context) {
        this.context = context;
    }

    private static final Singleton1<PackageInstallerManager, Context>
            sMe = new Singleton1<PackageInstallerManager, Context>() {

        @Override
        protected PackageInstallerManager create(Context context) {
            return new PackageInstallerManager(context);
        }
    };

    public static PackageInstallerManager from(Context context) {
        return sMe.get(context);
    }

    private static boolean isPackageVerifyIntent(Intent intent) {
        return intent != null && Intent.ACTION_PACKAGE_NEEDS_VERIFICATION.equals(intent.getAction());
    }

    // Assets/package_verify_intent
    public boolean checkPackageInstallVerifyIntent(Intent intent) {
        return true;
    }

    public void onSourceApkFileDetected(String path, String apkPackageName) {
        XposedLog.verbose(XposedLog.PREFIX_PM + "onSourceApkFileDetected: " + path + ", pkg: " + apkPackageName);
        if (path != null && apkPackageName != null) synchronized (SOURCE_APK_PATH_MAP) {
            SOURCE_APK_PATH_MAP.put(apkPackageName, path);
        }
    }

    // Nullable.
    public String getSourceApkFilePath(String apkPackageName) {
        synchronized (SOURCE_APK_PATH_MAP) {
            return SOURCE_APK_PATH_MAP.get(apkPackageName);
        }
    }

    // Note. This will block the calling thread.
    public void verifyIncomingInstallRequest(VerifyArgs args, VerifyReceiver receiver, Handler uiHandler) {
        Thread t = Thread.currentThread();
        XposedLog.verbose(XposedLog.PREFIX_PM + "verifyIncomingInstallRequest@thread: " + t);

        // First, check rule.
        @PMRuleCheck
        boolean alwaysAllow = RepoProxy.getProxy().getPm_rules().has(constructAlwaysAllowRules(args.installerPackageName));
        if (alwaysAllow) {
            XposedLog.verbose(XposedLog.PREFIX_PM + "always allow: " + args);
            receiver.onVerifyResult("Rule allow", XAppOpsManager.MODE_ALLOWED);
            return;
        }

        @PMRuleCheck
        boolean alwaysDeny = RepoProxy.getProxy().getPm_rules().has(constructAlwaysDenyRules(args.installerPackageName));
        if (alwaysDeny) {
            XposedLog.verbose(XposedLog.PREFIX_PM + "always deny: " + args);
            receiver.onVerifyResult("Rule deny", XAppOpsManager.MODE_IGNORED);
            showInstallDenyNotification(args);
            return;
        }

        @PMRuleCheck
        boolean alwaysAsk = RepoProxy.getProxy().getPm_rules().has(constructAlwaysAskRules(args.installerPackageName));
        if (alwaysAsk) {
            XposedLog.verbose(XposedLog.PREFIX_PM + "always ask: " + args);
            doAskUser(args, receiver, uiHandler);
            return;
        }

        // Default, allow.
        XposedLog.verbose(XposedLog.PREFIX_PM + "default allow: " + args);
        receiver.onVerifyResult("Default allow", XAppOpsManager.MODE_ALLOWED);
    }

    private void doAskUser(VerifyArgs args, VerifyReceiver receiver, Handler uiHandler) {
        InstallDialogResultWaiter waiter = new InstallDialogResultWaiter();
        VerifyReceiver receiverProxy = (reason, mode) -> {
            waiter.done();
            receiver.onVerifyResult(reason, mode);
            XposedLog.verbose(XposedLog.PREFIX_PM + "VerifyReceiver@onVerifyResult: " + mode + ", reason: " + reason);
        };

        InstallDialog dialog = new InstallDialog(args, receiverProxy);
        Context finalDContext = getContext();
        uiHandler.post(() -> {
            try {
                dialog.display(finalDContext);
            } catch (Throwable e) {
                // Serious err!
                XposedLog.wtf(XposedLog.PREFIX_PM + "Fail show dialog! " + Log.getStackTraceString(e));
                receiverProxy.onVerifyResult("Fail show dialog", XAppOpsManager.MODE_ALLOWED);
            }
        });

        XposedLog.verbose(XposedLog.PREFIX_PM + "call dialog.display: " + dialog);
        boolean res = waiter.waitForResult();
        XposedLog.verbose(XposedLog.PREFIX_PM + "waiter.waitForResult return: " + res);
        if (!res) {
            // Timeout waiting for res, ignore install request.
            receiver.onVerifyResult("Time out", XAppOpsManager.MODE_IGNORED);
            dialog.dismiss();
            onInstallVerifyTimeout(args);
        }
    }

    private static String[] constructAlwaysAllowRules(String installer) {
        return new String[]{String.format("ALWAYS ALLOW %s", installer),
                "ALWAYS ALLOW *"};
    }

    private static String[] constructAlwaysDenyRules(String installer) {
        return new String[]{String.format("ALWAYS DENY %s", installer),
                "ALWAYS DENY *"};
    }

    private static String[] constructAlwaysAskRules(String installer) {
        return new String[]{String.format("ALWAYS ASK %s", installer),
                "ALWAYS ASK *"};
    }

    private void onInstallVerifyTimeout(VerifyArgs args) {
        // Tell user about this.
        new ErrorCatchRunnable(() -> showInstallVerifyTimeoutNotification(args), "showInstallVerifyTimeoutNotification").run();
    }

    private void showInstallVerifyTimeoutNotification(VerifyArgs args) {
        if (XposedLog.isVerboseLoggable()) {
            XposedLog.verbose("showInstallVerifyTimeoutNotification");
        }

        createPMNotificationChannelForO();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(),
                NOTIFICATION_CHANNEL_ID_PM);

        AppResource appResource = new AppResource(context);
        String dialogTitle = appResource.loadStringFromAPMApp("package_install_verify_timeout_notification_title");
        String dialogMessage = appResource.loadStringFromAPMApp("package_install_verify_timeout_notification_message",
                args.getInstallerAppLabel(), args.getAppLabel());

        android.support.v4.app.NotificationCompat.BigTextStyle style =
                new android.support.v4.app.NotificationCompat.BigTextStyle();
        style.bigText(dialogMessage);
        style.setBigContentTitle(dialogTitle);

        Notification n = builder
                .setContentTitle(dialogTitle)
                .setContentText(dialogMessage)
                .setStyle(style)
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .setAutoCancel(true)
                .build();

        if (OSUtil.isMOrAbove()) {
            n.setSmallIcon(new AppResource(getContext()).loadIconFromAPMApp("ic_block_black_24dp"));
        }

        NotificationManagerCompat.from(getContext())
                .notify(UniqueIdFactory.getNextId(), n);
    }

    private void showInstallDenyNotification(VerifyArgs args) {
        if (XposedLog.isVerboseLoggable()) {
            XposedLog.verbose("showInstallDenyNotification");
        }

        createPMNotificationChannelForO();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(),
                NOTIFICATION_CHANNEL_ID_PM);

        AppResource appResource = new AppResource(context);
        String dialogTitle = appResource.loadStringFromAPMApp("package_install_verify_deny_notification_title");
        String dialogMessage = appResource.loadStringFromAPMApp("package_install_verify_deny_notification_message",
                args.getInstallerAppLabel(), args.getAppLabel());

        android.support.v4.app.NotificationCompat.BigTextStyle style =
                new android.support.v4.app.NotificationCompat.BigTextStyle();
        style.bigText(dialogMessage);
        style.setBigContentTitle(dialogTitle);

        Notification n = builder
                .setContentTitle(dialogTitle)
                .setContentText(dialogMessage)
                .setStyle(style)
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .setAutoCancel(true)
                .build();

        if (OSUtil.isMOrAbove()) {
            n.setSmallIcon(new AppResource(getContext()).loadIconFromAPMApp("ic_block_black_24dp"));
        }

        NotificationManagerCompat.from(getContext())
                .notify(UniqueIdFactory.getNextId(), n);
    }

    private void createPMNotificationChannelForO() {
        if (OSUtil.isOOrAbove()) {
            NotificationManager notificationManager = (NotificationManager)
                    getContext().getSystemService(
                            Context.NOTIFICATION_SERVICE);
            NotificationChannel nc = null;
            if (notificationManager != null) {
                nc = notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID_PM);
            }
            if (nc != null) {
                return;
            }
            NotificationChannel notificationChannel;
            notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID_PM,
                    new AppResource(getContext()).loadStringFromAPMApp("notification_channel_name_package_verifier"),
                    NotificationManager.IMPORTANCE_LOW);
            notificationChannel.enableLights(false);
            notificationChannel.enableVibration(false);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
    }

    private class InstallDialogResultWaiter {
        private CountDownLatch latch = new CountDownLatch(1);

        // True for timeout.
        boolean waitForResult() {
            try {
                return latch.await(PACKAGE_INSTALL_VERIFY_TIMEOUT_S, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {
                return false;
            }
        }

        void done() {
            while (latch.getCount() > 0) {
                latch.countDown();
            }
        }
    }

    @Builder
    @Getter
    @ToString
    public static class VerifyArgs {
        private String packageName;
        private String installerPackageName;
        private String installerAppLabel;
        private String appLabel;
        private String soucrePath;
        private Drawable appIcon;
        private int verificationId;
        private int installFlags;
        private int versionCode;
        private int installerUid;
        private int originatingUid;
        private boolean isReplacing;
        private Uri data;
    }

    public interface VerifyReceiver {
        /**
         * @see XAppOpsManager#MODE_ALLOWED
         * @see XAppOpsManager#MODE_IGNORED
         */
        void onVerifyResult(String reason, int mode);
    }
}
