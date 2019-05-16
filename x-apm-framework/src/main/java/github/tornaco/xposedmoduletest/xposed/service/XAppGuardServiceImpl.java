package github.tornaco.xposedmoduletest.xposed.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.IApplicationThread;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import github.tornaco.android.common.Collections;
import github.tornaco.android.common.Consumer;
import github.tornaco.android.common.Holder;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.util.OSUtil;
import github.tornaco.xposedmoduletest.xposed.AppGlobals;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.app.XAppLockManager;
import github.tornaco.xposedmoduletest.xposed.app.XAppVerifyMode;
import github.tornaco.xposedmoduletest.xposed.bean.BlurSettings;
import github.tornaco.xposedmoduletest.xposed.bean.VerifySettings;
import github.tornaco.xposedmoduletest.xposed.repo.MapRepo;
import github.tornaco.xposedmoduletest.xposed.repo.RepoProxy;
import github.tornaco.xposedmoduletest.xposed.repo.SetRepo;
import github.tornaco.xposedmoduletest.xposed.service.notification.SystemUI;
import github.tornaco.xposedmoduletest.xposed.service.notification.UniqueIdFactory;
import github.tornaco.xposedmoduletest.xposed.service.provider.XAPMServerSettings;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

import static android.content.Context.KEYGUARD_SERVICE;

/**
 * Created by guohao4 on 2017/10/23.
 * Email: Tornaco@163.com
 */
@SuppressWarnings("WeakerAccess")
class XAppGuardServiceImpl extends XAppGuardServiceAbs {

    private static final long TRANSACTION_EXPIRE_TIME = 60 * 1000;

    private static final String NOTIFICATION_CHANNEL_ID_DEBUG = "dev.tornaco.notification.channel.id.X-APM-DEBUG";
    // Warn user if dev mode is open.
    private final static int NOTIFICATION_ID_DEBUG_MODE = UniqueIdFactory.getIdByTag(NOTIFICATION_CHANNEL_ID_DEBUG);

    private Handler mServiceHandler;

    private AtomicBoolean mAppLockEnabled = new AtomicBoolean(false);
    private AtomicBoolean mUninstallProEnabled = new AtomicBoolean(false);
    private AtomicBoolean mBlurEnabled = new AtomicBoolean(false);
    private AtomicBoolean mDebugEnabled = new AtomicBoolean(false);

    private AtomicBoolean mInterruptFPSuccessVB = new AtomicBoolean(false);
    private AtomicBoolean mInterruptFPERRORVB = new AtomicBoolean(false);

    private VerifySettings mVerifySettings;
    private AtomicInteger mBlurRadius = new AtomicInteger(BlurSettings.BLUR_RADIUS);

    @SuppressLint("UseSparseArrays")
    private final Map<Integer, Transaction> mTransactionMap = new HashMap<>();

    private static final Set<String> PREBUILT_WHITE_LIST = new HashSet<>();

    private RepoProxy mRepoProxy;

    private final Set<String> mVerifiedPackages = new HashSet<>();

    private static int sClientUID = 0;

    private final Holder<String> mTopActivityPkg = new Holder<>();

    private XAshmanServiceImpl mService;

    public XAppGuardServiceImpl(XAshmanServiceImpl mService) {
        this.mService = mService;
    }

    static {
        PREBUILT_WHITE_LIST.add("com.android.systemui");
        PREBUILT_WHITE_LIST.add("android");
        // PREBUILT_WHITE_LIST.add(BuildConfig.APPLICATION_ID);
    }

    private boolean isInSystemAppList(String pkg) {
        return mService.isInSystemAppList(pkg);
    }

    private final ExecutorService mWorkingService = Executors.newCachedThreadPool();

