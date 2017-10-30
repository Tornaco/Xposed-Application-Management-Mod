package github.tornaco.xposedmoduletest.x;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
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
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.android.internal.os.AtomicFile;
import com.google.common.base.Preconditions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import github.tornaco.android.common.Collections;
import github.tornaco.android.common.Consumer;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.IWatcher;

import static github.tornaco.xposedmoduletest.x.XAppGuardManager.ACTION_APP_GUARD_VERIFY_DISPLAYER;
import static github.tornaco.xposedmoduletest.x.XAppGuardManager.Feature.FEATURE_COUNT;

/**
 * Created by guohao4 on 2017/10/23.
 * Email: Tornaco@163.com
 */
class XAppGuardServiceImpl extends XAppGuardServiceAbs implements Handler.Callback {

    private static final String META_DATA_KEY_APP_GUARD_VERIFY_DISPLAYER = "app_guard_verify_displayer";

    private static final String SETTINGS_APP_GUARD_ENABLED = "settings_app_guard_enabled";
    private static final String SETTINGS_APP_GUARD_UNINSTALL_PRO_ENABLED = "settings_app_guard_uninstall_pro_enabled";
    private static final String SETTINGS_APP_SCREENSHOT_BLUR_ENABLED = "settings_app_guard_app_screenshot_blur_enabled";
    private static final String SETTINGS_APP_SCREENSHOT_BLUR_SCALE = "settings_app_guard_app_screenshot_blur_sc";
    private static final String SETTINGS_APP_SCREENSHOT_BLUR_RADIUS = "settings_app_guard_app_screenshot_blur_ra";
    private static final String SETTINGS_APP_SCREENSHOT_BLUR_POLICY = "settings_app_guard_app_screenshot_blur_po";
    private static final String SETTINGS_ALLOW_3RD_VERIFIER = "settings_app_guard_allow_third_verifier";
    private static final String SETTINGS_PASSCODE = "settings_app_guard_passcode";
    private static final String SETTINGS_VERIFY_ON_HOME = "settings_app_guard_verify_on_home";
    private static final String SETTINGS_VERIFY_ON_SCREEN_OFF = "settings_app_guard_verify_on_sroff";

    private static int sClientUID = 0;

    private static final long TRANSACTION_EXPIRE_TIME = 60 * 1000;

    private static final boolean DEBUG_V = true;

    private static final int MSG_VERIFY_RES = 0x1;
    private static final int MSG_SET_ENABLED = 0x2;
    private static final int MSG_VERIFY = 0x3;
    private static final int MSG_READ_STATE = 0x4;
    private static final int MSG_WRITE_STATE = 0x5;
    private static final int MSG_ADD_PACKAGES = 0x6;
    private static final int MSG_REMOVE_PACKAGES = 0x7;
    private static final int MSG_PASS = 0x8;
    private static final int MSG_IGNORE = 0x9;
    private static final int MSG_SET_BLUR = 0x10;
    private static final int MSG_SET_BLUR_POLICY = 0x11;
    private static final int MSG_SET_BLUR_RADIUS = 0x12;
    private static final int MSG_SET_BLUR_SCALE = 0x13;
    private static final int MSG_SET_SET_ALLOW_3RD_VER = 0x14;
    private static final int MSG_SET_PASSCODE = 0x15;
    private static final int MSG_ON_HOME = 0x16;
    private static final int MSG_SET_VERIFY_ON_HOME = 0x17;
    private static final int MSG_SET_VERIFY_ON_SCREEN_OFF = 0x18;
    private static final int MSG_SET_APP_UNINSTALL_PRO = 0x19;
    private static final int MSG_FUCK_YR_SELF = 0x1024;
    private static final int MSG_TRANSACTION_EXPIRE_BASE = 0x99;

    private Context mContext;
    private Handler mHandler;

    private AtomicBoolean mEnabled = new AtomicBoolean(false);
    private AtomicBoolean mUninstallProEnabled = new AtomicBoolean(false);
    private AtomicBoolean mVerifyOnHome = new AtomicBoolean(false);
    private AtomicBoolean mVerifyOnScreenOff = new AtomicBoolean(false);
    private AtomicBoolean mBlur = new AtomicBoolean(false);
    private AtomicBoolean m3rdVerifierAllowed = new AtomicBoolean(false);
    private AtomicInteger mBlurPolicy = new AtomicInteger(XAppGuardManager.BlurPolicy.BLUR_WATCHED);

    private String mPasscode;

    private float mBlurRadius = XBitmapUtil.BLUR_RADIUS;
    private float mBlurScale = XBitmapUtil.BITMAP_SCALE;

