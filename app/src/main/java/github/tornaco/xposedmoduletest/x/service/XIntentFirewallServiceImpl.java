package github.tornaco.xposedmoduletest.x.service;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.os.ServiceManager;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import github.tornaco.android.common.Collections;
import github.tornaco.android.common.Consumer;
import github.tornaco.xposedmoduletest.x.app.XIntentFirewallManager;
import github.tornaco.xposedmoduletest.x.util.PkgUtil;
import github.tornaco.xposedmoduletest.x.util.XLog;
import lombok.AllArgsConstructor;

import static android.content.Context.INPUT_METHOD_SERVICE;

/**
 * Created by guohao4 on 2017/11/9.
 * Email: Tornaco@163.com
 */

public class XIntentFirewallServiceImpl extends XIntentFirewallServiceAbs {

    private static final Set<String> WHITE_LIST = new HashSet<>();

    static {
        WHITE_LIST.add("android");
        WHITE_LIST.add("github.tornaco.xposedmoduletest");
        WHITE_LIST.add("com.android.systemui");
        WHITE_LIST.add("com.android.packageinstaller");
        WHITE_LIST.add("eu.chainfire.supersu");
        WHITE_LIST.add("com.lenovo.launcher");
        WHITE_LIST.add("com.android.settings");
        WHITE_LIST.add("com.cyanogenmod.trebuchet");
        WHITE_LIST.add("de.robv.android.xposed.installer");
        WHITE_LIST.add("android.providers.telephony");
    }

    private UUID mSerialUUID = UUID.randomUUID();

    private final ExecutorService mWorkingService = Executors.newCachedThreadPool();

    private final SparseArray<String> mPackagesCache = new SparseArray<>();

    private Handler mFirewallHandler;

