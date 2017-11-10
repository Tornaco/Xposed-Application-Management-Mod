package github.tornaco.xposedmoduletest.x.service;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
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
import github.tornaco.xposedmoduletest.bean.AutoStartPackage;
import github.tornaco.xposedmoduletest.bean.AutoStartPackageDaoUtil;
import github.tornaco.xposedmoduletest.bean.BootCompletePackage;
import github.tornaco.xposedmoduletest.bean.BootCompletePackageDaoUtil;
import github.tornaco.xposedmoduletest.provider.AutoStartPackageProvider;
import github.tornaco.xposedmoduletest.provider.BootPackageProvider;
import github.tornaco.xposedmoduletest.x.app.XIntentFirewallManager;
import github.tornaco.xposedmoduletest.x.service.provider.TorSettings;
import github.tornaco.xposedmoduletest.x.util.Closer;
import github.tornaco.xposedmoduletest.x.util.PkgUtil;
import github.tornaco.xposedmoduletest.x.util.XLog;
import github.tornaco.xposedmoduletest.x.util.XStopWatch;

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

    private final SparseArray<String> mPackagesCache = new SparseArray<>();

    private Handler mFirewallHandler;

    private AtomicBoolean mIFWEnabled = new AtomicBoolean(false);

    private final Map<String, BootCompletePackage> mBootPackages = new HashMap<>();
    private final Map<String, AutoStartPackage> mStartPackages = new HashMap<>();

    // Safe mode is the last clear place user can stay.
    private boolean mIsSafeMode = false;

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
        mFirewallHandler.sendEmptyMessage(IntentFirewallHandlerMessages.MSG_ONSCREENON);
    }

    private void onScreenOff() {
        mFirewallHandler.sendEmptyMessage(IntentFirewallHandlerMessages.MSG_ONSCREENOFF);
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
                    XLog.logV("Cached pkg:" + pkg + "-" + uid + "-" + isSystemApp);
                    mPackagesCache.put(uid, pkg);
                }
            });
        } catch (Exception ignored) {
            XLog.logD("Can not get UID for our client:" + ignored);
        }
    }

    synchronized private void loadBootPackageSettings() {
        XLog.logV("loadPackageSettings...");
        ContentResolver contentResolver = getContext().getContentResolver();
        if (contentResolver == null) {
            // Happen when early start.
            return;
        }
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(BootPackageProvider.CONTENT_URI, null, null, null, null);
            if (cursor == null) {
                XLog.logF("Fail query boot pkgs, cursor is null");
                return;
            }

            mBootPackages.clear();

            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                BootCompletePackage bootCompletePackage = BootCompletePackageDaoUtil.readEntity(cursor, 0);
                XLog.logV("Boot pkg reader readEntity of: " + bootCompletePackage);
                String key = bootCompletePackage.getPkgName();
                if (TextUtils.isEmpty(key)) continue;
                mBootPackages.put(key, bootCompletePackage);
            }
        } catch (Throwable e) {
            XLog.logF("Fail query boot pkgs:\n" + Log.getStackTraceString(e));
        } finally {
            Closer.closeQuietly(cursor);
        }
    }

    synchronized private void loadStartPackageSettings() {
        XLog.logV("loadPackageSettings...");
        ContentResolver contentResolver = getContext().getContentResolver();
        if (contentResolver == null) {
            // Happen when early start.
            return;
        }
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(AutoStartPackageProvider.CONTENT_URI, null, null, null, null);
            if (cursor == null) {
                XLog.logF("Fail query start pkgs, cursor is null");
                return;
            }

            mStartPackages.clear();

            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                AutoStartPackage autoStartPackage = AutoStartPackageDaoUtil.readEntity(cursor, 0);
                XLog.logV("Start pkg reader readEntity of: " + autoStartPackage);
                String key = autoStartPackage.getPkgName();
                if (TextUtils.isEmpty(key)) continue;
                mStartPackages.put(key, autoStartPackage);
            }
        } catch (Throwable e) {
            XLog.logF("Fail query start pkgs:\n" + Log.getStackTraceString(e));
        } finally {
            Closer.closeQuietly(cursor);
        }
    }

    private void registerPackageObserver() {
        ContentResolver contentResolver = getContext().getContentResolver();
        if (contentResolver == null) {
            // Happen when early start.
            return;
        }
        try {
            contentResolver.registerContentObserver(BootPackageProvider.CONTENT_URI,
                    false, new ContentObserver(mFirewallHandler) {
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
        }

        try {
            contentResolver.registerContentObserver(AutoStartPackageProvider.CONTENT_URI,
                    false, new ContentObserver(mFirewallHandler) {
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

    private void checkSafeMode() {
        mIsSafeMode = getContext().getPackageManager().isSafeMode();
    }

    private void getConfigFromSettings() {
        try {
            boolean ifwEnabled = (boolean) TorSettings.IFW_ENABLED_B.readFromSystemSettings(getContext());
            mIFWEnabled.set(ifwEnabled);
            XLog.logV(String.valueOf(ifwEnabled));
        } catch (Throwable e) {
            XLog.logF("Fail getConfigFromSettings:" + Log.getStackTraceString(e));
        }
    }

    @Override
    public boolean checkService(String servicePkgName, int callerUid) {
        if (TextUtils.isEmpty(servicePkgName)) return true;
        String callerPkgName =
                mPackagesCache.get(callerUid);
        if (callerPkgName == null) {
            callerPkgName = PkgUtil.pkgForUid(getContext(), callerUid);
        }
        boolean res =
                isInWhiteList(servicePkgName)
                        || servicePkgName.equals(callerPkgName) // Service from/to same app is allowed.
                        || PkgUtil.isSystemApp(getContext(), servicePkgName)
                        || PkgUtil.isAppRunning(getContext(), servicePkgName);

        if (!res) {
            XLog.logVOnExecutor(String.format("SERVICE: %s--->%s  %s--->%s %s",
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
    public boolean checkBroadcast(String action, int receiverUid, int callerUid) {

        // Check if this is a boot complete action.
        if (isBootCompleteBroadcastAction(action)) {
            return checkBootCompleteBroadcast(receiverUid, callerUid);
        }

        String receiverPkgName =
                mPackagesCache.get(receiverUid);
        if (receiverPkgName == null) {
            PkgUtil.pkgForUid(getContext(), receiverUid);
        }
        if (TextUtils.isEmpty(receiverPkgName)) return true;

        boolean res =
                isInWhiteList(receiverPkgName)
                        || callerUid == receiverUid // Broadcast from/to same app is allowed.
                        || PkgUtil.isSystemApp(getContext(), receiverPkgName)
                        || PkgUtil.isAppRunning(getContext(), receiverPkgName);

        if (!res) {
            String callerPkgName =
                    mPackagesCache.get(callerUid);
            if (callerPkgName == null) {
                callerPkgName = PkgUtil.pkgForUid(getContext(), callerUid);
            }
            XLog.logVOnExecutor(String.format("BROADCAST: %s %s--->%s  %s--->%s %s",
                    action,
                    PkgUtil.loadNameByPkgName(getContext(), callerPkgName),
                    PkgUtil.loadNameByPkgName(getContext(), receiverPkgName),
                    callerPkgName,
                    receiverPkgName,
                    "拒绝×"),
                    mWorkingService);
        }
        return res;
    }

    private boolean checkBootCompleteBroadcast(int receiverUid, int callerUid) {

        return true;
    }

    private static boolean isBootCompleteBroadcastAction(String action) {
        return Intent.ACTION_BOOT_COMPLETED.equals(action);
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
        ServiceManager.addService(XIntentFirewallManager.INTENT_FIREWALL_SERVICE, asBinder());
        construct();
    }

    @Override
    public void systemReady() {
        checkSafeMode();
        getConfigFromSettings();
        cachePackages();
        loadBootPackageSettings();
        loadStartPackageSettings();
        registerPackageObserver();
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

    @Override
    @BinderCall
    public void clearProcess() throws RemoteException {
        enforceCallingPermissions();
        mFirewallHandler.sendEmptyMessage(IntentFirewallHandlerMessages.MSG_CLEARPROCESS);
    }

    @Override
    @BinderCall
    public void setIFWEnabled(boolean enabled) throws RemoteException {
        enforceCallingPermissions();
        mFirewallHandler.obtainMessage(IntentFirewallHandlerMessages.MSG_SETENABLED, enabled)
                .sendToTarget();
    }

    @Override
    @BinderCall
    public boolean isIFWEnabled() {
        enforceCallingPermissions();
        return !mIsSafeMode && mIFWEnabled.get();
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

    @SuppressLint("HandlerLeak")
    private class HandlerImpl extends Handler implements IntentFirewallHandler {

        private final Holder<FutureTask<String[]>> mClearingTask = new Holder<>();

        @Override
        public void handleMessage(Message msg) {
            XLog.logV("handleMessage: " + IntentFirewallHandlerMessages.decodeMessage(msg.what));
            super.handleMessage(msg);
            switch (msg.what) {
                case IntentFirewallHandlerMessages.MSG_CLEARPROCESS:
                    HandlerImpl.this.clearProcess();
                    break;
                case IntentFirewallHandlerMessages.MSG_SETENABLED:
                    HandlerImpl.this.setEnabled((Boolean) msg.obj);
                    break;
                case IntentFirewallHandlerMessages.MSG_ONSCREENOFF:
                    HandlerImpl.this.onScreenOff();
                    break;
                case IntentFirewallHandlerMessages.MSG_ONSCREENON:
                    HandlerImpl.this.onScreenOn();
                    break;
            }
        }

        @Override
        public void setEnabled(boolean enabled) {
            if (mIFWEnabled.compareAndSet(!enabled, enabled)) {
                TorSettings.IFW_ENABLED_B.writeToSystemSettings(getContext(), enabled);
            }
        }

        @Override
        public void clearProcess() {
            XStopWatch stopWatch = XStopWatch.start("onScreenOn, clear tasks");
            synchronized (mClearingTask) {
                if (mClearingTask.getData() != null && (!mClearingTask.getData().isDone()
                        && !mClearingTask.getData().isCancelled())) {
                    XLog.logV("clearProcess, Canceling existing clear task...");
                    mClearingTask.getData().cancel(true);
                    mClearingTask.setData(null);
                }
                stopWatch.split("cancel old one");
                FutureTask<String[]> futureTask = new FutureTask<>(new Callable<String[]>() {
                    @Override
                    public String[] call() throws Exception {
                        ActivityManager am = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
                        if (am == null) return null;
                        List<ActivityManager.RunningAppProcessInfo> processes =
                                am.getRunningAppProcesses();
                        int count = processes == null ? 0 : processes.size();
                        String[] cleared = new String[count];
                        for (int i = 0; i < count; i++) {
                            for (String runningPackageName : processes.get(i).pkgList) {
                                if (runningPackageName != null && !WHITE_LIST.contains(runningPackageName)) {
                                    if (PkgUtil.isSystemApp(getContext(), runningPackageName)) {
                                        continue;
                                    }
                                    if (PkgUtil.isAppRunningForeground(getContext(), runningPackageName)) {
                                        XLog.logV("App is in foreground, but will kill: " + runningPackageName);
                                    }
                                    am.forceStopPackage(runningPackageName);
                                    cleared[i] = runningPackageName;
                                    XLog.logV("Force stopped: " + runningPackageName);
                                }
                            }
                        }
                        return cleared;
                    }
                });
                mClearingTask.setData(futureTask);
            }
            mWorkingService.execute(mClearingTask.getData());
            stopWatch.stop();
        }

        @Override
        public void onScreenOff() {
            clearProcess();
        }

        @Override
        public void onScreenOn() {
            XStopWatch stopWatch = XStopWatch.start("onScreenOn, cancel clear task");
            synchronized (mClearingTask) {
                if (mClearingTask.getData() != null && (!mClearingTask.getData().isDone()
                        && !mClearingTask.getData().isCancelled())) {
                    XLog.logV("onScreenOn, Canceling existing clear task...");
                    mClearingTask.getData().cancel(true);
                    mClearingTask.setData(null);
                }
            }
            stopWatch.stop();
        }
    }
}