    private final Set<String> WATCHED_PACKAGES = new HashSet<>();
    private final Set<String> PASSED_PACKAGES = new HashSet<>();
    private final Map<String, Integer> VERIFIER_PACKAGES = new HashMap<>();

    private final Set<IWatcher> WATCHERS = new HashSet<>();

    private static final Set<String> PREBUILT_WHITE_LIST = new HashSet<>();

    @SuppressLint("UseSparseArrays")
    private final Map<Integer, Transaction> TRANSACTION_MAP = new HashMap<>();

    private final Set<String> FEATURES = new HashSet<>(FEATURE_COUNT);

    static {
        PREBUILT_WHITE_LIST.add("com.android.systemui");
        // PREBUILT_WHITE_LIST.add("com.android.packageinstaller");
        PREBUILT_WHITE_LIST.add("android");
        PREBUILT_WHITE_LIST.add("com.cyanogenmod.trebuchet");
        // It is good for user if our mod crash.
        // PREBUILT_WHITE_LIST.add("de.robv.android.xposed.installer");
        PREBUILT_WHITE_LIST.add(BuildConfig.APPLICATION_ID);
    }

    private AtomicFile mXmlFile;

    private final ExecutorService mWorkingService = Executors.newSingleThreadExecutor();

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

    private XStatus xStatus = XStatus.UNKNOWN;

    XAppGuardServiceImpl() {
    }

    void attachContext(Context context) {
        if (DEBUG_V) XLog.logD("attachContext: " + context);
        this.mContext = context;
    }

    @Override
    public Context getContext() {
        return mContext;
    }

    void publish() {
        try {
            if (DEBUG_V) XLog.logD("published: " + Binder.getCallingUid());
            ServiceManager.addService(XAppGuardManager.APP_GUARD_SERVICE, asBinder());
            publishFeature(XAppGuardManager.Feature.BASE);
        } catch (Exception e) {
            XLog.logD("*** FATAL*** Fail publish our svc:" + e);
        }
    }

