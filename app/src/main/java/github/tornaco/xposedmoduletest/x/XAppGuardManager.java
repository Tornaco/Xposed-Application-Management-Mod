package github.tornaco.xposedmoduletest.x;

import android.os.RemoteException;
import android.os.ServiceManager;

import org.newstand.logger.Logger;

import github.tornaco.xposedmoduletest.IAppGuardService;
import github.tornaco.xposedmoduletest.IWatcher;

/**
 * Created by guohao4 on 2017/10/23.
 * Email: Tornaco@163.com
 */

public class XAppGuardManager {

    private static XAppGuardManager sMe;

    private IAppGuardService mService;

    private XAppGuardManager() {
        mService = IAppGuardService.Stub.asInterface(ServiceManager.getService(XContext.APP_GUARD_SERVICE));
        Logger.d("XAppGuardManager, service: " + mService);
    }

    static void init() {
        sMe = new XAppGuardManager();
    }

    public static XAppGuardManager get() {
        return sMe;
    }

    public boolean isServiceConnected() {
        Logger.v("Service connected: " + mService);
        return mService != null;
    }

    public boolean isEnabled() {
        if (!isServiceConnected()) return false;
        try {
            return mService.isEnabled();
        } catch (RemoteException e) {
            Logger.e(Logger.getStackTraceString(e));
        }
        return false;
    }

    public void setEnabled(boolean enabled) {
        if (!isServiceConnected()) return;
        try {
            mService.setEnabled(enabled);
        } catch (RemoteException e) {
            Logger.e(Logger.getStackTraceString(e));
        }
    }

    public void setResult(int transactionID, int res) {
        if (!isServiceConnected()) return;
        try {
            mService.setResult(transactionID, res);
        } catch (RemoteException e) {
            Logger.e(Logger.getStackTraceString(e));
        }
    }

    public void testUI() {
        if (!isServiceConnected()) return;
        try {
            mService.testUI();
        } catch (RemoteException e) {
            Logger.e(Logger.getStackTraceString(e));
        }
    }

    public void addPackages(String[] pkgs) {
        if (!isServiceConnected()) return;
        try {
            mService.addPackages(pkgs);
        } catch (RemoteException e) {
            Logger.e(Logger.getStackTraceString(e));
        }
    }

    public void removePackages(String[] pkgs) {
        if (!isServiceConnected()) return;
        try {
            mService.removePackages(pkgs);
        } catch (RemoteException e) {
            Logger.e(Logger.getStackTraceString(e));
        }
    }

    public void watch(IWatcher w) {
        if (!isServiceConnected()) return;
        try {
            mService.watch(w);
        } catch (RemoteException e) {
            Logger.e(Logger.getStackTraceString(e));
        }
    }

    public void forceWriteState() {
        if (!isServiceConnected()) return;
        try {
            mService.forceWriteState();
        } catch (RemoteException e) {
            Logger.e(Logger.getStackTraceString(e));
        }
    }

    public void forceReadState() {
        if (!isServiceConnected()) return;
        try {
            mService.forceReadState();
        } catch (RemoteException e) {
            Logger.e(Logger.getStackTraceString(e));
        }
    }

    public String[] getPackages() {
        if (!isServiceConnected()) return new String[0];
        try {
            return mService.getPackages();
        } catch (RemoteException e) {
            Logger.e(Logger.getStackTraceString(e));
        }
        return new String[0];
    }
}
