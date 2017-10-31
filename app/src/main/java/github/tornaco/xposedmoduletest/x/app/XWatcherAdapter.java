package github.tornaco.xposedmoduletest.x.app;

import android.os.RemoteException;

import github.tornaco.xposedmoduletest.IWatcher;

/**
 * Created by guohao4 on 2017/10/27.
 * Email: Tornaco@163.com
 */

public class XWatcherAdapter extends IWatcher.Stub {
    @Override
    public void onServiceException(String trace) throws RemoteException {

    }

    @Override
    public void onUserLeaving(String reason) throws RemoteException {

    }
}
