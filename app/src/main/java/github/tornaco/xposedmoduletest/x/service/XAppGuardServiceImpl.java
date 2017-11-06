package github.tornaco.xposedmoduletest.x.service;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.google.common.base.Preconditions;

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
import github.tornaco.xposedmoduletest.bean.PackageInfo;
import github.tornaco.xposedmoduletest.bean.PackageInfoDaoUtil;
import github.tornaco.xposedmoduletest.provider.PackageProvider;
import github.tornaco.xposedmoduletest.x.app.XAppGuardManager;
import github.tornaco.xposedmoduletest.x.app.XMode;
import github.tornaco.xposedmoduletest.x.bean.BlurSettings;
import github.tornaco.xposedmoduletest.x.bean.VerifySettings;
import github.tornaco.xposedmoduletest.x.service.provider.TorSettings;
import github.tornaco.xposedmoduletest.x.util.Closer;
import github.tornaco.xposedmoduletest.x.util.XLog;

import static github.tornaco.xposedmoduletest.x.app.XAppGuardManager.Feature.FEATURE_COUNT;
import static github.tornaco.xposedmoduletest.x.app.XAppGuardManager.META_DATA_KEY_APP_GUARD_VERIFY_DISPLAYER;

/**
 * Created by guohao4 on 2017/10/23.
 * Email: Tornaco@163.com
 */
class XAppGuardServiceImpl extends XAppGuardServiceAbs {

    private static final long TRANSACTION_EXPIRE_TIME = 60 * 1000;

    private Handler mServiceHandler;

    private AtomicBoolean mEnabled = new AtomicBoolean(false);
    private AtomicBoolean mUninstallProEnabled = new AtomicBoolean(false);

    private BlurSettings mBlurSettings;
    private VerifySettings mVerifySettings;

    @SuppressLint("UseSparseArrays")
    private final Map<Integer, Transaction> TRANSACTION_MAP = new HashMap<>();

    private final Set<String> FEATURES = new HashSet<>(FEATURE_COUNT);

