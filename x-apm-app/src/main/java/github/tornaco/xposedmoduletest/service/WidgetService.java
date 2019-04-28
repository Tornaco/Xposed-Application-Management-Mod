package github.tornaco.xposedmoduletest.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.widget.Toast;

import org.newstand.logger.Logger;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.util.XExecutor;
import github.tornaco.xposedmoduletest.xposed.app.IProcessClearListenerAdapter;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;

/**
 * Created by guohao4 on 2017/12/4.
 * Email: Tornaco@163.com
 */

public class WidgetService extends Service {

    public static final String ACTION_CLEAR_PROCESS = "tornaco.appwidget.action.clear_process";

    private int mClearedPackageNum = 0;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_NOT_STICKY;

        Logger.d("WidgetService. onStartCommand");

        if (XAPMManager.get().isServiceAvailable() && ACTION_CLEAR_PROCESS.equals(intent.getAction())) {
            XAPMManager.get().clearProcess(new IProcessClearListenerAdapter() {

                @Override
                public void onPrepareClearing() throws RemoteException {
                    super.onPrepareClearing();
                    mClearedPackageNum = 0;
                }

                @Override
                public void onClearedPkg(String pkg) throws RemoteException {
                    super.onClearedPkg(pkg);
                    mClearedPackageNum++;
                }

                @Override
                public void onAllCleared(final String[] pkg) throws RemoteException {
                    super.onAllCleared(pkg);
                    XExecutor.getUIThreadHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), getString(R.string.clear_process_complete_with_num,
                                    String.valueOf(mClearedPackageNum)),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }, false, false);
        }

        return START_NOT_STICKY;
    }
}