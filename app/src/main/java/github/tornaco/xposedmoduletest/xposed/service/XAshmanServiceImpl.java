package github.tornaco.xposedmoduletest.xposed.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
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
import android.net.NetworkPolicyManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import com.android.internal.os.Zygote;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;

import github.tornaco.android.common.Collections;
import github.tornaco.android.common.Consumer;
import github.tornaco.android.common.Holder;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.IAshmanWatcher;
import github.tornaco.xposedmoduletest.IProcessClearListener;
import github.tornaco.xposedmoduletest.bean.AutoStartPackage;
import github.tornaco.xposedmoduletest.bean.AutoStartPackageDaoUtil;
import github.tornaco.xposedmoduletest.bean.BootCompletePackage;
import github.tornaco.xposedmoduletest.bean.BootCompletePackageDaoUtil;
import github.tornaco.xposedmoduletest.bean.LockKillPackage;
import github.tornaco.xposedmoduletest.bean.LockKillPackageDaoUtil;
import github.tornaco.xposedmoduletest.bean.RFKillPackage;
import github.tornaco.xposedmoduletest.bean.RFKillPackageDaoUtil;
import github.tornaco.xposedmoduletest.provider.AutoStartPackageProvider;
import github.tornaco.xposedmoduletest.provider.BootPackageProvider;
import github.tornaco.xposedmoduletest.provider.LockKillPackageProvider;
import github.tornaco.xposedmoduletest.provider.RFKillPackageProvider;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;
import github.tornaco.xposedmoduletest.xposed.bean.BlockRecord2;
import github.tornaco.xposedmoduletest.xposed.service.provider.SystemSettings;
import github.tornaco.xposedmoduletest.xposed.util.Closer;
import github.tornaco.xposedmoduletest.xposed.util.FileUtil;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;
import github.tornaco.xposedmoduletest.xposed.util.XPosedLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.content.pm.PackageManager.MATCH_UNINSTALLED_PACKAGES;

/**
 * Created by guohao4 on 2017/11/9.
 * Email: Tornaco@163.com
 */

public class XAshmanServiceImpl extends XAshmanServiceAbs {

    private static final boolean DEBUG_BROADCAST = true;
    private static final boolean DEBUG_SERVICE = true;

