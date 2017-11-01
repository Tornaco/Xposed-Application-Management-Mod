package github.tornaco.xposedmoduletest.x.service;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import github.tornaco.android.common.Collections;
import github.tornaco.android.common.Consumer;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.IWatcher;
import github.tornaco.xposedmoduletest.x.app.XAppGuardManager;
import github.tornaco.xposedmoduletest.x.app.XMode;
import github.tornaco.xposedmoduletest.x.bean.BlurSettings;
import github.tornaco.xposedmoduletest.x.bean.PackageSettings;
import github.tornaco.xposedmoduletest.x.bean.VerifySettings;
import github.tornaco.xposedmoduletest.x.service.provider.TorSettings;
import github.tornaco.xposedmoduletest.x.util.FileUtil;
import github.tornaco.xposedmoduletest.x.util.XLog;
import github.tornaco.xposedmoduletest.x.util.XStopWatch;

import static github.tornaco.xposedmoduletest.x.app.XAppGuardManager.Feature.FEATURE_COUNT;
import static github.tornaco.xposedmoduletest.x.app.XAppGuardManager.META_DATA_KEY_APP_GUARD_VERIFY_DISPLAYER;

/**
 * Created by guohao4 on 2017/10/23.
 * Email: Tornaco@163.com
 */
class XAppGuardServiceImpl extends XAppGuardServiceAbs {

    private static final long TRANSACTION_EXPIRE_TIME = 60 * 1000;

    private final Map<String, PackageSettings> FAST_PKG_SETTINGS_MAP = Maps.newHashMap();

    private PackageSettingsLoader mPkgSettingsLoader;

    private Handler mServiceHandler;

    private AtomicBoolean mEnabled = new AtomicBoolean(false);
    private AtomicBoolean mUninstallProEnabled = new AtomicBoolean(false);
    private AtomicBoolean mVerifyOnHome = new AtomicBoolean(false);
    private AtomicBoolean mVerifyOnScreenOff = new AtomicBoolean(false);
    private AtomicBoolean mBlur = new AtomicBoolean(false);
    private AtomicInteger mBlurPolicy = new AtomicInteger(XAppGuardManager.BlurPolicy.BLUR_WATCHED);

    @SuppressLint("UseSparseArrays")
    private final Map<Integer, Transaction> TRANSACTION_MAP = new HashMap<>();

    private final Set<String> FEATURES = new HashSet<>(FEATURE_COUNT);

    private static final Set<String> PREBUILT_WHITE_LIST = new HashSet<>();

    private static int sClientUID = 0;

    static {
        PREBUILT_WHITE_LIST.add("com.android.systemui");
        // PREBUILT_WHITE_LIST.add("com.android.packageinstaller");
        PREBUILT_WHITE_LIST.add("android");
        PREBUILT_WHITE_LIST.add("com.cyanogenmod.trebuchet");
        // It is good for user if our mod crash.
        // PREBUILT_WHITE_LIST.add("de.robv.android.xposed.installer");
        PREBUILT_WHITE_LIST.add(BuildConfig.APPLICATION_ID);
    }

    private final ExecutorService mWorkingService = Executors.newCachedThreadPool();

    private BroadcastReceiver mScreenReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    onScreenOff();
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
    private boolean mIsSafeMode = false;

    private final Object LOCK = new Object();

    @Override
    public void publish() {
        try {
            XLog.logD("published by uid: " + Binder.getCallingUid());
            ServiceManager.addService(XAppGuardManager.APP_GUARD_SERVICE, asBinder());
            publishFeature(XAppGuardManager.Feature.BASE);
        } catch (Exception e) {
            XLog.logD("*** FATAL*** Fail publish our svc:" + e);
        }
    }

