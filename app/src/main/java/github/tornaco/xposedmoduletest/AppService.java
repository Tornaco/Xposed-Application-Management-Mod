package github.tornaco.xposedmoduletest;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.SparseArray;

import org.newstand.logger.Logger;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by guohao4 on 2017/10/17.
 * Email: Tornaco@163.com
 */

public class AppService extends Service {

    private final SparseArray<Transaction> TRANSACTIONS = new SparseArray<>();

    private final Set<String> PASSED_PACKAGES = new HashSet<>();

    private BroadcastReceiver mScreenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                PASSED_PACKAGES.clear();
            }
        }
    };

    private Handler mUIHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        registerReceiver(mScreenReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        mUIHandler = new Handler(Looper.getMainLooper());
    }

    private void noteAppStart(final ICallback callback, final String pkg) {
        Logger.d("noteAppStart:" + pkg);
        // Check if already passed.
        if (PASSED_PACKAGES.contains(pkg)) {
            try {
                Logger.d("PASSED:" + pkg);
                callback.onRes(true);
            } catch (RemoteException e) {
                onRemoteError(e);
            }
            return;
        }

        final int transactionID = TransactionFactory.transactionID();
        Logger.d("noteAppStart with transaction id: %s", transactionID);

        synchronized (TRANSACTIONS) {
            TRANSACTIONS.put(transactionID, new Transaction(transactionID, pkg, callback));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            new AppStartNoter().note(mUIHandler, AppService.this, pkg, new ICallback.Stub() {
                @Override
                public void onRes(boolean res) throws RemoteException {
                    onTransactionRes(transactionID, res);
                }
            });
        }
    }

    private void onRemoteError(RemoteException e) {
        Logger.e("onRemoteError\n" + Logger.getStackTraceString(e));
    }

    private void onTransactionRes(int id, boolean res) {
        Logger.i("onTransactionRes: %s, %s", id, res);
        synchronized (TRANSACTIONS) {
            Transaction t = TRANSACTIONS.get(id);
            if (t == null) {
                Logger.e("Could not find transaction for %s", id);
                return;
            }
            try {
                if (t.dead) {
                    Logger.e("DEAD transaction for %s", id);
                    return;
                }
                if (res) {
                    PASSED_PACKAGES.add(t.getPkg());
                }
                t.getCallback().onRes(res);
            } catch (RemoteException e) {
                onRemoteError(e);
            } finally {
                t.unLinkToDeath();
                TRANSACTIONS.remove(id);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.d("onStartCommand: %s", intent);
        if (intent == null) return START_STICKY;

        if (AppStartNoterActivity.ACTION_TRANS_RES.equals(intent.getAction())) {
            onTransactionRes(intent.getIntExtra(AppStartNoterActivity.KEY_TRANS_ID, -1)
                    , intent.getBooleanExtra(AppStartNoterActivity.KEY_TRANS_RES, false));
        }

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new IAppService.Stub() {

            @Override
            public void noteAppStart(ICallback callback, String pkg) throws RemoteException {
                AppService.this.noteAppStart(callback, pkg);
            }
        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mScreenReceiver);
    }

    private static class TransactionFactory {

        private static final AtomicInteger TRANS_ID_BASE = new AtomicInteger(2017);

        static int transactionID() {
            return TRANS_ID_BASE.getAndIncrement();
        }
    }

    private static class Transaction implements IBinder.DeathRecipient {

        private int id;
        private String pkg;
        private ICallback callback;
        private boolean dead;

        public int getId() {
            return id;
        }

        public String getPkg() {
            return pkg;
        }

        public ICallback getCallback() {
            return callback;
        }

        public Transaction(int id, String pkg, ICallback callback) {
            this.id = id;
            this.pkg = pkg;
            this.callback = callback;
            try {
                callback.asBinder().linkToDeath(this, 0);
            } catch (RemoteException ignored) {

            }
        }

        void unLinkToDeath() {
            callback.asBinder().unlinkToDeath(this, 0);
        }

        @Override
        public void binderDied() {
            dead = true;
        }
    }
}
