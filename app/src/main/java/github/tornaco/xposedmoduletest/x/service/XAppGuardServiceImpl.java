package github.tornaco.xposedmoduletest.x.service;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.view.KeyEvent;

import com.google.common.base.Preconditions;

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
import github.tornaco.xposedmoduletest.IWatcher;
import github.tornaco.xposedmoduletest.bean.PackageInfo;
import github.tornaco.xposedmoduletest.bean.PackageInfoDaoUtil;
import github.tornaco.xposedmoduletest.provider.PackageProvider;
import github.tornaco.xposedmoduletest.x.app.XAppGuardManager;
import github.tornaco.xposedmoduletest.x.app.XMode;
import github.tornaco.xposedmoduletest.x.bean.BlurSettings;
import github.tornaco.xposedmoduletest.x.bean.VerifySettings;
import github.tornaco.xposedmoduletest.x.service.provider.TorSettings;
import github.tornaco.xposedmoduletest.x.submodules.AppGuardSubModuleManager;
import github.tornaco.xposedmoduletest.x.submodules.SubModule;
import github.tornaco.xposedmoduletest.x.util.Closer;
import github.tornaco.xposedmoduletest.x.util.XLog;
import lombok.Synchronized;

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
    private AtomicBoolean mDebugEnabled = new AtomicBoolean(false);

    private BlurSettings mBlurSettings;
    private VerifySettings mVerifySettings;

    @SuppressLint("UseSparseArrays")
    private final Map<Integer, Transaction> mTransactionMap = new HashMap<>();

    private final Set<String> mFeatures = new HashSet<>(FEATURE_COUNT);

    private static final Set<String> PREBUILT_WHITE_LIST = new HashSet<>();

    private final Map<String, PackageInfo> mGuardPackages = new HashMap<>();
    private final Set<String> mVerifiedPackages = new HashSet<>();
    private final Set<IWatcher> mWatchers = new HashSet<>();

    private static int sClientUID = 0;

    private final Holder<String> mTopActivityPkg = new Holder<>();

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

    // Safe mode is the last clear place user can stay.
    private boolean mIsSafeMode = false;

    private UUID mSerialUUID = UUID.randomUUID();

    @Override
    public void publish() {
        try {
            XLog.logD("published by uid: " + Binder.getCallingUid());
            construct();
            ServiceManager.addService(XAppGuardManager.APP_GUARD_SERVICE, asBinder());
            publishFeature(XAppGuardManager.Feature.BASE);
        } catch (Exception e) {
            XLog.logD("*** FATAL*** Fail publish our svc:" + e);
        }
    }

    private void checkSafeMode() {
        mIsSafeMode = getContext().getPackageManager().isSafeMode();
    }

    private void construct() {
        mServiceHandler = onCreateServiceHandler();
        XLog.logV("construct, mServiceHandler: " + mServiceHandler + " -" + serial());
    }

    protected Handler onCreateServiceHandler() {
        return new AppGuardServiceHandlerImpl();
    }

    @Override
    public void systemReady() {
        checkSafeMode();
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

            boolean uninstallProEnabled = (boolean) TorSettings.UNINSTALL_GUARD_ENABLED_B.readFromSystemSettings(getContext());
            mUninstallProEnabled.set(uninstallProEnabled);

            boolean debug = (boolean) TorSettings.APP_GUARD_DEBUG_MODE_B.readFromSystemSettings(getContext());
            mDebugEnabled.set(debug);

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

    synchronized private void loadPackageSettings() {
        XLog.logV("loadPackageSettings...");
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

            mGuardPackages.clear();

            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                PackageInfo packageInfo = PackageInfoDaoUtil.readEntity(cursor, 0);
                XLog.logV("readEntity of: " + packageInfo);
                String key = packageInfo.getPkgName();
                if (TextUtils.isEmpty(key)) continue;
                mGuardPackages.put(key, packageInfo);
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
                            mWorkingService.execute(new Runnable() {
                                @Override
                                public void run() {
                                    loadPackageSettings();
                                }
                            });
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
            sClientUID = applicationInfo.uid;
            XLog.logV("sClientUID:" + sClientUID);

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
    @Synchronized
    public void publishFeature(String f) {
        if (!mFeatures.contains(f)) {
            mFeatures.add(f);
        }
    }

    @Override
    public void shutdown() {
        XLog.logD("shutdown...");
    }

    @Override
    public boolean interruptPackageRemoval(String pkg) {
        return mUninstallProEnabled.get();
    }

    @Override
    public boolean onEarlyVerifyConfirm(String pkg) {
        if (pkg == null) {
            XLog.logV("onEarlyVerifyConfirm, false@pkg-null");
            return false;
        }
        if (mIsSafeMode) {
            XLog.logV("onEarlyVerifyConfirm, false@safe-mode:" + pkg);
            return false;
        }
        if (!mEnabled.get()) {
            XLog.logV("onEarlyVerifyConfirm, false@disabled:" + pkg);
            return false;
        }
        if (PREBUILT_WHITE_LIST.contains(pkg)) {
            XLog.logV("onEarlyVerifyConfirm, false@prebuilt-w-list:" + pkg);
            return false;
        } // White list.

        if (mVerifiedPackages.contains(pkg)) {
            XLog.logV("onEarlyVerifyConfirm, false@verified-list:" + pkg);
            return false;
        } // Passed.
        PackageInfo p = mGuardPackages.get(pkg);
        if (p == null) return false;
        return p.getGuard();
    }

    @Override
    public void verify(Bundle options, String pkg, int uid, int pid, VerifyListener listener) {
        verifyInternal(options, pkg, uid, pid, false, listener);
    }

    private void verifyInternal(Bundle options, String pkg, int uid, int pid,
                                boolean injectHomeOnFail, VerifyListener listener) {
        if (mServiceHandler == null) {
            XLog.logF("WTF? AppGuardServiceHandler is null?");
            return;
        }
        XLog.logV("verifyInternal: " + pkg);
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
    public void onKeyEvent(KeyEvent event) {
        mServiceHandler.obtainMessage(AppGuardServiceHandlerMessages.MSG_ONKEYEVENT, event).sendToTarget();
    }

    @Override
    @BinderCall(restrict = "anyone")
    public void onActivityPackageResume(String pkg) throws RemoteException {
        if (mServiceHandler == null) {
            XLog.logV("mServiceHandler@" + serial() + " : " + mServiceHandler);
            XLog.logV("onActivityPackageResume caller:" + Binder.getCallingUid());
            XLog.logF("WTF? AppGuardServiceHandler is null @onActivityPackageResume-" + serial());
            return;
        }
        mServiceHandler.obtainMessage(AppGuardServiceHandlerMessages.MSG_ONACTIVITYPACKAGERESUME, pkg).sendToTarget();
    }

    @Override
    public String[] getSubModules() throws RemoteException {
        enforceCallingPermissions();
        long id = Binder.clearCallingIdentity();
        try {
            Object[] modules = AppGuardSubModuleManager.getInstance().getAllSubModules().toArray();
            final String[] tokens = new String[modules.length];
            for (int i = 0; i < modules.length; i++) {
                SubModule subModule = (SubModule) modules[i];
                tokens[i] = String.valueOf(subModule.name());
            }
            return tokens;
        } finally {
            Binder.restoreCallingIdentity(id);
        }
    }

    @Override
    public int getSubModuleStatus(final String token) throws RemoteException {
        enforceCallingPermissions();
        Preconditions.checkNotNull(token);
        long id = Binder.clearCallingIdentity();
        final Holder<Integer> status = new Holder<>();
        status.setData(SubModule.SubModuleStatus.ERROR.ordinal());
        try {
            Collections.consumeRemaining(AppGuardSubModuleManager.getInstance().getAllSubModules(),
                    new Consumer<SubModule>() {
                        @Override
                        public void accept(SubModule subModule) {
                            if (token.equals(subModule.name())) {
                                status.setData(subModule.getStatus().ordinal());
                            }
                        }
                    });
        } finally {
            Binder.restoreCallingIdentity(id);
        }
        return status.getData();
    }

    @Override
    @Deprecated
    public void onActivityResume(Activity activity) {
        enforceCallingPermissions();
        if (mServiceHandler == null) {
            XLog.logV("mServiceHandler@" + serial() + " : " + mServiceHandler);
            XLog.logV("onActivityResume caller:" + Binder.getCallingUid());
            XLog.logF("WTF? AppGuardServiceHandler is null @onActivityResume-" + serial());
            return;
        }
        mServiceHandler.obtainMessage(AppGuardServiceHandlerMessages.MSG_ONACTIVITYRESUME, activity).sendToTarget();
    }

    @Override
    public String serial() {
        return mSerialUUID.toString();
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
                return mGuardPackages.containsKey(pkg);
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
        enforceCallingPermissions();
        mServiceHandler.removeMessages(AppGuardServiceHandlerMessages.MSG_SETENABLED);
        mServiceHandler.obtainMessage(AppGuardServiceHandlerMessages.MSG_SETENABLED,
                enabled ? 1 : 0, 0)
                .sendToTarget();
    }

    @Override
    @BinderCall
    public boolean isUninstallInterruptEnabled() throws RemoteException {
        enforceCallingPermissions();
        return mUninstallProEnabled.get();
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
    public void setBlurSettings(BlurSettings settings) throws RemoteException {
        enforceCallingPermissions();
        mServiceHandler.removeMessages(AppGuardServiceHandlerMessages.MSG_SETBLURSETTINGS);
        mServiceHandler.obtainMessage(AppGuardServiceHandlerMessages.MSG_SETBLURSETTINGS, settings).sendToTarget();
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
        mServiceHandler.obtainMessage(AppGuardServiceHandlerMessages.MSG_SETRESULT, transactionID, res).sendToTarget();
    }

    @Override
    @BinderCall
    public void watch(IWatcher w) throws RemoteException {
        XLog.logD("iWatcher.watch-" + w);
        enforceCallingPermissions();
        mServiceHandler.obtainMessage(AppGuardServiceHandlerMessages.MSG_WATCH, w).sendToTarget();
    }

    @Override
    @BinderCall
    public void unWatch(IWatcher w) throws RemoteException {
        XLog.logD("iWatcher.unWatch-" + w);
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
    public void injectHomeEvent() throws RemoteException {
        enforceCallingPermissions();
        mServiceHandler.sendEmptyMessage(AppGuardServiceHandlerMessages.MSG_INJECTHOMEEVENT);
    }

    @Override
    public void setDebug(boolean debug) throws RemoteException {
        enforceCallingPermissions();
        mServiceHandler.obtainMessage(AppGuardServiceHandlerMessages.MSG_SETDEBUG, debug).sendToTarget();
    }

    @Override
    public boolean isDebug() throws RemoteException {
        enforceCallingPermissions();
        return mDebugEnabled.get();
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
                    String displayerName =
                            applicationInfo.metaData.getString(META_DATA_KEY_APP_GUARD_VERIFY_DISPLAYER);
                    if (TextUtils.isEmpty(displayerName)) return;
                    int uid = applicationInfo.uid;
                    XLog.logD("Verifier pkg:" + displayerName + ", uid:" + uid);
                } catch (Exception ignored) {

                }
            }
        });
    }


    protected void enforceCallingPermissions() {
        int callingUID = Binder.getCallingUid();
        XLog.logV("enforceCallingPermissions@uid:" + callingUID);
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
                XLog.logV("SCREEN OFF, Clearing passed pkgs...");
                mVerifiedPackages.clear();
            }
        }
    }

    private void onUserPresent() {
        XLog.logV("onUserPresent");
    }

    private static Intent buildVerifyIntent(boolean injectHome, int transId, String pkg) {
        Intent intent = new Intent(XAppGuardManager.ACTION_APP_GUARD_VERIFY_DISPLAYER);
        intent.setClassName(BuildConfig.APPLICATION_ID,
                "github.tornaco.xposedmoduletest.ui.VerifyDisplayerActivity");
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
            XLog.logV("handleMessage@" + AppGuardServiceHandlerMessages.decodeMessage(wht));
            switch (wht) {
                case AppGuardServiceHandlerMessages.MSG_SETENABLED:
                    AppGuardServiceHandlerImpl.this.setEnabled(msg.arg1 == 1);
                    break;
                case AppGuardServiceHandlerMessages.MSG_SETBLURSETTINGS:
                    AppGuardServiceHandlerImpl.this.setBlurSettings((BlurSettings) msg.obj);
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
                    AppGuardServiceHandlerImpl.this.unWatch((IWatcher) msg.obj);
                    break;
                case AppGuardServiceHandlerMessages.MSG_WATCH:
                    AppGuardServiceHandlerImpl.this.watch((IWatcher) msg.obj);
                    break;
                case AppGuardServiceHandlerMessages.MSG_VERIFY:
                    AppGuardServiceHandlerImpl.this.verify((VerifyArgs) msg.obj);
                    break;
                case AppGuardServiceHandlerMessages.MSG_MSG_TRANSACTION_EXPIRE_BASE:
                    AppGuardServiceHandlerImpl.this.setResult(wht, XMode.MODE_IGNORED);
                    break;
                case AppGuardServiceHandlerMessages.MSG_ONKEYEVENT:
                    AppGuardServiceHandlerImpl.this.onKeyEvent((KeyEvent) msg.obj);
                    break;
                case AppGuardServiceHandlerMessages.MSG_INJECTHOMEEVENT:
                    AppGuardServiceHandlerImpl.this.injectHomeEvent();
                    break;
                case AppGuardServiceHandlerMessages.MSG_SETDEBUG:
                    AppGuardServiceHandlerImpl.this.setDebug((Boolean) msg.obj);
                    break;
                case AppGuardServiceHandlerMessages.MSG_ONACTIVITYRESUME:
                    AppGuardServiceHandlerImpl.this.onActivityResume((Activity) msg.obj);
                    break;
                case AppGuardServiceHandlerMessages.MSG_ONACTIVITYPACKAGERESUME:
                    onActivityPackageResume((String) msg.obj);
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
            if (mUninstallProEnabled.compareAndSet(!enabled, enabled)) {
                TorSettings.UNINSTALL_GUARD_ENABLED_B.writeToSystemSettings(getContext(), enabled);
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
            Transaction transaction = mTransactionMap.remove(transactionID);
            if (transaction == null) {
                XLog.logD("Can not find transaction for:" + transactionID);
                return;
            }
            if (res == XMode.MODE_ALLOWED) {
                mVerifiedPackages.add(transaction.pkg);
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
            Preconditions.checkNotNull(w);
            synchronized (mWatchers) { //FIXME Link to death~~~
                if (!mWatchers.contains(w)) {
                    mWatchers.add(w);
                    XLog.logD("iWatcher.watch-OK " + w);
                }
            }
        }

        @Override
        public void unWatch(IWatcher w) {
            enforceCallingPermissions();
            Preconditions.checkNotNull(w);
            synchronized (mWatchers) { //FIXME Link to death~~~
                if (mWatchers.contains(w)) {
                    mWatchers.remove(w);
                    XLog.logD("iWatcher.unWatch-OK " + w);
                }
            }
        }

        private void notifyWatcherUserLeaving(final String reason) {
            XLog.logV("notifyWatcherUserLeaving:" + reason);
            mWorkingService.execute(new Runnable() {
                @Override
                public void run() {
                    synchronized (mWatchers) {
                        Collections.consumeRemaining(mWatchers, new Consumer<IWatcher>() {
                            @Override
                            public void accept(final IWatcher iWatcher) {
                                try {
                                    iWatcher.onUserLeaving(reason);
                                    XLog.logD("iWatcher.onKeyEvent-" + reason);
                                } catch (Throwable ignored) {
                                }

                            }
                        });

                        // FIXME. FFFFFF?
                        mWatchers.clear();
                    }
                }
            });
        }

        @Override
        public void mockCrash() {
            throw new IllegalStateException("Let's CRASH, bye bye you...");
        }

        @Override
        public void onKeyEvent(KeyEvent keyEvent) {
            if (keyEvent.getAction() == KeyEvent.ACTION_UP
                    && (keyEvent.getKeyCode() == KeyEvent.KEYCODE_HOME
                    || keyEvent.getKeyCode() == KeyEvent.KEYCODE_APP_SWITCH)) {
                onHomeOrRecent();
            }
            if (keyEvent.getAction() == KeyEvent.ACTION_UP
                    && (keyEvent.getKeyCode() == KeyEvent.KEYCODE_POWER)) {
                onPowerKey();
            }
        }

        @Override
        public void injectHomeEvent() {
            KeyEventSender.injectHomeKey();
        }

        @Override
        public void setDebug(boolean debug) {
            if (mDebugEnabled.compareAndSet(!debug, debug)) {
                TorSettings.APP_GUARD_DEBUG_MODE_B.writeToSystemSettings(getContext(), debug);
            }
            XLog.setDebug(debug);
        }

        @Override
        public void onActivityResume(Activity activity) {
            XLog.logV("onActivityResume: " + activity);
            onActivityPackageResume(activity.getPackageName());
        }

        @Override
        public void onActivityPackageResume(String pkg) {
            XLog.logV("onPkgResume: " + pkg);
            mTopActivityPkg.setData(pkg);
            if (!onEarlyVerifyConfirm(pkg)) {
                XLog.logV("onEarlyVerifyConfirm...");
                return;
            }
            verifyInternal(null, pkg, 0, 0, true, VerifyListenerAdapter.getDefault());
        }

        private void onPowerKey() {
        }

        private void onHomeOrRecent() {
            notifyWatcherUserLeaving("Home");

            if (mVerifySettings != null) {
                boolean clearOnHome = mVerifySettings.isVerifyOnHome();
                if (clearOnHome) {
                    XLog.logV("onHomeOrRecent, clearing...");
                    mVerifiedPackages.clear();
                }
            }
        }
    }

}
