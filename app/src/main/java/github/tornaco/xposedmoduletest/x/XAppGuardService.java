package github.tornaco.xposedmoduletest.x;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;

import com.android.internal.os.AtomicFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.HashSet;
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

    private static final String TAG = "XAppGuardService";

    private static final int MSG_VERIFY_RES = 0x100;
    private static final int MSG_SET_ENABLED = 0x200;

    private Context mContext;
    private Handler mHandler;

    private AtomicBoolean mEnabled = new AtomicBoolean(false);

    private final Set<String> WATCHED_PACKAGES = new HashSet<>();

    private final Set<String> PASSED_PACKAGES = new HashSet<>();

    private static final Set<String> PREBUILT_WHITE_LIST = new HashSet<>();

    private final SparseArray<Transaction> TRANSACTIONS = new SparseArray<>();

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

    XAppGuardService(Context context) {
        this.mContext = context;
        File dataDir = Environment.getDataDirectory();
        File systemDir = new File(dataDir, "system");
        systemDir.mkdirs();
        mXmlFile = new AtomicFile(new File(systemDir, "app_guard.xml"));
        Slog.d(TAG, "xml file:" + mXmlFile.getBaseFile());
    }

    void publish() {
        ServiceManager.addService(XContext.APP_GUARD_SERVICE, asBinder());
        Slog.d(TAG, "published:" + Binder.getCallingUid());
    }

    void systemReady() {
        Slog.d(TAG, "systemReady:" + Binder.getCallingUid());
        mHandler = new Handler();
        readSettings();
        loadPackages();
        registerReceiver();
    }

    private void registerReceiver() {
        mContext.registerReceiver(mScreenReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
    }

    void shutdown() {
        Slog.d(TAG, "shutdown...");
        persistPackages();
    }

    boolean passed(String pkg) {
        return !mEnabled.get()
                || PREBUILT_WHITE_LIST.contains(pkg)
                || PASSED_PACKAGES.contains(pkg)
                || !WATCHED_PACKAGES.contains(pkg);
    }

    void verify(String pkg, int uid, int pid, VerifyListener listener) {
        int tid = TransactionFactory.transactionID();
        Transaction transaction = new Transaction(listener, uid, pid, tid, pkg);
        TRANSACTIONS.put(tid, transaction);
        Slog.d(TAG, "Put tid:" + tid);

        Intent intent = buildLockIntent(tid, pkg);
        mContext.startActivity(intent);
    }

    private void readSettings() {
        ContentResolver contentResolver = mContext.getContentResolver();
        try {
            boolean enabled = (Settings.System.getInt(contentResolver, XContext.SETTINGS_APP_GUARD_ENABLED) == 1);
            mEnabled.set(enabled);
            Slog.d(TAG, "enabled:" + enabled);
        } catch (Settings.SettingNotFoundException e) {
            Slog.w(TAG, "SettingNotFoundException:" + e);
        }
    }

    @Override
    public boolean isEnabled() throws RemoteException {
        return mEnabled.get();
    }

    @Override
    public void setEnabled(boolean enabled) throws RemoteException {
        mHandler.obtainMessage(MSG_SET_ENABLED, enabled ? 1 : 0, 0).sendToTarget();
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
        if (mEnabled.compareAndSet(!enabled, enabled)) {
            ContentResolver contentResolver = mContext.getContentResolver();
            Settings.System.putInt(contentResolver, XContext.SETTINGS_APP_GUARD_ENABLED, enabled ? 1 : 0);
        }
    }

    @Override
    public void setResult(int transactionID, final int res) throws RemoteException {
        Slog.d(TAG, "setResult:" + transactionID + ", res:" + res);
        mHandler.obtainMessage(MSG_VERIFY_RES, res, transactionID, null).sendToTarget();
        TRANSACTIONS.remove(transactionID);
    }

    @Override
    public void testUI() throws RemoteException {
        long id = Binder.clearCallingIdentity();
        Intent intent = buildLockIntent(TransactionFactory.transactionID(), "xxxxx");
        mContext.startActivity(intent);
        Binder.restoreCallingIdentity(id);
    }

    @Override
    public void addPackages(String[] pkgs) throws RemoteException {
        Collections.consumeRemaining(pkgs, new Consumer<String>() {
            @Override
            public void accept(String s) {
                if (!TextUtils.isEmpty(s) && !WATCHED_PACKAGES.contains(s)) {
                    WATCHED_PACKAGES.add(s);
                    Slog.d(TAG, "Add package:" + s);
                }
            }
        });
    }

    @Override
    public void removePackages(String[] pkgs) throws RemoteException {
        Collections.consumeRemaining(pkgs, new Consumer<String>() {
            @Override
            public void accept(String s) {
                if (!TextUtils.isEmpty(s) && WATCHED_PACKAGES.contains(s)) {
                    WATCHED_PACKAGES.remove(s);
                    Slog.d(TAG, "Remove package:" + s);
                }
            }
        });
    }

    @Override
    public void watch(IWatcher w) throws RemoteException {

    }

    @Override
    public void forceWriteState() throws RemoteException {
        long id = Binder.clearCallingIdentity();
        mWorkingService.execute(new Runnable() {
            @Override
            public void run() {
                persistPackages();
            }
        });
        Binder.restoreCallingIdentity(id);
    }

    @Override
    public void forceReadState() throws RemoteException {
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
            Slog.d(TAG, "reader:" + content);
            StringTokenizer stringTokenizer = new StringTokenizer(content, "|");
            WATCHED_PACKAGES.clear();
            while (stringTokenizer.hasMoreTokens()) {
                String p = stringTokenizer.nextToken();
                WATCHED_PACKAGES.add(p);
                Slog.d(TAG, "Add:" + p);
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

    private static Intent buildLockIntent(int transId, String pkg) {
        Intent intent = new Intent();
        intent.setClassName(BuildConfig.APPLICATION_ID, "github.tornaco.xposedmoduletest.ui.LockStubActivity");
        intent.putExtra(XKey.EXTRA_PKG_NAME, pkg);
        intent.putExtra(XKey.EXTRA_TRANS_ID, transId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    @Override
    public boolean handleMessage(Message msg) {
        Slog.d(TAG, "handleMessage:" + msg.what);
        switch (msg.what) {
            case MSG_VERIFY_RES:
                onRes(msg.arg1, msg.arg2);
                return true;
            case MSG_SET_ENABLED:
                onSetEnabled(msg.arg1 == 1);
                return true;
        }
        return false;
    }

    private void onRes(int res, int transactionID) {
        final Transaction transaction = TRANSACTIONS.get(transactionID);
        if (transaction == null) {
            Slog.e(TAG, "Can not find transaction for:" + transactionID);
            return;
        }
        if (res == XMode.MODE_ALLOWED) {
            PASSED_PACKAGES.add(transaction.pkg);
        }
        transaction.listener.onVerifyRes(transaction.pkg, transaction.uid, transaction.pid, res);
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
}
