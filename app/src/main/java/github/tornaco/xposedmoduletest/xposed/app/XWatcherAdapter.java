package github.tornaco.xposedmoduletest.xposed.app;

import android.os.RemoteException;

import github.tornaco.xposedmoduletest.IAppGuardWatcher;

/**
 * Created by guohao4 on 2017/10/27.
 * Email: Tornaco@163.com
 */

public class XWatcherAdapter extends IAppGuardWatcher.Stub {
    @Override
    public void onServiceException(String trace) throws RemoteException {

    }

    @Override
    public void onUserLeaving(String reason) throws RemoteException {

    }
}
