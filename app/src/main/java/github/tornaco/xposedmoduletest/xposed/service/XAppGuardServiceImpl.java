package github.tornaco.xposedmoduletest.xposed.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.app.Notification;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;
import android.util.Log;
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
import github.tornaco.xposedmoduletest.IAppGuardWatcher;
import github.tornaco.xposedmoduletest.bean.ComponentReplacement;
import github.tornaco.xposedmoduletest.bean.ComponentReplacementDaoUtil;
import github.tornaco.xposedmoduletest.provider.ComponentsReplacementProvider;
import github.tornaco.xposedmoduletest.xposed.app.XAppGuardManager;
import github.tornaco.xposedmoduletest.xposed.app.XAppVerifyMode;
import github.tornaco.xposedmoduletest.xposed.bean.BlurSettings;
import github.tornaco.xposedmoduletest.xposed.bean.VerifySettings;
import github.tornaco.xposedmoduletest.xposed.repo.RepoProxy;
import github.tornaco.xposedmoduletest.xposed.repo.StringSetRepo;
import github.tornaco.xposedmoduletest.xposed.service.provider.SystemSettings;
import github.tornaco.xposedmoduletest.xposed.util.Closer;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;
import lombok.Synchronized;

import static android.content.Context.KEYGUARD_SERVICE;
import static github.tornaco.xposedmoduletest.xposed.app.XAppGuardManager.Feature.FEATURE_COUNT;

/**
 * Created by guohao4 on 2017/10/23.
 * Email: Tornaco@163.com
 */
class XAppGuardServiceImpl extends XAppGuardServiceAbs {

    private static final long TRANSACTION_EXPIRE_TIME = 60 * 1000;

    private Handler mServiceHandler;

    private AtomicBoolean mEnabled = new AtomicBoolean(false);
    private AtomicBoolean mUninstallProEnabled = new AtomicBoolean(false);
    private AtomicBoolean mDebugEnabled = new AtomicBoolean(false);

    private AtomicBoolean mInterruptFPSuccessVB = new AtomicBoolean(false);
    private AtomicBoolean mInterruptFPERRORVB = new AtomicBoolean(false);

    private VerifySettings mVerifySettings;

    @SuppressLint("UseSparseArrays")
    private final Map<Integer, Transaction> mTransactionMap = new HashMap<>();

    private final Map<ComponentName, ComponentName> mComponentReplacementsMap = new HashMap<>();

    private final Set<String> mFeatures = new HashSet<>(FEATURE_COUNT);

    private static final Set<String> PREBUILT_WHITE_LIST = new HashSet<>();

    private RepoProxy mRepoProxy;

    private final Set<String> mVerifiedPackages = new HashSet<>();
    private final Set<IAppGuardWatcher> mWatchers = new HashSet<>();

    private static int sClientUID = 0;

    @SuppressLint("UseSparseArrays")
    private final Map<Integer, String> mPackagesCache = new HashMap<>();

    private final Holder<String> mTopActivityPkg = new Holder<>();

    static {
        PREBUILT_WHITE_LIST.add("com.android.systemui");
        PREBUILT_WHITE_LIST.add("android");
        PREBUILT_WHITE_LIST.add(BuildConfig.APPLICATION_ID);
    }

    private final ExecutorService mWorkingService = Executors.newCachedThreadPool();

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

    private BroadcastReceiver mPackageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (action == null || intent.getData() == null) {
                // They send us bad action~
                return;
            }

            String packageName = intent.getData().getSchemeSpecificPart();
            if (packageName == null) return;

