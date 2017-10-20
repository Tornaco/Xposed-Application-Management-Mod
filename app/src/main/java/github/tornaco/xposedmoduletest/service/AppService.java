package github.tornaco.xposedmoduletest.service;

import android.app.Service;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.SparseArray;

import org.newstand.logger.Logger;

import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import github.tornaco.android.common.Collections;
import github.tornaco.android.common.Consumer;
import github.tornaco.xposedmoduletest.IAppService;
import github.tornaco.xposedmoduletest.ICallback;
import github.tornaco.xposedmoduletest.IXModuleToken;
import github.tornaco.xposedmoduletest.bean.DaoManager;
import github.tornaco.xposedmoduletest.bean.DaoSession;
import github.tornaco.xposedmoduletest.bean.PackageInfo;
import github.tornaco.xposedmoduletest.ui.AppStartNoter;
import github.tornaco.xposedmoduletest.x.XExecutor;
import github.tornaco.xposedmoduletest.x.XMode;
import github.tornaco.xposedmoduletest.x.XSettings;
import github.tornaco.xposedmoduletest.x.XStatus;

/**
 * Created by guohao4 on 2017/10/17.
 * Email: Tornaco@163.com
 */

public class AppService extends Service {

    private final SparseArray<Transaction> TRANSACTIONS = new SparseArray<>();
    private final Set<String> GUARD_PACKAGES = new HashSet<>();

    private AtomicBoolean mGuardEnabled = new AtomicBoolean(false);

    private XSettings xSettings;

    private ServiceBinder mServiceBinder;

    private AppStartNoter mAppStartNoter;

