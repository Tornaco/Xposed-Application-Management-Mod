package github.tornaco.xposedmoduletest.xposed.app;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.RemoteException;
import android.os.ServiceManager;

import com.google.common.base.Preconditions;

import org.newstand.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import github.tornaco.xposedmoduletest.IAshmanService;
import github.tornaco.xposedmoduletest.xposed.bean.BlockRecord2;

/**
 * Created by guohao4 on 2017/11/9.
 * Email: Tornaco@163.com
 */

public class XAshmanManager {
    public static final String ASH_MAN_SERVICE_NAME = "user.tor_ash";

    private IAshmanService mService;

    private static XAshmanManager sMe;

    private XAshmanManager() {
        mService = IAshmanService.Stub.asInterface(ServiceManager.getService(ASH_MAN_SERVICE_NAME));
    }

    public static void init() {
        if (sMe == null || !sMe.isServiceAvailable()) {
            sMe = new XAshmanManager();
        }
    }

    public static XAshmanManager singleInstance() {
        return sMe;
    }

    public boolean isServiceAvailable() {
        return mService != null;
    }

    private void ensureService() {
        Preconditions.checkNotNull(mService, "Service not available");
    }

    public void clearProcess(IProcessClearListenerAdapter adapter) {
        ensureService();
        try {
            mService.clearProcess(adapter);
        } catch (RemoteException e) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(e));
        }
    }

    public void setBootBlockEnabled(boolean enabled) {
        ensureService();
        try {
            mService.setBootBlockEnabled(enabled);
        } catch (RemoteException e) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(e));
        }
    }

    public boolean isBlockBlockEnabled() {
        ensureService();
        try {
            return mService.isBlockBlockEnabled();
        } catch (RemoteException e) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(e));
            return false;
        }
    }

    public void setStartBlockEnabled(boolean enabled) {
        ensureService();
        try {
            mService.setStartBlockEnabled(enabled);
        } catch (RemoteException e) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(e));
        }
    }

    public boolean isStartBlockEnabled() {
        ensureService();
        try {
            return mService.isStartBlockEnabled();
        } catch (RemoteException e) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(e));
            return false;
        }
    }

    public void setLockKillEnabled(boolean enabled) {
        ensureService();
        try {
            mService.setLockKillEnabled(enabled);
        } catch (RemoteException e) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(e));
        }
    }

    public List<BlockRecord2> getBlockRecords() {
        ensureService();
        try {
            return mService.getBlockRecords();
        } catch (RemoteException e) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(e));
            return new ArrayList<>(0);
        }
    }

    public void clearBlockRecords() {
        ensureService();
        try {
            mService.clearBlockRecords();
        } catch (RemoteException e) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(e));
        }
    }

    public boolean isLockKillEnabled() {
        ensureService();
        try {
            return mService.isLockKillEnabled();
        } catch (RemoteException e) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(e));
            return false;
        }
    }

    public void setLockKillDelay(long delay) {
        ensureService();
        try {
            mService.setLockKillDelay(delay);
        } catch (RemoteException e) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(e));
        }
    }

    public long getLockKillDelay() {
        ensureService();
        try {
            return mService.getLockKillDelay();
        } catch (RemoteException e) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(e));
            return 0L;
        }
    }

    public boolean checkService(ComponentName servicePkgName, int callerUid) {
        try {
            return mService.checkService(servicePkgName, callerUid);
        } catch (RemoteException ignored) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(ignored));
            return true;
        }
    }

    public boolean checkBroadcast(String action, int receiverUid, int callerUid) {
        try {
            return mService.checkBroadcast(action, receiverUid, callerUid);
        } catch (RemoteException ignored) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(ignored));
            return true;
        }
    }

    public void setComponentEnabledSetting(ComponentName componentName, int newState, int flags) {
        try {
            mService.setComponentEnabledSetting(componentName, newState, flags);
        } catch (RemoteException e) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(e));
        }
    }

    public int getComponentEnabledSetting(ComponentName componentName) {
        try {
            return mService.getComponentEnabledSetting(componentName);
        } catch (RemoteException e) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(e));
            return Integer.MIN_VALUE + PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;
        }
    }
}
