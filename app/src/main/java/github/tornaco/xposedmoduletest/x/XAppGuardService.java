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
import android.util.Slog;

import com.android.internal.os.AtomicFile;
import com.android.internal.util.Preconditions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import github.tornaco.android.common.Collections;
import github.tornaco.android.common.Consumer;
import github.tornaco.apigen.GithubCommitSha;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.IAppGuardService;
import github.tornaco.xposedmoduletest.IWatcher;

import static github.tornaco.xposedmoduletest.x.XAppGuardManager.ACTION_APP_GUARD_VERIFY_DISPLAYER;
import static github.tornaco.xposedmoduletest.x.XAppGuardManager.Feature.FEATURE_COUNT;

/**
 * Created by guohao4 on 2017/10/23.
 * Email: Tornaco@163.com
 */
@GithubCommitSha
class XAppGuardService extends IAppGuardService.Stub implements Handler.Callback {

    private static final String SETTINGS_APP_GUARD_ENABLED = "settings_app_guard_enabled";
    private static final String SETTINGS_APP_SCREENSHOT_BLUR_ENABLED = "settings_app_screenshot_blur_enabled";
    private static final String SETTINGS_APP_SCREENSHOT_BLUR_SCALE = "settings_app_screenshot_blur_sc";
    private static final String SETTINGS_APP_SCREENSHOT_BLUR_RADIUS = "settings_app_screenshot_blur_ra";
    private static final String SETTINGS_APP_SCREENSHOT_BLUR_POLICY = "settings_app_screenshot_blur_po";

    private static int sClientUID = 0;

    private static final long TRANSACTION_EXPIRE_TIME = 60 * 1000;

    private static final boolean DEBUG_V = true;

    private static final String TAG = "XAppGuardService";

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
    private static final int MSG_TRANSACTION_EXPIRE_BASE = 0x99;

    private Context mContext;
    private Handler mHandler;

    private AtomicBoolean mEnabled = new AtomicBoolean(false);
    private AtomicBoolean mBlur = new AtomicBoolean(false);
    private AtomicInteger mBlurPolicy = new AtomicInteger(XAppGuardManager.BlurPolicy.BLUR_WATCHED);

    private float mBlurRadius = XBitmapUtil.BLUR_RADIUS;
    private float mBlurScale = XBitmapUtil.BITMAP_SCALE;

    private final Set<String> WATCHED_PACKAGES = new HashSet<>();

    private final Set<String> PASSED_PACKAGES = new HashSet<>();

    private static final Set<String> PREBUILT_WHITE_LIST = new HashSet<>();

    @SuppressLint("UseSparseArrays")
    private final Map<Integer, Transaction> TRANSACTION_MAP = new HashMap<>();

    private final Set<String> FEATURES = new HashSet<>(FEATURE_COUNT);

    static {
        PREBUILT_WHITE_LIST.add("com.android.systemui");
        PREBUILT_WHITE_LIST.add("com.android.packageinstaller");
        PREBUILT_WHITE_LIST.add("android");
        PREBUILT_WHITE_LIST.add("com.cyanogenmod.trebuchet");
        // It is good for user if our mod crash.
        PREBUILT_WHITE_LIST.add("de.robv.android.xposed.installer");
        PREBUILT_WHITE_LIST.add(BuildConfig.APPLICATION_ID);
    }

    private AtomicFile mXmlFile;

    private final ExecutorService mWorkingService = Executors.newSingleThreadExecutor();