    private BroadcastReceiver mScreenReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                        onScreenOff();
                    }

                    if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
                        onUserPresent();
                    }
                }
            };

    private void onUserPresent() {

    }

    private void onScreenOff() {
        mFirewallHandler.sendEmptyMessage(IntentFirewallHandlerMessages.MSG_CLEARPROCESSONSCREENOFF);
    }

    private BroadcastReceiver mPackageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (action == null || intent.getData() == null) {
                // They send us bad action~
                return;
            }

            switch (action) {
                case Intent.ACTION_PACKAGE_ADDED:
                case Intent.ACTION_PACKAGE_REPLACED:
                    String packageName = intent.getData().getSchemeSpecificPart();
                    if (packageName == null) return;
                    parsePackageAsync(packageName);
                    break;
            }
        }
    };

    private void parsePackageAsync(final String... pkg) {
        mWorkingService.execute(new Runnable() {
            @Override
            public void run() {
                cachePackages(pkg);
            }
        });
    }

    private void cachePackages(final String... pkg) {
        final PackageManager pm = getContext().getPackageManager();

        Collections.consumeRemaining(pkg, new Consumer<String>() {
            @Override
            public void accept(String s) {
                ApplicationInfo applicationInfo;
                try {
                    applicationInfo = pm.getApplicationInfo(s, 0);
                    int uid = applicationInfo.uid;
                    String pkg = applicationInfo.packageName;
                    if (TextUtils.isEmpty(pkg)) return;
                    XLog.logV("Cached pkg:" + pkg + "-" + uid);
                    mPackagesCache.put(uid, pkg);
                    if (isIME(pkg)) {
                        addToWhiteList(pkg);
                    }
                } catch (Exception ignored) {

                }
            }
        });
    }

    private void cachePackages() {
        PackageManager pm = this.getContext().getPackageManager();
        try {
            // Filter all apps.
            List<ApplicationInfo> applicationInfos = pm.getInstalledApplications(0);
            Collections.consumeRemaining(applicationInfos, new Consumer<ApplicationInfo>() {
                @Override
                public void accept(ApplicationInfo applicationInfo) {
                    String pkg = applicationInfo.packageName;
                    int uid = applicationInfo.uid;
                    if (TextUtils.isEmpty(pkg)) return;
                    boolean isSystemApp = (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
                    if (isSystemApp) {
                        addToWhiteList(pkg);
                    }
                    XLog.logV("Cached pkg:" + pkg + "-" + uid + "-" + isSystemApp);
                    mPackagesCache.put(uid, pkg);
                }
            });
        } catch (Exception ignored) {
            XLog.logD("Can not get UID for our client:" + ignored);
        }
    }

    private void whiteIMEPackages() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(INPUT_METHOD_SERVICE);
        List<InputMethodInfo> methodInfos = imm != null ? imm.getInputMethodList() : null;
        if (methodInfos == null) return;
        for (InputMethodInfo inputMethodInfo : methodInfos) {
            String pkg = inputMethodInfo.getPackageName();
            addToWhiteList(pkg);
            XLog.logV("whiteIMEPackages: " + pkg);
        }
    }

    private boolean isIME(String pkg) {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(INPUT_METHOD_SERVICE);
        List<InputMethodInfo> methodInfos = imm != null ? imm.getInputMethodList() : null;
        if (methodInfos == null) return false;
        for (InputMethodInfo inputMethodInfo : methodInfos) {
            String pkgIME = inputMethodInfo.getPackageName();
            if (pkg.equals(pkgIME)) return true;
        }
        return false;
    }

    private static boolean isInWhiteList(String pkg) {
        return WHITE_LIST.contains(pkg);
    }

    private synchronized static void addToWhiteList(String pkg) {
        if (!WHITE_LIST.contains(pkg)) {
            WHITE_LIST.add(pkg);
        }
    }

    @Override
    public boolean checkService(String servicePkgName, int callerUid) {
        if (TextUtils.isEmpty(servicePkgName)) return true;
        boolean res =
                isInWhiteList(servicePkgName)
                        || PkgUtil.isSystemApp(getContext(), servicePkgName)
                        || PkgUtil.isAppRunning(getContext(), servicePkgName);

        if (!res) {
            String callerPkgName =
                    mPackagesCache.get(callerUid);
            if (callerPkgName == null) {
                callerPkgName = PkgUtil.pkgForUid(getContext(), callerUid);
            }
            XLog.logVOnExecutor(String.format("BROADCAST: %s--->%s  %s--->%s %s",
                    PkgUtil.loadNameByPkgName(getContext(), callerPkgName),
                    PkgUtil.loadNameByPkgName(getContext(), servicePkgName),
                    callerPkgName,
                    servicePkgName,
                    "拒绝×"),
                    mWorkingService);
        }
        return res;
    }

    @Override
    public boolean checkBroadcast(int receiverUid, int callerUid) {
        String receiverPkgName =
                mPackagesCache.get(receiverUid);
        if (receiverPkgName == null) {
            PkgUtil.pkgForUid(getContext(), receiverUid);
        }
        if (TextUtils.isEmpty(receiverPkgName)) return true;

        boolean res =
                isInWhiteList(receiverPkgName)
                        || PkgUtil.isSystemApp(getContext(), receiverPkgName)
                        || PkgUtil.isAppRunning(getContext(), receiverPkgName);

        if (!res) {
            String callerPkgName =
                    mPackagesCache.get(callerUid);
            if (callerPkgName == null) {
                callerPkgName = PkgUtil.pkgForUid(getContext(), callerUid);
            }
            XLog.logVOnExecutor(String.format("SERVICE: %s--->%s  %s--->%s %s",
                    PkgUtil.loadNameByPkgName(getContext(), callerPkgName),
                    PkgUtil.loadNameByPkgName(getContext(), receiverPkgName),
                    callerPkgName,
                    receiverPkgName,
                    "拒绝×"),
                    mWorkingService);
        }
        return res;
    }

    private void registerReceiver() {
        getContext().registerReceiver(mScreenReceiver,
                new IntentFilter(Intent.ACTION_SCREEN_OFF));
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        intentFilter.addDataScheme("package");
        getContext().registerReceiver(mPackageReceiver, intentFilter);
    }

    @Override
    public void publish() {
        ServiceManager.addService(XIntentFirewallManager.INTENT_FIREWALL_SERVICE, asBinder());
        construct();
    }

    @Override
    public void systemReady() {
        cachePackages();
        whiteIMEPackages();
        registerReceiver();
    }

    private void construct() {
        mFirewallHandler = onCreateServiceHandler();
        XLog.logV("construct, mFirewallHandler: " + mFirewallHandler + " -" + serial());
    }

    protected Handler onCreateServiceHandler() {
        return new HandlerImpl();
    }

    @Override
    public void publishFeature(String f) {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public String serial() {
        return mSerialUUID.toString();
    }

    @SuppressLint("HandlerLeak")
    @AllArgsConstructor
    private class HandlerImpl extends Handler implements IntentFirewallHandler {

        @Override
        public void handleMessage(Message msg) {
            XLog.logV("handleMessage: " + IntentFirewallHandlerMessages.decodeMessage(msg.what));
            super.handleMessage(msg);
            switch (msg.what) {
                case IntentFirewallHandlerMessages.MSG_CLEARPROCESSONSCREENOFF:
                    HandlerImpl.this.clearProcessOnScreenOff();
                    break;
            }
        }

        @Override
        public void setEnabled(boolean enabled) {

        }

        @Override
        public void clearProcessOnScreenOff() {
            FutureTask<Void> futureTask = new FutureTask<>(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    ActivityManager am = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
                    if (am == null) return null;
                    List<ActivityManager.RunningAppProcessInfo> processes =
                            am.getRunningAppProcesses();
                    int count = processes == null ? 0 : processes.size();
                    for (int i = 0; i < count; i++) {
                        for (String runningPackageName : processes.get(i).pkgList) {
                            if (runningPackageName != null && !WHITE_LIST.contains(runningPackageName)) {
                                if (PkgUtil.isSystemApp(getContext(), runningPackageName)) {
                                    continue;
                                }
                                if (PkgUtil.isAppRunningForeground(getContext(), runningPackageName)) {
                                    XLog.logV("App is in foreground, wont kill: " + runningPackageName);
                                    continue;
                                }
                                am.forceStopPackage(runningPackageName);
                                XLog.logV("Force stopped: " + runningPackageName);
                            }
                        }
                    }
                    return null;
                }
            });
            mWorkingService.execute(futureTask);
        }
    }
}
