package github.tornaco.xposedmoduletest.xposed.app;

import android.os.RemoteException;

import github.tornaco.xposedmoduletest.IAshmanWatcher;

/**
 * Created by guohao4 on 2017/11/22.
 * Email: Tornaco@163.com
 */

public class AshmanWatcherAdapter extends IAshmanWatcher.Stub {
    @Override
    public void onStartBlocked(String packageName) throws RemoteException {

    }
}