    void systemReady() {
        if (DEBUG_V) XLog.logD("systemReady: " + Binder.getCallingUid());
        construct();
        getConfigFromSettings();
        loadPackages();
        registerReceiver();
        cacheUIDForPackages();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void construct() {
        mHandler = new Handler(this);
        File dataDir = Environment.getDataDirectory();
        File systemDir = new File(dataDir, "system");
        systemDir.mkdirs();
        mXmlFile = new AtomicFile(new File(systemDir, "app_guard.xml"));
        if (DEBUG_V) XLog.logD("xml file: " + mXmlFile.getBaseFile());
    }

    void publishFeature(String f) {
        if (DEBUG_V) XLog.logD("publishFeature: " + f);
        synchronized (FEATURES) {
            if (!FEATURES.contains(f)) FEATURES.add(f);
        }
    }

    private void cacheUIDForPackages() {
        PackageManager pm = this.mContext.getPackageManager();
        try {
            ApplicationInfo applicationInfo = pm.getApplicationInfo(BuildConfig.APPLICATION_ID, 0);
            sClientUID = applicationInfo.uid;
            if (DEBUG_V) XLog.logD("sClientUID:" + sClientUID);

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

    void setStatus(XStatus xStatus) {
        this.xStatus = xStatus;
        if (DEBUG_V) XLog.logD("setStatus:" + xStatus);
    }

    private void registerReceiver() {
        mContext.registerReceiver(mScreenReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        intentFilter.addDataScheme("package");
        mContext.registerReceiver(mPackageReceiver, intentFilter);
    }

    void shutdown() {
        if (DEBUG_V) XLog.logD("shutdown...");
        persistPackages();
    }

    boolean passed(String pkg) {
        return !mEnabled.get()
                && (!TextUtils.isEmpty(mPasscode))
                || PREBUILT_WHITE_LIST.contains(pkg)
                || PASSED_PACKAGES.contains(pkg)
                || VERIFIER_PACKAGES.containsKey(pkg)
                || !WATCHED_PACKAGES.contains(pkg);
    }

    @Override
    boolean interruptPackageRemoval(String pkg) {
        return
                BuildConfig.APPLICATION_ID.equals(pkg)
                        ? isEnabled()
                        : isEnabled() && WATCHED_PACKAGES.contains(pkg);
    }

    void verify(Bundle options, String pkg, int uid, int pid, VerifyListener listener) {
        VerifyArgs args = new VerifyArgs(options, pkg, uid, pid, listener);
        mHandler.obtainMessage(MSG_VERIFY, args).sendToTarget();
    }

    private void onVerify(VerifyArgs args) {
        if (DEBUG_V) XLog.logD("onVerify:" + args);
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

        Intent intent = buildVerifyIntent(m3rdVerifierAllowed.get(), tid, pkg);
        try {
            mContext.startActivity(intent, bnds);
        } catch (ActivityNotFoundException anf) {
            XLog.logD("*** FATAL ERROR *** ActivityNotFoundException!!!");
            setResult(tid, XMode.MODE_IGNORED);
        }
    }

    private void onNewTransaction(int transaction) {
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_TRANSACTION_EXPIRE_BASE
                        + transaction,
                transaction), TRANSACTION_EXPIRE_TIME);
    }

    private void getConfigFromSettings() {
        ContentResolver contentResolver = mContext.getContentResolver();

        boolean enabled = (Settings.System.getInt(contentResolver, SETTINGS_APP_GUARD_ENABLED, 0) == 1);
        mEnabled.set(enabled);

        boolean uninstallPro = (Settings.System.getInt(contentResolver, SETTINGS_APP_GUARD_UNINSTALL_PRO_ENABLED, 0) == 1);
        mUninstallProEnabled.set(enabled);

        boolean verifyOnHome = (Settings.System.getInt(contentResolver, SETTINGS_VERIFY_ON_HOME, 0) == 1);
        mVerifyOnHome.set(enabled);

        // Default is 1.
        boolean verifyOnScreenOff = (Settings.System.getInt(contentResolver, SETTINGS_VERIFY_ON_SCREEN_OFF, 1) == 1);
        mVerifyOnScreenOff.set(enabled);

        boolean blur = (Settings.System.getInt(contentResolver, SETTINGS_APP_SCREENSHOT_BLUR_ENABLED, 0) == 1);
        mBlur.set(blur);

        int blurPolicy = (Settings.System.getInt(contentResolver, SETTINGS_APP_SCREENSHOT_BLUR_POLICY,
                XAppGuardManager.BlurPolicy.BLUR_WATCHED));
        mBlurPolicy.set(blurPolicy);

        mBlurScale = (Settings.System.getFloat(contentResolver, SETTINGS_APP_SCREENSHOT_BLUR_SCALE,
                XBitmapUtil.BITMAP_SCALE));

        mBlurRadius = (Settings.System.getFloat(contentResolver, SETTINGS_APP_SCREENSHOT_BLUR_RADIUS,
                XBitmapUtil.BLUR_RADIUS));
        boolean allow3rdVer = (Settings.System.getInt(contentResolver, SETTINGS_ALLOW_3RD_VERIFIER, 0) == 1);
        m3rdVerifierAllowed.set(allow3rdVer);

        try {
            mPasscode = (Settings.System.getString(contentResolver, SETTINGS_PASSCODE));
        } catch (Exception ignored) {
        }

        if (DEBUG_V) XLog.logD("enabled:" + enabled);
        if (DEBUG_V) XLog.logD("uninstallPro:" + uninstallPro);
        if (DEBUG_V) XLog.logD("blur:" + blur);
        if (DEBUG_V) XLog.logD("blurPolicy:" + blurPolicy);
        if (DEBUG_V) XLog.logD("mBlurScale:" + mBlurScale);
        if (DEBUG_V) XLog.logD("mBlurRadius:" + mBlurRadius);
        if (DEBUG_V) XLog.logD("allow3rdVer:" + allow3rdVer);
        if (DEBUG_V) XLog.logD("mPasscode:" + mPasscode);
        if (DEBUG_V) XLog.logD("verifyOnHome:" + verifyOnHome);
        if (DEBUG_V) XLog.logD("verifyOnScreenOff:" + verifyOnScreenOff);

        // TODO. Register observer.
    }

    @Override
    public boolean isEnabled() {
        enforceCallingPermissions();
        return mEnabled.get();
    }

    @Override
    public void setEnabled(boolean enabled) throws RemoteException {
        enforceCallingPermissions();
        if (DEBUG_V) XLog.logD("setEnabled:" + enabled + ", mEnabled:" + mEnabled.get());
        mHandler.obtainMessage(MSG_SET_ENABLED, enabled ? 1 : 0, 0, null).sendToTarget();
    }


    private void onSetEnabled(boolean enabled) {
        if (DEBUG_V) XLog.logD("onSetEnabled:" + enabled);
        if (mEnabled.compareAndSet(!enabled, enabled)) {
            ContentResolver contentResolver = mContext.getContentResolver();
            Settings.System.putInt(contentResolver, SETTINGS_APP_GUARD_ENABLED, enabled ? 1 : 0);
        }
    }

    @Override
    public boolean isUninstallInterruptEnabled() throws RemoteException {
        return mUninstallProEnabled.get();
    }

    @Override
    public void setUninstallInterruptEnabled(boolean enabled) throws RemoteException {
        enforceCallingPermissions();
        mHandler.obtainMessage(MSG_SET_APP_UNINSTALL_PRO, enabled ? 1 : 0, 0).sendToTarget();
    }

    private void onSetAppUninstallPro(boolean enabled) {
        if (DEBUG_V) XLog.logD("onSetAppUninstallPro:" + enabled);
        if (mUninstallProEnabled.compareAndSet(!enabled, enabled)) {
            ContentResolver contentResolver = mContext.getContentResolver();
            Settings.System.putInt(contentResolver, SETTINGS_APP_GUARD_UNINSTALL_PRO_ENABLED, enabled ? 1 : 0);
        }
    }

    @Override
    public void setVerifyOnScreenOff(boolean ver) throws RemoteException {
        enforceCallingPermissions();
        mHandler.obtainMessage(MSG_SET_VERIFY_ON_SCREEN_OFF, ver ? 1 : 0, 0).sendToTarget();
    }

    @Override
    public boolean isVerifyOnScreenOff() {
        enforceCallingPermissions();
        return mVerifyOnScreenOff.get();
    }

    private void onSetVerifyOnScreenOff(boolean ver) {
        if (mVerifyOnScreenOff.compareAndSet(!ver, ver)) {
            ContentResolver contentResolver = mContext.getContentResolver();
            Settings.System.putInt(contentResolver, SETTINGS_VERIFY_ON_SCREEN_OFF, ver ? 1 : 0);
        }
    }

    @Override
    public void setVerifyOnHome(boolean ver) throws RemoteException {
        enforceCallingPermissions();
        mHandler.obtainMessage(MSG_SET_VERIFY_ON_HOME, ver ? 1 : 0, 0).sendToTarget();
    }

    private void onSetVerifyOnHome(boolean ver) {
        if (mVerifyOnHome.compareAndSet(!ver, ver)) {
            ContentResolver contentResolver = mContext.getContentResolver();
            Settings.System.putInt(contentResolver, SETTINGS_VERIFY_ON_HOME, ver ? 1 : 0);
        }
    }

    @Override
    public boolean isVerifyOnHome() {
        enforceCallingPermissions();
        return mVerifyOnHome.get();
    }

    @Override
    public boolean isBlur() {
        enforceCallingPermissions();
        return mBlur.get();
    }

    boolean isBlurForPkg(String pkg) {
        return isBlur() && mBlurPolicy.get() == XAppGuardManager.BlurPolicy.BLUR_ALL
                || isBlur() && pkg != null && WATCHED_PACKAGES.contains(pkg);
    }

    @Override
    public void setBlur(boolean blur) throws RemoteException {
        enforceCallingPermissions();
        mHandler.obtainMessage(MSG_SET_BLUR, blur ? 1 : 0, 0).sendToTarget();
    }

    private void onSetBlur(boolean b) {
        if (DEBUG_V) XLog.logD("onSetBlur: " + b);

        if (mBlur.compareAndSet(!b, b)) {
            ContentResolver contentResolver = mContext.getContentResolver();
            Settings.System.putInt(contentResolver, SETTINGS_APP_SCREENSHOT_BLUR_ENABLED, b ? 1 : 0);
        }
    }

    @Override
    public void setBlurPolicy(int policy) throws RemoteException {
        enforceCallingPermissions();
        Preconditions.checkArgument(XAppGuardManager.BlurPolicy.Checker.valid(policy));
        mHandler.obtainMessage(MSG_SET_BLUR_POLICY, policy, policy).sendToTarget();
    }

    private void onSetBlurPolicy(int policy) {
        if (DEBUG_V) XLog.logD("onSetBlurPolicy: " + policy);
        mBlurPolicy.set(policy);
        ContentResolver contentResolver = mContext.getContentResolver();
        Settings.System.putInt(contentResolver, SETTINGS_APP_SCREENSHOT_BLUR_POLICY, policy);
    }


    @Override
    public int getBlurPolicy() throws RemoteException {
        enforceCallingPermissions();
        return mBlurPolicy.get();
    }

    @Override
    public void setBlurRadius(int radius) throws RemoteException {
        enforceCallingPermissions();
        mHandler.obtainMessage(MSG_SET_BLUR_RADIUS, radius, radius).sendToTarget();
    }

    private void onSetBlurRadius(int radius) {
        if (DEBUG_V) XLog.logD("onSetBlurRadius: " + radius);
        mBlurRadius = radius;
        ContentResolver contentResolver = mContext.getContentResolver();
        Settings.System.putFloat(contentResolver, SETTINGS_APP_SCREENSHOT_BLUR_RADIUS, radius);
    }

    @Override
    public int getBlurRadius() throws RemoteException {
        enforceCallingPermissions();
        return (int) mBlurRadius;
    }

    @Override
    public void setBlurScale(float scale) throws RemoteException {
        enforceCallingPermissions();
        mHandler.obtainMessage(MSG_SET_BLUR_SCALE, scale).sendToTarget();
    }

    private void onSetBlurScale(float scale) {
        if (DEBUG_V) XLog.logD("onSetBlurScale: " + scale);
        mBlurScale = scale;
        ContentResolver contentResolver = mContext.getContentResolver();
        Settings.System.putFloat(contentResolver, SETTINGS_APP_SCREENSHOT_BLUR_SCALE, scale);
    }

    @Override
    public float getBlurScale() throws RemoteException {
        enforceCallingPermissions();
        return mBlurScale;
    }

    @Override
    public void setAllow3rdVerifier(boolean allow) throws RemoteException {
        enforceCallingPermissions();
        mHandler.obtainMessage(MSG_SET_SET_ALLOW_3RD_VER, allow ? 1 : 0, 0).sendToTarget();
    }

    private void onSetAllow3rdVerifier(boolean allow) {
        if (DEBUG_V) XLog.logD("onSetAllow3rdVerifier: " + allow);
        if (m3rdVerifierAllowed.compareAndSet(!allow, allow)) {
            ContentResolver contentResolver = mContext.getContentResolver();
            Settings.System.putInt(contentResolver, SETTINGS_ALLOW_3RD_VERIFIER, allow ? 1 : 0);
        }
    }

    @Override
    public boolean isAllow3rdVerifier() throws RemoteException {
        enforceCallingPermissions();
        return m3rdVerifierAllowed.get();
    }

    @Override
    public void setPasscode(String passcode) throws RemoteException {
        enforceCallingPermissions();
        Preconditions.checkArgument(XEnc.isPassCodeValid(passcode));
        mHandler.obtainMessage(MSG_SET_PASSCODE, passcode).sendToTarget();
    }

    private void onSetPasscode(String passcode) {
        if (DEBUG_V) XLog.logD("onSetPasscode: " + passcode);
        mPasscode = passcode;
        ContentResolver contentResolver = mContext.getContentResolver();
        Settings.System.putString(contentResolver, SETTINGS_PASSCODE, passcode);
    }

    @Override
    public String getPasscode() throws RemoteException {
        enforceCallingPermissions();
        return mPasscode;
    }

    @Override
    public boolean hasFeature(String feature) throws RemoteException {
        enforceCallingPermissions();
        Preconditions.checkNotNull(feature);
        return FEATURES.contains(feature);
    }

    @Override
    public void ignore(String pkg) throws RemoteException {
        enforceCallingPermissions();
        Preconditions.checkNotNull(pkg);
        mHandler.obtainMessage(MSG_IGNORE, pkg).sendToTarget();
    }

    @Override
    public void pass(String pkg) throws RemoteException {
        enforceCallingPermissions();
        Preconditions.checkNotNull(pkg);
        mHandler.obtainMessage(MSG_PASS, pkg).sendToTarget();
    }

    @Override
    public int getStatus() throws RemoteException {
        enforceCallingPermissions();
        return xStatus.ordinal();
    }

    @Override
    public String[] getPackages() throws RemoteException {
        enforceCallingPermissions();
        Object[] all = WATCHED_PACKAGES.toArray();
        String[] pkgs = new String[all.length];
        for (int i = 0; i < all.length; i++) {
            pkgs[i] = String.valueOf(all[i]);
        }
        return pkgs;
    }

    @Override
    public void setResult(int transactionID, final int res) {
        enforceCallingPermissions();
        if (DEBUG_V) XLog.logD("setResult:" + transactionID + ", res:" + res);
        mHandler.obtainMessage(MSG_VERIFY_RES, res, transactionID, null).sendToTarget();
    }

    private void onSetResult(int res, int transactionID) {
        synchronized (TRANSACTION_MAP) {
            Transaction transaction = TRANSACTION_MAP.remove(transactionID);
            if (transaction == null) {
                XLog.logD("Can not find transaction for:" + transactionID);
                if (DEBUG_V)
                    XLog.logD("We have transactions count of:" + TRANSACTION_MAP.values().size());

                return;
            }
            if (res == XMode.MODE_ALLOWED) {
                PASSED_PACKAGES.add(transaction.pkg);
            }
            transaction.listener.onVerifyRes(transaction.pkg, transaction.uid, transaction.pid, res);
            mHandler.removeMessages(MSG_TRANSACTION_EXPIRE_BASE + transactionID);
        }
    }

    @Override
    public void testUI() throws RemoteException {
        enforceCallingPermissions();
        long id = Binder.clearCallingIdentity();
        Intent intent = buildVerifyIntent(m3rdVerifierAllowed.get(),
                TransactionFactory.transactionID(), "xxxxx");
        mContext.startActivity(intent);
        Binder.restoreCallingIdentity(id);
    }

    @Override
    public void addPackages(String[] pkgs) throws RemoteException {
        enforceCallingPermissions();
        Preconditions.checkNotNull(pkgs);
        mHandler.obtainMessage(MSG_ADD_PACKAGES, pkgs).sendToTarget();
    }

    private void onAddPackages(String[] pkgs) {
        Collections.consumeRemaining(pkgs, new Consumer<String>() {
            @Override
            public void accept(String s) {
                if (!TextUtils.isEmpty(s) && !WATCHED_PACKAGES.contains(s)) {
                    WATCHED_PACKAGES.add(s);
                    if (DEBUG_V) XLog.logD("Add package:" + s);
                }
            }
        });
    }

    @Override
    public void removePackages(String[] pkgs) throws RemoteException {
        enforceCallingPermissions();
        Preconditions.checkNotNull(pkgs);
        mHandler.obtainMessage(MSG_REMOVE_PACKAGES, pkgs).sendToTarget();
    }

    private void onRemovePackages(String[] pkgs) {
        Collections.consumeRemaining(pkgs, new Consumer<String>() {
            @Override
            public void accept(String s) {
                if (!TextUtils.isEmpty(s) && WATCHED_PACKAGES.contains(s)) {
                    WATCHED_PACKAGES.remove(s);
                    if (DEBUG_V) XLog.logD("Remove package:" + s);
                }
            }
        });
    }

    @Override
    public void watch(IWatcher w) throws RemoteException {
        if (DEBUG_V) XLog.logD("iWatcher.watch-" + w);
        enforceCallingPermissions();
        Preconditions.checkNotNull(w);
        synchronized (WATCHERS) { //FIXME Link to death~~~
            if (!WATCHERS.contains(w)) {
                WATCHERS.add(w);
                if (DEBUG_V) XLog.logD("iWatcher.watch-OK " + w);
            }
        }
    }

    @Override
    public void unWatch(IWatcher w) throws RemoteException {
        if (DEBUG_V) XLog.logD("iWatcher.unWatch-" + w);
        enforceCallingPermissions();
        Preconditions.checkNotNull(w);
        synchronized (WATCHERS) { //FIXME Link to death~~~
            if (WATCHERS.contains(w)) {
                WATCHERS.remove(w);
                if (DEBUG_V) XLog.logD("iWatcher.unWatch-OK " + w);
            }
        }
    }

    private void notifyWatcherUserLeaving(final String reason) {
        synchronized (WATCHERS) {
            Collections.consumeRemaining(WATCHERS, new Consumer<IWatcher>() {
                @Override
                public void accept(final IWatcher iWatcher) {
                    mWorkingService.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                iWatcher.onUserLeaving(reason);
                                if (DEBUG_V) XLog.logD("iWatcher.onUserLeaving-" + reason);
                            } catch (Throwable ignored) {
                            }
                        }
                    });
                }
            });