    private static final Set<String> PREBUILT_WHITE_LIST = new HashSet<>();
    private static final Map<String, PackageInfo> GUARD_PACKAGES = new HashMap<>();
    private static final Set<String> VERIFIED_PACKAGES = new HashSet<>();

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
    }

    @Override
    public void systemReady() {
        checkSafeMode();
        construct();
        getConfigFromSettings();
        cacheUIDForPackages();
        loadPackageSettings();
        registerPackageObserver();
        registerReceiver();
    }

    private void getConfigFromSettings() {
        try {
            boolean appGuardEnabled = (boolean) TorSettings.APP_GUARD_ENABLED_B.readFromSystemSettings(getContext());
            mEnabled.set(appGuardEnabled);

            ContentResolver resolver = getContext().getContentResolver();
            if (resolver == null) return;
            mBlurSettings = BlurSettings.from(Settings.System.getString(resolver, BlurSettings.KEY_SETTINGS));
            mVerifySettings = VerifySettings.from(Settings.System.getString(resolver, VerifySettings.KEY_SETTINGS));

            XLog.logV(String.valueOf(mBlurSettings));
            XLog.logV(String.valueOf(mVerifySettings));
            XLog.logV(String.valueOf(mEnabled));
        } catch (Throwable e) {
            XLog.logF("Fail getConfigFromSettings:" + Log.getStackTraceString(e));
        }
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
        ContentResolver contentResolver = getContext().getContentResolver();
        if (contentResolver == null) {
            // Happen when early start.
            return;
        }
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(PackageProvider.CONTENT_URI, null, null, null, null);
            if (cursor == null) {
                XLog.logF("Fail query pkgs, cursor is null");
                return;
            }
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                PackageInfo packageInfo = PackageInfoDaoUtil.readEntity(cursor, 0);
                XLog.logV("readEntity of: " + packageInfo.getPkgName());
                String key = packageInfo.getPkgName();
                if (TextUtils.isEmpty(key)) continue;
                GUARD_PACKAGES.put(key, packageInfo);
            }
        } catch (Throwable e) {
            XLog.logF("Fail query pkgs:\n" + Log.getStackTraceString(e));
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
            contentResolver.registerContentObserver(PackageProvider.CONTENT_URI,
                    false, new ContentObserver(mServiceHandler) {
                        @Override
                        public void onChange(boolean selfChange, Uri uri) {
                            super.onChange(selfChange, uri);
                            XLog.logV("onChange 2");
                        }

                        @Override
                        public void onChange(boolean selfChange) {
                            super.onChange(selfChange);
                            XLog.logV("onChange 1");
                        }

                        @Override
                        public void onChange(boolean selfChange, Uri uri, int userId) {
                            super.onChange(selfChange, uri, userId);
                            XLog.logV("onChange 3");
                        }
                    });
        } catch (Exception e) {
            XLog.logF("Fail registerContentObserver:\n" + Log.getStackTraceString(e));
        }
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
        boolean prefy = !mIsSafeMode
                && mEnabled.get()
                && !PREBUILT_WHITE_LIST.contains(pkg) // White list.
                && !VERIFIED_PACKAGES.contains(pkg); // Passed.
        if (!prefy) return false;
        PackageInfo p = GUARD_PACKAGES.get(pkg);
        if (p == null) return false;
        return p.getGuard();
    }

    @Override
    public void verify(Bundle options, String pkg, int uid, int pid, VerifyListener listener) {
        VerifyArgs args = new VerifyArgs(options, pkg, uid, pid, listener);
        mServiceHandler.obtainMessage(ServiceHandlerMessages.MSG_VERIFY, args).sendToTarget();
    }

    @Override
    public void onUserLeaving(String reason) {
        mServiceHandler.obtainMessage(ServiceHandlerMessages.MSG_USERLEAVING, reason).sendToTarget();
    }

    @Override
    public boolean isBlurForPkg(String pkg) {
        if (mBlurSettings == null) return false;
        int policy = mBlurSettings.getPolicy();
        switch (policy) {
            case XAppGuardManager.BlurPolicy.BLUR_ALL:
                return true;
            case XAppGuardManager.BlurPolicy.BLUR_POLICY_UNKNOWN:
                return false;
            case XAppGuardManager.BlurPolicy.BLUR_WATCHED:
                return GUARD_PACKAGES.containsKey(pkg);
            default:
                return false;
        }
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
        enforceCallingPermissions();
        return mVerifySettings == null ? null : mVerifySettings.duplicate();
    }

    @Override
    @BinderCall
    public void setBlurSettings(BlurSettings settings) throws RemoteException {
        enforceCallingPermissions();
        mServiceHandler.obtainMessage(ServiceHandlerMessages.MSG_SETBLURSETTINGS, settings).sendToTarget();
    }

    @Override
    @BinderCall
    public BlurSettings getBlurSettings() throws RemoteException {
        enforceCallingPermissions();
        return mBlurSettings == null ? null : mBlurSettings.duplicate();
    }

    @Override
    @BinderCall
    public void setResult(int transactionID, int res) throws RemoteException {
        enforceCallingPermissions();
        mServiceHandler.obtainMessage(ServiceHandlerMessages.MSG_SETRESULT, transactionID, res).sendToTarget();
    }

    @Override
    @BinderCall
    public void watch(IWatcher w) throws RemoteException {
        enforceCallingPermissions();
    }

    @Override
    @BinderCall
    public void unWatch(IWatcher w) throws RemoteException {
        enforceCallingPermissions();
    }

    @Override
    @BinderCall
    public void mockCrash() throws RemoteException {
        enforceCallingPermissions();
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
                    ServiceHandlerImpl.this.setEnabled(msg.arg1 == 1);
                    break;
                case ServiceHandlerMessages.MSG_SETBLURSETTINGS:
                    ServiceHandlerImpl.this.setBlurSettings((BlurSettings) msg.obj);
                    break;
                case ServiceHandlerMessages.MSG_MOCKCRASH:
                    ServiceHandlerImpl.this.mockCrash();
                    break;
                case ServiceHandlerMessages.MSG_SETRESULT:
                    ServiceHandlerImpl.this.setResult(msg.arg1, msg.arg2);
                    break;
                case ServiceHandlerMessages.MSG_SETUNINSTALLINTERRUPTENABLED:
                    ServiceHandlerImpl.this.setUninstallInterruptEnabled(msg.arg1 == 1);
                    break;
                case ServiceHandlerMessages.MSG_SETVERIFYSETTINGS:
                    ServiceHandlerImpl.this.setVerifySettings((VerifySettings) msg.obj);
                    break;
                case ServiceHandlerMessages.MSG_UNWATCH:
                    ServiceHandlerImpl.this.unWatch((IWatcher) msg.obj);
                    break;
                case ServiceHandlerMessages.MSG_WATCH:
                    ServiceHandlerImpl.this.watch((IWatcher) msg.obj);
                    break;
                case ServiceHandlerMessages.MSG_VERIFY:
                    ServiceHandlerImpl.this.verify((VerifyArgs) msg.obj);
                    break;
                case ServiceHandlerMessages.MSG_MSG_TRANSACTION_EXPIRE_BASE:
                    ServiceHandlerImpl.this.setResult(wht, XMode.MODE_IGNORED);
                    break;
                case ServiceHandlerMessages.MSG_USERLEAVING:
                    ServiceHandlerImpl.this.userLeaving((String) msg.obj);
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
        public void setBlurSettings(final BlurSettings settings) {
            mBlurSettings = Preconditions.checkNotNull(settings);
            mWorkingService.execute(new Runnable() {
                @Override
                public void run() {
                    // Saving to db.
                    ContentResolver resolver = getContext().getContentResolver();
                    Settings.System.putString(resolver, BlurSettings.KEY_SETTINGS, settings.formatJson());
                }
            });
        }


        @Override
        public void setResult(int transactionID, int res) {
            Transaction transaction = TRANSACTION_MAP.remove(transactionID);
            if (transaction == null) {
                XLog.logD("Can not find transaction for:" + transactionID);
                return;
            }
            if (res == XMode.MODE_ALLOWED) {
                VERIFIED_PACKAGES.add(transaction.pkg);
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
        public void watch(IWatcher w) {

        }

        @Override
        public void unWatch(IWatcher w) {

        }

        @Override
        public void mockCrash() {

        }

        @Override
        public void userLeaving(String res) {
            if (mVerifySettings != null) {
                boolean clearOnHome = mVerifySettings.isVerifyOnHome();
                if (clearOnHome) {
                    VERIFIED_PACKAGES.clear();
                }
            }
        }
    }

}