    private static final Set<String> WHITE_LIST = new HashSet<>();
    // Installed in system/, not contains system-packages and persist packages.
    private static final Set<String> SYSTEM_APPS = new HashSet<>();

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
        WHITE_LIST.add("com.android.smspush");
        WHITE_LIST.add("com.android.providers.downloads.ui");
        WHITE_LIST.add("com.android.providers.contacts");
        WHITE_LIST.add("com.android.providers.media");
        WHITE_LIST.add("com.android.providers.calendar");
        WHITE_LIST.add("com.android.vending");
        // FIXME???
        WHITE_LIST.add("com.ghostflying.locationreportenabler");
    }

    private UUID mSerialUUID = UUID.randomUUID();

    private static int sClientUID = 0;

    private final ExecutorService mWorkingService = Executors.newCachedThreadPool();
    private final ExecutorService mLoggingService = Executors.newSingleThreadExecutor();

    @SuppressLint("UseSparseArrays")
    private final Map<Integer, String> mPackagesCache = new HashMap<>();

    private final Map<String, BlockRecord2> mBlockRecords = new HashMap<>();

    private Handler h, lazyH;

    private AtomicBoolean mBootBlockEnabled = new AtomicBoolean(false);
    private AtomicBoolean mStartBlockEnabled = new AtomicBoolean(false);
    private AtomicBoolean mLockKillEnabled = new AtomicBoolean(false);
    private AtomicBoolean mRootActivityFinishKillEnabled = new AtomicBoolean(false);
    private AtomicBoolean mCompSettingBlockEnabled = new AtomicBoolean(false);

    private long mLockKillDelay;

    private final Map<String, BootCompletePackage> mBootWhiteListPackages = new HashMap<>();
    private final Map<String, AutoStartPackage> mStartWhiteListPackages = new HashMap<>();
    private final Map<String, LockKillPackage> mLockKillWhileListPackages = new HashMap<>();
    private final Map<String, RFKillPackage> mRFKillWhileListPackages = new HashMap<>();

    private final Set<AshManHandler.WatcherClient> mWatcherClients = new HashSet<>();

    // Safe mode is the last clear place user can stay.
    private boolean mIsSafeMode = false;

    private boolean mIsSystemReady = false;

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
        h.sendEmptyMessage(AshManHandlerMessages.MSG_ONSCREENON);
    }

    private void onScreenOff() {
        h.sendEmptyMessage(AshManHandlerMessages.MSG_ONSCREENOFF);
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

                    XPosedLog.verbose("Cached pkg:" + pkg + "-" + uid);
                    mPackagesCache.put(uid, pkg);

                    if (isIME(pkg)) {
                        addToWhiteList(pkg);
                    }
                    if (PkgUtil.isHomeApp(getContext(), pkg)) {
                        addToWhiteList(pkg);
                    }
                    if (PkgUtil.isDefaultSmsApp(getContext(), pkg)) {
                        addToWhiteList(pkg);
                    }
                } catch (Exception ignored) {

                }
            }
        });
    }

    private void cachePackages() {
        final PackageManager pm = this.getContext().getPackageManager();

        // Retrieve our package first.
        try {
            ApplicationInfo applicationInfo = pm.getApplicationInfo(BuildConfig.APPLICATION_ID, 0);
            sClientUID = applicationInfo.uid;
            XPosedLog.verbose("Our client app uid: " + sClientUID);
        } catch (PackageManager.NameNotFoundException e) {
            XPosedLog.debug("Can not get client UID for our client:" + e);
        }

        try {
            // Filter all apps.
            List<ApplicationInfo> applicationInfos =
                    android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N ?
                            pm.getInstalledApplications(MATCH_UNINSTALLED_PACKAGES)
                            : pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
            Collections.consumeRemaining(applicationInfos,
                    new Consumer<ApplicationInfo>() {
                        @Override
                        public void accept(ApplicationInfo applicationInfo) {
                            String pkg = applicationInfo.packageName;
                            int uid = applicationInfo.uid;
                            if (TextUtils.isEmpty(pkg)) return;

                            // Add to package cache.
                            mPackagesCache.put(uid, pkg);

                            // Add system apps to system list.
                            boolean isSystemApp = (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
                            if (isSystemApp) {

                                // Check if persist.
                                boolean isPersist = (applicationInfo.flags & ApplicationInfo.FLAG_PERSISTENT) != 0;
                                if (isPersist) {
                                    addToWhiteList(pkg);
                                    return;
                                }
                                // Check if is system package.
                                try {
                                    @SuppressLint("PackageManagerGetSignatures") boolean isSystemPackage =
                                            PkgUtil.isSystemPackage(getContext().getResources(),
                                                    pm,
                                                    pm.getPackageInfo(pkg, PackageManager.GET_SIGNATURES));
                                    if (isSystemPackage) {
                                        XPosedLog.verbose("Adding system package: " + pkg);
                                        addToWhiteList(pkg);
                                        return;
                                    }

                                } catch (Throwable e) {
                                    XPosedLog.wtf("Fail check isSystemPackage: " + Log.getStackTraceString(e));
                                }

                                addToSystemApps(pkg);
                            }

                            if (PkgUtil.isHomeApp(getContext(), pkg)) {
                                addToWhiteList(pkg);
                            }

                            if (PkgUtil.isDefaultSmsApp(getContext(), pkg)) {
                                addToWhiteList(pkg);
                            }

                            if (isIME(pkg)) {
                                addToWhiteList(pkg);
                            }
                        }
                    });
        } catch (Exception ignored) {
            XPosedLog.debug("Can not getSingleton UID for our client:" + ignored);
        }
    }

    synchronized private ValueExtra<Boolean, String> loadBootPackageSettings() {
        ContentResolver contentResolver = getContext().getContentResolver();
        if (contentResolver == null) {
            // Happen when early start.
            return new ValueExtra<>(false, "contentResolver is null");
        }
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(BootPackageProvider.CONTENT_URI, null, null, null, null);
            if (cursor == null) {
                return new ValueExtra<>(false, "cursor is null");
            }

            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                BootCompletePackage bootCompletePackage = BootCompletePackageDaoUtil.readEntity(cursor, 0);
                XPosedLog.verbose("Boot pkg reader readEntity of: " + bootCompletePackage);
                String key = bootCompletePackage.getPkgName();
                if (TextUtils.isEmpty(key)) continue;
                mBootWhiteListPackages.put(key, bootCompletePackage);
            }
        } catch (Throwable e) {
            XPosedLog.wtf("Fail query boot pkgs:\n" + Log.getStackTraceString(e));
            return new ValueExtra<>(false, String.valueOf(e));
        } finally {
            Closer.closeQuietly(cursor);
        }
        return new ValueExtra<>(true, "Read count: " + mBootWhiteListPackages.size());
    }

    synchronized private ValueExtra<Boolean, String> loadStartPackageSettings() {
        ContentResolver contentResolver = getContext().getContentResolver();
        if (contentResolver == null) {
            // Happen when early start.
            return new ValueExtra<>(false, "contentResolver is null");
        }
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(AutoStartPackageProvider.CONTENT_URI, null, null, null, null);
            if (cursor == null) {
                return new ValueExtra<>(false, "cursor is null");
            }

            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                AutoStartPackage autoStartPackage = AutoStartPackageDaoUtil.readEntity(cursor, 0);
                XPosedLog.verbose("Start white list pkg reader readEntity of: " + autoStartPackage);
                String key = autoStartPackage.getPkgName();
                if (TextUtils.isEmpty(key)) continue;
                mStartWhiteListPackages.put(key, autoStartPackage);
            }
        } catch (Throwable e) {
            XPosedLog.wtf("Fail query start pkgs:\n" + Log.getStackTraceString(e));
            return new ValueExtra<>(false, String.valueOf(e));
        } finally {
            Closer.closeQuietly(cursor);
        }

        return new ValueExtra<>(true, "Read count: " + mStartWhiteListPackages.size());
    }

    synchronized private ValueExtra<Boolean, String> loadLockKillPackageSettings() {
        ContentResolver contentResolver = getContext().getContentResolver();
        if (contentResolver == null) {
            // Happen when early start.
            return new ValueExtra<>(false, "contentResolver is null");
        }
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(LockKillPackageProvider.CONTENT_URI, null, null, null, null);
            if (cursor == null) {
                return new ValueExtra<>(false, "cursor is null");
            }

            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                LockKillPackage lockKillPackage = LockKillPackageDaoUtil.readEntity(cursor, 0);
                XPosedLog.verbose("Lock kill white list pkg reader readEntity of: " + lockKillPackage);
                String key = lockKillPackage.getPkgName();
                if (TextUtils.isEmpty(key)) continue;
                mLockKillWhileListPackages.put(key, lockKillPackage);
            }
        } catch (Throwable e) {
            XPosedLog.wtf("Fail query lk pkgs:\n" + Log.getStackTraceString(e));
        } finally {
            Closer.closeQuietly(cursor);
        }

        return new ValueExtra<>(true, "Read count: " + mLockKillWhileListPackages.size());
    }

    synchronized private ValueExtra<Boolean, String> loadRFKillPackageSettings() {
        ContentResolver contentResolver = getContext().getContentResolver();
        if (contentResolver == null) {
            // Happen when early start.
            return new ValueExtra<>(false, "contentResolver is null");
        }
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(RFKillPackageProvider.CONTENT_URI, null, null, null, null);
            if (cursor == null) {
                return new ValueExtra<>(false, "cursor is null");
            }

            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                RFKillPackage rfKillPackage = RFKillPackageDaoUtil.readEntity(cursor, 0);
                XPosedLog.verbose("RF kill white list pkg reader readEntity of: " + rfKillPackage);
                String key = rfKillPackage.getPkgName();
                if (TextUtils.isEmpty(key)) continue;
                mRFKillWhileListPackages.put(key, rfKillPackage);
            }
        } catch (Throwable e) {
            XPosedLog.wtf("Fail query rf pkgs:\n" + Log.getStackTraceString(e));
        } finally {
            Closer.closeQuietly(cursor);
        }

        return new ValueExtra<>(true, "Read count: " + mRFKillWhileListPackages.size());
    }

    private ValueExtra<Boolean, String> registerBootPackageObserver() {
        ContentResolver contentResolver = getContext().getContentResolver();
        if (contentResolver == null) {
            // Happen when early start.
            return new ValueExtra<>(false, "contentResolver is null");
        }
        try {
            contentResolver.registerContentObserver(BootPackageProvider.CONTENT_URI,
                    false, new ContentObserver(h) {
                        @Override
                        public void onChange(boolean selfChange, Uri uri) {
                            super.onChange(selfChange, uri);
                            mWorkingService.execute(new Runnable() {
                                @Override
                                public void run() {
                                    loadBootPackageSettings();
                                }
                            });
                        }
                    });
        } catch (Exception e) {
            XPosedLog.wtf("Fail registerContentObserver@BootPackageProvider:\n" + Log.getStackTraceString(e));
            return new ValueExtra<>(false, String.valueOf(e));
        }
        return new ValueExtra<>(true, "OK");
    }

    private ValueExtra<Boolean, String> registerStartPackageObserver() {
        ContentResolver contentResolver = getContext().getContentResolver();
        if (contentResolver == null) {
            // Happen when early start.
            return new ValueExtra<>(false, "contentResolver is null");
        }
        try {
            contentResolver.registerContentObserver(AutoStartPackageProvider.CONTENT_URI,
                    false, new ContentObserver(h) {
                        @Override
                        public void onChange(boolean selfChange, Uri uri) {
                            super.onChange(selfChange, uri);
                            mWorkingService.execute(new Runnable() {
                                @Override
                                public void run() {
                                    loadStartPackageSettings();
                                }
                            });
                        }
                    });
        } catch (Exception e) {
            XPosedLog.wtf("Fail registerContentObserver@AutoStartPackageProvider:\n" + Log.getStackTraceString(e));
            return new ValueExtra<>(false, String.valueOf(e));
        }
        return new ValueExtra<>(true, "OK");
    }


    private ValueExtra<Boolean, String> registerLKPackageObserver() {
        ContentResolver contentResolver = getContext().getContentResolver();
        if (contentResolver == null) {
            // Happen when early start.
            return new ValueExtra<>(false, "contentResolver is null");
        }
        try {
            contentResolver.registerContentObserver(LockKillPackageProvider.CONTENT_URI,
                    false, new ContentObserver(h) {
                        @Override
                        public void onChange(boolean selfChange, Uri uri) {
                            super.onChange(selfChange, uri);
                            mWorkingService.execute(new Runnable() {
                                @Override
                                public void run() {
                                    loadLockKillPackageSettings();
                                }
                            });
                        }
                    });
        } catch (Exception e) {
            XPosedLog.wtf("Fail registerContentObserver@LockKillPackageProvider:\n" + Log.getStackTraceString(e));
            return new ValueExtra<>(false, String.valueOf(e));
        }
        return new ValueExtra<>(true, "OK");
    }

    private ValueExtra<Boolean, String> registerRFKPackageObserver() {
        ContentResolver contentResolver = getContext().getContentResolver();
        if (contentResolver == null) {
            // Happen when early start.
            return new ValueExtra<>(false, "contentResolver is null");
        }
        try {
            contentResolver.registerContentObserver(RFKillPackageProvider.CONTENT_URI,
                    false, new ContentObserver(h) {
                        @Override
                        public void onChange(boolean selfChange, Uri uri) {
                            super.onChange(selfChange, uri);
                            mWorkingService.execute(new Runnable() {
                                @Override
                                public void run() {
                                    loadRFKillPackageSettings();
                                }
                            });
                        }
                    });
        } catch (Exception e) {
            XPosedLog.wtf("Fail registerContentObserver@RFKillPackageProvider:\n" + Log.getStackTraceString(e));
            return new ValueExtra<>(false, String.valueOf(e));
        }
        return new ValueExtra<>(true, "OK");
    }


    private void whiteIMEPackages() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(INPUT_METHOD_SERVICE);
        List<InputMethodInfo> methodInfos = imm != null ? imm.getInputMethodList() : null;
        if (methodInfos == null) return;
        for (InputMethodInfo inputMethodInfo : methodInfos) {
            String pkg = inputMethodInfo.getPackageName();
            addToWhiteList(pkg);
            XPosedLog.verbose("whiteIMEPackages: " + pkg);
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
        if (pkg == null) return false;
        // Do not block qcom app.
        if (pkg.contains("com.qualcomm.qti")
                || pkg.contains("com.qti.smq")) {
            return true;
        }
        if (pkg.contains("com.google.android")) {
            return true;
        }
        return WHITE_LIST.contains(pkg);
    }

    private synchronized static void addToWhiteList(String pkg) {
        if (!WHITE_LIST.contains(pkg)) {
            WHITE_LIST.add(pkg);
        }
    }

    private static boolean isInSystemAppList(String pkg) {
        return SYSTEM_APPS.contains(pkg);
    }

    private synchronized static void addToSystemApps(String pkg) {
        if (!SYSTEM_APPS.contains(pkg)) {
            SYSTEM_APPS.add(pkg);
        }
    }

    private void checkSafeMode() {
        mIsSafeMode = getContext().getPackageManager().isSafeMode();
    }

    private boolean isSystemReady() {
        return mIsSystemReady;
    }

    private void getConfigFromSettings() {
        try {
            boolean bootBlockEnabled = (boolean) SystemSettings.BOOT_BLOCK_ENABLED_B.readFromSystemSettings(getContext());
            mBootBlockEnabled.set(bootBlockEnabled);
            XPosedLog.verbose("bootBlockEnabled: " + String.valueOf(bootBlockEnabled));
        } catch (Throwable e) {
            XPosedLog.wtf("Fail getConfigFromSettings:" + Log.getStackTraceString(e));
        }
        try {
            boolean startBlockEnabled = (boolean) SystemSettings.START_BLOCK_ENABLED_B.readFromSystemSettings(getContext());
            mStartBlockEnabled.set(startBlockEnabled);
            XPosedLog.verbose("startBlockEnabled:" + String.valueOf(startBlockEnabled));
        } catch (Throwable e) {
            XPosedLog.wtf("Fail getConfigFromSettings:" + Log.getStackTraceString(e));
        }
        try {
            boolean lockKillEnabled = (boolean) SystemSettings.LOCK_KILL_ENABLED_B.readFromSystemSettings(getContext());
            mLockKillEnabled.set(lockKillEnabled);
            XPosedLog.verbose("lockKillEnabled: " + String.valueOf(lockKillEnabled));
        } catch (Throwable e) {
            XPosedLog.wtf("Fail getConfigFromSettings:" + Log.getStackTraceString(e));
        }

        try {
            boolean rootKillEnabled = (boolean) SystemSettings.ROOT_ACTIVITY_KILL_ENABLED_B
                    .readFromSystemSettings(getContext());
            mRootActivityFinishKillEnabled.set(rootKillEnabled);
            XPosedLog.verbose("rootKillEnabled: " + String.valueOf(rootKillEnabled));
        } catch (Throwable e) {
            XPosedLog.wtf("Fail getConfigFromSettings:" + Log.getStackTraceString(e));
        }

        try {
            boolean compSettingBlockEnabled = (boolean) SystemSettings.COMP_SETTING_BLOCK_ENABLED_B
                    .readFromSystemSettings(getContext());
            mCompSettingBlockEnabled.set(compSettingBlockEnabled);
            XPosedLog.verbose("compSettingBlockEnabled: " + String.valueOf(compSettingBlockEnabled));
        } catch (Throwable e) {
            XPosedLog.wtf("Fail getConfigFromSettings:" + Log.getStackTraceString(e));
        }

        try {
            mLockKillDelay = (long) SystemSettings.LOCK_KILL_DELAY_L.readFromSystemSettings(getContext());
            XPosedLog.verbose("mLockKillDelay: " + String.valueOf(mLockKillDelay));
        } catch (Throwable e) {
            XPosedLog.wtf("Fail getConfigFromSettings:" + Log.getStackTraceString(e));
        }
    }

    @Override
    @InternalCall
    public boolean checkService(ComponentName serviceComp, int callerUid) {
        if (serviceComp == null) return true;
        String appPkg = serviceComp.getPackageName();
        CheckResult res = checkServiceDetailed(appPkg, callerUid);
        // Saving res record.
        if (!res.res) logServiceEventToMemory(
                ServiceEvent.builder()
                        .service("Service")
                        .why(res.why)
                        .allowed(res.res)
                        .appName(null)
                        .pkg(appPkg)
                        .when(System.currentTimeMillis())
                        .build());
        if (DEBUG_SERVICE && res.logRecommended)
            XPosedLog.verboseOn("XAshmanService checkService returning: " + res + "for: " +
                            PkgUtil.loadNameByPkgName(getContext(), appPkg)
                            + ", comp: " + serviceComp,
                    mLoggingService);
        return res.res;
    }

    private CheckResult checkServiceDetailed(String servicePkgName, int callerUid) {
        // Disabled case.
        if (!isStartBlockEnabled()) return CheckResult.SERVICE_CHECK_DISABLED;

        if (TextUtils.isEmpty(servicePkgName)) return CheckResult.BAD_ARGS;

        String callerPkgName =
                mPackagesCache.get(callerUid);
        if (callerPkgName == null) {
            callerPkgName = PkgUtil.pkgForUid(getContext(), callerUid);
        }

        // If this app is not in good condition, but user
        // does not block, we also allow it to start.
        boolean allowedByUser = isStartAllowedByUser(servicePkgName);
        if (allowedByUser) {
            return CheckResult.USER_ALLOWED;
        }

        if (isInWhiteList(servicePkgName)) {
            return CheckResult.WHITE_LISTED;
        }

        // Service from/to same app is allowed.
        if (servicePkgName.equals(callerPkgName)) {
            return CheckResult.SAME_CALLER;
        }

        if (PkgUtil.isHomeApp(getContext(), servicePkgName)) {
            return CheckResult.HOME_APP;
        }

        if (PkgUtil.isDefaultSmsApp(getContext(), servicePkgName)) {
            return CheckResult.SMS_APP;
        }

        if (PkgUtil.isAppRunning(getContext(), servicePkgName)) {
            return CheckResult.APP_RUNNING;
        }

        return CheckResult.DENIED_GENERAL;
    }

    @Override
    @InternalCall
    public boolean checkBroadcast(String action, int receiverUid, int callerUid) {
        CheckResult res = checkBroadcastDetailed(action, receiverUid, callerUid);
        // Saving res record.
        if (!res.res) logBroadcastEventToMemory(
                BroadcastEvent.builder()
                        .action(action)
                        .allowed(res.res)
                        .appName(null)
                        .receiver(receiverUid)
                        .caller(callerUid)
                        .when(System.currentTimeMillis())
                        .why(res.why)
                        .build());

        if (DEBUG_BROADCAST && res.logRecommended)
            XPosedLog.verboseOn("XAshmanService checkBroadcast returning: "
                            + res + " for: "
                            + PkgUtil.loadNameByPkgName(getContext(), mPackagesCache.get(receiverUid))
                            + " receiverUid: " + receiverUid
                            + " callerUid: " + callerUid
                            + " action: " + action,
                    mLoggingService);
        return res.res;
    }

    @Override
    @InternalCall
    public boolean checkComponentSetting(ComponentName componentName, int newState,
                                         int flags, int callingUid) {
        XPosedLog.verbose("checkComponentSetting: " + componentName
                + ", calling uid: " + callingUid
                + ", state: " + newState);
        if (newState != PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                && newState != PackageManager.COMPONENT_ENABLED_STATE_DEFAULT) {
            XPosedLog.verbose("It is not enable state, allow component setting.");
            return true;
        }
        if (componentName == null) return true;

        String pkgName = componentName.getPackageName();

        //noinspection ConstantConditions
        if (pkgName == null) return true;

        if (isInWhiteList(pkgName)) {
            XPosedLog.verbose("It is from while list, allow component setting.");
            return true;
        }

        if (pkgName.contains("com.google.android")) {
            XPosedLog.verbose("It is maybe from google apps list, allow component setting.");
            return true;
        }

        if (pkgName.contains("com.qualcomm.qti")
                || pkgName.contains("com.qti.smq")) {
            XPosedLog.verbose("It is maybe from qcom apps list, allow component setting.");
            return true;
        }

        if (callingUid == sClientUID || callingUid <= 1000
                || callingUid == android.os.Process.myUid()) {
            // Do not block system settings.
            XPosedLog.verbose("It is us or the system, allow component setting.");
            return true;
        }

        if (!isCompSettingBlockEnabledEnabled()) {
            XPosedLog.verbose("Block is not enabled, allow component setting.");
            return true;
        }

        XPosedLog.verbose("Block component setting.");
        return false;
    }

    @Override
    @InternalCall
    public void onActivityDestroy(Intent intent, String reason) {
        XPosedLog.verbose("onActivityDestroy: " + intent + ", reason: " + reason);
        if (!isRFKillEnabled()) return;
        lazyH.obtainMessage(AshManLZHandlerMessages.MSG_ONACTIVITYDESTROY, intent).sendToTarget();
    }

    private boolean isPackageRFKillEnabled(String pkg) {
        if (!isRFKillEnabled()) return false;
        // If this app is not in good condition, but user
        // does not block, we also allow it to start.
        boolean allowedByUser = isInRFKillWhiteList(pkg);
        if (allowedByUser) {
            return false;
        }

        if (isInWhiteList(pkg)) {
            return false;
        }

        if (PkgUtil.isHomeApp(getContext(), pkg)) {
            return false;
        }

        if (PkgUtil.isDefaultSmsApp(getContext(), pkg)) {
            return false;
        }
        return true;
    }

    @Override
    @BinderCall
    public List<BlockRecord2> getBlockRecords() throws RemoteException {
        enforceCallingPermissions();
        synchronized (mBlockRecords) {
            return Lists.newArrayList(mBlockRecords.values());
        }
    }

    @Override
    @BinderCall
    public void clearBlockRecords() throws RemoteException {
        enforceCallingPermissions();
        h.removeMessages(AshManHandlerMessages.MSG_CLEARBLOCKRECORDS);
        h.obtainMessage(AshManHandlerMessages.MSG_CLEARBLOCKRECORDS).sendToTarget();
    }

    @Override
    @BinderCall
    public void setComponentEnabledSetting(ComponentName componentName, int newState, int flags) throws RemoteException {
        enforceCallingPermissions();
        h.obtainMessage(AshManHandlerMessages.MSG_SETCOMPONENTENABLEDSETTING, newState, flags, componentName).sendToTarget();
    }

    @Override
    @BinderCall
    public int getComponentEnabledSetting(ComponentName componentName) throws RemoteException {
        enforceCallingPermissions();
        long id = Binder.clearCallingIdentity();
        try {
            PackageManager pm = getContext().getPackageManager();
            return pm.getComponentEnabledSetting(componentName);
        } finally {
            Binder.restoreCallingIdentity(id);
        }
    }

    @Override
    public int getApplicationEnabledSetting(final String packageName) throws RemoteException {
        enforceCallingPermissions();
        long id = Binder.clearCallingIdentity();
        try {
            PackageManager pm = getContext().getPackageManager();
            return pm.getApplicationEnabledSetting(packageName);
        } finally {
            Binder.restoreCallingIdentity(id);
        }

    }

    @Override
    public void setApplicationEnabledSetting(String packageName, int newState, int flags) throws RemoteException {
        enforceCallingPermissions();
        h.obtainMessage(AshManHandlerMessages.MSG_SETAPPLICATIONENABLEDSETTING, newState, flags, packageName).sendToTarget();
    }

    @Override
    public void watch(IAshmanWatcher w) throws RemoteException {
        enforceCallingPermissions();
        Preconditions.checkNotNull(w, "IAshmanWatcher is null");
        AshManHandler.WatcherClient watcherClient = new AshManHandler.WatcherClient(w);
        h.obtainMessage(AshManHandlerMessages.MSG_WATCH, watcherClient).sendToTarget();
    }

    @Override
    @BinderCall
    public void unWatch(IAshmanWatcher w) throws RemoteException {
        enforceCallingPermissions();
        Preconditions.checkNotNull(w, "IAshmanWatcher is null");
        AshManHandler.WatcherClient watcherClient = new AshManHandler.WatcherClient(w);
        h.obtainMessage(AshManHandlerMessages.MSG_UNWATCH, watcherClient).sendToTarget();
    }

    @Override
    @BinderCall
    public void setNetworkPolicyUidPolicy(int uid, int policy) throws RemoteException {
        enforceCallingPermissions();
        h.obtainMessage(AshManHandlerMessages.MSG_SETNETWORKPOLICYUIDPOLICY, uid, policy).sendToTarget();
    }

    @Override
    public void restart() throws RemoteException {
        enforceCallingPermissions();
        lazyH.post(new Runnable() {
            @Override
            public void run() {
                Zygote.execShell("reboot"); //FIXME Change to soft reboot?
            }
        });
    }

    @Override
    @BinderCall
    public void setCompSettingBlockEnabled(boolean enabled) throws RemoteException {
        enforceCallingPermissions();
        h.obtainMessage(AshManHandlerMessages.MSG_SETCOMPSETTINGBLOCKENABLED, enabled)
                .sendToTarget();
    }

    @Override
    @BinderCall
    public boolean isCompSettingBlockEnabledEnabled() {
        enforceCallingPermissions();
        return mCompSettingBlockEnabled.get();
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
            if (o instanceof AutoStartPackage) {
                pkg = ((AutoStartPackage) o).getPkgName();
            } else if (o instanceof BootCompletePackage) {
                pkg = ((BootCompletePackage) o).getPkgName();
            } else if (o instanceof LockKillPackage) {
                pkg = ((LockKillPackage) o).getPkgName();
            } else if (o instanceof RFKillPackage) {
                pkg = ((RFKillPackage) o).getPkgName();
            }
            out[i] = pkg;
        }
        return out;
    }

    @Override
    public String[] getWhiteListApps(int filterOptions) throws RemoteException {
        XPosedLog.verbose("getWhiteListApps: " + filterOptions);
        enforceCallingPermissions();
        Object[] data = WHITE_LIST.toArray(); // FIXME, no sync protect?
        return convertObjectArrayToStringArray(data);
    }

    @Override
    public String[] getBootBlockApps(boolean block) throws RemoteException {
        XPosedLog.verbose("getBootBlockApps: " + block);
        enforceCallingPermissions();
        if (block) {
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
                    if (isBootAllowedByUser(s)) return;
                    if (isInWhiteList(s)) return;
                    outList.add(s);
                }
            });

            if (outList.size() == 0) {
                return new String[0];
            }
            Object[] objArr = outList.toArray();
            return convertObjectArrayToStringArray(objArr);
        } else {
            Collection<BootCompletePackage> packages = mBootWhiteListPackages.values();
            if (packages.size() == 0) {
                return new String[0];
            }
            return convertObjectArrayToStringArray(packages.toArray());
        }
    }

    @Override
    public void addOrRemoveBootBlockApps(String[] packages, int op) throws RemoteException {
        XPosedLog.verbose("addOrRemoveBootBlockApps: " + Arrays.toString(packages));
        enforceCallingPermissions();
        if (packages == null || packages.length == 0) return;
        switch (op) {
            case XAshmanManager.Op.ADD:
                try {
                    Collections.consumeRemaining(packages, new Consumer<String>() {
                        @Override
                        public void accept(String s) {
                            try {
                                BootCompletePackage b = new BootCompletePackage();
                                b.setPkgName(s);
                                b.setAppName(String.valueOf(PkgUtil.loadNameByPkgName(getContext(), s)));
                                mBootWhiteListPackages.put(s, b);
                                BootPackageProvider.insert(getContext(), b);
                            } catch (Throwable e) {
                                XPosedLog.wtf("Fail add boot pkg: " + s);
                            }
                        }
                    });
                } catch (Throwable e) {
                    XPosedLog.wtf("Fail add boot packages...");
                    throw new IllegalStateException(e);
                }
                break;
            case XAshmanManager.Op.REMOVE:
                try {
                    Collections.consumeRemaining(packages, new Consumer<String>() {
                        @Override
                        public void accept(String s) {
                            try {
                                BootCompletePackage b = new BootCompletePackage();
                                b.setPkgName(s);
                                mBootWhiteListPackages.remove(s);
                                BootPackageProvider.delete(getContext(), b);
                            } catch (Throwable e) {
                                XPosedLog.wtf("Fail delete boot pkg: " + s);
                            }
                        }
                    });
                } catch (Throwable e) {
                    XPosedLog.wtf("Fail remove boot packages...");
                    throw new IllegalStateException(e);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public String[] getStartBlockApps(boolean block) throws RemoteException {
        XPosedLog.verbose("getStartBlockApps: " + block);
        enforceCallingPermissions();
        if (block) {
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
                    if (isStartAllowedByUser(s)) return;
                    if (isInWhiteList(s)) return;
                    outList.add(s);
                }
            });

            if (outList.size() == 0) {
                return new String[0];
            }
            Object[] objArr = outList.toArray();
            return convertObjectArrayToStringArray(objArr);
        } else {
            Collection<AutoStartPackage> packages = mStartWhiteListPackages.values();
            if (packages.size() == 0) {
                return new String[0];
            }
            return convertObjectArrayToStringArray(packages.toArray());
        }
    }

    @Override
    public void addOrRemoveStartBlockApps(String[] packages, int op) throws RemoteException {
        XPosedLog.verbose("addOrRemoveStartBlockApps: " + Arrays.toString(packages));
        enforceCallingPermissions();
        if (packages == null || packages.length == 0) return;
        switch (op) {
            case XAshmanManager.Op.ADD:
                try {
                    Collections.consumeRemaining(packages, new Consumer<String>() {
                        @Override
                        public void accept(String s) {
                            try {
                                AutoStartPackage b = new AutoStartPackage();
                                b.setPkgName(s);
                                b.setAppName(String.valueOf(PkgUtil.loadNameByPkgName(getContext(), s)));
                                mStartWhiteListPackages.put(s, b);
                                AutoStartPackageProvider.insert(getContext(), b);
                            } catch (Throwable e) {
                                XPosedLog.wtf("Fail add start pkg: " + s);
                            }
                        }
                    });
                } catch (Throwable e) {
                    XPosedLog.wtf("Fail add start packages...");
                    throw new IllegalStateException(e);
                }
                break;
            case XAshmanManager.Op.REMOVE:
                try {
                    Collections.consumeRemaining(packages, new Consumer<String>() {
                        @Override
                        public void accept(String s) {
                            try {
                                AutoStartPackage b = new AutoStartPackage();
                                b.setPkgName(s);
                                mStartWhiteListPackages.remove(s);
                                AutoStartPackageProvider.delete(getContext(), b);
                            } catch (Throwable e) {
                                XPosedLog.wtf("Fail delete start pkg: " + s);
                            }
                        }
                    });
                } catch (Throwable e) {
                    XPosedLog.wtf("Fail remove start packages...");
                    throw new IllegalStateException(e);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public String[] getLKApps(boolean kill) throws RemoteException {
        XPosedLog.verbose("getLKApps: " + kill);
        enforceCallingPermissions();
        if (kill) {
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
                    if (isInLockKillWhiteList(s)) return;
                    if (isInWhiteList(s)) return;
                    outList.add(s);
                }
            });

            if (outList.size() == 0) {
                return new String[0];
            }
            Object[] objArr = outList.toArray();
            return convertObjectArrayToStringArray(objArr);
        } else {
            Collection<LockKillPackage> packages = mLockKillWhileListPackages.values();
            if (packages.size() == 0) {
                return new String[0];
            }
            return convertObjectArrayToStringArray(packages.toArray());
        }
    }

    @Override
    public void addOrRemoveLKApps(String[] packages, int op) throws RemoteException {
        XPosedLog.verbose("addOrRemoveLKApps: " + Arrays.toString(packages));
        enforceCallingPermissions();
        if (packages == null || packages.length == 0) return;
        switch (op) {
            case XAshmanManager.Op.ADD:
                try {
                    Collections.consumeRemaining(packages, new Consumer<String>() {
                        @Override
                        public void accept(String s) {
                            try {
                                LockKillPackage b = new LockKillPackage();
                                b.setPkgName(s);
                                b.setAppName(String.valueOf(PkgUtil.loadNameByPkgName(getContext(), s)));
                                mLockKillWhileListPackages.put(s, b);
                                LockKillPackageProvider.insert(getContext(), b);
                            } catch (Throwable e) {
                                XPosedLog.wtf("Fail add lk pkg: " + s);
                            }
                        }
                    });
                } catch (Throwable e) {
                    XPosedLog.wtf("Fail add lk packages...");
                    throw new IllegalStateException(e);
                }
                break;
            case XAshmanManager.Op.REMOVE:
                try {
                    Collections.consumeRemaining(packages, new Consumer<String>() {
                        @Override
                        public void accept(String s) {
                            try {
                                LockKillPackage b = new LockKillPackage();
                                b.setPkgName(s);
                                mLockKillWhileListPackages.remove(s);
                                LockKillPackageProvider.delete(getContext(), b);
                            } catch (Throwable e) {
                                XPosedLog.wtf("Fail delete lk pkg: " + s);
                            }
                        }
                    });
                } catch (Throwable e) {
                    XPosedLog.wtf("Fail remove lk packages...");
                    throw new IllegalStateException(e);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public String[] getRFKApps(boolean kill) throws RemoteException {
        XPosedLog.verbose("getRFKApps: " + kill);
        enforceCallingPermissions();
        if (kill) {
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
                    if (isInRFKillWhiteList(s)) return;
                    if (isInWhiteList(s)) return;
                    outList.add(s);
                }
            });

            if (outList.size() == 0) {
                return new String[0];
            }
            Object[] objArr = outList.toArray();
            return convertObjectArrayToStringArray(objArr);
        } else {
            Collection<RFKillPackage> packages = mRFKillWhileListPackages.values();
            if (packages.size() == 0) {
                return new String[0];
            }
            return convertObjectArrayToStringArray(packages.toArray());
        }
    }

    @Override
    public void addOrRemoveRFKApps(String[] packages, int op) throws RemoteException {
        XPosedLog.verbose("addOrRemoveRFKApps: " + Arrays.toString(packages));
        enforceCallingPermissions();
        if (packages == null || packages.length == 0) return;
        switch (op) {
            case XAshmanManager.Op.ADD:
                try {
                    Collections.consumeRemaining(packages, new Consumer<String>() {
                        @Override
                        public void accept(String s) {
                            try {
                                RFKillPackage b = new RFKillPackage();
                                b.setPkgName(s);
                                b.setAppName(String.valueOf(PkgUtil.loadNameByPkgName(getContext(), s)));
                                mRFKillWhileListPackages.put(s, b);
                                RFKillPackageProvider.insert(getContext(), b);
                            } catch (Throwable e) {
                                XPosedLog.wtf("Fail add rf pkg: " + s);
                            }
                        }
                    });
                } catch (Throwable e) {
                    XPosedLog.wtf("Fail add rf packages...");
                    throw new IllegalStateException(e);
                }
                break;
            case XAshmanManager.Op.REMOVE:
                try {
                    Collections.consumeRemaining(packages, new Consumer<String>() {
                        @Override
                        public void accept(String s) {
                            try {
                                RFKillPackage b = new RFKillPackage();
                                b.setPkgName(s);
                                mRFKillWhileListPackages.remove(s);
                                RFKillPackageProvider.delete(getContext(), b);
                            } catch (Throwable e) {
                                XPosedLog.wtf("Fail delete rf pkg: " + s);
                            }
                        }
                    });
                } catch (Throwable e) {
                    XPosedLog.wtf("Fail remove rf packages...");
                    throw new IllegalStateException(e);
                }
                break;
            default:
                break;
        }
    }

    private CheckResult checkBroadcastDetailed(String action, int receiverUid, int callerUid) {

        // Check if this is a boot complete action.
        if (isBootCompleteBroadcastAction(action)) {
            return checkBootCompleteBroadcast(receiverUid, callerUid);
        }

        // Disabled case.
        if (!isStartBlockEnabled()) return CheckResult.BROADCAST_CHECK_DISABLED;

        String receiverPkgName =
                mPackagesCache.get(receiverUid);
        if (receiverPkgName == null) {
            receiverPkgName = PkgUtil.pkgForUid(getContext(), receiverUid);
        }
        if (TextUtils.isEmpty(receiverPkgName)) return CheckResult.BAD_ARGS;

        // Broadcast from/to same app is allowed.
        if (callerUid == receiverUid) {
            return CheckResult.SAME_CALLER;
        }

        return checkBroadcastDetailed(receiverPkgName);
    }

    private CheckResult checkBroadcastDetailed(String receiverPkgName) {
        // If this app is not in good condition, but user
        // does not block, we also allow it to start.
        boolean allowedByUser = isStartAllowedByUser(receiverPkgName);
        if (allowedByUser) {
            return CheckResult.USER_ALLOWED;
        }

        if (isInWhiteList(receiverPkgName)) {
            return CheckResult.WHITE_LISTED;
        }

        if (PkgUtil.isHomeApp(getContext(), receiverPkgName)) {
            return CheckResult.HOME_APP;
        }

        if (PkgUtil.isDefaultSmsApp(getContext(), receiverPkgName)) {
            return CheckResult.SMS_APP;
        }

        if (PkgUtil.isAppRunning(getContext(), receiverPkgName)) {
            return CheckResult.APP_RUNNING;
        }

        return CheckResult.DENIED_GENERAL;
    }

    private boolean isBootAllowedByUser(String pkg) {
        BootCompletePackage bootCompletePackage = mBootWhiteListPackages.get(pkg);
        return bootCompletePackage != null;
    }

    private boolean isStartAllowedByUser(String pkg) {
        AutoStartPackage autoStartPackage = mStartWhiteListPackages.get(pkg);
        return autoStartPackage != null;
    }

    private boolean isInLockKillWhiteList(String pkg) {
        LockKillPackage lockKillPackage = mLockKillWhileListPackages.get(pkg);
        return lockKillPackage != null;
    }

    private boolean isInRFKillWhiteList(String pkg) {
        RFKillPackage rfKillPackage = mRFKillWhileListPackages.get(pkg);
        return rfKillPackage != null;
    }

    private CheckResult checkBootCompleteBroadcast(int receiverUid, int callerUid) {

        // Disabled case.
        if (!isBlockBlockEnabled()) return CheckResult.BOOT_CHECK_DISABLED;

        String receiverPkgName =
                mPackagesCache.get(receiverUid);
        if (receiverPkgName == null) {
            receiverPkgName = PkgUtil.pkgForUid(getContext(), receiverUid);
        }

        if (TextUtils.isEmpty(receiverPkgName)) return CheckResult.BAD_ARGS;

        boolean allowedByUser = isBootAllowedByUser(receiverPkgName);

        if (allowedByUser) {
            return CheckResult.USER_ALLOWED;
        }

        if (isInWhiteList(receiverPkgName)) {
            return CheckResult.WHITE_LISTED;
        }

        if (PkgUtil.isHomeApp(getContext(), receiverPkgName)) {
            return CheckResult.HOME_APP;
        }

        if (PkgUtil.isDefaultSmsApp(getContext(), receiverPkgName)) {
            return CheckResult.SMS_APP;
        }

        return CheckResult.DENIED_GENERAL;
    }

    private static boolean isBootCompleteBroadcastAction(String action) {
        return Intent.ACTION_BOOT_COMPLETED.equals(action);
    }

    private void logServiceEventToMemory(final ServiceEvent serviceEvent) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                BlockRecord2 old = getBlockRecord(serviceEvent.pkg);
                long oldTimes = old == null ? 0 : old.getHowManyTimes();
                BlockRecord2 blockRecord2 = BlockRecord2.builder()
                        .pkgName(serviceEvent.pkg)
                        .appName(String.valueOf(
                                PkgUtil.loadNameByPkgName(getContext(),
                                        serviceEvent.pkg)))
                        .howManyTimes(oldTimes + 1)
                        .timeWhen(System.currentTimeMillis())
                        .build();
                XPosedLog.verbose("SVC BlockRecord2: " + blockRecord2);
                addBlockRecord(blockRecord2);
            }
        };

        mLoggingService.execute(r);

        h.obtainMessage(AshManHandlerMessages.MSG_NOTIFYSTARTBLOCK, serviceEvent.getPkg()).sendToTarget();
    }

    private void logBroadcastEventToMemory(final BroadcastEvent broadcastEvent) {
        Runnable r = new Runnable() {
            @Override
            public void run() {

                String receiverPkgName =
                        mPackagesCache.get(broadcastEvent.receiver);
                if (receiverPkgName == null) {
                    receiverPkgName = PkgUtil.pkgForUid(getContext(), broadcastEvent.receiver);
                    if (receiverPkgName == null) return;
                }

                h.obtainMessage(AshManHandlerMessages.MSG_NOTIFYSTARTBLOCK, receiverPkgName).sendToTarget();

                BlockRecord2 old = getBlockRecord(receiverPkgName);
                long oldTimes = old == null ? 0 : old.getHowManyTimes();
                BlockRecord2 blockRecord2 = BlockRecord2.builder()
                        .pkgName(receiverPkgName)
                        .appName(String.valueOf(
                                PkgUtil.loadNameByPkgName(getContext(),
                                        receiverPkgName)))
                        .howManyTimes(oldTimes + 1)
                        .timeWhen(System.currentTimeMillis())
                        .build();
                XPosedLog.verbose("BRD BlockRecord2: " + blockRecord2);
                addBlockRecord(blockRecord2);
            }
        };
        mLoggingService.execute(r);
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

    @Override
    public void publish() {
        ServiceManager.addService(XAshmanManager.ASH_MAN_SERVICE_NAME, asBinder());
        construct();
    }

    @Override
    public void systemReady() {
        XPosedLog.wtf("systemReady@" + getClass().getSimpleName());
        // Update system ready, since we can call providers now.
        mIsSystemReady = true;
        checkSafeMode();
        registerReceiver();

        // Try load providers.
        AsyncTrying.tryTillSuccess(mWorkingService, new AsyncTrying.Once() {
            @Override
            public boolean once() {
                ValueExtra<Boolean, String> res = loadBootPackageSettings();
                String extra = res.getExtra();
                XPosedLog.verbose("loadBootPackageSettings, extra: " + extra);
                return res.getValue();
            }
        }, new Runnable() {
            @Override
            public void run() {
                AsyncTrying.tryTillSuccess(mWorkingService, new AsyncTrying.Once() {
                    @Override
                    public boolean once() {
                        ValueExtra<Boolean, String> res = registerBootPackageObserver();
                        XPosedLog.verbose("registerBootPackageObserver, extra: " + res.getExtra());
                        return res.getValue();
                    }
                });
            }
        });

        AsyncTrying.tryTillSuccess(mWorkingService, new AsyncTrying.Once() {
            @Override
            public boolean once() {
                ValueExtra<Boolean, String> res = loadStartPackageSettings();
                String extra = res.getExtra();
                XPosedLog.verbose("loadStartPackageSettings, extra: " + extra);
                return res.getValue();
            }
        }, new Runnable() {
            @Override
            public void run() {
                AsyncTrying.tryTillSuccess(mWorkingService, new AsyncTrying.Once() {
                    @Override
                    public boolean once() {
                        ValueExtra<Boolean, String> res = registerStartPackageObserver();
                        XPosedLog.verbose("registerStartPackageObserver, extra: " + res.getExtra());
                        return res.getValue();
                    }
                });
            }
        });

        AsyncTrying.tryTillSuccess(mWorkingService, new AsyncTrying.Once() {
            @Override
            public boolean once() {
                ValueExtra<Boolean, String> res = loadLockKillPackageSettings();
                String extra = res.getExtra();
                XPosedLog.verbose("loadLockKillPackageSettings, extra: " + extra);
                return res.getValue();
            }
        }, new Runnable() {
            @Override
            public void run() {
                AsyncTrying.tryTillSuccess(mWorkingService, new AsyncTrying.Once() {
                    @Override
                    public boolean once() {
                        ValueExtra<Boolean, String> res = registerLKPackageObserver();
                        XPosedLog.verbose("registerLKPackageObserver, extra: " + res.getExtra());
                        return res.getValue();
                    }
                });
            }
        });

        AsyncTrying.tryTillSuccess(mWorkingService, new AsyncTrying.Once() {
            @Override
            public boolean once() {
                ValueExtra<Boolean, String> res = loadRFKillPackageSettings();
                String extra = res.getExtra();
                XPosedLog.verbose("loadRFKillPackageSettings, extra: " + extra);
                return res.getValue();
            }
        }, new Runnable() {
            @Override
            public void run() {
                AsyncTrying.tryTillSuccess(mWorkingService, new AsyncTrying.Once() {
                    @Override
                    public boolean once() {
                        ValueExtra<Boolean, String> res = registerRFKPackageObserver();
                        XPosedLog.verbose("registerRFKPackageObserver, extra: " + res.getExtra());
                        return res.getValue();
                    }
                });
            }
        });
    }

    @Override
    public void retrieveSettings() {
        XPosedLog.wtf("retrieveSettings@" + getClass().getSimpleName());
        getConfigFromSettings();
        cachePackages();
        whiteIMEPackages();
    }

    private void construct() {
        h = onCreateServiceHandler();
        HandlerThread hr = new HandlerThread("ASHMAN-LAZY-H");
        hr.start();
        lazyH = new LazyHandler(hr.getLooper());
        XPosedLog.verbose("construct, h: " + h
                + ", lazyH: " + lazyH
                + " -" + serial());
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
    @InternalCall
    public void onPackageMoveToFront(String who) {
        lazyH.removeMessages(AshManLZHandlerMessages.MSG_ONPACKAGEMOVETOFRONT);
        lazyH.obtainMessage(AshManLZHandlerMessages.MSG_ONPACKAGEMOVETOFRONT, who).sendToTarget();
    }

    @Override
    public String serial() {
        return mSerialUUID.toString();
    }

    @Override
    @BinderCall
    public void clearProcess(IProcessClearListener listener) throws RemoteException {
        enforceCallingPermissions();
        h.obtainMessage(AshManHandlerMessages.MSG_CLEARPROCESS, listener)
                .sendToTarget();
    }

    @Override
    @BinderCall
    public void setLockKillDelay(long delay) throws RemoteException {
        enforceCallingPermissions();
        h.obtainMessage(AshManHandlerMessages.MSG_SETLOCKKILLDELAY, delay).sendToTarget();
    }

    @Override
    @BinderCall
    public long getLockKillDelay() throws RemoteException {
        enforceCallingPermissions();
        return mLockKillDelay;
    }

    @Override
    @BinderCall
    public void setBootBlockEnabled(boolean enabled) {
        enforceCallingPermissions();
        h.obtainMessage(AshManHandlerMessages.MSG_SETBOOTBLOCKENABLED, enabled)
                .sendToTarget();
    }

    @Override
    @BinderCall
    public boolean isBlockBlockEnabled() {
        enforceCallingPermissions();
        return !mIsSafeMode && mBootBlockEnabled.get();
    }

    @Override
    @BinderCall
    public void setStartBlockEnabled(boolean enabled) {
        enforceCallingPermissions();
        h.obtainMessage(AshManHandlerMessages.MSG_SETSTARTBLOCKENABLED, enabled)
                .sendToTarget();
    }

    @Override
    @BinderCall
    public boolean isStartBlockEnabled() {
        enforceCallingPermissions();
        return !mIsSafeMode && mStartBlockEnabled.get();
    }

    @Override
    @BinderCall
    public void setLockKillEnabled(boolean enabled) {
        enforceCallingPermissions();
        h.obtainMessage(AshManHandlerMessages.MSG_SETLOCKKILLENABLED, enabled)
                .sendToTarget();
    }

    @Override
    @BinderCall
    public boolean isLockKillEnabled() {
        enforceCallingPermissions();
        return !mIsSafeMode && mLockKillEnabled.get();
    }

    @Override
    @BinderCall
    public void setRFKillEnabled(boolean enabled) throws RemoteException {
        enforceCallingPermissions();
        h.obtainMessage(AshManHandlerMessages.MSG_SETRFKILLENABLED, enabled)
                .sendToTarget();
    }

    @Override
    @BinderCall
    public boolean isRFKillEnabled() {
        enforceCallingPermissions();
        return !mIsSafeMode && mRootActivityFinishKillEnabled.get();
    }

    @Override
    @BinderCall
    protected void dump(FileDescriptor fd, final PrintWriter fout, String[] args) {
        super.dump(fd, fout, args);
        // For secure and CTS.
        if (getContext().checkCallingOrSelfPermission(Manifest.permission.DUMP) != PackageManager.PERMISSION_GRANTED) {
            fout.println("Permission denial: can not dump Ashman service from pid= " + Binder.getCallingPid()
                    + ", uid= " + Binder.getCallingUid());
            return;
        }

        synchronized (this) {
            // Dump switch.
            fout.println("Start block enabled: " + mStartBlockEnabled.get());
            fout.println("Boot block enabled: " + mBootBlockEnabled.get());
            fout.println("LK enabled: " + mLockKillEnabled.get());
            fout.println("RF kill enabled: " + mRootActivityFinishKillEnabled.get());
            fout.println("CompSettingBlockEnabled enabled: " + mCompSettingBlockEnabled.get());
            fout.println("LK delay: " + mLockKillDelay);

            // Dump while list.
            fout.println("White list: ");
            Object[] whileListObjects = WHITE_LIST.toArray();
            Collections.consumeRemaining(whileListObjects, new Consumer<Object>() {
                @Override
                public void accept(Object o) {
                    fout.println(o);
                }
            });

            // Dump System list.
            fout.println("System list: ");
            Object[] systemListObjects = SYSTEM_APPS.toArray();
            Collections.consumeRemaining(systemListObjects, new Consumer<Object>() {
                @Override
                public void accept(Object o) {
                    fout.println(o);
                }
            });

            // Dump boot list.
            fout.println("Boot list: ");
            Object[] bootListObjects = mBootWhiteListPackages.values().toArray();
            Collections.consumeRemaining(bootListObjects, new Consumer<Object>() {
                @Override
                public void accept(Object o) {
                    fout.println(o);
                }
            });

            // Dump start list.
            fout.println("Start list: ");
            Object[] startListObjects = mStartWhiteListPackages.values().toArray();
            Collections.consumeRemaining(startListObjects, new Consumer<Object>() {
                @Override
                public void accept(Object o) {
                    fout.println(o);
                }
            });

            // Dump lk list.
            fout.println("LK list: ");
            Object[] lkListObjects = mLockKillWhileListPackages.values().toArray();
            Collections.consumeRemaining(lkListObjects, new Consumer<Object>() {
                @Override
                public void accept(Object o) {
                    fout.println(o);
                }
            });

            // Dump rf list.
            fout.println("RF list: ");
            Object[] rfListObjects = mRFKillWhileListPackages.values().toArray();
            Collections.consumeRemaining(rfListObjects, new Consumer<Object>() {
                @Override
                public void accept(Object o) {
                    fout.println(o);
                }
            });

            // Dump watcher.
            fout.println("Watcher list: ");
            Object[] watcherListObjects = mWatcherClients.toArray();
            Collections.consumeRemaining(watcherListObjects, new Consumer<Object>() {
                @Override
                public void accept(Object o) {
                    fout.println(o);
                }
            });
        }
    }

    @Override
    public void dump(FileDescriptor fd, String[] args) {
        super.dump(fd, args);
        enforceCallingPermissions();
    }

    protected void enforceCallingPermissions() {
        int callingUID = Binder.getCallingUid();
        XPosedLog.verbose("enforceCallingPermissions@uid:" + callingUID);
        if (callingUID == android.os.Process.myUid() || (sClientUID > 0 && sClientUID == callingUID)) {
            return;
        }
        throw new SecurityException("Package of uid:" + callingUID
                + ", does not require permission to interact with XIntentFirewallService");
    }

    private void addBlockRecord(BlockRecord2 blockRecord2) {
        synchronized (mBlockRecords) {
            mBlockRecords.put(blockRecord2.getPkgName(), blockRecord2);
        }
    }

    private BlockRecord2 getBlockRecord(String pkg) {
        synchronized (mBlockRecords) {
            return mBlockRecords.get(pkg);
        }
    }

    @SuppressLint("HandlerLeak")
    private class HandlerImpl extends Handler implements AshManHandler {

        private final Runnable clearProcessRunnable = new Runnable() {
            @Override
            public void run() {
                clearProcess(null);
            }
        };

        @Override
        public void handleMessage(Message msg) {
            XPosedLog.verbose("HandlerImpl handleMessage: " + AshManHandlerMessages.decodeMessage(msg.what));
            super.handleMessage(msg);
            switch (msg.what) {
                case AshManHandlerMessages.MSG_CLEARPROCESS:
                    IProcessClearListener listener = msg.obj == null ? null : (IProcessClearListener) msg.obj;
                    HandlerImpl.this.clearProcess(listener);
                    break;
                case AshManHandlerMessages.MSG_SETBOOTBLOCKENABLED:
                    HandlerImpl.this.setBootBlockEnabled((Boolean) msg.obj);
                    break;
                case AshManHandlerMessages.MSG_SETSTARTBLOCKENABLED:
                    HandlerImpl.this.setStartBlockEnabled((Boolean) msg.obj);
                    break;
                case AshManHandlerMessages.MSG_SETLOCKKILLENABLED:
                    HandlerImpl.this.setLockKillEnabled((Boolean) msg.obj);
                    break;
                case AshManHandlerMessages.MSG_SETRFKILLENABLED:
                    HandlerImpl.this.setRFKillEnabled((Boolean) msg.obj);
                    break;
                case AshManHandlerMessages.MSG_SETCOMPSETTINGBLOCKENABLED:
                    HandlerImpl.this.setCompSettingBlockEnabled((Boolean) msg.obj);
                    break;
                case AshManHandlerMessages.MSG_ONSCREENOFF:
                    HandlerImpl.this.onScreenOff();
                    break;
                case AshManHandlerMessages.MSG_ONSCREENON:
                    HandlerImpl.this.onScreenOn();
                    break;
                case AshManHandlerMessages.MSG_SETLOCKKILLDELAY:
                    HandlerImpl.this.setLockKillDelay((Long) msg.obj);
                    break;
                case AshManHandlerMessages.MSG_CLEARBLOCKRECORDS:
                    HandlerImpl.this.clearBlockRecords();
                    break;
                case AshManHandlerMessages.MSG_SETCOMPONENTENABLEDSETTING:
                    HandlerImpl.this.setComponentEnabledSetting((ComponentName) msg.obj, msg.arg1, msg.arg2);
                    break;
                case AshManHandlerMessages.MSG_SETAPPLICATIONENABLEDSETTING:
                    HandlerImpl.this.setApplicationEnabledSetting((String) msg.obj, msg.arg1, msg.arg2);
                    break;
                case AshManHandlerMessages.MSG_WATCH:
                    HandlerImpl.this.watch((WatcherClient) msg.obj);
                    break;
                case AshManHandlerMessages.MSG_UNWATCH:
                    HandlerImpl.this.unWatch((WatcherClient) msg.obj);
                    break;
                case AshManHandlerMessages.MSG_NOTIFYSTARTBLOCK:
                    HandlerImpl.this.notifyStartBlock((String) msg.obj);
                    break;
                case AshManHandlerMessages.MSG_SETNETWORKPOLICYUIDPOLICY:
                    HandlerImpl.this.setNetworkPolicyUidPolicy(msg.arg1, msg.arg2);
                    break;
            }
        }

        @Override
        public void setBootBlockEnabled(boolean enabled) {
            if (mBootBlockEnabled.compareAndSet(!enabled, enabled)) {
                SystemSettings.BOOT_BLOCK_ENABLED_B.writeToSystemSettings(getContext(), enabled);
            }
        }

        @Override
        public void setStartBlockEnabled(boolean enabled) {
            if (mStartBlockEnabled.compareAndSet(!enabled, enabled)) {
                SystemSettings.START_BLOCK_ENABLED_B.writeToSystemSettings(getContext(), enabled);
            }
        }

        @Override
        public void setLockKillEnabled(boolean enabled) {
            if (mLockKillEnabled.compareAndSet(!enabled, enabled)) {
                SystemSettings.LOCK_KILL_ENABLED_B.writeToSystemSettings(getContext(), enabled);
            }
        }

        @Override
        public void setRFKillEnabled(boolean enabled) {
            if (mRootActivityFinishKillEnabled.compareAndSet(!enabled, enabled)) {
                SystemSettings.ROOT_ACTIVITY_KILL_ENABLED_B.writeToSystemSettings(getContext(), enabled);
            }
        }

        @Override
        public void setCompSettingBlockEnabled(boolean enabled) {
            if (mCompSettingBlockEnabled.compareAndSet(!enabled, enabled)) {
                SystemSettings.COMP_SETTING_BLOCK_ENABLED_B.writeToSystemSettings(getContext(), enabled);
            }
        }

        @Override
        public void clearProcess(final IProcessClearListener listener) {

            if (listener != null) try {
                listener.onPrepareClearing();
            } catch (RemoteException ignored) {

            }
            FutureTask<String[]> futureTask = new FutureTask<>(new SignalCallable<String[]>() {

                @Override
                public String[] call() throws Exception {

                    PowerManager power = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
                    ActivityManager am = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
                    if (am == null) return null;
                    List<ActivityManager.RunningAppProcessInfo> processes =
                            am.getRunningAppProcesses();
                    int count = processes == null ? 0 : processes.size();

                    if (listener != null) try {
                        listener.onStartClearing(count);
                    } catch (RemoteException ignored) {

                    }

                    String[] cleared = new String[count];

                    for (int i = 0; i < count; i++) {

                        // Check if canceled.
                        if (power != null && power.isInteractive()) {
                            XPosedLog.wtf("isInteractive, skip clearing");
                            return cleared;
                        }

                        ActivityManager.RunningAppProcessInfo runningAppProcessInfo = processes.get(i);
                        String runningPackageName = mPackagesCache.get(runningAppProcessInfo.uid);
                        if (runningPackageName == null) {
                            if (listener != null) try {
                                listener.onIgnoredPkg(null, "null");
                            } catch (RemoteException ignored) {

                            }
                            continue;
                        }

                        // Check if we can control.
                        boolean whiteApp = isInLockKillWhiteList(runningPackageName)
                                || WHITE_LIST.contains(runningPackageName);
                        if (whiteApp) {
                            if (listener != null) try {
                                listener.onIgnoredPkg(runningPackageName, "white-list");
                            } catch (RemoteException ignored) {

                            }
                            // XPosedLog.verbose("App is in white-listed, wont kill: " + runningPackageName);
                            continue;
                        }
                        if (PkgUtil.isAppRunningForeground(getContext(), runningPackageName)) {

                            if (listener != null) try {
                                listener.onIgnoredPkg(runningPackageName, "foreground-app");
                            } catch (RemoteException ignored) {

                            }

                            XPosedLog.verbose("App is in foreground, wont kill: " + runningPackageName);
                            continue;
                        }
                        if (PkgUtil.isHomeApp(getContext(), runningPackageName)) {
                            addToWhiteList(runningPackageName);
                            if (listener != null) try {
                                listener.onIgnoredPkg(runningPackageName, "home-app");
                            } catch (RemoteException ignored) {

                            }

                            XPosedLog.verbose("App is in isHomeApp, wont kill: " + runningPackageName);
                            continue;
                        }
                        if (PkgUtil.isDefaultSmsApp(getContext(), runningPackageName)) {
                            addToWhiteList(runningPackageName);
                            if (listener != null) try {
                                listener.onIgnoredPkg(runningPackageName, "sms-app");
                            } catch (RemoteException ignored) {

                            }

                            XPosedLog.verbose("App is in isDefaultSmsApp, wont kill: " + runningPackageName);
                            continue;
                        }

                        if (listener != null) try {
                            listener.onClearingPkg(runningPackageName);
                        } catch (RemoteException ignored) {

                        }

                        // Clearing using kill command.
                        if (power != null && power.isInteractive()) {
                            XPosedLog.wtf("isInteractive, skip clearing");
                            return cleared;
                        }
                        PkgUtil.kill(getContext(), runningAppProcessInfo);

                        cleared[i] = runningPackageName;
                        XPosedLog.verbose("Force stopped: " + runningPackageName);

                        if (listener != null) try {
                            listener.onClearedPkg(runningPackageName);
                        } catch (RemoteException ignored) {

                        }
                    }

                    if (listener != null) try {
                        listener.onAllCleared(cleared);
                    } catch (RemoteException ignored) {

                    }

                    return cleared;
                }
            });
            mWorkingService.execute(futureTask);
        }

        @Override
        public void clearBlockRecords() {
            Runnable clear = new Runnable() {
                @Override
                public void run() {
                    synchronized (mBlockRecords) {
                        mBlockRecords.clear();
                    }
                }
            };
            mWorkingService.execute(clear);
        }

        @Override
        public void setLockKillDelay(long delay) {
            mLockKillDelay = delay;
            SystemSettings.LOCK_KILL_DELAY_L.writeToSystemSettings(getContext(), delay);
            XPosedLog.verbose("setLockKillDelay to: " + mLockKillDelay);
        }

        @Override
        public void onScreenOff() {
            if (isLockKillEnabled()) {
                removeCallbacks(clearProcessRunnable);
                postDelayed(clearProcessRunnable, mLockKillDelay);
            }
        }

        @Override
        public void onScreenOn() {
            cancelProcessClearing("SCREEN ON");
        }

        private void cancelProcessClearing(String why) {
            XPosedLog.verbose("cancelProcessClearing: " + why);
            removeCallbacks(clearProcessRunnable);
        }

        @Override
        public void setComponentEnabledSetting(ComponentName componentName, int newState, int flags) {
            PackageManager pm = getContext().getPackageManager();
            pm.setComponentEnabledSetting(componentName, newState, flags);
        }

        @Override
        public int getComponentEnabledSetting(ComponentName componentName) {
            PackageManager pm = getContext().getPackageManager();
            return pm.getComponentEnabledSetting(componentName);
        }

        @Override
        public void setApplicationEnabledSetting(String packageName, int newState, int flags) {
            PackageManager pm = getContext().getPackageManager();
            pm.setApplicationEnabledSetting(packageName, newState, flags);
        }

        @Override
        public int getApplicationEnabledSetting(String packageName) {
            PackageManager pm = getContext().getPackageManager();
            return pm.getApplicationEnabledSetting(packageName);
        }

        @Override
        public void watch(WatcherClient w) {
            if (!mWatcherClients.contains(w)) {
                mWatcherClients.add(w);
            }
        }

        @Override
        public void unWatch(WatcherClient w) {
            mWatcherClients.remove(w);
        }

        @Override
        public void notifyStartBlock(final String pkg) {
            if (pkg == null) return;
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    Object[] objects = mWatcherClients.toArray();
                    Collections.consumeRemaining(objects, new Consumer<Object>() {
                        @Override
                        public void accept(Object o) {
                            WatcherClient w = (WatcherClient) o;
                            if (!w.isAlive()) {
                                return; // FIXME Try remove from set.
                            }
                            try {
                                w.getWatcher().onStartBlocked(pkg);
                            } catch (RemoteException ignored) {
                            }
                        }
                    });
                }
            };
            mWorkingService.execute(r);
        }

        @Override
        public void setNetworkPolicyUidPolicy(int uid, int policy) {
            NetworkPolicyManager.from(getContext()).setUidPolicy(uid, policy);
        }
    }

    private class LazyHandler extends Handler implements AshManLZHandler {

        private final Holder<String> mTopPackage = new Holder<>();

//        private StringListStorage mCompSettingPkgs;

        LazyHandler(Looper looper) {
            super(looper);

//            try {
//                File dataDir = Environment.getDataDirectory();
//                File systemDir = new File(dataDir, "system/tor/apm/");
//                File compStorageFile = new File(systemDir, "comp_setting_uids.config");
//                mCompSettingPkgs = new StringListStorage(compStorageFile.getPath());
//                XPosedLog.verbose("StringListStorage: " + mCompSettingPkgs);
//            } catch (Throwable e) {
//                XPosedLog.wtf("Fail init StringListStorage: " + e);
//            }
        }

//        public boolean isCompSettingByUs(String pkg) {
//            return mCompSettingPkgs != null && mCompSettingPkgs.contains(pkg);
//        }

        @Override
        public void onActivityDestroy(Intent intent) {
            boolean isMainIntent = PkgUtil.isMainIntent(intent);

            final String packageName = PkgUtil.packageNameOf(intent);
            if (packageName == null) return;


            XPosedLog.verbose("onActivityDestroy, packageName: " + packageName
                    + ", isMainIntent: " + isMainIntent + ", topPkg: " + getTopPackage());

            if (!isPackageRFKillEnabled(packageName)) {
                XPosedLog.verbose("PackageRFKill not enabled");
                return;
            }

            boolean maybeRootActivityFinish = !packageName.equals(getTopPackage());

            if (maybeRootActivityFinish) {
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        XPosedLog.verbose("Killing maybeRootActivityFinish: " + packageName);

                        if (packageName.equals(getTopPackage())) {
                            XPosedLog.verbose("Top package is now him, let it go~");
                            return;
                        }

                        PkgUtil.kill(getContext(), packageName);
                    }
                }, 666);
            }
        }

        @Override
        public void onPackageMoveToFront(String who) {
            mTopPackage.setData(who);
        }

        @Override
        public void onCompSetting(String pkg, boolean enable) {

        }

        private String getTopPackage() {
            return mTopPackage.getData();
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            XPosedLog.verbose("LazyHandler handle message: "
                    + AshManLZHandlerMessages.decodeMessage(msg.what));
            switch (msg.what) {
                case AshManLZHandlerMessages.MSG_ONACTIVITYDESTROY:
                    LazyHandler.this.onActivityDestroy((Intent) msg.obj);
                    break;
                case AshManLZHandlerMessages.MSG_ONPACKAGEMOVETOFRONT:
                    LazyHandler.this.onPackageMoveToFront((String) msg.obj);
                    break;
                case AshManLZHandlerMessages.MSG_ONCOMPSETTING:
                    LazyHandler.this.onCompSetting((String) msg.obj, msg.arg1 == 1);
                    break;
            }
        }
    }

    @Builder
    @Getter
    @ToString
    private static class ServiceEvent {
        private String pkg;
        private String service;
        private String why;
        private String appName;
        private long when;
        private boolean allowed;
    }

    @Builder
    @Getter
    @ToString
    private static class BootEvent {
        private String pkg;
        private String appName;
        private long when;
        private String why;
        private boolean allowed;
    }

    @Builder
    @Getter
    @ToString
    private static class BroadcastEvent {
        private int receiver;
        private int caller;
        private String appName;
        private String action;
        private String why;
        private long when;
        private boolean allowed;
    }

    @AllArgsConstructor
    @Getter
    @ToString
    private static class CheckResult {
        // Allowed cases.
        public static final CheckResult SERVICE_CHECK_DISABLED = new CheckResult(true, "SERVICE_CHECK_DISABLED", false);
        public static final CheckResult BOOT_CHECK_DISABLED = new CheckResult(true, "BOOT_CHECK_DISABLED", false);
        public static final CheckResult BROADCAST_CHECK_DISABLED = new CheckResult(true, "BROADCAST_CHECK_DISABLED", false);

        public static final CheckResult WHITE_LISTED = new CheckResult(true, "WHITE_LISTED", false);

        public static final CheckResult SYSTEM_APP = new CheckResult(true, "SYSTEM_APP", false);
        public static final CheckResult HOME_APP = new CheckResult(true, "HOME_APP", true);
        public static final CheckResult LAUNCHER_APP = new CheckResult(true, "LAUNCHER_APP", true);
        public static final CheckResult SMS_APP = new CheckResult(true, "SMS_APP", true);

        public static final CheckResult APP_RUNNING = new CheckResult(true, "APP_RUNNING", true);
        public static final CheckResult SAME_CALLER = new CheckResult(true, "SAME_CALLER", true);

        public static final CheckResult BAD_ARGS = new CheckResult(true, "BAD_ARGS", false);
        public static final CheckResult USER_ALLOWED = new CheckResult(true, "USER_ALLOWED", true);

        // Denied cases.
        public static final CheckResult DENIED_GENERAL = new CheckResult(false, "DENIED_GENERAL", true);

        private boolean res;
        private String why;
        private boolean logRecommended;
    }

    @Getter
    @Setter
    private abstract class SignalCallable<V> implements Callable<V> {
        boolean canceled = false;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @ToString
    private class StringList {
        private List<String> uids;

        private boolean contains(String str) {
            return uids != null && uids.contains(str);
        }

        public void add(String str) {
            if (uids == null) {
                uids = new ArrayList<>();
            }
            uids.add(str);
            XPosedLog.verbose("StringList Adding: " + str);
        }

        public void remove(String s) {
            XPosedLog.verbose("StringList Remove: " + s);
            if (uids == null) {
                uids = new ArrayList<>();
                return;
            }
            uids.remove(s);
        }
    }

    @ToString
    private class StringListStorage {

        @Getter
        private StringList stringList;

        private String filePath;

        StringListStorage(String filePath) {
            this.filePath = filePath;
            asyncRead();
        }

        public boolean contains(String s) {
            return stringList != null && stringList.contains(s);
        }

        public void add(String s) {
            if (stringList == null) {
                stringList = new StringList(new ArrayList<String>());
            }
            stringList.add(s);
            asyncWrite();
        }

        public void remove(String s) {
            if (stringList == null) {
                return;
            }
            stringList.remove(s);
            asyncWrite();
        }

        void asyncRead() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    synchronized (StringListStorage.this) {
                        try {
                            File file = new File(filePath);
                            if (!file.exists()) return;
                            String content = FileUtil.readString(filePath);
                            XPosedLog.verbose("StringListStorage, content: " + content);
                            stringList = new Gson()
                                    .fromJson(content, StringList.class);
                        } catch (Exception e) {
                            XPosedLog.wtf("StringListStorage asyncRead error: " + e);
                        }
                    }
                }
            }).start();
        }

        void asyncWrite() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    synchronized (StringListStorage.this) {
                        if (stringList == null) return;
                        try {
                            String content = new Gson().toJson(stringList);
                            FileUtil.writeString(content, filePath);
                        } catch (Exception e) {
                            XPosedLog.wtf("StringListStorage asyncWrite error: " + e);
                        }
                    }
                }
            }).start();
        }
    }
}
