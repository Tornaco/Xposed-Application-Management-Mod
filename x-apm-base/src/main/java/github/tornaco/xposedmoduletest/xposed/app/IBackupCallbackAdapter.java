package github.tornaco.xposedmoduletest.xposed.app;

import android.os.RemoteException;

import github.tornaco.xposedmoduletest.IBackupCallback;

/**
 * Created by Tornaco on 2018/5/29 13:53.
 * This file is writen for project X-APM at host guohao4.
 */
@SuppressWarnings("RedundantThrows")
public class IBackupCallbackAdapter extends IBackupCallback.Stub {
    @Override
    public void onBackupFinished(String domain, String path) throws RemoteException {

    }

    @Override
    public void onRestoreFinished(String domain, String path) throws RemoteException {

    }

    @Override
    public void onFail(String message) throws RemoteException {

    }

    @Override
    public void onProgress(String progressMessage) throws RemoteException {

    }
}