    private BroadcastReceiver mScreenReceiver =
            new ProtectedBroadcastReceiver(new BroadcastReceiver() {

                public void onReceive(Context context, Intent intent) {
                    if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                        onScreenOff();
                    }

                    if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
                        onUserPresent();
                    }
                }
            });

    private BroadcastReceiver mPackageReceiver =
            new ProtectedBroadcastReceiver(new BroadcastReceiver() {

                public void onReceive(Context context, Intent intent) {

                    String action = intent.getAction();
                    if (action == null || intent.getData() == null) {
                        // They send us bad action~
                        return;
                    }

                    String packageName = intent.getData().getSchemeSpecificPart();
                    if (packageName == null) {
                        return;
                    }

                    switch (action) {
                        case Intent.ACTION_PACKAGE_ADDED:
                        case Intent.ACTION_PACKAGE_REPLACED:
                            cacheUIDForUs();
                            break;
                        case Intent.ACTION_PACKAGE_REMOVED:
                            break;
                    }
                }
            });

    private static final String ACTION_DISABLE_DEBUG_MODE = "github.tornaco.broadcast.action.disable_debug_mode";

    private BroadcastReceiver mDebugModeReceiver =
            new ProtectedBroadcastReceiver(new BroadcastReceiver() {

                public void onReceive(Context context, Intent intent) {
                    if (ACTION_DISABLE_DEBUG_MODE.equals(intent.getAction())) {
                        // Disable!
                        XAppLockManager.get().setDebug(false);
                    }
                }
            });

    private PackageManager mPackageManager;
    private PowerManager mPowerManager;

    // Safe mode is the last clear place user can stay.
    private boolean mIsSafeMode = false;

    private UUID mSerialUUID = UUID.randomUUID();


    public void publish() {
        construct();
    }

    private void checkSafeMode() {
        mIsSafeMode = getContext().getPackageManager().isSafeMode();
    }

    private void construct() {
        mServiceHandler = onCreateServiceHandler();
        if (XposedLog.isVerboseLoggable()) {
            XposedLog.verbose("construct, mServiceHandler: " + mServiceHandler + " -" + serial());
        }
        mPackageManager = getContext().getPackageManager();
        mRepoProxy = RepoProxy.getProxy();
        XposedLog.verbose("Repo proxy: " + mRepoProxy);
    }

    protected Handler onCreateServiceHandler() {
        return new AppGuardServiceHandlerImpl();
    }

    private boolean mSystemReady;

    public void systemReady() {
        XposedLog.wtf("systemReady@" + getClass().getSimpleName());
        checkSafeMode();
        cacheUIDForUs();
        registerReceiver();
        updateDebugMode();

        // Retrieve power here.
        mPowerManager = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
        XposedLog.verbose("systemReady power: " + mPowerManager);

        mSystemReady = true;
    }

    public void retrieveSettings() {
        XposedLog.wtf("retrieveSettings@" + getClass().getSimpleName());
        loadConfigFromSettings();
    }

    private void loadConfigFromSettings() {
        try {
            boolean appGuardEnabled = XAPMServerSettings.APP_GUARD_ENABLED_NEW_B.read();
            mAppLockEnabled.set(appGuardEnabled);

            boolean uninstallProEnabled = XAPMServerSettings.UNINSTALL_GUARD_ENABLED_B.read();
            mUninstallProEnabled.set(uninstallProEnabled);

            boolean blurEnabled = XAPMServerSettings.BLUR_ENABLED_B.read();
            mBlurEnabled.set(blurEnabled);

            int blurR = XAPMServerSettings.BLUR_RADIUS_I.read();
            mBlurRadius.set(blurR);

            boolean interruptFPS = XAPMServerSettings.INTERRUPT_FP_SUCCESS_VB_ENABLED_B.read();
            mInterruptFPSuccessVB.set(interruptFPS);

            boolean interruptFPE = XAPMServerSettings.INTERRUPT_FP_ERROR_VB_ENABLED_B.read();
            mInterruptFPERRORVB.set(interruptFPE);

            boolean debug = BuildConfig.DEBUG;
            mDebugEnabled.set(debug);
            XposedLog.setLogLevel(mDebugEnabled.get() ? XposedLog.LogLevel.ALL : XposedLog.LogLevel.WARN);

            ContentResolver resolver = getContext().getContentResolver();
            if (resolver != null) {
                mVerifySettings = VerifySettings.from(Settings.System.getString(resolver, VerifySettings.KEY_SETTINGS));
            } else {
                XposedLog.boot("resolver is null ,fail retrieve VerifySettings");
            }

            // Use default value.
            if (mVerifySettings == null) {
                mVerifySettings = VerifySettings.DEFAULT_SETTINGS;
            }

            XposedLog.boot("mVerifySettings: " + String.valueOf(mVerifySettings));
            XposedLog.boot("mUninstallProEnabled: " + String.valueOf(mUninstallProEnabled));
            XposedLog.boot("mBlurEnabled: " + String.valueOf(mBlurEnabled));
            XposedLog.boot("mInterruptFPSuccessVB: " + String.valueOf(mInterruptFPSuccessVB));
            XposedLog.boot("mAppLockEnabled: " + String.valueOf(mAppLockEnabled));
            XposedLog.boot("mBlurRadius: " + String.valueOf(mBlurRadius));
        } catch (Throwable e) {
            XposedLog.wtf("Fail loadConfigFromSettings:" + Log.getStackTraceString(e));
        }
    }

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_USER_PRESENT);
        getContext().registerReceiver(mScreenReceiver, intentFilter);

        intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        intentFilter.addDataScheme("package");
        getContext().registerReceiver(mPackageReceiver, intentFilter);

        intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_DISABLE_DEBUG_MODE);
        getContext().registerReceiver(mDebugModeReceiver, intentFilter);
    }

    private void cacheUIDForUs() {
        PackageManager pm = this.getContext().getPackageManager();
        try {
            ApplicationInfo applicationInfo = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                applicationInfo = pm.getApplicationInfo(BuildConfig.APPLICATION_ID,
                        PackageManager.MATCH_UNINSTALLED_PACKAGES);
            } else {
                applicationInfo = pm.getApplicationInfo(BuildConfig.APPLICATION_ID,
                        PackageManager.GET_UNINSTALLED_PACKAGES);
            }
            sClientUID = applicationInfo.uid;
            AppGlobals.setXAPMCUid(sClientUID);
        } catch (Exception ignored) {
            XposedLog.debug("Can not getSingleton UID for our client:" + ignored);
        }
    }

    public void shutdown() {
        XposedLog.debug("shutdown...");
    }

    public boolean interruptPackageRemoval(String pkg) {
        boolean enabled = isUninstallInterruptEnabled();

        if (!enabled) {
            if (XposedLog.isVerboseLoggable()) {
                XposedLog.verbose("interruptPackageRemoval false: not enabled.");
            }
            return false;
        }


        if (BuildConfig.APPLICATION_ID.equals(pkg)) {
            return true;
        }

        boolean userSet = isPackageInUPList(pkg);
        if (!userSet) {
            if (XposedLog.isVerboseLoggable()) {
                XposedLog.verbose("interruptPackageRemoval false: not in user list.");
            }
            return false;
        }
        return true;
    }


    @InternalCall
    public Intent checkIntent(Intent from) {
        if (XposedLog.isVerboseLoggable()) {
            XposedLog.verbose("checkIntent: " + from);
        }
        ComponentName fromComp = from.getComponent();
        if (fromComp == null) {
            return from;
        }

        // Check if this component is disabled.
        if (getPackageManager() == null) {
            return from;
        }
        int compState = PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;

        try {
            compState = getPackageManager().getComponentEnabledSetting(fromComp);
        } catch (Throwable e) {
            XposedLog.wtf("Fail getComponentEnabledSetting for: " + fromComp + ", err: " + Log.getStackTraceString(e));
        }

        if (compState == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                || compState == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER) {
            XposedLog.wtf("ComponentName is disabled, do not launch:" + fromComp);
            return null;
        }

        MapRepo<String, String> compMap = RepoProxy.getProxy().getComponentReplacement();
        if (compMap.size() == 0) {
            return from;
        }

        String key = fromComp.flattenToString();

        if (!compMap.containsKey(key)) {
            return from;
        }

        // Avoid null value.
        if (!compMap.hasNoneNullValue(key)) {
            return from;
        }

        String replacementValue = compMap.get(key);
        ComponentName toComp = ComponentName.unflattenFromString(replacementValue);

        if (!validateComponentName(toComp)) {
            XposedLog.debug("Invalid component replacement: " + toComp);
            return from;
        }

        if (XposedLog.isVerboseLoggable()) {
            XposedLog.verbose("Replacing using: " + toComp);
        }

        return from.setComponent(toComp);
    }


    public long wrapCallingUidForIntent(long from, Intent intent) {
        return from;
    }

    private static boolean validateComponentName(ComponentName componentName) {
        return componentName != null;
    }


    @BinderCall
    public void addOrRemoveComponentReplacement(ComponentName from, ComponentName to, boolean add) {
        if (XposedLog.isVerboseLoggable()) {
            XposedLog.verbose("addOrRemoveComponentReplacement, from: "
                    + from + ", to: " + to + ", add? " + add);
        }

        enforceCallingPermissions();
        Preconditions.checkNotNull(from);

        // Key is not null, value can be null.
        String key = from.flattenToString();
        if (add) {
            String value = to == null ? null : to.flattenToString();
            RepoProxy.getProxy().getComponentReplacement().put(key, value);
        } else {
            RepoProxy.getProxy().getComponentReplacement().remove(key);
        }
    }


    @BinderCall
    public Map getComponentReplacements() {
        return RepoProxy.getProxy().getComponentReplacement().dup();
    }

    protected void dump(FileDescriptor fd, final PrintWriter fout, String[] args) {
        // For secure and CTS.
        if (getContext().checkCallingOrSelfPermission(Manifest.permission.DUMP) != PackageManager.PERMISSION_GRANTED) {
            fout.println("Permission denial: can not dump AppGuard service from pid= " + Binder.getCallingPid()
                    + ", uid= " + Binder.getCallingUid());
            return;
        }
        synchronized (this) {
            fout.println("mInterruptFPSuccessVB enabled: " + mInterruptFPSuccessVB.get());
            fout.println("mInterruptFPERRORVB enabled: " + mInterruptFPERRORVB.get());

            fout.println();
            fout.println();
        }
    }

    private PackageManager getPackageManager() {
        if (mPackageManager == null) {
            mPackageManager = getContext().getPackageManager();
        }
        return mPackageManager;
    }

    private boolean isPackageInLockList(String pkg) {
        return mRepoProxy.getLocks().has(pkg);
    }

    private boolean isPackageInBlurList(String pkg) {
        return mRepoProxy.getBlurs().has(pkg);
    }

    private boolean isPackageInUPList(String pkg) {
        return mRepoProxy.getUninstall().has(pkg);
    }


    public boolean onEarlyVerifyConfirm(String pkg, String res) {
        if (BuildConfig.DEBUG && XposedLog.isVerboseLoggable()) {
            XposedLog.verbose("onEarlyVerifyConfirm: " + res + " calling by: "
                    + Binder.getCallingUid());
            Collections.consumeRemaining(mVerifiedPackages, s -> XposedLog.verbose("@@@@ " + s));
        }
        if (pkg == null) {
            if (XposedLog.isVerboseLoggable()) {
                XposedLog.verbose("onEarlyVerifyConfirm, false@pkg-null");
            }
            return false;
        }
        if (mIsSafeMode) {
            if (XposedLog.isVerboseLoggable()) {
                XposedLog.verbose("onEarlyVerifyConfirm, false@safe-mode:" + pkg);
            }
            return false;
        }
        if (!mAppLockEnabled.get()) {
            if (XposedLog.isVerboseLoggable()) {
                XposedLog.verbose("onEarlyVerifyConfirm, false@disabled:" + pkg);
            }
            return false;
        }
        if (PREBUILT_WHITE_LIST.contains(pkg)) {
            if (XposedLog.isVerboseLoggable()) {
                XposedLog.verbose("onEarlyVerifyConfirm, false@prebuilt-w-list:" + pkg);
            }
            return false;
        } // White list.

        if (mVerifiedPackages.contains(pkg)) {
            if (XposedLog.isVerboseLoggable()) {
                XposedLog.verbose("onEarlyVerifyConfirm, false@in-verified-list:" + pkg);
            }
            return false;
        } // Passed.

        boolean inUserSetList = isPackageInLockList(pkg);
        if (!inUserSetList) {
            if (XposedLog.isVerboseLoggable()) {
                XposedLog.verbose("onEarlyVerifyConfirm, false@not-in-guard-list:" + pkg);
            }
            return false;
        }
        return true;
    }


    @InternalCall
    public void verify(Bundle options, String pkg, ComponentName componentName, int uid, int pid, VerifyListener listener) {
        verifyInternal(options, pkg, componentName, uid, pid, false, listener);
    }

    private void verifyInternal(Bundle options, String pkg, ComponentName componentName,
                                int uid, int pid,
                                boolean injectHomeOnFail, VerifyListener listener) {
        if (XposedLog.isVerboseLoggable()) {
            XposedLog.verbose("verifyInternal: " + pkg + "-"
                    + (componentName == null ? "NULL" : componentName.flattenToShortString()));
        }

        if (!mSystemReady) {
            XposedLog.wtf("System not ready, won't verify");
            listener.onVerifyRes(pkg, uid, pid, XAppVerifyMode.MODE_ALLOWED);
            return;
        }

        if (componentName != null) {
            boolean isAPM = XAPMManager.VERIFIER_CLASS_NAME.equals(componentName.getClassName());
            if (BuildConfig.DEBUG) {
                XposedLog.verbose("is APM?" + isAPM);
            }
            if (isAPM) {
                listener.onVerifyRes(pkg, uid, pid, XAppVerifyMode.MODE_ALLOWED);
                XposedLog.verbose("Do not ever verify APM-VERIFIER it self.");
                return;
            }
        } else if (BuildConfig.APPLICATION_ID.equals(pkg)) {
            listener.onVerifyRes(pkg, uid, pid, XAppVerifyMode.MODE_ALLOWED);
            XposedLog.verbose("We do not know which one of us is top now, in-case it loops. skip verify us.");
            return;
        }

        // Fix issues that WeChat and QQ can not active it self when receive a video chat.
        boolean screenOn = mPowerManager != null && mPowerManager.isInteractive();
        if (!screenOn) {
            boolean inActivityWhiteList =
                    RepoProxy.getProxy().getLock_white_list_activity().has(pkg);
            if (inActivityWhiteList) {
                listener.onVerifyRes(pkg, uid, pid, XAppVerifyMode.MODE_ALLOWED);
                XposedLog.verbose("Allow activity to start in white list and screen off");
                return;
            }
        }
        // Check if in white list.
        if (componentName != null) {
            String shortString = componentName.flattenToShortString();
            boolean inActivityWhiteList =
                    RepoProxy.getProxy().getLock_white_list_activity().has(shortString);
            if (inActivityWhiteList) {
                listener.onVerifyRes(pkg, uid, pid, XAppVerifyMode.MODE_ALLOWED);
                XposedLog.verbose("Allow activity to start in white list");
                return;
            }
        }

        VerifyArgs args = VerifyArgs.builder()
                .bnds(options)
                .pid(pid)
                .uid(uid)
                .injectHomeOnFail(injectHomeOnFail)
                .listener(listener)
                .pkg(pkg)
                .build();
        mServiceHandler.obtainMessage(AppGuardServiceHandlerMessages.MSG_VERIFY, args).sendToTarget();
    }

    @BinderCall(restrict = "hooks")
    public void onPackageMoveToFront(Intent who) {
        onActivityPackageResume(PkgUtil.packageNameOf(who));
    }

    @BinderCall(restrict = "anyone")
    public void onActivityPackageResume(String pkg) {
        if (mServiceHandler == null) {
            return;
        }
        mServiceHandler.obtainMessage(AppGuardServiceHandlerMessages.MSG_ONACTIVITYPACKAGERESUME, pkg).sendToTarget();
    }

    public boolean isInterruptFPEventVBEnabled(int event) {
        enforceCallingPermissions();
        switch (event) {
            case XAppLockManager.FPEvent.SUCCESS:
                return interruptFPSuccessVibrate();
            case XAppLockManager.FPEvent.ERROR:
                return interruptFPErrorVibrate();
        }
        return false;
    }

    private boolean isDeviceLocked() {
        KeyguardManager keyguardManager = (KeyguardManager)
                getContext()
                        .getSystemService(KEYGUARD_SERVICE);
        return keyguardManager != null && keyguardManager.isKeyguardLocked();
    }

    @BinderCall
    public void setInterruptFPEventVBEnabled(int event, boolean enabled) {
        enforceCallingPermissions();
        mServiceHandler.obtainMessage(AppGuardServiceHandlerMessages
                .MSG_SETINTERRUPTFPEVENTVBENABLED, event, event, enabled)
                .sendToTarget();
    }

    public String serial() {
        return mSerialUUID.toString();
    }

    public boolean onKeyEvent(KeyEvent keyEvent, String source) {
        // Nothing to do.
        return false;
    }

    // FIXME Below is a good way to receive screen broadcast, try it.

    public boolean checkBroadcastIntent(IApplicationThread caller, Intent intent) {
        if (intent != null) {
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())
                    || Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
                // Make sure we received the intent.
                mScreenReceiver.onReceive(getContext(), intent);
            }
            if (BuildConfig.DEBUG && Intent.ACTION_USER_SWITCHED.equals(intent.getAction())) {
                XposedLog.verbose("ACTION_USER_SWITCHED");
            }
        }
        return true;
    }

    @InternalCall
    public boolean isBlurForPkg(String pkg) {
        return isBlurEnabled() && isPackageInBlurList(pkg);
    }

    @InternalCall
    public float getBlurScale() {
        return BlurSettings.BITMAP_SCALE;
    }

    @InternalCall
    public int getBlurRadius() {
        return mBlurRadius.get();
    }

    public void setBlurRadius(int r) {
        XposedLog.verbose("setBlurRadius: " + r);
        enforceCallingPermissions();
        int checkedR = r >= BlurSettings.BLUR_RADIUS_MAX ? BlurSettings.BLUR_RADIUS_MAX : r;
        mServiceHandler.obtainMessage(AppGuardServiceHandlerMessages.MSG_SETBLURRADIUS, checkedR)
                .sendToTarget();
    }

    public boolean interruptFPSuccessVibrate() {
        boolean isKeyguard = isDeviceLocked();
        if (XposedLog.isVerboseLoggable()) {
            XposedLog.verbose("Device is locked: " + isKeyguard);
        }
        return !isKeyguard && mInterruptFPSuccessVB.get();
    }

    public boolean interruptFPErrorVibrate() {
        boolean isKeyguard = isDeviceLocked();
        if (XposedLog.isVerboseLoggable()) {
            XposedLog.verbose("Device is locked: " + isKeyguard);
        }
        return !isKeyguard && mInterruptFPERRORVB.get();
    }

    public boolean isActivityStartShouldBeInterrupted(ComponentName componentName) {
        return false;
    }

    public void forceReloadPackages() {

        mWorkingService.execute(new Runnable() {

            public void run() {
                // Remove onwer package to fix previous bugs.
                try {
                    mRepoProxy.getLocks().remove(BuildConfig.APPLICATION_ID);
                } catch (Throwable e) {
                    XposedLog.wtf("Fail remove owner package from repo: " + Log.getStackTraceString(e));
                }
            }
        });
    }

    private void addOrRemoveFromRepo(String[] packages, SetRepo<String> repo, boolean add) {
        long id = Binder.clearCallingIdentity();
        try {
            for (String p : packages) {
                if (add) {
                    repo.add(p);
                } else {
                    repo.remove(p);
                }
            }
        } finally {
            Binder.restoreCallingIdentity(id);
        }
    }

    private static String[] convertObjectArrayToStringArray(Object[] objArr) {
        if (objArr == null || objArr.length == 0) {
            return new String[0];
        }
        String[] out = new String[objArr.length];
        for (int i = 0; i < objArr.length; i++) {
            Object o = objArr[i];
            if (o == null) {
                continue;
            }
            String pkg = String.valueOf(o);
            out[i] = pkg;
        }
        return out;
    }

    public String[] getLockApps(boolean lock) {
        if (lock) {
            Set<String> packages = mRepoProxy.getLocks().getAll();
            if (packages.size() == 0) {
                return new String[0];
            }
            final List<String> noSys = Lists.newArrayList();
            Collections.consumeRemaining(packages, new Consumer<String>() {
                @Override
                public void accept(String p) {
                    noSys.add(p);
                }
            });
            return convertObjectArrayToStringArray(noSys.toArray());
        } else {
            Collection<String> packages = mService.getPackagesCache().keySet();
            if (packages.size() == 0) {
                return new String[0];
            }

            final List<String> outList = Lists.newArrayList();

            // Remove those not in blocked list.
            String[] allPackagesArr = convertObjectArrayToStringArray(packages.toArray());
            Collections.consumeRemaining(allPackagesArr, new Consumer<String>() {
                public void accept(String s) {
                    if (outList.contains(s)) {
                        return;// Kik dup package.
                    }
                    if (isPackageInLockList(s)) {
                        return;
                    }
                    if (PREBUILT_WHITE_LIST.contains(s)) {
                        return; // Do not set lock for these.
                    }
                    outList.add(s);
                }
            });

            if (outList.size() == 0) {
                return new String[0];
            }
            Object[] objArr = outList.toArray();
            return convertObjectArrayToStringArray(objArr);
        }
    }

    public void addOrRemoveLockApps(String[] packages, boolean add) {
        if (XposedLog.isVerboseLoggable()) {
            XposedLog.verbose("addOrRemoveLockApps: " + Arrays.toString(packages));
        }
        enforceCallingPermissions();
        if (packages == null || packages.length == 0) {
            return;
        }
        addOrRemoveFromRepo(packages, mRepoProxy.getLocks(), add);
    }

    public String[] getBlurApps(boolean blur) {
        if (blur) {
            Set<String> packages = mRepoProxy.getBlurs().getAll();
            if (packages.size() == 0) {
                return new String[0];
            }
            final List<String> noSys = Lists.newArrayList();
            Collections.consumeRemaining(packages, new Consumer<String>() {
                @Override
                public void accept(String p) {
                    noSys.add(p);
                }
            });
            return convertObjectArrayToStringArray(noSys.toArray());
        } else {
            Collection<String> packages = mService.getPackagesCache().keySet();
            if (packages.size() == 0) {
                return new String[0];
            }

            final List<String> outList = Lists.newArrayList();

            // Remove those not in blocked list.
            String[] allPackagesArr = convertObjectArrayToStringArray(packages.toArray());
            Collections.consumeRemaining(allPackagesArr, new Consumer<String>() {

                public void accept(String s) {
                    if (outList.contains(s)) {
                        return;// Kik dup package.
                    }
                    if (isPackageInBlurList(s)) {
                        return;
                    }
                    if (PREBUILT_WHITE_LIST.contains(s)) {
                        return; // Do not set lock for these.
                    }
                    outList.add(s);
                }
            });

            if (outList.size() == 0) {
                return new String[0];
            }
            Object[] objArr = outList.toArray();
            return convertObjectArrayToStringArray(objArr);
        }
    }

    public void addOrRemoveBlurApps(String[] packages, boolean blur) {
        if (XposedLog.isVerboseLoggable()) {
            XposedLog.verbose("addOrRemoveBlurApps: " + Arrays.toString(packages));
        }
        enforceCallingPermissions();
        if (packages == null || packages.length == 0) {
            return;
        }
        addOrRemoveFromRepo(packages, mRepoProxy.getBlurs(), blur);
    }

    public String[] getUPApps(boolean lock) {
        if (lock) {
            Set<String> packages = mRepoProxy.getUninstall().getAll();
            if (packages.size() == 0) {
                return new String[0];
            }
            final List<String> noSys = Lists.newArrayList();
            Collections.consumeRemaining(packages, new Consumer<String>() {
                @Override
                public void accept(String p) {
                    if (isInSystemAppList(p)) {
                        return;
                    }
                    noSys.add(p);
                }
            });
            return convertObjectArrayToStringArray(noSys.toArray());
        } else {
            Collection<String> packages = mService.getPackagesCache().keySet();
            if (packages.size() == 0) {
                return new String[0];
            }

            final List<String> outList = Lists.newArrayList();

            // Remove those not in blocked list.
            String[] allPackagesArr = convertObjectArrayToStringArray(packages.toArray());
            Collections.consumeRemaining(allPackagesArr, new Consumer<String>() {

                public void accept(String s) {
                    if (outList.contains(s)) {
                        return;// Kik dup package.
                    }
                    if (isPackageInUPList(s)) {
                        return;
                    }
                    if (PREBUILT_WHITE_LIST.contains(s)) {
                        return; // Do not set lock for these.
                    }
                    if (isInSystemAppList(s)) {
                        return;
                    }
                    outList.add(s);
                }
            });

            if (outList.size() == 0) {
                return new String[0];
            }
            Object[] objArr = outList.toArray();
            return convertObjectArrayToStringArray(objArr);
        }
    }


    public void addOrRemoveUPApps(String[] packages, boolean add) {
        if (XposedLog.isVerboseLoggable()) {
            XposedLog.verbose("addOrRemoveUPApps: " + Arrays.toString(packages));
        }
        enforceCallingPermissions();
        if (packages == null || packages.length == 0) {
            return;
        }
        addOrRemoveFromRepo(packages, mRepoProxy.getUninstall(), add);
    }


    public void restoreDefaultSettings() {
        enforceCallingPermissions();
        mServiceHandler.obtainMessage(AppGuardServiceHandlerMessages.MSG_RESTOREDEFAULTSETTINGS)
                .sendToTarget();
    }


    public void onTaskRemoving(String pkg) {
        mServiceHandler.obtainMessage(AppGuardServiceHandlerMessages.MSG_ONAPPTASKREMOVED,
                pkg).sendToTarget();
    }


    @BinderCall
    public boolean isAppLockEnabled() {
        enforceCallingPermissions();
        return !mIsSafeMode && mAppLockEnabled.get();
    }


    @BinderCall
    public void setAppLockEnabled(boolean enabled) {
        enforceCallingPermissions();
        mServiceHandler.obtainMessage(AppGuardServiceHandlerMessages.MSG_SETENABLED,
                enabled ? 1 : 0, 0)
                .sendToTarget();
    }


    public boolean isBlurEnabled() {
        return mBlurEnabled.get();
    }


    public void setBlurEnabled(boolean enabled) {
        enforceCallingPermissions();
        mServiceHandler.obtainMessage(
                AppGuardServiceHandlerMessages.MSG_SETBLURENABLED,
                enabled ? 1 : 0, 0)
                .sendToTarget();
    }


    @BinderCall
    public boolean isUninstallInterruptEnabled() {
        enforceCallingPermissions();
        return mUninstallProEnabled.get();
    }


    @BinderCall
    public void setUninstallInterruptEnabled(boolean enabled) {
        enforceCallingPermissions();
        mServiceHandler.obtainMessage(
                AppGuardServiceHandlerMessages.MSG_SETUNINSTALLINTERRUPTENABLED,
                enabled ? 1 : 0, 0)
                .sendToTarget();
    }


    @BinderCall
    public void setVerifySettings(VerifySettings settings) {
        enforceCallingPermissions();
        mServiceHandler.removeMessages(AppGuardServiceHandlerMessages.MSG_SETVERIFYSETTINGS);
        mServiceHandler.obtainMessage(AppGuardServiceHandlerMessages.MSG_SETVERIFYSETTINGS, settings).sendToTarget();
    }


    @BinderCall
    public VerifySettings getVerifySettings() {
        enforceCallingPermissions();
        return mVerifySettings == null ? null : mVerifySettings.duplicate();
    }


    @BinderCall
    public void setResult(int transactionID, int res) {
        enforceCallingPermissions();
        mServiceHandler.obtainMessage(AppGuardServiceHandlerMessages.MSG_SETRESULT, transactionID, res).sendToTarget();
    }


    public boolean isTransactionValid(int transactionID) {
        enforceCallingPermissions();
        Transaction transaction = mTransactionMap.get(transactionID);
        return transaction != null;
    }


    @BinderCall
    public void mockCrash() {
        enforceCallingPermissions();
        mServiceHandler.sendEmptyMessage(AppGuardServiceHandlerMessages.MSG_MOCKCRASH);
    }


    @BinderCall
    public void setVerifierPackage(String pkg) {
        enforceCallingPermissions();
        // TODO.
    }


    @Deprecated
    public void injectHomeEvent() {
        throw new IllegalStateException("injectHomeEvent is Deprecated api");
    }


    public void setDebug(boolean debug) {
        enforceCallingPermissions();
        mServiceHandler.obtainMessage(AppGuardServiceHandlerMessages.MSG_SETDEBUG, debug).sendToTarget();
    }

    private void updateDebugMode() {
        mServiceHandler.sendEmptyMessageDelayed(AppGuardServiceHandlerMessages.MSG_WARNIFDEBUG, 10 * 1000);
    }

    public boolean isDebug() {
        enforceCallingPermissions();
        return mDebugEnabled.get();
    }

    protected void enforceCallingPermissions() {
        int callingUID = Binder.getCallingUid();
        if (XposedLog.isVerboseLoggable()) {
            XposedLog.verbose("enforceCallingPermissions@uid:" + callingUID);
        }
        if (callingUID == android.os.Process.myUid() || (sClientUID > 0 && sClientUID == callingUID)) {
            return;
        }
        throw new SecurityException("Package of uid:" + callingUID
                + ", does not require permission to interact with XAppGuardServiceImpl");
    }

    private void onScreenOff() {
        XposedLog.verbose("onScreenOff@AppGuard: " + mVerifySettings);
        if (mVerifySettings != null) {
            boolean verifyOnScreenOff = mVerifySettings.isVerifyOnScreenOff();
            if (verifyOnScreenOff) {
                if (XposedLog.isVerboseLoggable()) {
                    XposedLog.verbose("SCREEN OFF, Clearing passed pkgs...");
                }
                mVerifiedPackages.clear();
                XposedLog.verbose("SCREEN OFF, mVerifiedPackages cleared...");
            }
        }
    }

    private void onUserPresent() {
        mServiceHandler.obtainMessage(AppGuardServiceHandlerMessages.MSG_ONUSERPRESENT).sendToTarget();
    }

    private static Intent buildVerifyIntent(boolean injectHome, int transId, String pkg) {
        Intent intent = new Intent(XAppLockManager.ACTION_APP_GUARD_VERIFY_DISPLAYER);
        intent.setClassName(BuildConfig.APPLICATION_ID,
                XAPMManager.VERIFIER_CLASS_NAME);
        intent.putExtra(XAppLockManager.EXTRA_PKG_NAME, pkg);
        intent.putExtra(XAppLockManager.EXTRA_TRANS_ID, transId);
        intent.putExtra(XAppLockManager.EXTRA_INJECT_HOME_WHEN_FAIL_ID, injectHome);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return intent;
    }

    private static class TransactionFactory {

        private static final AtomicInteger TRANS_ID_BASE = new AtomicInteger(2017);

        static int transactionID() {
            return TRANS_ID_BASE.getAndIncrement();
        }
    }

    @SuppressLint("HandlerLeak")
    private class AppGuardServiceHandlerImpl extends Handler
            implements AppGuardServiceHandler {


        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int wht = msg.what;
            if (XposedLog.isVerboseLoggable()) {
                XposedLog.verbose("AppGuardServiceHandlerImpl-handleMessage@"
                        + AppGuardServiceHandlerMessages.decodeMessage(wht));
            }
            switch (wht) {
                case AppGuardServiceHandlerMessages.MSG_SETENABLED:
                    AppGuardServiceHandlerImpl.this.setEnabled(msg.arg1 == 1);
                    break;
                case AppGuardServiceHandlerMessages.MSG_MOCKCRASH:
                    AppGuardServiceHandlerImpl.this.mockCrash();
                    break;
                case AppGuardServiceHandlerMessages.MSG_SETRESULT:
                    AppGuardServiceHandlerImpl.this.setResult(msg.arg1, msg.arg2);
                    break;
                case AppGuardServiceHandlerMessages.MSG_SETUNINSTALLINTERRUPTENABLED:
                    AppGuardServiceHandlerImpl.this.setUninstallInterruptEnabled(msg.arg1 == 1);
                    break;
                case AppGuardServiceHandlerMessages.MSG_SETBLURENABLED:
                    AppGuardServiceHandlerImpl.this.setBlurEnabled(msg.arg1 == 1);
                    break;
                case AppGuardServiceHandlerMessages.MSG_SETVERIFYSETTINGS:
                    AppGuardServiceHandlerImpl.this.setVerifySettings((VerifySettings) msg.obj);
                    break;
                case AppGuardServiceHandlerMessages.MSG_VERIFY:
                    AppGuardServiceHandlerImpl.this.verify((VerifyArgs) msg.obj);
                    break;
                case AppGuardServiceHandlerMessages.MSG_SETDEBUG:
                    AppGuardServiceHandlerImpl.this.setDebug((Boolean) msg.obj);
                    break;
                case AppGuardServiceHandlerMessages.MSG_ONACTIVITYPACKAGERESUME:
                    AppGuardServiceHandlerImpl.this.onActivityPackageResume((String) msg.obj);
                    break;
                case AppGuardServiceHandlerMessages.MSG_ONUSERPRESENT:
                    AppGuardServiceHandlerImpl.this.onUserPresent();
                    break;
                case AppGuardServiceHandlerMessages.MSG_SETINTERRUPTFPEVENTVBENABLED:
                    AppGuardServiceHandlerImpl.this.setInterruptFPEventVBEnabled(msg.arg1, (Boolean) msg.obj);
                    break;
                case AppGuardServiceHandlerMessages.MSG_RESTOREDEFAULTSETTINGS:
                    AppGuardServiceHandlerImpl.this.restoreDefaultSettings();
                    break;
                case AppGuardServiceHandlerMessages.MSG_SETBLURRADIUS:
                    AppGuardServiceHandlerImpl.this.setBlurRadius((Integer) msg.obj);
                    break;
                case AppGuardServiceHandlerMessages.MSG_WARNIFDEBUG:
                    AppGuardServiceHandlerImpl.this.warnIfDebug();
                    break;
                case AppGuardServiceHandlerMessages.MSG_ONAPPTASKREMOVED:
                    AppGuardServiceHandlerImpl.this.onAppTaskRemoved((String) msg.obj);
                    break;
                default:
                    if (msg.obj == null) {
                        return;
                    }
                    AppGuardServiceHandlerImpl.this.setResult((Integer) msg.obj,
                            XAppVerifyMode.MODE_IGNORED);
                    break;
            }
        }


        public void setEnabled(boolean enabled) {
            if (mAppLockEnabled.compareAndSet(!enabled, enabled)) {
                XAPMServerSettings.APP_GUARD_ENABLED_NEW_B.write(enabled);
            }
        }


        public void setUninstallInterruptEnabled(boolean enabled) {
            if (mUninstallProEnabled.compareAndSet(!enabled, enabled)) {
                XAPMServerSettings.UNINSTALL_GUARD_ENABLED_B.write(enabled);
            }
        }

        public void setBlurEnabled(boolean enabled) {
            if (mBlurEnabled.compareAndSet(!enabled, enabled)) {
                XAPMServerSettings.BLUR_ENABLED_B.write(enabled);

                if (enabled) {
                    RepoProxy.createFileIndicator("blur_indicator");
                } else {
                    RepoProxy.deleteFileIndicator("blur_indicator");
                }
            }
        }

        public void setBlurRadius(int r) {
            mBlurRadius.set(r);
            XAPMServerSettings.BLUR_RADIUS_I.write(r);
        }

        public void setVerifySettings(final VerifySettings settings) {
            mVerifySettings = Preconditions.checkNotNull(settings);
            mWorkingService.execute(new ErrorCatchRunnable(new Runnable() {

                public void run() {
                    // Saving to db.
                    ContentResolver resolver = getContext().getContentResolver();
                    String js = settings.formatJson();
                    if (js != null) {
                        Settings.System.putString(resolver, VerifySettings.KEY_SETTINGS, js);
                        XposedLog.verbose("setVerifySettings, putting: " + js);
                    }
                }
            }, "setVerifySettings"));
        }

        public void setResult(int transactionID, int res) {
            Transaction transaction = mTransactionMap.remove(transactionID);
            if (transaction == null) {
                XposedLog.debug("Can not find transaction for:" + transactionID);
                return;
            }

            if (XposedLog.isVerboseLoggable()) {
                XposedLog.verbose("setResult: " + res);
            }

            if (res == XAppVerifyMode.MODE_ALLOWED) {
                mVerifiedPackages.add(transaction.pkg);
            }
            transaction.listener.onVerifyRes(transaction.pkg, transaction.uid, transaction.pid, res);
            mServiceHandler.removeMessages(MSG_TRANSACTION_EXPIRE_BASE + transactionID);
        }

        private int mFatalErrorTimes = 0;

        public void verify(VerifyArgs args) {
            XposedLog.debug("onVerify:" + args);
            int tid = TransactionFactory.transactionID();
            int uid = args.uid;
            int pid = args.pid;
            String pkg = args.pkg;
            Bundle bnds = args.bnds;
            boolean injectHome = args.injectHomeOnFail;
            VerifyListener listener = args.listener;

            Transaction transaction = new Transaction(listener, uid, pid, tid, pkg);

            synchronized (mTransactionMap) {
                mTransactionMap.put(tid, transaction);
            }

            onNewTransaction(tid);

            Intent intent = buildVerifyIntent(injectHome, tid, pkg);
            try {
                getContext().startActivity(intent, bnds);
            } catch (Throwable anf) {
                mFatalErrorTimes++;
                XposedLog.wtf("*** FATAL ERROR *** ActivityNotFoundException!!!");
                setResult(tid, XAppVerifyMode.MODE_ALLOWED);

                // Disable app lock when error too many times.
                if (mFatalErrorTimes >= 5) {
                    // Disable to make sure we are safe.
                    setEnabled(false);
                }
            }
        }

        private void onNewTransaction(int transaction) {
            sendMessageDelayed(obtainMessage(MSG_TRANSACTION_EXPIRE_BASE + transaction, transaction), TRANSACTION_EXPIRE_TIME);
        }

        public void mockCrash() {
            throw new IllegalStateException("Let's CRASH, bye...");
        }

        public void setDebug(boolean debug) {
            if (mDebugEnabled.compareAndSet(!debug, debug)) {
                XAPMServerSettings.APP_GUARD_DEBUG_MODE_B_S.write(debug);
            }
            XposedLog.setLogLevel(mDebugEnabled.get() ? XposedLog.LogLevel.ALL : XposedLog.LogLevel.WARN);
            warnIfDebug();
        }

        public void onActivityPackageResume(String pkg) {
            onAppSwitchedTo(pkg);
            mTopActivityPkg.setData(pkg);
        }

        public void onUserPresent() {
            String pkg = mTopActivityPkg.getData();
            XposedLog.verbose("onUserPresent@AppGuard: " + pkg);
            if (pkg == null) {
                return;
            }
            if (!onEarlyVerifyConfirm(pkg, "onUserPresent")) {
                return;
            }
            verifyInternal(null, pkg, null, 0, 0, true, VerifyListenerAdapter.getDefault());
        }

        public void setInterruptFPEventVBEnabled(int event, boolean enabled) {
            switch (event) {
                case XAppLockManager.FPEvent.SUCCESS:
                    if (mInterruptFPSuccessVB.compareAndSet(!enabled, enabled)) {
                        XAPMServerSettings.INTERRUPT_FP_SUCCESS_VB_ENABLED_B.write(enabled);
                    }
                    break;
                case XAppLockManager.FPEvent.ERROR:
                    if (mInterruptFPERRORVB.compareAndSet(!enabled, enabled)) {
                        XAPMServerSettings.INTERRUPT_FP_ERROR_VB_ENABLED_B.write(enabled);
                    }
                    break;
                default:
                    break;
            }
        }

        public void restoreDefaultSettings() {
            RepoProxy.getProxy().deleteAll();
            loadConfigFromSettings();
        }

        void createDebugNotificationChannelForO() {
            if (OSUtil.isOOrAbove()) {
                NotificationManager notificationManager = (NotificationManager)
                        getContext().getSystemService(
                                Context.NOTIFICATION_SERVICE);
                NotificationChannel nc = null;
                if (notificationManager != null) {
                    nc = notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID_DEBUG);
                }
                if (nc != null) {
                    return;
                }
                NotificationChannel notificationChannel;
                notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID_DEBUG,
                        new AppResource(getContext()).loadStringFromAPMApp("notification_channel_name_debug"),
                        NotificationManager.IMPORTANCE_LOW);
                notificationChannel.enableLights(false);
                notificationChannel.enableVibration(false);
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(notificationChannel);
                }
            }
        }

        // Show notification while in Debug mode.
        public void warnIfDebug() {
            createDebugNotificationChannelForO();

            boolean isDevMode = mDebugEnabled.get();
            try {
                if (isDevMode) {
                    Intent disableBroadcastIntent = new Intent(ACTION_DISABLE_DEBUG_MODE);
                    PendingIntent disableIntent = PendingIntent.getBroadcast(getContext(),
                            UniqueIdFactory.getNextId(),
                            disableBroadcastIntent, 0);

                    NotificationCompat.Builder builder
                            = new NotificationCompat.Builder(getContext(), NOTIFICATION_CHANNEL_ID_DEBUG);

                    try {
                        SystemUI.overrideNotificationAppName(getContext(), builder, "X-APM");
                    } catch (Throwable ignored) {
                    }

                    AppResource appRes = new AppResource(getContext());
                    Notification n = builder
                            .setOngoing(true)
                            .setContentTitle(appRes.loadStringFromAPMApp("notification_title_debug_mode"))
                            .setContentText(appRes.loadStringFromAPMApp("notification_content_debug_mode"))
                            .setSmallIcon(android.R.drawable.stat_sys_warning)
                            .addAction(0, appRes.loadStringFromAPMApp("notification_action_debug_mode_disable"), disableIntent)
                            .build();

                    if (OSUtil.isMOrAbove()) {
                        n.setSmallIcon(new AppResource(getContext()).loadIconFromAPMApp("ic_stat_debug_mode"));
                    }

                    NotificationManagerCompat.from(getContext()).cancel(NOTIFICATION_ID_DEBUG_MODE);
                    NotificationManagerCompat.from(getContext())
                            .notify(NOTIFICATION_ID_DEBUG_MODE, n);
                } else {
                    NotificationManagerCompat.from(getContext()).cancel(NOTIFICATION_ID_DEBUG_MODE);
                }
            } catch (Throwable e) {
                Toast.makeText(getContext(),
                        new AppResource(getContext()).loadStringFromAPMApp("notification_toast_debug_mode"),
                        Toast.LENGTH_LONG)
                        .show();
            }
        }

        /**
         * @param who Only Keep the switched package.
         */
        private void onAppSwitchedTo(String who) {
            XposedLog.verbose("AppGuard onAppSwitchedTo: " + who);
            if (mVerifySettings != null) {
                boolean clearOnAppSwitch = mVerifySettings.isVerifyOnAppSwitch();
                if (clearOnAppSwitch) {
                    mVerifiedPackages.clear();

                    // Add to verified list only when screen on.
                    PowerManager powerManager = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
                    boolean isScreenOn = powerManager != null && powerManager.isInteractive();
                    if (isScreenOn) {
                        XposedLog.verbose("add to verified list when screen id ON");
                        mVerifiedPackages.add(who);
                    }
                }
            }
        }


        public void onAppTaskRemoved(String pkg) {
            XposedLog.verbose("onAppTaskRemoved: " + pkg);
            if (mVerifySettings != null) {
                boolean clearOnTaskRemoved = mVerifySettings.isVerifyOnTaskRemoved();
                if (clearOnTaskRemoved) {
                    mVerifiedPackages.remove(pkg);
                }
            }
        }
    }

}
