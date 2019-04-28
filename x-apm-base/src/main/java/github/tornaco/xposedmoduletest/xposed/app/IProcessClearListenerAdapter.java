package github.tornaco.xposedmoduletest.xposed.app;

import android.os.RemoteException;

import github.tornaco.xposedmoduletest.IProcessClearListener;

/**
 * Created by guohao4 on 2017/11/15.
 * Email: Tornaco@163.com
 */

public class IProcessClearListenerAdapter extends IProcessClearListener.Stub {
    @Override
    public void onPrepareClearing() throws RemoteException {

    }

    @Override
    public void onStartClearing(int plan) throws RemoteException {

    }

    @Override
    public void onClearingPkg(String pkg) throws RemoteException {

    }

    @Override
    public void onClearedPkg(String pkg) throws RemoteException {

    }

    @Override
    public void onAllCleared(String[] pkg) throws RemoteException {

    }

    @Override
    public void onIgnoredPkg(String pkg, String reason) throws RemoteException {

    }
}