    private BroadcastReceiver mScreenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            PASSED_PACKAGES.clear();
        }
    };

    private XStatus xStatus = XStatus.UNKNOWN;

    XAppGuardService() {
    }

    void attachContext(Context context) {
        if (DEBUG_V) Slog.d(TAG, "attachContext: " + context);
        this.mContext = context;
    }

    void publish() {
        try {
            if (DEBUG_V) Slog.d(TAG, "published: " + Binder.getCallingUid());
            ServiceManager.addService(XAppGuardManager.APP_GUARD_SERVICE, asBinder());
            publishFeature(XAppGuardManager.Feature.BASE);
        } catch (Exception e) {
            Slog.e(TAG, "*** FATAL*** Fail publish our svc:" + e);
        }
    }

    void systemReady() {
        if (DEBUG_V) Slog.d(TAG, "systemReady: " + Binder.getCallingUid());
        construct();
        readSettings();
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
        if (DEBUG_V) Slog.d(TAG, "xml file: " + mXmlFile.getBaseFile());
    }

    void publishFeature(String f) {
        if (DEBUG_V) Slog.d(TAG, "publishFeature: " + f);
        synchronized (FEATURES) {
            if (!FEATURES.contains(f)) FEATURES.add(f);
        }
    }

    private void cacheUIDForPackages() {
        PackageManager pm = this.mContext.getPackageManager();
        try {
            ApplicationInfo applicationInfo = pm.getApplicationInfo(BuildConfig.APPLICATION_ID, 0);
            sClientUID = applicationInfo.uid;
            if (DEBUG_V) Slog.d(TAG, "sClientUID:" + sClientUID);
        } catch (PackageManager.NameNotFoundException ignored) {
            Slog.e(TAG, "Can not get UID for our client:" + ignored);
        }
    }

    void setStatus(XStatus xStatus) {
        this.xStatus = xStatus;
        if (DEBUG_V) Slog.d(TAG, "setStatus:" + xStatus);
    }

    private void registerReceiver() {
        mContext.registerReceiver(mScreenReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
    }

    void shutdown() {
        if (DEBUG_V) Slog.d(TAG, "shutdown...");
        persistPackages();
    }

    boolean passed(String pkg) {
        return !mEnabled.get()
                || PREBUILT_WHITE_LIST.contains(pkg)
                || PASSED_PACKAGES.contains(pkg)
                || !WATCHED_PACKAGES.contains(pkg);
    }

    void verify(Bundle bnds, String pkg, int uid, int pid, VerifyListener listener) {
        VerifyArgs args = new VerifyArgs(bnds, pkg, uid, pid, listener);
        mHandler.obtainMessage(MSG_VERIFY, args).sendToTarget();
    }

    private void onVerify(VerifyArgs args) {
        if (DEBUG_V) Slog.d(TAG, "onVerify:" + args);
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

        Intent intent = buildVerifyIntent(tid, pkg);
        try {
            mContext.startActivity(intent, bnds);
        } catch (ActivityNotFoundException anf) {
            Slog.e(TAG, "*** FATAL ERROR *** ActivityNotFoundException!!!");
            setResult(tid, XMode.MODE_IGNORED);
        }
    }

    private void onNewTransaction(int transaction) {
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_TRANSACTION_EXPIRE_BASE
                        + transaction,
                transaction), TRANSACTION_EXPIRE_TIME);
    }

    private void readSettings() {
        ContentResolver contentResolver = mContext.getContentResolver();
        boolean enabled = (Settings.System.getInt(contentResolver, SETTINGS_APP_GUARD_ENABLED, 0) == 1);
        mEnabled.set(enabled);
        boolean blur = (Settings.System.getInt(contentResolver, SETTINGS_APP_SCREENSHOT_BLUR_ENABLED, 0) == 1);
        mBlur.set(blur);
        int blurPolicy = (Settings.System.getInt(contentResolver, SETTINGS_APP_SCREENSHOT_BLUR_POLICY,
                XAppGuardManager.BlurPolicy.BLUR_WATCHED));
        mBlurPolicy.set(blurPolicy);
        mBlurScale = (Settings.System.getFloat(contentResolver, SETTINGS_APP_SCREENSHOT_BLUR_SCALE,
                XBitmapUtil.BITMAP_SCALE));
        mBlurRadius = (Settings.System.getFloat(contentResolver, SETTINGS_APP_SCREENSHOT_BLUR_RADIUS,
                XBitmapUtil.BLUR_RADIUS));
        if (DEBUG_V) Slog.d(TAG, "enabled:" + enabled);
        if (DEBUG_V) Slog.d(TAG, "blur:" + blur);
        if (DEBUG_V) Slog.d(TAG, "blurPolicy:" + blurPolicy);
        if (DEBUG_V) Slog.d(TAG, "mBlurScale:" + mBlurScale);
        if (DEBUG_V) Slog.d(TAG, "mBlurRadius:" + mBlurRadius);
    }

    @Override
    public boolean isEnabled() throws RemoteException {
        enforceCallingPermissions();
        return mEnabled.get();
    }

    @Override
    public void setEnabled(boolean enabled) throws RemoteException {
        enforceCallingPermissions();
        if (DEBUG_V) Slog.d(TAG, "setEnabled:" + enabled + ", mEnabled:" + mEnabled.get());
        mHandler.obtainMessage(MSG_SET_ENABLED, enabled ? 1 : 0, 0, null).sendToTarget();
    }

    private void onSetEnabled(boolean enabled) {
        if (DEBUG_V) Slog.d(TAG, "onSetEnabled:" + enabled);
        if (mEnabled.compareAndSet(!enabled, enabled)) {
            ContentResolver contentResolver = mContext.getContentResolver();
            Settings.System.putInt(contentResolver, SETTINGS_APP_GUARD_ENABLED, enabled ? 1 : 0);
        }
    }

    @Override
    public boolean isBlur() {
        enforceCallingPermissions();
        return mBlur.get();
    }

    boolean isBlurForPkg(String pkg) {
        return isBlur() && pkg != null && WATCHED_PACKAGES.contains(pkg);
    }

    @Override
    public void setBlur(boolean blur) throws RemoteException {
        enforceCallingPermissions();
        mHandler.obtainMessage(MSG_SET_BLUR, blur ? 1 : 0, 0).sendToTarget();
    }

    private void onSetBlur(boolean b) {
        if (DEBUG_V) Slog.d(TAG, "onSetBlur: " + b);

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
        if (DEBUG_V) Slog.d(TAG, "onSetBlurPolicy: " + policy);
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
        Preconditions.checkArgumentInRange(radius, 1, 25, "radius");
        mHandler.obtainMessage(MSG_SET_BLUR_RADIUS, radius, radius).sendToTarget();
    }

    private void onSetBlurRadius(int radius) {
        if (DEBUG_V) Slog.d(TAG, "onSetBlurRadius: " + radius);
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
        Preconditions.checkArgumentInRange(scale, 0f, 1f, "scale");
        mHandler.obtainMessage(MSG_SET_BLUR_SCALE, scale).sendToTarget();
    }

    private void onSetBlurScale(float scale) {
        if (DEBUG_V) Slog.d(TAG, "onSetBlurScale: " + scale);
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
        if (DEBUG_V) Slog.d(TAG, "setResult:" + transactionID + ", res:" + res);
        mHandler.obtainMessage(MSG_VERIFY_RES, res, transactionID, null).sendToTarget();
    }

    private void onSetResult(int res, int transactionID) {
        synchronized (TRANSACTION_MAP) {
            Transaction transaction = TRANSACTION_MAP.remove(transactionID);
            if (transaction == null) {
                Slog.e(TAG, "Can not find transaction for:" + transactionID);
                if (DEBUG_V)
                    Slog.e(TAG, "We have transactions count of:" + TRANSACTION_MAP.values().size());

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
        Intent intent = buildVerifyIntent(TransactionFactory.transactionID(), "xxxxx");
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
                    if (DEBUG_V) Slog.d(TAG, "Add package:" + s);
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
                    if (DEBUG_V) Slog.d(TAG, "Remove package:" + s);
                }
            }
        });
    }

    @Override
    public void watch(IWatcher w) throws RemoteException {
        enforceCallingPermissions();
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
            if (DEBUG_V) Slog.d(TAG, "reader:" + content);
            StringTokenizer stringTokenizer = new StringTokenizer(content, "|");
            WATCHED_PACKAGES.clear();
            while (stringTokenizer.hasMoreTokens()) {
                String p = stringTokenizer.nextToken();
                WATCHED_PACKAGES.add(p);
                if (DEBUG_V) Slog.d(TAG, "Read:" + p);
            }
        } catch (Exception e) {
            Slog.e(TAG, "Fail loadPackages:" + Log.getStackTraceString(e));
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
            Slog.e(TAG, "Fail persistPackages:" + Log.getStackTraceString(e));
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

    private static Intent buildVerifyIntent(int transId, String pkg) {
        Intent intent = new Intent(ACTION_APP_GUARD_VERIFY_DISPLAYER);
        // intent.setClassName(BuildConfig.APPLICATION_ID, "github.tornaco.xposedmoduletest.ui.VerifyDisplayerActivity");
        intent.putExtra(XKey.EXTRA_PKG_NAME, pkg);
        intent.putExtra(XKey.EXTRA_TRANS_ID, transId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return intent;
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (DEBUG_V) Slog.d(TAG, "handleMessage:" + decodeMsg(msg.what));
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
            case MSG_PASS:
            case MSG_IGNORE:
                return false;
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
            default:
                return "MSG_TRANSACTION_EXPIRE";
        }
    }

    private static void enforceCallingPermissions() {
        int callingUID = Binder.getCallingUid();
        if (callingUID == Process.myUid() || (sClientUID > 0 && sClientUID == callingUID)) {
            return;
        }
        throw new SecurityException("Package of uid:" + callingUID
                + ", does not have permission to interact with XAppGuardService");
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
