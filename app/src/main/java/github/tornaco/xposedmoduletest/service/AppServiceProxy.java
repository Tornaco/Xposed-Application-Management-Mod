package github.tornaco.xposedmoduletest.service;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import github.tornaco.android.common.Holder;
import github.tornaco.android.common.service.ServiceProxy;
import github.tornaco.xposedmoduletest.IAppService;
import github.tornaco.xposedmoduletest.ICallback;
import github.tornaco.xposedmoduletest.IXModuleToken;

/**
 * Created by guohao4 on 2017/10/20.
 * Email: Tornaco@163.com
 */

public class AppServiceProxy extends ServiceProxy implements IAppService {

    private IAppService service;

    public AppServiceProxy(Context context) {
        super(context, new Intent(context, AppService.class));
    }

    @Override
    public void onConnected(IBinder iBinder) {
        service = IAppService.Stub.asInterface(iBinder);
    }

    @Override
    public void noteAppStart(final ICallback callback, final String pkg,
                             final int callingUID, final int callingPID)
            throws RemoteException {
        setTask(new ProxyTask() {
            @Override
            public void run() throws RemoteException {
                service.noteAppStart(callback, pkg, callingUID, callingPID);
            }
        });
    }

    @Override
    public void onHome() throws RemoteException {
        throw new RuntimeException("Not supported");
    }

    @Override
    public void registerXModuleToken(IXModuleToken token) throws RemoteException {
        throw new RuntimeException("Not supported");
    }

    @Override
    @android.support.annotation.WorkerThread
    public int getXModuleStatus() throws RemoteException {
        final Holder<Integer> res = new Holder<>();
        setTask(new ProxyTask() {
            @Override
            public void run() throws RemoteException {
                res.setData(service.getXModuleStatus());
            }
        });
        waitForCompletion();
        return res.getData();
    }

    @Override
    public String getXModuleCodeName() throws RemoteException {
        final Holder<String> res = new Holder<>();
        setTask(new ProxyTask() {
            @Override
            public void run() throws RemoteException {
                res.setData(service.getXModuleCodeName());
            }
        });
        waitForCompletion();
        return res.getData();
    }

    @Override
    public IBinder asBinder() {
        return service.asBinder();
    }
}