            // FIXME. FFFFFF?
            WATCHERS.clear();
        }
    }

    @Override
    public void forceWriteState() throws RemoteException {
        enforceCallingPermissions();
        mHandler.obtainMessage(MSG_WRITE_STATE).sendToTarget();
    }

    private void onWriteState() {
        mWorkingService.execute(new Runnable() {
            @Override
            public void run() {
                persistPackages();
            }
        });
    }

    @Override
    public void forceReadState() throws RemoteException {
        enforceCallingPermissions();
        mHandler.obtainMessage(MSG_READ_STATE).sendToTarget();
    }

    @Override
    public void mockCrash() throws RemoteException {
        enforceCallingPermissions();
        mHandler.obtainMessage(MSG_FUCK_YR_SELF).sendToTarget();
    }

    protected void onMockCrash() {
        throw new IllegalStateException("Let's CRASH, bye bye you...");
    }

    private void onReadState() {
        mWorkingService.execute(new Runnable() {
            @Override
            public void run() {
                loadPackages();
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void loadPackages() {
        try {
            FileReader fileReader = new FileReader(mXmlFile.getBaseFile());
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            fileReader.close();
            bufferedReader.close();
            String content = stringBuilder.toString();
            if (DEBUG_V) XLog.logD("reader:" + content);
            StringTokenizer stringTokenizer = new StringTokenizer(content, "|");
            WATCHED_PACKAGES.clear();
            while (stringTokenizer.hasMoreTokens()) {
                String p = stringTokenizer.nextToken();
                WATCHED_PACKAGES.add(p);
                if (DEBUG_V) XLog.logD("Read:" + p);
            }
        } catch (Exception e) {
            XLog.logD("Fail loadPackages:" + Log.getStackTraceString(e));
            // Delete bad file.
            mXmlFile.delete();
        }
    }

    private void persistPackages() {
        FileOutputStream os = null;
        try {
            mXmlFile.delete();
            os = mXmlFile.startWrite();
            PrintWriter printWriter = new PrintWriter(os, false);
            printWriter.write(formatPackages());
            printWriter.flush();
        } catch (Exception e) {
            XLog.logD("Fail persistPackages:" + Log.getStackTraceString(e));
        } finally {
            if (os != null) {
                mXmlFile.finishWrite(os);
            }
        }
    }

    private String formatPackages() {
        StringBuilder stringBuilder = new StringBuilder();
        Object[] arr = WATCHED_PACKAGES.toArray();
        for (int i = 0; i < arr.length; i++) {
            stringBuilder.append(String.valueOf(arr[i]));
            if (i != arr.length - 1) stringBuilder.append("|");
        }
        return stringBuilder.toString();
    }

    private static Intent buildVerifyIntent(boolean allow3rd, int transId, String pkg) {
        Intent intent = new Intent(ACTION_APP_GUARD_VERIFY_DISPLAYER);
        if (!allow3rd)
            intent.setClassName(BuildConfig.APPLICATION_ID,
                    "github.tornaco.xposedmoduletest.ui.VerifyDisplayerActivity");
        intent.putExtra(XKey.EXTRA_PKG_NAME, pkg);
        intent.putExtra(XKey.EXTRA_TRANS_ID, transId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return intent;
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (DEBUG_V) XLog.logD("handleMessage:" + decodeMsg(msg.what));
        switch (msg.what) {
            case MSG_VERIFY_RES:
                onSetResult(msg.arg1, msg.arg2);
                return true;
            case MSG_SET_ENABLED:
                onSetEnabled(msg.arg1 == 1);
                return true;
            case MSG_VERIFY:
                onVerify((VerifyArgs) msg.obj);
                return true;
            case MSG_ADD_PACKAGES:
                onAddPackages((String[]) msg.obj);
                return true;
            case MSG_REMOVE_PACKAGES:
                onRemovePackages((String[]) msg.obj);
                return true;
            case MSG_READ_STATE:
                onReadState();
                return true;
            case MSG_WRITE_STATE:
                onWriteState();
                return true;
            case MSG_SET_BLUR:
                onSetBlur(msg.arg1 == 1);
                return true;
            case MSG_SET_BLUR_POLICY:
                onSetBlurPolicy(msg.arg1);
                return true;
            case MSG_SET_BLUR_RADIUS:
                onSetBlurRadius(msg.arg1);
                return true;
            case MSG_SET_BLUR_SCALE:
                onSetBlurScale((Float) msg.obj);
                return true;
            case MSG_SET_SET_ALLOW_3RD_VER:
                onSetAllow3rdVerifier(msg.arg1 == 1);
                return true;
            case MSG_SET_PASSCODE:
                onSetPasscode((String) msg.obj);
                return true;
            case MSG_PASS:
            case MSG_IGNORE:
                return false;
            case MSG_ON_HOME:
                onHomeInternal();
                return true;
            case MSG_SET_VERIFY_ON_HOME:
                onSetVerifyOnHome(msg.arg1 == 1);
                return true;
            case MSG_SET_VERIFY_ON_SCREEN_OFF:
                onSetVerifyOnScreenOff(msg.arg1 == 1);
                return true;
            case MSG_FUCK_YR_SELF:
                onMockCrash();
                return true;
            case MSG_SET_APP_UNINSTALL_PRO:
                onSetAppUninstallPro(msg.arg1 == 1);
                return true;
            default:
                int transaction = (int) msg.obj;
                onSetResult(XMode.MODE_IGNORED, transaction);
                return true;
        }
    }

    private String decodeMsg(int what) {
        switch (what) {
            case MSG_ADD_PACKAGES:
                return "MSG_ADD_PACKAGES";
            case MSG_READ_STATE:
                return "MSG_READ_STATE";
            case MSG_REMOVE_PACKAGES:
                return "MSG_REMOVE_PACKAGES";
            case MSG_SET_ENABLED:
                return "MSG_SET_ENABLED";
            case MSG_VERIFY:
                return "MSG_VERIFY";
            case MSG_VERIFY_RES:
                return "MSG_VERIFY_RES";
            case MSG_WRITE_STATE:
                return "MSG_WRITE_STATE";
            case MSG_PASS:
                return "MSG_PASS";
            case MSG_IGNORE:
                return "MSG_IGNORE";
            case MSG_SET_BLUR:
                return "MSG_SET_BLUR";
            case MSG_SET_BLUR_POLICY:
                return "MSG_SET_BLUR_POLICY";
            case MSG_SET_BLUR_RADIUS:
                return "MSG_SET_BLUR_RADIUS";
            case MSG_SET_BLUR_SCALE:
                return "MSG_SET_BLUR_SCALE";
            case MSG_SET_SET_ALLOW_3RD_VER:
                return "MSG_SET_SET_ALLOW_3RD_VER";
            case MSG_SET_PASSCODE:
                return "MSG_SET_PASSCODE";
            case MSG_ON_HOME:
                return "MSG_ON_HOME";
            case MSG_SET_VERIFY_ON_HOME:
                return "MSG_SET_VERIFY_ON_HOME";
            case MSG_SET_VERIFY_ON_SCREEN_OFF:
                return "MSG_SET_VERIFY_ON_SCREEN_OFF";
            case MSG_FUCK_YR_SELF:
                return "MSG_FUCK_YR_SELF";
            case MSG_SET_APP_UNINSTALL_PRO:
                return "MSG_SET_APP_UNINSTALL_PRO";
            default:
                return "MSG_TRANSACTION_EXPIRE";
        }
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
        final PackageManager pm = mContext.getPackageManager();

        Collections.consumeRemaining(pkg, new Consumer<String>() {
            @Override
            public void accept(String s) {
                ApplicationInfo applicationInfo;
                try {
                    applicationInfo = pm.getApplicationInfo(s, PackageManager.GET_META_DATA);
                    if (s.equals("github.tornaco.dialogstyledveifier")) {
                        int uid = applicationInfo.uid;
                        int code = applicationInfo.versionCode;
                        XLog.logD("Verifier pkg:" + s + ", uid:" + uid);
                        VERIFIER_PACKAGES.put(s, uid);
                        return;
                    }
                    if (applicationInfo.metaData == null) return;
                    String displayerName = applicationInfo.metaData.getString(META_DATA_KEY_APP_GUARD_VERIFY_DISPLAYER);
                    if (TextUtils.isEmpty(displayerName)) return;
                    int uid = applicationInfo.uid;
                    XLog.logD("Verifier pkg:" + displayerName + ", uid:" + uid);
                    VERIFIER_PACKAGES.put(s, uid);
                } catch (Exception ignored) {

                }
            }
        });
    }


    protected void enforceCallingPermissions() {
        int callingUID = Binder.getCallingUid();
        if (VERIFIER_PACKAGES.containsValue(callingUID)) return;
        if (callingUID == Process.myUid() || (sClientUID > 0 && sClientUID == callingUID)) {
            return;
        }
        throw new SecurityException("Package of uid:" + callingUID
                + ", does not have permission to interact with XAppGuardServiceImpl");
    }

    void onUserLeaving() {
        super.onUserLeaving();
        // Skip when early startup.
        if (mHandler != null) mHandler.obtainMessage(MSG_ON_HOME).sendToTarget();
    }

    private void onHomeInternal() {
        if (isVerifyOnHome()) {
            XLog.logV("HOME, Clearing passed pkgs...");
            PASSED_PACKAGES.clear();
        }
        notifyWatcherUserLeaving("Home");
    }

    private void onScreenOff() {
        if (isVerifyOnScreenOff()) {
            XLog.logV("SCREEN OFF, Clearing passed pkgs...");
            PASSED_PACKAGES.clear();
        }
    }

    private static class TransactionFactory {

        private static final AtomicInteger TRANS_ID_BASE = new AtomicInteger(2017);

        static int transactionID() {
            return TRANS_ID_BASE.getAndIncrement();
        }
    }

    interface VerifyListener {
        void onVerifyRes(String pkg, int uid, int pid, int res);
    }

    private class Transaction {
        VerifyListener listener;
        int uid, pid, tid;
        String pkg;

        Transaction(VerifyListener listener, int uid, int pid, int tid, String pkg) {
            this.listener = listener;
            this.uid = uid;
            this.pid = pid;
            this.tid = tid;
            this.pkg = pkg;
        }

        @Override
        public String toString() {
            return "Transaction{" +
                    "uid=" + uid +
                    ", pid=" + pid +
                    ", tid=" + tid +
                    ", pkg='" + pkg + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Transaction that = (Transaction) o;

            return tid == that.tid && pkg.equals(that.pkg);
        }

        @Override
        public int hashCode() {
            int result = tid;
            result = 31 * result + pkg.hashCode();
            return result;
        }
    }

    private class VerifyArgs {
        Bundle bnds;
        String pkg;
        int uid;
        int pid;
        VerifyListener listener;

        VerifyArgs(Bundle bnds, String pkg, int uid, int pid, VerifyListener listener) {
            this.bnds = bnds;
            this.pkg = pkg;
            this.uid = uid;
            this.pid = pid;
            this.listener = listener;
        }
    }
}
