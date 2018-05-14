package github.tornaco.xposedmoduletest.xposed.app;

import android.os.RemoteException;

import github.tornaco.xposedmoduletest.IPackageUninstallCallback;

/**
 * Created by guohao4 on 2017/12/3.
 * Email: Tornaco@163.com
 */

public class IPackageUninstallCallbackAdapter extends IPackageUninstallCallback.Stub {
    @Override
    public void onSuccess() throws RemoteException {

    }

    @Override
    public void onFail(int reason) throws RemoteException {

    }
}
