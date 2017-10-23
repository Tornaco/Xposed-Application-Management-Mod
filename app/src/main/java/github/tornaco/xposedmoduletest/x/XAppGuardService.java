package github.tornaco.xposedmoduletest.x;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;

import com.android.internal.os.AtomicFile;

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

/**
 * Created by guohao4 on 2017/10/23.
 * Email: Tornaco@163.com
 */
@GithubCommitSha
class XAppGuardService extends IAppGuardService.Stub implements Handler.Callback {

    private static final boolean DEBUG_V = true;

    private static final String TAG = "XAppGuardService";

    private static final int MSG_VERIFY_RES = 0x1;
    private static final int MSG_SET_ENABLED = 0x2;
    private static final int MSG_VERIFY = 0x3;
    private static final int MSG_READ_STATE = 0x4;
    private static final int MSG_WRITE_STATE = 0x5;
    private static final int MSG_ADD_PACKAGES = 0x6;
    private static final int MSG_REMOVE_PACKAGES = 0x7;

    private Context mContext;
    private Handler mHandler;

    private AtomicBoolean mEnabled = new AtomicBoolean(false);

    private final Set<String> WATCHED_PACKAGES = new HashSet<>();

    private final Set<String> PASSED_PACKAGES = new HashSet<>();

    private static final Set<String> PREBUILT_WHITE_LIST = new HashSet<>();

    @SuppressLint("UseSparseArrays")
    private final Map<Integer, Transaction> TRANSACTION_MAP = new HashMap<>();

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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    XAppGuardService(Context context) {
        this.mContext = context;
        File dataDir = Environment.getDataDirectory();
        File systemDir = new File(dataDir, "system");
        systemDir.mkdirs();
        mXmlFile = new AtomicFile(new File(systemDir, "app_guard.xml"));
        if (DEBUG_V) Slog.d(TAG, "xml file: " + mXmlFile.getBaseFile());
    }

    void publish() {
        ServiceManager.addService(XContext.APP_GUARD_SERVICE, asBinder());
        if (DEBUG_V) Slog.d(TAG, "published: " + Binder.getCallingUid());
    }

    void systemReady() {
        if (DEBUG_V) Slog.d(TAG, "systemReady: " + Binder.getCallingUid());
        mHandler = new Handler(this);
        readSettings();
        loadPackages();
        registerReceiver();
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

        Intent intent = buildVerifyIntent(tid, pkg);
        try {
            mContext.startActivity(intent, bnds);
        } catch (ActivityNotFoundException anf) {
            Slog.e(TAG, "*** FATAL ERROR *** ActivityNotFoundException!!!");
        }
    }

    private void readSettings() {
        ContentResolver contentResolver = mContext.getContentResolver();
        try {
            boolean enabled = (Settings.System.getInt(contentResolver, XContext.SETTINGS_APP_GUARD_ENABLED) == 1);
            mEnabled.set(enabled);
            if (DEBUG_V) Slog.d(TAG, "enabled:" + enabled);
        } catch (Settings.SettingNotFoundException e) {
            if (DEBUG_V) Slog.w(TAG, "SettingNotFoundException:" + e);
        }
    }

    @Override
    public boolean isEnabled() throws RemoteException {
        return mEnabled.get();
    }

    @Override
    public void setEnabled(boolean enabled) throws RemoteException {
        if (DEBUG_V) Slog.d(TAG, "setEnabled:" + enabled + ", mEnabled:" + mEnabled.get());
        mHandler.obtainMessage(MSG_SET_ENABLED, enabled ? 1 : 0, 0, null).sendToTarget();
    }

    @Override
    public String[] getPackages() throws RemoteException {
        Object[] all = WATCHED_PACKAGES.toArray();
        String[] pkgs = new String[all.length];
        for (int i = 0; i < all.length; i++) {
            pkgs[i] = String.valueOf(all[i]);
        }
        return pkgs;
    }

    private void onSetEnabled(boolean enabled) {
        if (DEBUG_V) Slog.d(TAG, "onSetEnabled:" + enabled);
        if (mEnabled.compareAndSet(!enabled, enabled)) {
            ContentResolver contentResolver = mContext.getContentResolver();
            Settings.System.putInt(contentResolver, XContext.SETTINGS_APP_GUARD_ENABLED, enabled ? 1 : 0);
        }
    }

    @Override
    public void setResult(int transactionID, final int res) throws RemoteException {
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
        }
    }

    @Override
    public void testUI() throws RemoteException {
        long id = Binder.clearCallingIdentity();
        Intent intent = buildVerifyIntent(TransactionFactory.transactionID(), "xxxxx");
        mContext.startActivity(intent);
        Binder.restoreCallingIdentity(id);
    }

    @Override
    public void addPackages(String[] pkgs) throws RemoteException {
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

    }

    @Override
    public void forceWriteState() throws RemoteException {
        mHandler.obtainMessage(MSG_WRITE_STATE).sendToTarget();
    }

    private void onReadState() {
        mWorkingService.execute(new Runnable() {
            @Override
            public void run() {
                loadPackages();
            }
        });
    }

    @Override
    public void forceReadState() throws RemoteException {
        mHandler.obtainMessage(MSG_READ_STATE).sendToTarget();
    }

    private void onWriteState() {
        mWorkingService.execute(new Runnable() {
            @Override
            public void run() {
                persistPackages();
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
        Intent intent = new Intent();
        intent.setClassName(BuildConfig.APPLICATION_ID, "github.tornaco.xposedmoduletest.ui.VerifyDisplayerActivity");
        intent.putExtra(XKey.EXTRA_PKG_NAME, pkg);
        intent.putExtra(XKey.EXTRA_TRANS_ID, transId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
        }
        return false;
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
            default:
                return "UNKNOWN";
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

            if (tid != that.tid) return false;
            return pkg.equals(that.pkg);

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
