package github.tornaco.xposedmoduletest.xposed.service;

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
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.io.FileDescriptor;
import java.io.PrintWriter;
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
import github.tornaco.xposedmoduletest.bean.BlockRecord;
import github.tornaco.xposedmoduletest.bean.BootCompletePackage;
import github.tornaco.xposedmoduletest.bean.BootCompletePackageDaoUtil;
import github.tornaco.xposedmoduletest.bean.LockKillPackage;
import github.tornaco.xposedmoduletest.bean.LockKillPackageDaoUtil;
import github.tornaco.xposedmoduletest.provider.AutoStartPackageProvider;
import github.tornaco.xposedmoduletest.provider.BlockRecordProvider;
import github.tornaco.xposedmoduletest.provider.BootPackageProvider;
import github.tornaco.xposedmoduletest.provider.LockKillPackageProvider;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;
import github.tornaco.xposedmoduletest.xposed.bean.BlockRecord2;
import github.tornaco.xposedmoduletest.xposed.service.provider.SystemSettings;
import github.tornaco.xposedmoduletest.xposed.util.Closer;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;
import github.tornaco.xposedmoduletest.xposed.util.XLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import static android.content.Context.INPUT_METHOD_SERVICE;

/**
 * Created by guohao4 on 2017/11/9.
 * Email: Tornaco@163.com
 */

public class XAshmanServiceImpl extends XAshmanServiceAbs {

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

    private static int sClientUID = 0;

    private final ExecutorService mWorkingService = Executors.newCachedThreadPool();
    private final ExecutorService mLoggingService = Executors.newSingleThreadExecutor();

    private final SparseArray<String> mPackagesCache = new SparseArray<>();

    private final Map<String, BlockRecord2> mBlockRecords = new HashMap<>();

    private Handler h;

    private AtomicBoolean mBootBlockEnabled = new AtomicBoolean(false);
    private AtomicBoolean mStartBlockEnabled = new AtomicBoolean(false);
    private AtomicBoolean mLockKillEnabled = new AtomicBoolean(false);

    private long mLockKillDelay;

    private final Map<String, BootCompletePackage> mBootWhiteListPackages = new HashMap<>();
    private final Map<String, AutoStartPackage> mStartWhiteListPackages = new HashMap<>();
    private final Map<String, LockKillPackage> mLockKillWhileListPackages = new HashMap<>();

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