    @Override
    public void onCreate() {
        super.onCreate();

        xSettings = XSettings.get();
        xSettings.addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                Logger.v("Settings changed");
                mGuardEnabled.set(xSettings.enabled(getApplicationContext()));
            }
        });
        mGuardEnabled.set(xSettings.enabled(getApplicationContext()));
        mServiceBinder = new ServiceBinder();

        Handler handler = new Handler(Looper.getMainLooper());
        mAppStartNoter = new AppStartNoter(handler, this);
    }

    private void noteAppStart(final ICallback callback, final String pkg,
                              int callingUID, int callingPID) {
        Logger.d("noteAppStart: %s %s %s", pkg, callingUID, callingPID);
        if (!mGuardEnabled.get() || bypass(pkg)) {
            try {
                callback.onRes(XMode.MODE_IGNORED);
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

        CallingInfo callingInfo = CallingInfo.from(getPackageManager(), callingUID, pkg);
        Logger.d("Calling info: %s", callingInfo);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mAppStartNoter.note(callingInfo.callingName,
                    callingInfo.targetPkg,
                    callingInfo.targetName,
                    new ICallback.Stub() {
                        @Override
                        public void onRes(int res) throws RemoteException {
                            onTransactionRes(transactionID, res);
                        }
                    });
        }
    }

    private void onRemoteError(RemoteException e) {
        Logger.e("onRemoteError\n" + Logger.getStackTraceString(e));
    }

    private void onTransactionRes(int id, int res) {
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
        loadGuardPackages();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // FIXME Should check any permission here.
        return mServiceBinder;
    }

    private boolean bypass(String pkg) {
        return !GUARD_PACKAGES.contains(pkg);
    }

    private void loadGuardPackages() {
        Logger.i("loadGuardPackages");
        XExecutor.execute(new Runnable() {
            @Override
            public void run() {
                DaoSession session = DaoManager.getInstance().getSession(AppService.this);
                if (session == null) return;//FIXME.
                GUARD_PACKAGES.clear();
                Collections.consumeRemaining(session.getPackageInfoDao().loadAll(),
                        new Consumer<PackageInfo>() {
                            @Override
                            public void accept(PackageInfo packageInfo) {
                                if (packageInfo.getGuard())
                                    GUARD_PACKAGES.add(packageInfo.getPkgName());
                            }
                        });
                Logger.i("loadGuardPackages complete.");
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private static class TransactionFactory {

        private static final AtomicInteger TRANS_ID_BASE = new AtomicInteger(2017);

        static int transactionID() {
            return TRANS_ID_BASE.getAndIncrement();
        }
    }

    private class ServiceBinder extends IAppService.Stub {

        @Nullable
        private XModuleTokenClient xModuleTokenClient;

        @Override
        public void noteAppStart(ICallback callback, String pkg,
                                 int callingUID, int callingPID) throws RemoteException {
            AppService.this.noteAppStart(callback, pkg, callingUID, callingPID);
        }

        @Override
        public void onHome() throws RemoteException {

        }

        @Override
        public void registerXModuleToken(IXModuleToken token) throws RemoteException {
            if (token == null) return;
            if (this.xModuleTokenClient != null) {
                this.xModuleTokenClient.unLinkToDeath();
            }
            this.xModuleTokenClient = new XModuleTokenClient(token);
            Logger.d("registerXModuleToken, token:" + token);
        }

        @Override
        public int getXModuleStatus() throws RemoteException {
            if (this.xModuleTokenClient == null || !this.xModuleTokenClient.alive) {
                return XStatus.UNKNOWN.ordinal();
            }
            return this.xModuleTokenClient.token.status();
        }

        @Override
        public String getXModuleCodeName() throws RemoteException {
            if (this.xModuleTokenClient == null || !this.xModuleTokenClient.alive) {
                return "UNKNOWN";
            }
            return this.xModuleTokenClient.token.codename();
        }
    }

    private class XModuleTokenClient implements IBinder.DeathRecipient {
        private IXModuleToken token;
        private boolean alive;

        XModuleTokenClient(IXModuleToken token) {
            this.token = token;
            this.alive = true;
            try {
                this.token.asBinder().linkToDeath(this, 0);
            } catch (RemoteException ignored) {

            }
        }

        @Override
        public void binderDied() {
            alive = false;
            unLinkToDeath();
        }

        void unLinkToDeath() {
            if (alive) {
                token.asBinder().unlinkToDeath(this, 0);
            }
        }
    }

    private static class CallingInfo {
        String callingPkg = "", callingName = "";
        String targetName = "", targetPkg = "";

        CallingInfo() {
        }

        @Override
        public String toString() {
            return "CallingInfo{" +
                    "callingPkg='" + callingPkg + '\'' +
                    ", callingName='" + callingName + '\'' +
                    ", targetName='" + targetName + '\'' +
                    '}';
        }

        static CallingInfo from(PackageManager pm, int callingUID, String targetPkg) {
            CallingInfo callInfo = new CallingInfo();
            callInfo.targetPkg = targetPkg;
//            String[] pkgs = pm.getPackagesForUid(callingUID);
//            String callingPkg = null;
//            if (pkgs != null && pkgs.length > 0) {
//                callingPkg = pkgs[0];
//                callInfo.callingPkg = callingPkg;
//            }
//            if (!TextUtils.isEmpty(callingPkg)) try {
//                ApplicationInfo callingName = pm.getApplicationInfo(callingPkg, 0);
//                if (callingName != null)
//                    callInfo.callingName = String.valueOf(callingName.loadLabel(pm));
//            } catch (PackageManager.NameNotFoundException e) {
//                Logger.w("NameNotFoundException:" + callingPkg);
//            }
            try {
                ApplicationInfo targetAppInfo = pm.getApplicationInfo(targetPkg, 0);
                if (targetAppInfo != null)
                    callInfo.targetName = String.valueOf(targetAppInfo.loadLabel(pm));
            } catch (PackageManager.NameNotFoundException e) {
                Logger.w("NameNotFoundException:" + targetPkg);
            }
            return callInfo;
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

        String getPkg() {
            return pkg;
        }

        ICallback getCallback() {
            return callback;
        }

        Transaction(int id, String pkg, ICallback callback) {
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