    private void checkSafeMode() {
        mIsSafeMode = getContext().getPackageManager().isSafeMode();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void construct() {
        mServiceHandler = new ServiceHandlerImpl();
        mPkgSettingsLoader = new PackageSettingsLoader();
    }

    @Override
    public void systemReady() {
        checkSafeMode();
        construct();
        registerReceiver();
        getConfigFromSettings();
        loadPackageSettings();
        cacheUIDForPackages();
    }

    private void getConfigFromSettings() {
        boolean appGuardEnabled = (boolean) TorSettings.APP_GUARD_ENABLED_B.readFromSystemSettings(getContext());
        mEnabled.set(appGuardEnabled);
    }

    private void registerReceiver() {
        getContext().registerReceiver(mScreenReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        intentFilter.addDataScheme("package");
        getContext().registerReceiver(mPackageReceiver, intentFilter);
    }

    private void loadPackageSettings() {
        Set<PackageSettings> packageSettings = mPkgSettingsLoader.loadPackageSettings();
        Collections.consumeRemaining(packageSettings, new Consumer<PackageSettings>() {
            @Override
            public void accept(PackageSettings packageSettings) {
                FAST_PKG_SETTINGS_MAP.put(packageSettings.getPkgName(), packageSettings);
            }
        });
    }

    private void cacheUIDForPackages() {
        PackageManager pm = this.getContext().getPackageManager();
        try {
            ApplicationInfo applicationInfo = pm.getApplicationInfo(BuildConfig.APPLICATION_ID, 0);
            if (BuildConfig.APPLICATION_ID.equals(applicationInfo.packageName)) {
                sClientUID = applicationInfo.uid;
            }

            // Filter all apps.
            List<ApplicationInfo> applicationInfos = pm.getInstalledApplications(0);
            Collections.consumeRemaining(applicationInfos, new Consumer<ApplicationInfo>() {
                @Override
                public void accept(ApplicationInfo applicationInfo) {
                    String pkg = applicationInfo.packageName;
                    parsePackage(pkg);
                }
            });
        } catch (Exception ignored) {
            XLog.logD("Can not get UID for our client:" + ignored);
        }
    }

    @Override
    public void publishFeature(String f) {
    }

    @Override
    public void shutdown() {

    }

    @Override
    public boolean interruptPackageRemoval(String pkg) {
        return false;
    }

    @Override
    public boolean onEarlyVerifyConfirm(String pkg) {
        return !mIsSafeMode
                && mEnabled.get()
                && !PREBUILT_WHITE_LIST.contains(pkg)
                && FAST_PKG_SETTINGS_MAP.containsKey(pkg)
                && FAST_PKG_SETTINGS_MAP.get(pkg).isVerify();
    }

    @Override
    public void verify(Bundle options, String pkg, int uid, int pid, VerifyListener listener) {
        VerifyArgs args = new VerifyArgs(options, pkg, uid, pid, listener);
        mServiceHandler.obtainMessage(ServiceHandlerMessages.MSG_VERIFY, args).sendToTarget();
    }

    @Override
    public void onUserLeaving(String reason) {

    }

    @Override
    public boolean isBlurForPkg(String pkg) {
        return false;
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
        mServiceHandler.obtainMessage(ServiceHandlerMessages.MSG_SETENABLED, enabled ? 1 : 0, 0).sendToTarget();
    }

    @Override
    @BinderCall
    public boolean isUninstallInterruptEnabled() throws RemoteException {
        return mUninstallProEnabled.get();
    }

    @Override
    @BinderCall
    public void setUninstallInterruptEnabled(boolean enabled) throws RemoteException {
        mServiceHandler.obtainMessage(ServiceHandlerMessages.MSG_SETUNINSTALLINTERRUPTENABLED, enabled ? 1 : 0, 0).sendToTarget();
    }

    @Override
    public void setVerifySettings(VerifySettings settings) throws RemoteException {
        mServiceHandler.obtainMessage(ServiceHandlerMessages.MSG_SETVERIFYSETTINGS, settings).sendToTarget();
    }

    @Override
    @BinderCall
    public VerifySettings getVerifySettings() throws RemoteException {
        return null;
    }

    @Override
    @BinderCall
    public void setBlurSettings(BlurSettings settings) throws RemoteException {
        mServiceHandler.obtainMessage(ServiceHandlerMessages.MSG_SETBLURSETTINGS, settings).sendToTarget();
    }

    @Override
    @BinderCall
    public BlurSettings getBlurSettings() throws RemoteException {
        return null;
    }

    @Override
    @BinderCall
    public void setResult(int transactionID, int res) throws RemoteException {
        mServiceHandler.obtainMessage(ServiceHandlerMessages.MSG_SETRESULT, transactionID, res).sendToTarget();
    }

    @Override
    @BinderCall
    public void testUI() throws RemoteException {

    }

    @Override
    @BinderCall
    public void addPackages(PackageSettings pkg) throws RemoteException {
        mServiceHandler.obtainMessage(ServiceHandlerMessages.MSG_ADDPACKAGES, pkg).sendToTarget();
    }

    @Override
    @BinderCall
    public void removePackages(PackageSettings pkg) throws RemoteException {
        mServiceHandler.obtainMessage(ServiceHandlerMessages.MSG_REMOVEPACKAGES, pkg).sendToTarget();
    }

    @Override
    @BinderCall
    public List<PackageSettings> getPackageSettings() throws RemoteException {
        final XStopWatch stopWatch = XStopWatch.start("getPackageSettings");
        enforceCallingPermissions();
        if (FAST_PKG_SETTINGS_MAP.values().size() == 0)
            return Lists.newArrayListWithCapacity(0);
        Object[] arr = FAST_PKG_SETTINGS_MAP.values().toArray();
        final List<PackageSettings> out = Lists.newArrayListWithCapacity(arr.length);
        Collections.consumeRemaining(arr, new Consumer<Object>() {
            @Override
            public void accept(Object o) {
                out.add((PackageSettings) o);
                stopWatch.split(((PackageSettings) o).getPkgName());
            }
        });
        stopWatch.stop();
        return out;
    }

    @Override
    @BinderCall
    public void watch(IWatcher w) throws RemoteException {

    }

    @Override
    @BinderCall
    public void unWatch(IWatcher w) throws RemoteException {

    }

    @Override
    @BinderCall
    public void mockCrash() throws RemoteException {

    }

    private void parsePackageAsync(final String... pkg) {
        mWorkingService.execute(new Runnable() {
            @Override
            public void run() {
                parsePackage(pkg);
            }
        });
    }

    private void parsePackage(final String... pkg) {
        final PackageManager pm = getContext().getPackageManager();

        Collections.consumeRemaining(pkg, new Consumer<String>() {
            @Override
            public void accept(String s) {
                ApplicationInfo applicationInfo;
                try {
                    applicationInfo = pm.getApplicationInfo(s, PackageManager.GET_META_DATA);
                    if (applicationInfo.metaData == null) return;
                    String displayerName = applicationInfo.metaData.getString(META_DATA_KEY_APP_GUARD_VERIFY_DISPLAYER);
                    if (TextUtils.isEmpty(displayerName)) return;
                    int uid = applicationInfo.uid;
                    XLog.logD("Verifier pkg:" + displayerName + ", uid:" + uid);
                } catch (Exception ignored) {

                }
            }
        });
    }


    protected void enforceCallingPermissions() {
        if (BuildConfig.DEBUG) return;
        int callingUID = Binder.getCallingUid();
        if (callingUID == android.os.Process.myUid() || (sClientUID > 0 && sClientUID == callingUID)) {
            return;
        }
        throw new SecurityException("Package of uid:" + callingUID
                + ", does not have permission to interact with XAppGuardServiceImpl");
    }

    private void onScreenOff() {
        XLog.logV("SCREEN OFF, Clearing passed pkgs...");
    }


    private static Intent buildVerifyIntent(boolean allow3rd, int transId, String pkg) {
        Intent intent = new Intent(XAppGuardManager.ACTION_APP_GUARD_VERIFY_DISPLAYER);
        intent.setClassName(BuildConfig.APPLICATION_ID,
                "github.tornaco.xposedmoduletest.ui.VerifyDisplayerActivity");
        intent.putExtra(XAppGuardManager.EXTRA_PKG_NAME, pkg);
        intent.putExtra(XAppGuardManager.EXTRA_TRANS_ID, transId);
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
    private class ServiceHandlerImpl extends Handler
            implements ServiceHandler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int wht = msg.what;
            XLog.logV("handleMessage@" + ServiceHandlerMessages.decodeMessage(wht));
            switch (wht) {
                case ServiceHandlerMessages.MSG_SETENABLED:
                    setEnabled(msg.arg1 == 1);
                    break;
                case ServiceHandlerMessages.MSG_SETBLURSETTINGS:
                    setBlurSettings((BlurSettings) msg.obj);
                    break;
                case ServiceHandlerMessages.MSG_ADDPACKAGES:
                    addPackages((PackageSettings) msg.obj);
                    break;
                case ServiceHandlerMessages.MSG_MOCKCRASH:
                    mockCrash();
                    break;
                case ServiceHandlerMessages.MSG_REMOVEPACKAGES:
                    removePackages((PackageSettings) msg.obj);
                    break;
                case ServiceHandlerMessages.MSG_SETRESULT:
                    setResult(msg.arg1, msg.arg2);
                    break;
                case ServiceHandlerMessages.MSG_SETUNINSTALLINTERRUPTENABLED:
                    setUninstallInterruptEnabled(msg.arg1 == 1);
                    break;
                case ServiceHandlerMessages.MSG_SETVERIFYSETTINGS:
                    setVerifySettings((VerifySettings) msg.obj);
                    break;
                case ServiceHandlerMessages.MSG_TESTUI:
                    testUI();
                    break;
                case ServiceHandlerMessages.MSG_UNWATCH:
                    unWatch((IWatcher) msg.obj);
                    break;
                case ServiceHandlerMessages.MSG_WATCH:
                    watch((IWatcher) msg.obj);
                    break;
                case ServiceHandlerMessages.MSG_VERIFY:
                    verify((VerifyArgs) msg.obj);
                    break;
                default:
                    XLog.logF("Unknown msg:" + wht);
                    break;
            }
        }

        @Override
        public void setEnabled(boolean enabled) {
            if (mEnabled.compareAndSet(!enabled, enabled)) {
                TorSettings.APP_GUARD_ENABLED_B.writeToSystemSettings(getContext(), enabled);
            }
        }

        @Override
        public void setUninstallInterruptEnabled(boolean enabled) {

        }

        @Override
        public void setVerifySettings(VerifySettings settings) {

        }


        @Override
        public void setBlurSettings(BlurSettings settings) {

        }


        @Override
        public void setResult(int transactionID, int res) {
            Transaction transaction = TRANSACTION_MAP.remove(transactionID);
            if (transaction == null) {
                XLog.logD("Can not find transaction for:" + transactionID);
                return;
            }
            if (res == XMode.MODE_ALLOWED) {
//                PASSED_PACKAGES.add(transaction.pkg);
            }
            transaction.listener.onVerifyRes(transaction.pkg, transaction.uid, transaction.pid, res);
            mServiceHandler.removeMessages(MSG_TRANSACTION_EXPIRE_BASE + transactionID);
        }

        @Override
        public void verify(VerifyArgs args) {
            XLog.logD("onVerify:" + args);
            int tid = TransactionFactory.transactionID();
            int uid = args.uid;
            int pid = args.pid;
            String pkg = args.pkg;
            Bundle bnds = args.bnds;
            VerifyListener listener = args.listener;

            Transaction transaction = new Transaction(listener, uid, pid, tid, pkg);

            synchronized (TRANSACTION_MAP) {
                TRANSACTION_MAP.put(tid, transaction);
            }

            onNewTransaction(tid);

            Intent intent = buildVerifyIntent(false, tid, pkg);
            try {
                getContext().startActivity(intent, bnds);
            } catch (ActivityNotFoundException anf) {
                XLog.logD("*** FATAL ERROR *** ActivityNotFoundException!!!");
                setResult(tid, XMode.MODE_ALLOWED);
            }
        }

        private void onNewTransaction(int transaction) {
            sendMessageDelayed(obtainMessage(MSG_TRANSACTION_EXPIRE_BASE
                            + transaction,
                    transaction), TRANSACTION_EXPIRE_TIME);
        }

        @Override
        public void testUI() {

        }

        @Override
        public void addPackages(final PackageSettings pkg) {
            FAST_PKG_SETTINGS_MAP.put(pkg.getPkgName(), pkg);
            mWorkingService.execute(new Runnable() {
                @Override
                public void run() {
                    synchronized (LOCK) {
                        pkg.writeTo(PackageSettingsLoader.PKG_SETTINGS_DIR);
                    }
                }
            });
        }

        @Override
        public void removePackages(PackageSettings pkg) {
            FAST_PKG_SETTINGS_MAP.remove(pkg.getPkgName());
            synchronized (LOCK) {
                pkg.deleteFrom(PackageSettingsLoader.PKG_SETTINGS_DIR);
            }
        }


        @Override
        public void watch(IWatcher w) {

        }

        @Override
        public void unWatch(IWatcher w) {

        }

        @Override
        public void mockCrash() {

        }
    }

    private static class PackageSettingsLoader {

        private static final File PKG_SETTINGS_DIR;

        static {
            File dataDir = Environment.getDataDirectory();
            File systemDir = new File(dataDir, "system/app_guard/");
            PKG_SETTINGS_DIR = new File(systemDir, "pkgs");
        }

        Set<PackageSettings> loadPackageSettings() {
            if (FileUtil.isEmptyDir(PKG_SETTINGS_DIR)) return Sets.newHashSet();
            final Set<PackageSettings> all = Sets.newHashSet();
            github.tornaco.android.common.Collections.consumeRemaining(
                    Files.fileTreeTraverser().children(PKG_SETTINGS_DIR),
                    new Consumer<File>() {
                        @Override
                        public void accept(File file) {
                            try {
                                PackageSettings packageSettings = PackageSettings.readFrom(file);
                                if (TextUtils.isEmpty(packageSettings.getPkgName())) {
                                    // Skip bad one.
                                    return;
                                }
                                XLog.logV("Read PackageSettings:" + packageSettings);
                                all.add(packageSettings);
                            } catch (Exception e) {
                                XLog.logF("Fail read PackageSettings:"
                                        + file
                                        + ", Exception:"
                                        + Log.getStackTraceString(e));
                            }
                        }
                    });
            return all;
        }
    }
}