            switch (action) {
                case Intent.ACTION_PACKAGE_ADDED:
                case Intent.ACTION_PACKAGE_REPLACED:
                    cacheUIDForUs();
                    parsePackageAsync(packageName);
                    break;
                case Intent.ACTION_PACKAGE_REMOVED:
                    boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
                    if (!replacing) {
                        int uid = intent.getIntExtra(Intent.EXTRA_UID, -1);
                        if (uid > 0) {
                            String removed = mPackagesCache.remove(uid);
                            XposedLog.debug("Package uninstalled, remove from cache: " + removed);
                        }
                    }
            }
        }
    };

    private PackageManager mPackageManager;

    // Safe mode is the last clear place user can stay.
    private boolean mIsSafeMode = false;

    private UUID mSerialUUID = UUID.randomUUID();

    @Override
    public void publish() {
        try {
            XposedLog.debug("published by uid: " + Binder.getCallingUid());
            construct();
            ServiceManager.addService(XAppGuardManager.APP_GUARD_SERVICE, asBinder());
            publishFeature(XAppGuardManager.Feature.BASE);
        } catch (Exception e) {
            XposedLog.debug("*** FATAL*** Fail publish our svc:" + e);
        }
    }

    private void checkSafeMode() {
        mIsSafeMode = getContext().getPackageManager().isSafeMode();
    }

    private void construct() {
        mServiceHandler = onCreateServiceHandler();
        if (XposedLog.isVerboseLoggable())
            XposedLog.verbose("construct, mServiceHandler: " + mServiceHandler + " -" + serial());
        mPackageManager = getContext().getPackageManager();
        mRepoProxy = RepoProxy.getProxy();
        XposedLog.verbose("Repo proxy: " + mRepoProxy);
    }

    protected Handler onCreateServiceHandler() {
        return new AppGuardServiceHandlerImpl();
    }

    @Override
    public void systemReady() {
        XposedLog.wtf("systemReady@" + getClass().getSimpleName());
        checkSafeMode();
        cacheUIDForUs();
        cachePackages();

        AsyncTrying.tryTillSuccess(mWorkingService, new AsyncTrying.Once() {
            @Override
            public boolean once() {
                ValueExtra<Boolean, String> res = loadComponentReplacements();
                String extra = res.getExtra();
                if (XposedLog.isVerboseLoggable())
                    XposedLog.verbose("loadComponentReplacements, extra: " + extra);
                return res.getValue();
            }
        }, new Runnable() {
            @Override
            public void run() {
                AsyncTrying.tryTillSuccess(mWorkingService, new AsyncTrying.Once() {
                    @Override
                    public boolean once() {
                        ValueExtra<Boolean, String> res = registerComponentReplacementsObserver();
                        if (XposedLog.isVerboseLoggable())
                            XposedLog.verbose("registerComponentReplacementsObserver, extra: " + res.getExtra());
                        return res.getValue();
                    }
                });
            }
        });

        registerReceiver();
        updateDebugMode();
    }

    @Override
    public void retrieveSettings() {
        XposedLog.wtf("retrieveSettings@" + getClass().getSimpleName());
        getConfigFromSettings();
    }

    private void getConfigFromSettings() {
        try {
            boolean appGuardEnabled = (boolean) SystemSettings.APP_GUARD_ENABLED_B.readFromSystemSettings(getContext());
            mEnabled.set(appGuardEnabled);

            boolean uninstallProEnabled = (boolean) SystemSettings.UNINSTALL_GUARD_ENABLED_B.readFromSystemSettings(getContext());
            mUninstallProEnabled.set(uninstallProEnabled);

            boolean interruptFPS = (boolean) SystemSettings.INTERRUPT_FP_SUCCESS_VB_ENABLED_B.readFromSystemSettings(getContext());
            mInterruptFPSuccessVB.set(interruptFPS);

            boolean interruptFPE = (boolean) SystemSettings.INTERRUPT_FP_ERROR_VB_ENABLED_B.readFromSystemSettings(getContext());
            mInterruptFPERRORVB.set(interruptFPE);

            boolean debug = BuildConfig.DEBUG;
            mDebugEnabled.set(debug);

            ContentResolver resolver = getContext().getContentResolver();
            if (resolver == null) return;
            mVerifySettings = VerifySettings.from(Settings.System.getString(resolver, VerifySettings.KEY_SETTINGS));

            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("mVerifySettings: " + String.valueOf(mVerifySettings));
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("mUninstallProEnabled: " + String.valueOf(mUninstallProEnabled));
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("mInterruptFPSuccessVB: " + String.valueOf(mInterruptFPSuccessVB));
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("mEnabled: " + String.valueOf(mEnabled));
        } catch (Throwable e) {
            XposedLog.wtf("Fail getConfigFromSettings:" + Log.getStackTraceString(e));
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
    }

    synchronized private ValueExtra<Boolean, String> loadComponentReplacements() {
        ContentResolver contentResolver = getContext().getContentResolver();
        if (contentResolver == null) {
            // Happen when early start.
            return new ValueExtra<>(false, "contentResolver is null");
        }
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(ComponentsReplacementProvider.CONTENT_URI, null, null, null, null);
            if (cursor == null) {
                return new ValueExtra<>(false, "cursor is null");
            }

            mComponentReplacementsMap.clear();

            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {

                ComponentReplacement componentReplacement = ComponentReplacementDaoUtil.readEntity(cursor, 0);

                if (XposedLog.isVerboseLoggable())
                    XposedLog.verbose("Lock componentReplacements readEntity of: " + componentReplacement);

                try {
                    ComponentName key = ComponentName.unflattenFromString(componentReplacement.fromFlattenToString());
                    if (key == null) continue;
                    ComponentName value = ComponentName.unflattenFromString(componentReplacement.toFlattenToString());

                    if (XposedLog.isVerboseLoggable()) {
                        XposedLog.verbose("Put replacement: " + value);
                    }
                    mComponentReplacementsMap.put(key, value);
                } catch (Throwable e) {
                    XposedLog.wtf("Fail load comp entry:\n" + Log.getStackTraceString(e));
                }
            }
        } catch (Throwable e) {
            XposedLog.wtf("Fail loadComponentReplacements:\n" + Log.getStackTraceString(e));
        } finally {
            Closer.closeQuietly(cursor);
        }

        return new ValueExtra<>(true, "Read count: " + mComponentReplacementsMap.size());
    }

    private ValueExtra<Boolean, String> registerComponentReplacementsObserver() {
        ContentResolver contentResolver = getContext().getContentResolver();
        if (contentResolver == null) {
            // Happen when early start.
            return new ValueExtra<>(false, "contentResolver is null");
        }
        try {
            contentResolver.registerContentObserver(ComponentsReplacementProvider.CONTENT_URI,
                    false, new ContentObserver(mServiceHandler) {
                        @Override
                        public void onChange(boolean selfChange, Uri uri) {
                            super.onChange(selfChange, uri);
                            mWorkingService.execute(new Runnable() {
                                @Override
                                public void run() {
                                    loadComponentReplacements();
                                }
                            });
                        }
                    });
        } catch (Exception e) {
            XposedLog.wtf("Fail registerContentObserver@ComponentsReplacementProvider:\n" + Log.getStackTraceString(e));
            return new ValueExtra<>(false, String.valueOf(e));
        }
        return new ValueExtra<>(true, "OK");
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
        } catch (Exception ignored) {
            XposedLog.debug("Can not getSingleton UID for our client:" + ignored);
        }
    }

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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        applicationInfo = pm.getApplicationInfo(s, PackageManager.MATCH_UNINSTALLED_PACKAGES);
                    } else {
                        applicationInfo = pm.getApplicationInfo(s, PackageManager.GET_UNINSTALLED_PACKAGES);
                    }
                    int uid = applicationInfo.uid;
                    String pkg = applicationInfo.packageName;
                    if (TextUtils.isEmpty(pkg)) return;

                    if (XposedLog.isVerboseLoggable())
                        XposedLog.verbose("Cached pkg:" + pkg + "-" + uid);
                    mPackagesCache.put(uid, pkg);
                } catch (Exception ignored) {

                }
            }
        });
    }

    private void cachePackages() {
        final PackageManager pm = this.getContext().getPackageManager();

        try {
            // Filter all apps.
            List<ApplicationInfo> applicationInfos =
                    android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N ?
                            pm.getInstalledApplications(android.content.pm.PackageManager.MATCH_UNINSTALLED_PACKAGES)
                            : pm.getInstalledApplications(android.content.pm.PackageManager.GET_UNINSTALLED_PACKAGES);

            Collections.consumeRemaining(applicationInfos,
                    new Consumer<ApplicationInfo>() {
                        @Override
                        public void accept(ApplicationInfo applicationInfo) {
                            String pkg = applicationInfo.packageName;
                            int uid = applicationInfo.uid;
                            if (TextUtils.isEmpty(pkg)) return;

                            // Add to package cache.
                            mPackagesCache.put(uid, pkg);
                        }
                    });
        } catch (Exception ignored) {
            XposedLog.debug("Can not cachePackages:" + ignored);
        }
    }

    @Override
    @Synchronized
    public void publishFeature(String f) {
        if (!mFeatures.contains(f)) {
            mFeatures.add(f);
        }
    }

    @Override
    public void shutdown() {
        XposedLog.debug("shutdown...");
    }

    @Override
    public boolean interruptPackageRemoval(String pkg) {
        boolean enabled = isUninstallInterruptEnabled();

        if (enabled && BuildConfig.APPLICATION_ID.equals(pkg)) {
            return true;
        }

        if (!enabled) {
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("interruptPackageRemoval false: not enabled.");
            return false;
        }
        boolean confirm = onInterruptConfirm(pkg);
        if (!confirm) {
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("interruptPackageRemoval false: not confirmed.");
            return false;
        }
        return true;
    }

    @Override
    @InternalCall
    public Intent checkIntent(Intent from) {
        if (XposedLog.isVerboseLoggable()) XposedLog.verbose("checkIntent: " + from);
        ComponentName fromComp = from.getComponent();
        if (fromComp == null) return from;

        if (!mComponentReplacementsMap.containsKey(fromComp)) {
            return from;
        }

        ComponentName toComp = mComponentReplacementsMap.get(fromComp);

        if (!validateComponentName(toComp)) {
            XposedLog.debug("Invalid component replacement: " + toComp);
            return from;
        }

        if (XposedLog.isVerboseLoggable()) XposedLog.verbose("Replacing using: " + toComp);
        if (toComp == null) return null;

        return from.setComponent(toComp);
    }

    @Override
    public long wrapCallingUidForIntent(long from, Intent intent) {
        return from;
    }

    private static boolean validateComponentName(ComponentName componentName) {
        return true;
    }

    @Override
    @BinderCall
    public void addOrRemoveComponentReplacement(ComponentName from, ComponentName to, boolean add)
            throws RemoteException {
        if (XposedLog.isVerboseLoggable()) {
            XposedLog.verbose("addOrRemoveComponentReplacement, from: "
                    + from + ", to: " + to + ", add? " + add);
        }

        enforceCallingPermissions();
        Preconditions.checkNotNull(from);

        Object[] data = new Object[3];
        data[0] = from;
        data[1] = to;
        data[2] = add;

        mServiceHandler.obtainMessage(AppGuardServiceHandlerMessages.MSG_ADDORREMOVECOMPONENTREPLACEMENT
                , data).sendToTarget();

        // Insert into db.
        ComponentReplacement replacement = new ComponentReplacement();
        replacement.setAppPackageName(from.getPackageName());

        replacement.setCompFromClassName(from.getClassName());
        replacement.setCompFromPackageName(from.getPackageName());

        replacement.setCompToClassName(to == null ? null : to.getClassName());
        replacement.setCompToPackageName(to == null ? null : to.getPackageName());

        if (add) {
            ComponentsReplacementProvider.insertOrUpdate(getContext(), replacement);
        } else {
            ComponentsReplacementProvider.delete(getContext(), replacement);
        }
    }

    @Override
    protected void dump(FileDescriptor fd, final PrintWriter fout, String[] args) {
        super.dump(fd, fout, args);
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

    public PackageManager getPackageManager() {
        if (mPackageManager == null) mPackageManager = getContext().getPackageManager();
        return mPackageManager;
    }

    private boolean onInterruptConfirm(String pkg) {
        if (pkg == null) {
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("onEarlyVerifyConfirm, false@pkg-null");
            return false;
        }
        if (mIsSafeMode) {
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("onEarlyVerifyConfirm, false@safe-mode:" + pkg);
            return false;
        }
        if (!mEnabled.get()) {
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("onEarlyVerifyConfirm, false@disabled:" + pkg);
            return false;
        }
        if (PREBUILT_WHITE_LIST.contains(pkg)) {
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("onEarlyVerifyConfirm, false@prebuilt-w-list:" + pkg);
            return false;
        } // White list.

        boolean inUserSetList = isPackageInLockList(pkg);
        if (!inUserSetList) {
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("onEarlyVerifyConfirm, false@not-in-guard-list:" + pkg);
            return false;
        }
        return true;
    }

    private boolean isPackageInLockList(String pkg) {
        return mRepoProxy.getLocks().has(pkg);
    }

    private boolean isPackageInBlurList(String pkg) {
        return mRepoProxy.getBlurs().has(pkg);
    }

    @Override
    public boolean onEarlyVerifyConfirm(String pkg) {
        if (pkg == null) {
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("onEarlyVerifyConfirm, false@pkg-null");
            return false;
        }
        if (mIsSafeMode) {
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("onEarlyVerifyConfirm, false@safe-mode:" + pkg);
            return false;
        }
        if (!mEnabled.get()) {
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("onEarlyVerifyConfirm, false@disabled:" + pkg);
            return false;
        }
        if (PREBUILT_WHITE_LIST.contains(pkg)) {
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("onEarlyVerifyConfirm, false@prebuilt-w-list:" + pkg);
            return false;
        } // White list.

        if (mVerifiedPackages.contains(pkg)) {
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("onEarlyVerifyConfirm, false@in-verified-list:" + pkg);
            return false;
        } // Passed.

        boolean inUserSetList = isPackageInLockList(pkg);
        if (!inUserSetList) {
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("onEarlyVerifyConfirm, false@not-in-guard-list:" + pkg);
            return false;
        }
        return true;
    }

    @Override
    @InternalCall
    public void verify(Bundle options, String pkg, int uid, int pid, VerifyListener listener) {
        verifyInternal(options, pkg, uid, pid, false, listener);
    }

    private void verifyInternal(Bundle options, String pkg, int uid, int pid,
                                boolean injectHomeOnFail, VerifyListener listener) {
        if (XposedLog.isVerboseLoggable()) XposedLog.verbose("verifyInternal: " + pkg);
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


    @Override
    @BinderCall(restrict = "hooks")
    public void onPackageMoveToFront(Intent who) {
        if (XposedLog.isVerboseLoggable()) XposedLog.verbose("onPackageMoveToFront: " + who);
        onActivityPackageResume(PkgUtil.packageNameOf(who));
    }

    @Override
    @BinderCall(restrict = "anyone")
    public void onActivityPackageResume(String pkg) {
        if (mServiceHandler == null) return;
        mServiceHandler.obtainMessage(AppGuardServiceHandlerMessages.MSG_ONACTIVITYPACKAGERESUME, pkg).sendToTarget();
    }

    @Override
    public boolean isInterruptFPEventVBEnabled(int event) throws RemoteException {
        enforceCallingPermissions();
        switch (event) {
            case XAppGuardManager.FPEvent.SUCCESS:
                return interruptFPSuccessVibrate();
            case XAppGuardManager.FPEvent.ERROR:
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

    @Override
    @BinderCall
    public void setInterruptFPEventVBEnabled(int event, boolean enabled) throws RemoteException {
        enforceCallingPermissions();
        mServiceHandler.obtainMessage(AppGuardServiceHandlerMessages
                .MSG_SETINTERRUPTFPEVENTVBENABLED, event, event, enabled)
                .sendToTarget();
    }

    @Override
    public String serial() {
        return mSerialUUID.toString();
    }

    @Override
    public boolean isBlurForPkg(String pkg) {
        return isPackageInBlurList(pkg);
    }

    @Override
    @InternalCall
    public float getBlurScale() {
        return BlurSettings.BITMAP_SCALE;
    }

    @Override
    @InternalCall
    public int getBlurRadius() {
        return BlurSettings.BLUR_RADIUS;
    }

    @Override
    public boolean interruptFPSuccessVibrate() {
        boolean isKeyguard = isDeviceLocked();
        if (XposedLog.isVerboseLoggable()) XposedLog.verbose("Device is locked: " + isKeyguard);
        return !isKeyguard && mInterruptFPSuccessVB.get();
    }

    @Override
    public boolean interruptFPErrorVibrate() {
        boolean isKeyguard = isDeviceLocked();
        if (XposedLog.isVerboseLoggable()) XposedLog.verbose("Device is locked: " + isKeyguard);
        return !isKeyguard && mInterruptFPERRORVB.get();
    }

    @Override
    public boolean isActivityStartShouldBeInterrupted(ComponentName componentName) {
        return false;
    }

    @Override
    public void forceReloadPackages() throws RemoteException {
        enforceCallingPermissions();

        mWorkingService.execute(new Runnable() {
            @Override
            public void run() {
                cachePackages();
                // Remove onwer package to fix previous bugs.
                try {
                    mRepoProxy.getLocks().remove(BuildConfig.APPLICATION_ID);
                } catch (Throwable e) {
                    XposedLog.wtf("Fail remove owner package from repo: " + Log.getStackTraceString(e));
                }
            }
        });
    }

    private void addOrRemoveFromRepo(String[] packages, StringSetRepo repo, boolean add) {
        long id = Binder.clearCallingIdentity();
        try {
            for (String p : packages) {
                if (add) repo.add(p);
                else repo.remove(p);
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
            if (o == null) continue;
            String pkg = String.valueOf(o);
            out[i] = pkg;
        }
        return out;
    }

    @Override
    public String[] getLockApps(boolean lock) throws RemoteException {
        if (lock) {
            Set<String> packages = mRepoProxy.getLocks().getAll();
            if (packages.size() == 0) {
                return new String[0];
            }
            return convertObjectArrayToStringArray(packages.toArray());
        } else {
            Collection<String> packages = mPackagesCache.values();
            if (packages.size() == 0) {
                return new String[0];
            }

            final List<String> outList = Lists.newArrayList();

            // Remove those not in blocked list.
            String[] allPackagesArr = convertObjectArrayToStringArray(packages.toArray());
            Collections.consumeRemaining(allPackagesArr, new Consumer<String>() {
                @Override
                public void accept(String s) {
                    if (outList.contains(s)) return;// Kik dup package.
                    if (isPackageInLockList(s)) return;
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

    @Override
    public void addOrRemoveLockApps(String[] packages, boolean add) throws RemoteException {
        if (XposedLog.isVerboseLoggable())
            XposedLog.verbose("addOrRemoveLockApps: " + Arrays.toString(packages));
        enforceCallingPermissions();
        if (packages == null || packages.length == 0) return;
        addOrRemoveFromRepo(packages, mRepoProxy.getLocks(), add);
    }

    @Override
    public String[] getBlurApps(boolean blur) throws RemoteException {
        if (blur) {
            Set<String> packages = mRepoProxy.getBlurs().getAll();
            if (packages.size() == 0) {
                return new String[0];
            }
            return convertObjectArrayToStringArray(packages.toArray());
        } else {
            Collection<String> packages = mPackagesCache.values();
            if (packages.size() == 0) {
                return new String[0];
            }

            final List<String> outList = Lists.newArrayList();

            // Remove those not in blocked list.
            String[] allPackagesArr = convertObjectArrayToStringArray(packages.toArray());
            Collections.consumeRemaining(allPackagesArr, new Consumer<String>() {
                @Override
                public void accept(String s) {
                    if (outList.contains(s)) return;// Kik dup package.
                    if (isPackageInBlurList(s)) return;
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

    @Override
    public void addOrRemoveBlurApps(String[] packages, boolean blur) throws RemoteException {
        if (XposedLog.isVerboseLoggable())
            XposedLog.verbose("addOrRemoveBlurApps: " + Arrays.toString(packages));
        enforceCallingPermissions();
        if (packages == null || packages.length == 0) return;
        addOrRemoveFromRepo(packages, mRepoProxy.getBlurs(), blur);
    }

    @Override
    @BinderCall
    public boolean isEnabled() {
        enforceCallingPermissions();
        return !mIsSafeMode && mEnabled.get();
    }

    @Override
    @BinderCall
    public void setEnabled(boolean enabled) throws RemoteException {
        enforceCallingPermissions();
        mServiceHandler.removeMessages(AppGuardServiceHandlerMessages.MSG_SETENABLED);
        mServiceHandler.obtainMessage(AppGuardServiceHandlerMessages.MSG_SETENABLED,
                enabled ? 1 : 0, 0)
                .sendToTarget();
    }

    @Override
    @BinderCall
    public boolean isUninstallInterruptEnabled() {
        enforceCallingPermissions();
        return isEnabled() && mUninstallProEnabled.get();
    }

    @Override
    @BinderCall
    public void setUninstallInterruptEnabled(boolean enabled) throws RemoteException {
        enforceCallingPermissions();
        mServiceHandler.removeMessages(AppGuardServiceHandlerMessages.MSG_SETUNINSTALLINTERRUPTENABLED);
        mServiceHandler.obtainMessage(
                AppGuardServiceHandlerMessages.MSG_SETUNINSTALLINTERRUPTENABLED,
                enabled ? 1 : 0, 0)
                .sendToTarget();
    }

    @Override
    @BinderCall
    public void setVerifySettings(VerifySettings settings) throws RemoteException {
        enforceCallingPermissions();
        mServiceHandler.removeMessages(AppGuardServiceHandlerMessages.MSG_SETVERIFYSETTINGS);
        mServiceHandler.obtainMessage(AppGuardServiceHandlerMessages.MSG_SETVERIFYSETTINGS, settings).sendToTarget();
    }

    @Override
    @BinderCall
    public VerifySettings getVerifySettings() throws RemoteException {
        enforceCallingPermissions();
        return mVerifySettings == null ? null : mVerifySettings.duplicate();
    }

    @Override
    @BinderCall
    public void setResult(int transactionID, int res) throws RemoteException {
        enforceCallingPermissions();
        mServiceHandler.obtainMessage(AppGuardServiceHandlerMessages.MSG_SETRESULT, transactionID, res).sendToTarget();
    }

    @Override
    public boolean isTransactionValid(int transactionID) {
        enforceCallingPermissions();
        Transaction transaction = mTransactionMap.get(transactionID);
        return transaction != null;
    }

    @Override
    @BinderCall
    public void watch(IAppGuardWatcher w) throws RemoteException {
        XposedLog.debug("iWatcher.watch-" + w);
        enforceCallingPermissions();
        mServiceHandler.obtainMessage(AppGuardServiceHandlerMessages.MSG_WATCH, w).sendToTarget();
    }

    @Override
    @BinderCall
    public void unWatch(IAppGuardWatcher w) throws RemoteException {
        XposedLog.debug("iWatcher.unWatch-" + w);
        mServiceHandler.obtainMessage(AppGuardServiceHandlerMessages.MSG_UNWATCH, w).sendToTarget();
    }

    @Override
    @BinderCall
    public void mockCrash() throws RemoteException {
        enforceCallingPermissions();
        mServiceHandler.sendEmptyMessage(AppGuardServiceHandlerMessages.MSG_MOCKCRASH);
    }

    @Override
    @BinderCall
    public void setVerifierPackage(String pkg) throws RemoteException {
        enforceCallingPermissions();
        // TODO.
    }

    @Override
    @Deprecated
    public void injectHomeEvent() throws RemoteException {
        throw new IllegalStateException("injectHomeEvent is Deprecated api");
    }

    @Override
    public void setDebug(boolean debug) throws RemoteException {
        enforceCallingPermissions();
        mServiceHandler.obtainMessage(AppGuardServiceHandlerMessages.MSG_SETDEBUG, debug).sendToTarget();
    }


    // Warn user if dev mode is open.
    private final static int NOTIFICATION_ID = 20182018;

    private void updateDebugMode() {
        XposedLog.setLogLevel(mDebugEnabled.get() ? XposedLog.LogLevel.ALL : XposedLog.LogLevel.WARN);

        boolean isDevMode = mDebugEnabled.get();
        if (isDevMode) {
            try {
                NotificationManagerCompat.from(getContext()).cancel(NOTIFICATION_ID);
                NotificationManagerCompat.from(getContext())
                        .notify(NOTIFICATION_ID,
                                new Notification.Builder(getContext())
                                        .setOngoing(true)
                                        .setContentTitle("应用管理")
                                        .setContentText("调试模式已经打开。")
                                        .setSmallIcon(android.R.drawable.stat_sys_warning)
                                        .build());
            } catch (Throwable e) {
                Toast.makeText(getContext(),
                        "应用管理 调试模式已经打开，如果使用完毕请及时关闭，否则会新增加耗电，造成系统卡顿。", Toast.LENGTH_LONG).show();
            }
        } else {
            NotificationManagerCompat.from(getContext()).cancel(NOTIFICATION_ID);
        }
    }

    @Override
    public boolean isDebug() throws RemoteException {
        enforceCallingPermissions();
        return mDebugEnabled.get();
    }

    protected void enforceCallingPermissions() {
        int callingUID = Binder.getCallingUid();
        if (XposedLog.isVerboseLoggable())
            XposedLog.verbose("enforceCallingPermissions@uid:" + callingUID);
        if (callingUID == android.os.Process.myUid() || (sClientUID > 0 && sClientUID == callingUID)) {
            return;
        }
        throw new SecurityException("Package of uid:" + callingUID
                + ", does not require permission to interact with XAppGuardServiceImpl");
    }

    private void onScreenOff() {
        if (mVerifySettings != null) {
            boolean verifyOnScreenOff = mVerifySettings.isVerifyOnScreenOff();
            if (verifyOnScreenOff) {
                if (XposedLog.isVerboseLoggable())
                    XposedLog.verbose("SCREEN OFF, Clearing passed pkgs...");
                mVerifiedPackages.clear();
            }
        }
    }

    private void onUserPresent() {
        mServiceHandler.obtainMessage(AppGuardServiceHandlerMessages.MSG_ONUSERPRESENT).sendToTarget();
    }

    private static Intent buildVerifyIntent(boolean injectHome, int transId, String pkg) {
        Intent intent = new Intent(XAppGuardManager.ACTION_APP_GUARD_VERIFY_DISPLAYER);
        intent.setClassName(BuildConfig.APPLICATION_ID,
                "github.tornaco.xposedmoduletest.ui.activity.ag.VerifyDisplayerActivity");
        intent.putExtra(XAppGuardManager.EXTRA_PKG_NAME, pkg);
        intent.putExtra(XAppGuardManager.EXTRA_TRANS_ID, transId);
        intent.putExtra(XAppGuardManager.EXTRA_INJECT_HOME_WHEN_FAIL_ID, injectHome);
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

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int wht = msg.what;
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("handleMessage@" + AppGuardServiceHandlerMessages.decodeMessage(wht));
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
                case AppGuardServiceHandlerMessages.MSG_SETVERIFYSETTINGS:
                    AppGuardServiceHandlerImpl.this.setVerifySettings((VerifySettings) msg.obj);
                    break;
                case AppGuardServiceHandlerMessages.MSG_UNWATCH:
                    AppGuardServiceHandlerImpl.this.unWatch((IAppGuardWatcher) msg.obj);
                    break;
                case AppGuardServiceHandlerMessages.MSG_WATCH:
                    AppGuardServiceHandlerImpl.this.watch((IAppGuardWatcher) msg.obj);
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
                case AppGuardServiceHandlerMessages.MSG_ADDORREMOVECOMPONENTREPLACEMENT:
                    Object[] data = (Object[]) msg.obj;
                    AppGuardServiceHandlerImpl.this.addOrRemoveComponentReplacement((ComponentName) data[0],
                            (ComponentName) data[1], (Boolean) data[2]);
                    break;
                default:
                    AppGuardServiceHandlerImpl.this.setResult((Integer) msg.obj,
                            XAppVerifyMode.MODE_IGNORED);
                    break;
            }
        }

        @Override
        public void setEnabled(boolean enabled) {
            if (mEnabled.compareAndSet(!enabled, enabled)) {
                SystemSettings.APP_GUARD_ENABLED_B.writeToSystemSettings(getContext(), enabled);
            }
        }

        @Override
        public void setUninstallInterruptEnabled(boolean enabled) {
            if (mUninstallProEnabled.compareAndSet(!enabled, enabled)) {
                SystemSettings.UNINSTALL_GUARD_ENABLED_B.writeToSystemSettings(getContext(), enabled);
            }
        }

        @Override
        public void setVerifySettings(final VerifySettings settings) {
            mVerifySettings = Preconditions.checkNotNull(settings);
            mWorkingService.execute(new Runnable() {
                @Override
                public void run() {
                    // Saving to db.
                    ContentResolver resolver = getContext().getContentResolver();
                    Settings.System.putString(resolver, VerifySettings.KEY_SETTINGS, settings.formatJson());
                }
            });
        }

        @Override
        public void setResult(int transactionID, int res) {
            Transaction transaction = mTransactionMap.remove(transactionID);
            if (transaction == null) {
                XposedLog.debug("Can not find transaction for:" + transactionID);
                return;
            }
            if (res == XAppVerifyMode.MODE_ALLOWED) {
                mVerifiedPackages.add(transaction.pkg);
            }
            transaction.listener.onVerifyRes(transaction.pkg, transaction.uid, transaction.pid, res);
            mServiceHandler.removeMessages(MSG_TRANSACTION_EXPIRE_BASE + transactionID);
        }

        @Override
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
            } catch (ActivityNotFoundException anf) {
                XposedLog.debug("*** FATAL ERROR *** ActivityNotFoundException!!!");
                setResult(tid, XAppVerifyMode.MODE_ALLOWED);
            }
        }

        private void onNewTransaction(int transaction) {
            sendMessageDelayed(obtainMessage(MSG_TRANSACTION_EXPIRE_BASE + transaction, transaction), TRANSACTION_EXPIRE_TIME);
        }

        @Override
        public void watch(IAppGuardWatcher w) {
            Preconditions.checkNotNull(w);
            synchronized (mWatchers) { //FIXME Link to death~~~
                if (!mWatchers.contains(w)) {
                    mWatchers.add(w);
                    XposedLog.debug("iWatcher.watch-OK " + w);
                }
            }
        }

        @Override
        public void unWatch(IAppGuardWatcher w) {
            enforceCallingPermissions();
            Preconditions.checkNotNull(w);
            synchronized (mWatchers) { //FIXME Link to death~~~
                if (mWatchers.contains(w)) {
                    mWatchers.remove(w);
                    XposedLog.debug("iWatcher.unWatch-OK " + w);
                }
            }
        }

        @Override
        public void mockCrash() {
            throw new IllegalStateException("Let's CRASH, bye bye you...");
        }

        @Override
        public void setDebug(boolean debug) {
            if (mDebugEnabled.compareAndSet(!debug, debug)) {
                SystemSettings.APP_GUARD_DEBUG_MODE_B_S.writeToSystemSettings(getContext(), debug);
            }
            updateDebugMode();
        }

        @Override
        public void onActivityPackageResume(String pkg) {
            onAppSwitchedTo(pkg);
            mTopActivityPkg.setData(pkg);
        }

        @Override
        public void onUserPresent() {
            String pkg = mTopActivityPkg.getData();
            if (pkg == null) {
                return;
            }
            if (!onEarlyVerifyConfirm(pkg)) {
                return;
            }
            verifyInternal(null, pkg, 0, 0, true, VerifyListenerAdapter.getDefault());
        }

        @Override
        public void setInterruptFPEventVBEnabled(int event, boolean enabled) {
            switch (event) {
                case XAppGuardManager.FPEvent.SUCCESS:
                    if (mInterruptFPSuccessVB.compareAndSet(!enabled, enabled)) {
                        SystemSettings.INTERRUPT_FP_SUCCESS_VB_ENABLED_B.writeToSystemSettings(getContext(), enabled);
                    }
                    break;
                case XAppGuardManager.FPEvent.ERROR:
                    if (mInterruptFPERRORVB.compareAndSet(!enabled, enabled)) {
                        SystemSettings.INTERRUPT_FP_ERROR_VB_ENABLED_B.writeToSystemSettings(getContext(), enabled);
                    }
                    break;
                default:
                    break;
            }
        }

        @Override
        public void addOrRemoveComponentReplacement(ComponentName from, ComponentName to, boolean add) {
            if (add) {
                mComponentReplacementsMap.put(from, to);
            } else {
                mComponentReplacementsMap.remove(from);
            }
        }

        /**
         * @param who Only Keep the switched package.
         */
        private void onAppSwitchedTo(String who) {
            if (mVerifySettings != null) {
                boolean clearOnAppSwitch = mVerifySettings.isVerifyOnAppSwitch();
                if (clearOnAppSwitch) {
                    mVerifiedPackages.clear();
                    mVerifiedPackages.add(who);
                }
            }
        }
    }

}
