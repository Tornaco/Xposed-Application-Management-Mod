package github.tornaco.xposedmoduletest.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;

import org.newstand.logger.Logger;

import java.util.concurrent.atomic.AtomicInteger;

import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.provider.XSettings;
import github.tornaco.xposedmoduletest.xposed.app.AshmanWatcherAdapter;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;

/**
 * Created by guohao4 on 2017/11/13.
 * Email: Tornaco@163.com
 */

public class WatchDogService extends Service implements Handler.Callback, WatchDog {

    private Handler h;

    private static final AtomicInteger NOTIFICATION_ID = new AtomicInteger(0);

    private boolean adapterRegistered = false;

    private final AshmanWatcherAdapter ashmanWatcherAdapter = new AshmanWatcherAdapter() {
        @Override
        public void onStartBlocked(String packageName) throws RemoteException {
            super.onStartBlocked(packageName);
            h.obtainMessage(WatchDogMessages.MSG_ONSTARTBLOCKED, packageName).sendToTarget();
        }
    };

    private SharedPreferences sharedPreferences;

    private static final String PREF_BLOCK_NOTI_NAME = "start_block_notified";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        h = new Handler(this);
        sharedPreferences = getSharedPreferences(PREF_BLOCK_NOTI_NAME, MODE_PRIVATE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.d("WatchDogService start.");
        if (!adapterRegistered) {
            if (XAshmanManager.singleInstance().isServiceAvailable()) {
                XAshmanManager.singleInstance().watch(ashmanWatcherAdapter);
                adapterRegistered = true;
            }
        }

        if (BuildConfig.DEBUG && !XAshmanManager.singleInstance().isServiceAvailable()) {
            NotificationManagerCompat.from(this)
                    .notify(NOTIFICATION_ID.getAndIncrement(),
                            new Notification.Builder(this)
                                    .setContentTitle(getString(R.string.app_name))
                                    .setContentText(getString(R.string.title_service_not_connected))
                                    .setSmallIcon(R.drawable.ic_noti_app)
                                    .build());
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (XAshmanManager.singleInstance().isServiceAvailable()) {
            XAshmanManager.singleInstance().unWatch(ashmanWatcherAdapter);
        }
        adapterRegistered = false;
    }

    @Override
    public boolean handleMessage(Message msg) {
        Logger.d("WatchDogService handle message: " + WatchDogMessages.decodeMessage(msg.what));
        switch (msg.what) {
            case WatchDogMessages.MSG_ONSTARTBLOCKED:
                onStartBlocked((String) msg.obj);
                break;
        }
        return false;
    }

    @Override
    public void onStartBlocked(String pkg) {
        Logger.d("WatchDogService onStartBlocked: " + pkg);

        if (isAppNotified(pkg) || !XSettings.isStartBlockNotify(this)) return;

        String appName = String.valueOf(PkgUtil.loadNameByPkgName(this, pkg));

        NotificationManagerCompat.from(this)
                .notify(NOTIFICATION_ID.getAndIncrement(),
                        new Notification.Builder(this)
                                .setContentTitle(getString(R.string.title_app_auto_start))
                                .setContentText(getString(R.string.notification_app_auto_start_blocked, appName))
                                .setSmallIcon(R.drawable.ic_noti_start_block)
                                .build());

        setAppNotified(pkg);
    }

    private boolean isAppNotified(String pkg) {
        return sharedPreferences.getBoolean(pkg, false);
    }

    private void setAppNotified(String pkg) {
        sharedPreferences.edit().putBoolean(pkg, true).apply();
    }
}