        // Retrieve our package first.
        try {
            ApplicationInfo applicationInfo = pm.getApplicationInfo(BuildConfig.APPLICATION_ID, 0);
            sClientUID = applicationInfo.uid;
            XLog.logV("sClientUID:" + sClientUID);
        } catch (PackageManager.NameNotFoundException e) {
            XLog.logD("Can not get UID for our client:" + e);
        }

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
                    mPackagesCache.put(uid, pkg);
                }
            });
        } catch (Exception ignored) {
            XLog.logD("Can not get UID for our client:" + ignored);
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

            mBootWhiteListPackages.clear();

            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                BootCompletePackage bootCompletePackage = BootCompletePackageDaoUtil.readEntity(cursor, 0);
                XLog.logV("Boot pkg reader readEntity of: " + bootCompletePackage);
                String key = bootCompletePackage.getPkgName();
                if (TextUtils.isEmpty(key)) continue;
                mBootWhiteListPackages.put(key, bootCompletePackage);
            }
        } catch (Throwable e) {
            XLog.logF("Fail query boot pkgs:\n" + Log.getStackTraceString(e));
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

            mStartWhiteListPackages.clear();

            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                AutoStartPackage autoStartPackage = AutoStartPackageDaoUtil.readEntity(cursor, 0);
                String key = autoStartPackage.getPkgName();
                if (TextUtils.isEmpty(key)) continue;
                mStartWhiteListPackages.put(key, autoStartPackage);
            }
        } catch (Throwable e) {
            XLog.logF("Fail query start pkgs:\n" + Log.getStackTraceString(e));
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

            mLockKillWhileListPackages.clear();

            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                LockKillPackage lockKillPackage = LockKillPackageDaoUtil.readEntity(cursor, 0);
                XLog.logV("Lock kill white list pkg reader readEntity of: " + lockKillPackage);
                String key = lockKillPackage.getPkgName();
                if (TextUtils.isEmpty(key)) continue;
                mLockKillWhileListPackages.put(key, lockKillPackage);
            }
        } catch (Throwable e) {
            XLog.logF("Fail query start pkgs:\n" + Log.getStackTraceString(e));
            XLog.logF("Fail query start pkgs:\n" + Log.getStackTraceString(e));
        } finally {
            Closer.closeQuietly(cursor);
        }

        return new ValueExtra<>(true, "Read count: " + mStartWhiteListPackages.size());
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
            XLog.logF("Fail registerContentObserver@BootPackageProvider:\n" + Log.getStackTraceString(e));
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
            XLog.logF("Fail registerContentObserver@AutoStartPackageProvider:\n" + Log.getStackTraceString(e));
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
            XLog.logF("Fail registerContentObserver@LockKillPackageProvider:\n" + Log.getStackTraceString(e));
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
            XLog.logV("bootBlockEnabled: " + String.valueOf(bootBlockEnabled));
        } catch (Throwable e) {
            XLog.logF("Fail getConfigFromSettings:" + Log.getStackTraceString(e));
        }
        try {
            boolean startBlockEnabled = (boolean) SystemSettings.START_BLOCK_ENABLED_B.readFromSystemSettings(getContext());
            mStartBlockEnabled.set(startBlockEnabled);
            XLog.logV("startBlockEnabled:" + String.valueOf(startBlockEnabled));
        } catch (Throwable e) {
            XLog.logF("Fail getConfigFromSettings:" + Log.getStackTraceString(e));
        }
        try {
            boolean lockKillEnabled = (boolean) SystemSettings.LOCK_KILL_ENABLED_B.readFromSystemSettings(getContext());
            mLockKillEnabled.set(lockKillEnabled);
            XLog.logV("lockKillEnabled: " + String.valueOf(lockKillEnabled));
        } catch (Throwable e) {
            XLog.logF("Fail getConfigFromSettings:" + Log.getStackTraceString(e));
        }

        try {
            mLockKillDelay = (long) SystemSettings.LOCK_KILL_DELAY_L.readFromSystemSettings(getContext());
            XLog.logV("mLockKillDelay: " + String.valueOf(mLockKillDelay));
        } catch (Throwable e) {
            XLog.logF("Fail getConfigFromSettings:" + Log.getStackTraceString(e));
        }
    }

    @Override
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
        if (res.logRecommended)
            XLog.logVOnExecutor("XAshmanService checkService returning: " + res + "for: " +
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

        if (PkgUtil.isSystemApp(getContext(), servicePkgName)) {
            return CheckResult.SYSTEM_APP;
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

        if (res.logRecommended)
            XLog.logVOnExecutor("XAshmanService checkBroadcast returning: "
                    + res + " for: "
                    + PkgUtil.loadNameByPkgName(getContext(),
                    mPackagesCache.get(receiverUid))
                    + " action: " + action, mLoggingService);
        return res.res;
    }


    @Override
    public List<BlockRecord2> getBlockRecords() throws RemoteException {
        enforceCallingPermissions();
        synchronized (mBlockRecords) {
            return Lists.newArrayList(mBlockRecords.values());
        }
    }

    @Override
    public void clearBlockRecords() throws RemoteException {
        enforceCallingPermissions();
        h.removeMessages(AshManHandlerMessages.MSG_CLEARBLOCKRECORDS);
        h.obtainMessage(AshManHandlerMessages.MSG_CLEARBLOCKRECORDS).sendToTarget();
    }

    @Override
    public void setComponentEnabledSetting(ComponentName componentName, int newState, int flags) throws RemoteException {
        enforceCallingPermissions();
        h.obtainMessage(AshManHandlerMessages.MSG_SETCOMPONENTENABLEDSETTING, newState, flags, componentName).sendToTarget();
    }

    @Override
    public int getComponentEnabledSetting(ComponentName componentName) throws RemoteException {
        long id = Binder.clearCallingIdentity();
        try {
            PackageManager pm = getContext().getPackageManager();
            return pm.getComponentEnabledSetting(componentName);
        } finally {
            Binder.restoreCallingIdentity(id);
        }
    }

    @Override
    public void watch(IAshmanWatcher w) throws RemoteException {
        enforceCallingPermissions();
        Preconditions.checkNotNull(w, "IAshmanWatcher is null");
        AshManHandler.WatcherClient watcherClient = new AshManHandler.WatcherClient(w);
        h.obtainMessage(AshManHandlerMessages.MSG_WATCH, watcherClient).sendToTarget();
    }

    @Override
    public void unWatch(IAshmanWatcher w) throws RemoteException {
        enforceCallingPermissions();
        Preconditions.checkNotNull(w, "IAshmanWatcher is null");
        AshManHandler.WatcherClient watcherClient = new AshManHandler.WatcherClient(w);
        h.obtainMessage(AshManHandlerMessages.MSG_UNWATCH, watcherClient).sendToTarget();
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

        // If this app is not in good condition, but user
        // does not block, we also allow it to start.
        boolean allowedByUser = isStartAllowedByUser(receiverPkgName);
        if (allowedByUser) {
            return CheckResult.USER_ALLOWED;
        }

        if (isInWhiteList(receiverPkgName)) {
            return CheckResult.WHITE_LISTED;
        }

        // Broadcast from/to same app is allowed.
        if (callerUid == receiverUid) {
            return CheckResult.SAME_CALLER;
        }

        if (PkgUtil.isSystemApp(getContext(), receiverPkgName)) {
            return CheckResult.SYSTEM_APP;
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
        return bootCompletePackage != null && bootCompletePackage.getAllow();
    }

    private boolean isStartAllowedByUser(String pkg) {
        AutoStartPackage autoStartPackage = mStartWhiteListPackages.get(pkg);
        return autoStartPackage != null && autoStartPackage.getAllow();
    }

    private boolean isInLockKillWhiteList(String pkg) {
        LockKillPackage lockKillPackage = mLockKillWhileListPackages.get(pkg);
        return lockKillPackage != null && !lockKillPackage.getKill();
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

        if (PkgUtil.isSystemApp(getContext(), receiverPkgName)) {
            return CheckResult.SYSTEM_APP;
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
                addBlockRecord(blockRecord2);
            }
        };
        mLoggingService.execute(r);
    }

    /**
     * @deprecated Use {@link #logServiceEventToMemory(ServiceEvent)} instead.
     */
    @Deprecated
    private void logServiceEvent(final ServiceEvent serviceEvent) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    BlockRecord blockRecord = new BlockRecord();
                    blockRecord.setAppName(String.valueOf(
                            PkgUtil.loadNameByPkgName(getContext(),
                                    serviceEvent.pkg)));
                    blockRecord.setPkgName(serviceEvent.pkg);
                    blockRecord.setTimeWhen(serviceEvent.when);
                    blockRecord.setAllow(serviceEvent.allowed);
                    blockRecord.setDescription("SERVICE");
                    blockRecord.setWhy(serviceEvent.why);

                    if (isSystemReady()) BlockRecordProvider.insert(getContext(), blockRecord);
                } catch (Throwable e) {
                    XLog.logF("Fail logServiceEvent: " + e);
                }
            }
        };
        mLoggingService.execute(r);
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
                addBlockRecord(blockRecord2);
            }
        };
        mLoggingService.execute(r);
    }

    /**
     * @deprecated Use {@link #logBroadcastEventToMemory(BroadcastEvent)} instead.
     */
    @Deprecated
    private void logBroadcastEvent(final BroadcastEvent broadcastEvent) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    String receiverPkgName =
                            mPackagesCache.get(broadcastEvent.receiver);
                    if (receiverPkgName == null) {
                        receiverPkgName = PkgUtil.pkgForUid(getContext(), broadcastEvent.receiver);
                    }

                    BlockRecord blockRecord = new BlockRecord();
                    blockRecord.setAppName(String.valueOf(
                            PkgUtil.loadNameByPkgName(getContext(),
                                    receiverPkgName)));
                    blockRecord.setPkgName(receiverPkgName);
                    blockRecord.setTimeWhen(broadcastEvent.when);
                    blockRecord.setWhy(broadcastEvent.why);
                    blockRecord.setAllow(broadcastEvent.allowed);
                    blockRecord.setDescription("BROADCAST");

                    if (isSystemReady()) BlockRecordProvider.insert(getContext(), blockRecord);
                } catch (Throwable e) {
                    XLog.logF("Fail logBroadcastEvent: " + e);
                }
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
        XLog.logF("systemReady@" + getClass().getSimpleName());
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
                XLog.logV("loadBootPackageSettings, extra: " + extra);
                return res.getValue();
            }
        }, new Runnable() {
            @Override
            public void run() {
                AsyncTrying.tryTillSuccess(mWorkingService, new AsyncTrying.Once() {
                    @Override
                    public boolean once() {
                        ValueExtra<Boolean, String> res = registerBootPackageObserver();
                        XLog.logV("registerBootPackageObserver, extra: " + res.getExtra());
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
                XLog.logV("loadStartPackageSettings, extra: " + extra);
                return res.getValue();
            }
        }, new Runnable() {
            @Override
            public void run() {
                AsyncTrying.tryTillSuccess(mWorkingService, new AsyncTrying.Once() {
                    @Override
                    public boolean once() {
                        ValueExtra<Boolean, String> res = registerStartPackageObserver();
                        XLog.logV("registerStartPackageObserver, extra: " + res.getExtra());
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
                XLog.logV("loadLockKillPackageSettings, extra: " + extra);
                return res.getValue();
            }
        }, new Runnable() {
            @Override
            public void run() {
                AsyncTrying.tryTillSuccess(mWorkingService, new AsyncTrying.Once() {
                    @Override
                    public boolean once() {
                        ValueExtra<Boolean, String> res = registerLKPackageObserver();
                        XLog.logV("registerLKPackageObserver, extra: " + res.getExtra());
                        return res.getValue();
                    }
                });
            }
        });
    }

    @Override
    public void retrieveSettings() {
        XLog.logF("retrieveSettings@" + getClass().getSimpleName());
        getConfigFromSettings();
        cachePackages();
        whiteIMEPackages();
    }

    private void construct() {
        h = onCreateServiceHandler();
        XLog.logV("construct, h: " + h + " -" + serial());
    }


    protected Handler onCreateServiceHandler() {
        return new HandlerImpl();
    }

    @Override
    public void publishFeature(String f) {

    }

    @Override
    public void shutdown() {
        cleanUpBlockRecords();
    }

    private void cleanUpBlockRecords() {
        // Clear all block records.
        try {
            ContentResolver contentResolver = getContext().getContentResolver();
            if (contentResolver == null) {
                // Happen when early start.
                return;
            }
            contentResolver.delete(BlockRecordProvider.CONTENT_URI, "all", null);
        } catch (Throwable ignored) {
        }
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
    public void setLockKillDelay(long delay) throws RemoteException {
        enforceCallingPermissions();
        h.obtainMessage(AshManHandlerMessages.MSG_SETLOCKKILLDELAY, delay).sendToTarget();
    }

    @Override
    public long getLockKillDelay() throws RemoteException {
        enforceCallingPermissions();
        return mLockKillDelay;
    }

    @Override
    public void setBootBlockEnabled(boolean enabled) {
        enforceCallingPermissions();
        h.obtainMessage(AshManHandlerMessages.MSG_SETBOOTBLOCKENABLED, enabled)
                .sendToTarget();
    }

    @Override
    public boolean isBlockBlockEnabled() {
        enforceCallingPermissions();
        return !mIsSafeMode && mBootBlockEnabled.get();
    }

    @Override
    public void setStartBlockEnabled(boolean enabled) {
        enforceCallingPermissions();
        h.obtainMessage(AshManHandlerMessages.MSG_SETSTARTBLOCKENABLED, enabled)
                .sendToTarget();
    }

    @Override
    public boolean isStartBlockEnabled() {
        enforceCallingPermissions();
        return !mIsSafeMode && mStartBlockEnabled.get();
    }

    @Override
    public void setLockKillEnabled(boolean enabled) {
        enforceCallingPermissions();
        h.obtainMessage(AshManHandlerMessages.MSG_SETLOCKKILLENABLED, enabled)
                .sendToTarget();
    }

    @Override
    public boolean isLockKillEnabled() {
        enforceCallingPermissions();
        return !mIsSafeMode && mLockKillEnabled.get();
    }

    @Override
    @BinderCall
    protected void dump(FileDescriptor fd, PrintWriter fout, String[] args) {
        enforceCallingPermissions();
        super.dump(fd, fout, args);
    }

    protected void enforceCallingPermissions() {
        int callingUID = Binder.getCallingUid();
        XLog.logV("enforceCallingPermissions@uid:" + callingUID);
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

        private final Holder<FutureTask<String[]>> mClearingTask = new Holder<>();

        private final Runnable clearProcessRunnable = new Runnable() {
            @Override
            public void run() {
                clearProcess(null);
            }
        };

        @Override
        public void handleMessage(Message msg) {
            XLog.logV("handleMessage: " + AshManHandlerMessages.decodeMessage(msg.what));
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
                case AshManHandlerMessages.MSG_WATCH:
                    HandlerImpl.this.watch((WatcherClient) msg.obj);
                    break;
                case AshManHandlerMessages.MSG_UNWATCH:
                    HandlerImpl.this.unWatch((WatcherClient) msg.obj);
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
        public void clearProcess(final IProcessClearListener listener) {
            if (listener != null) try {
                listener.onPrepareClearing();
            } catch (RemoteException ignored) {

            }
            synchronized (mClearingTask) {
                if (mClearingTask.getData() != null && (!mClearingTask.getData().isDone()
                        && !mClearingTask.getData().isCancelled())) {
                    XLog.logV("clearProcess, Canceling existing clear task...");
                    mClearingTask.getData().cancel(true);
                    mClearingTask.setData(null);
                }
                FutureTask<String[]> futureTask = new FutureTask<>(new Callable<String[]>() {
                    @Override
                    public String[] call() throws Exception {
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
                            for (String runningPackageName : processes.get(i).pkgList) {
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
                                    XLog.logV("App is in white-listed, wont kill: " + runningPackageName);
                                    continue;
                                }

                                if (PkgUtil.isSystemApp(getContext(), runningPackageName)) {

                                    if (listener != null) try {
                                        listener.onIgnoredPkg(runningPackageName, "system-app");
                                    } catch (RemoteException ignored) {

                                    }
                                    // XLog.logV("App is in system app, wont kill: " + runningPackageName);
                                    continue;
                                }
                                if (PkgUtil.isAppRunningForeground(getContext(), runningPackageName)) {

                                    if (listener != null) try {
                                        listener.onIgnoredPkg(runningPackageName, "foreground-app");
                                    } catch (RemoteException ignored) {

                                    }

                                    XLog.logV("App is in foreground, wont kill: " + runningPackageName);
                                    continue;
                                }
                                if (PkgUtil.isHomeApp(getContext(), runningPackageName)) {

                                    if (listener != null) try {
                                        listener.onIgnoredPkg(runningPackageName, "home-app");
                                    } catch (RemoteException ignored) {

                                    }

                                    XLog.logV("App is in isHomeApp, wont kill: " + runningPackageName);
                                    continue;
                                }
                                if (PkgUtil.isDefaultSmsApp(getContext(), runningPackageName)) {

                                    if (listener != null) try {
                                        listener.onIgnoredPkg(runningPackageName, "sms-app");
                                    } catch (RemoteException ignored) {

                                    }

                                    XLog.logV("App is in isDefaultSmsApp, wont kill: " + runningPackageName);
                                    continue;
                                }

                                if (listener != null) try {
                                    listener.onClearingPkg(runningPackageName);
                                } catch (RemoteException ignored) {

                                }

                                am.forceStopPackage(runningPackageName);
                                cleared[i] = runningPackageName;
                                XLog.logV("Force stopped: " + runningPackageName);

                                if (listener != null) try {
                                    listener.onClearedPkg(runningPackageName);
                                } catch (RemoteException ignored) {

                                }
                            }
                        }

                        if (listener != null) try {
                            listener.onAllCleared(cleared);
                        } catch (RemoteException ignored) {

                        }

                        return cleared;
                    }
                });
                mClearingTask.setData(futureTask);
            }

            mWorkingService.execute(mClearingTask.getData());
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
            XLog.logV("setLockKillDelay to: " + mLockKillDelay);
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
            removeCallbacks(clearProcessRunnable);
            synchronized (mClearingTask) {
                if (mClearingTask.getData() != null && (!mClearingTask.getData().isDone()
                        && !mClearingTask.getData().isCancelled())) {
                    mClearingTask.getData().cancel(true);
                    mClearingTask.setData(null);
                }
            }
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
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    Object[] objects = mWatcherClients.toArray();
                    Collections.consumeRemaining(objects, new Consumer<Object>() {
                        @Override
                        public void accept(Object o) {
                            WatcherClient w = (WatcherClient) o;
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
}
