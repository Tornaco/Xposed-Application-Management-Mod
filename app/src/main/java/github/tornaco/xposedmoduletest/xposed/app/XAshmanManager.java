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
import github.tornaco.xposedmoduletest.IAshmanWatcher;
import github.tornaco.xposedmoduletest.IPackageUninstallCallback;
import github.tornaco.xposedmoduletest.util.Singleton;
import github.tornaco.xposedmoduletest.xposed.bean.BlockRecord2;

/**
 * Created by guohao4 on 2017/11/9.
 * Email: Tornaco@163.com
 */

public class XAshmanManager {

    private static String[] convertObjectArrayToStringArray(Object[] objArr) {
        if (objArr == null || objArr.length == 0) {
            return new String[0];
        }
        String[] out = new String[objArr.length];
        for (int i = 0; i < objArr.length; i++) {
            Object o = objArr[i];
            if (o == null) continue;
            String pkg = String.valueOf(o);
            out[i] = pkg;
        }
        return out;
    }

    /**
     * Reject application network traffic on wifi network
     **/
    public static final int POLICY_REJECT_ON_WIFI = 0x8000;
    /**
     * Reject application network traffic on cellular network
     **/
    public static final int POLICY_REJECT_ON_DATA = 0x10000;

    /**
     * Reject none.
     **/
    public static final int POLICY_REJECT_NONE = 0;

    public interface Op {
        int ADD = 0x1;
        int REMOVE = 0x2;
    }

    public interface ControlMode {
        int WHITE_LIST = 1;
        int BLACK_LIST = 2;
        int UNKNOWN = -1;
    }

    public static final String ASH_MAN_SERVICE_NAME = "user.tor_ash";

    private final IAshmanService mService;

    private static final Singleton<XAshmanManager> sManager
            = new Singleton<XAshmanManager>() {
        @Override
        protected XAshmanManager create() {
            return new XAshmanManager();
        }
    };

    private XAshmanManager() {
        mService = IAshmanService.Stub.asInterface(ServiceManager.getService(ASH_MAN_SERVICE_NAME));
    }

    public static XAshmanManager get() {
        return sManager.get();
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

    public void setRFKillEnabled(boolean enabled) {
        try {
            mService.setRFKillEnabled(enabled);
        } catch (RemoteException e) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(e));
        }
    }

    public boolean isRFKillEnabled() {
        try {
            return mService.isRFKillEnabled();
        } catch (RemoteException e) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(e));
            return false;
        }
    }

    public boolean checkService(ComponentName servicePkgName, int callerUid) {
        ensureService();
        try {
            return mService.checkService(servicePkgName, callerUid);
        } catch (RemoteException ignored) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(ignored));
            return true;
        }
    }

    public boolean checkBroadcast(String action, int receiverUid, int callerUid) {
        ensureService();
        try {
            return mService.checkBroadcast(action, receiverUid, callerUid);
        } catch (RemoteException ignored) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(ignored));
            return true;
        }
    }

    public void setComponentEnabledSetting(ComponentName componentName, int newState, int flags) {
        ensureService();
        try {
            mService.setComponentEnabledSetting(componentName, newState, flags);
        } catch (RemoteException e) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(e));
        }
    }

    public int getComponentEnabledSetting(ComponentName componentName) {
        ensureService();
        try {
            return mService.getComponentEnabledSetting(componentName);
        } catch (RemoteException e) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(e));
            return Integer.MIN_VALUE + PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;
        }
    }

    public int getApplicationEnabledSetting(String packageName) {
        try {
            return mService.getApplicationEnabledSetting(packageName);
        } catch (RemoteException e) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(e));
            return Integer.MIN_VALUE + PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;
        }
    }

    public void setApplicationEnabledSetting(String packageName, int newState, int flags) {
        try {
            mService.setApplicationEnabledSetting(packageName, newState, flags);
        } catch (RemoteException e) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(e));
        }
    }

    public void watch(IAshmanWatcher w) {
        ensureService();
        try {
            mService.watch(w);
        } catch (RemoteException e) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(e));
        }
    }

    public void unWatch(IAshmanWatcher w) {
        ensureService();
        try {
            mService.unWatch(w);
        } catch (RemoteException e) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(e));
        }
    }

    public void setNetworkPolicyUidPolicy(int uid, int policy) {
        ensureService();
        try {
            mService.setNetworkPolicyUidPolicy(uid, policy);
        } catch (RemoteException e) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(e));
        }
    }

    public void restart() {
        ensureService();
        try {
            mService.restart();
        } catch (RemoteException e) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(e));
        }
    }

    public void setCompSettingBlockEnabled(boolean enabled) {
        ensureService();
        try {
            mService.setCompSettingBlockEnabled(enabled);
        } catch (RemoteException e) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(e));
        }
    }

    public boolean isCompSettingBlockEnabledEnabled() {
        ensureService();
        try {
            return mService.isCompSettingBlockEnabledEnabled();
        } catch (RemoteException e) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(e));
            return false;
        }
    }

    public String[] getWhiteListApps(int filterOptions) {
        ensureService();
        try {
            return mService.getWhiteListApps(filterOptions);
        } catch (RemoteException e) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(e));
            return new String[0];
        }
    }

    public String[] getBootBlockApps(boolean block) {
        ensureService();
        try {
            return mService.getBootBlockApps(block);
        } catch (RemoteException e) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(e));
            return new String[0];
        }
    }

    public void addOrRemoveBootBlockApps(String[] packages, int op) {
        ensureService();
        try {
            mService.addOrRemoveBootBlockApps(packages, op);
        } catch (RemoteException e) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(e));
        }
    }

    public String[] getStartBlockApps(boolean block) {
        ensureService();
        try {
            return mService.getStartBlockApps(block);
        } catch (RemoteException e) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(e));
            return new String[0];
        }
    }

    public void addOrRemoveStartBlockApps(String[] packages, int op) {
        ensureService();
        try {
            mService.addOrRemoveStartBlockApps(packages, op);
        } catch (RemoteException e) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(e));
        }
    }

    public String[] getLKApps(boolean kill) {
        ensureService();
        try {
            return mService.getLKApps(kill);
        } catch (RemoteException e) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(e));
            return new String[0];
        }
    }

    public void addOrRemoveLKApps(String[] packages, int op) {
        ensureService();
        try {
            mService.addOrRemoveLKApps(packages, op);
        } catch (RemoteException e) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(e));
        }
    }

    public String[] getRFKApps(boolean kill) {
        ensureService();
        try {
            return mService.getRFKApps(kill);
        } catch (RemoteException e) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(e));
            return new String[0];
        }
    }

    public void addOrRemoveRFKApps(String[] packages, int op) {
        ensureService();
        try {
            mService.addOrRemoveRFKApps(packages, op);
        } catch (RemoteException e) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(e));
        }
    }

    public void setWhiteSysAppEnabled(boolean enabled) {
        ensureService();
        try {
            mService.setWhiteSysAppEnabled(enabled);
        } catch (RemoteException e) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(e));
        }
    }

    public boolean isWhiteSysAppEnabled() {
        ensureService();
        try {
            return mService.isWhiteSysAppEnabled();
        } catch (RemoteException e) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(e));
            return true;
        }
    }

    public void unInstallPackage(String pkg, IPackageUninstallCallback callback) {
        ensureService();
        try {
            mService.unInstallPackage(pkg, callback);
        } catch (RemoteException e) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(e));
        }
    }

    public void restrictAppOnData(int uid, boolean restrict) {
        ensureService();
        try {
            mService.restrictAppOnData(uid, restrict);
        } catch (RemoteException e) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(e));
        }
    }

    public void restrictAppOnWifi(int uid, boolean restrict) {
        ensureService();
        try {
            mService.restrictAppOnWifi(uid, restrict);
        } catch (RemoteException e) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(e));
        }
    }

    public boolean isRestrictOnData(int uid) {
        ensureService();
        try {
            return mService.isRestrictOnData(uid);
        } catch (RemoteException e) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(e));
            return false;
        }
    }

    public boolean isRestrictOnWifi(int uid) {
        ensureService();
        try {
            return mService.isRestrictOnWifi(uid);
        } catch (RemoteException e) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(e));
            return false;
        }
    }

    public boolean isLockKillDoNotKillAudioEnabled() {
        ensureService();
        try {
            return mService.isLockKillDoNotKillAudioEnabled();
        } catch (RemoteException e) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(e));
            return false;
        }
    }

    public void setLockKillDoNotKillAudioEnabled(boolean enabled) {
        ensureService();
        try {
            mService.setLockKillDoNotKillAudioEnabled(enabled);
        } catch (RemoteException e) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(e));
        }
    }

    public int getControlMode() {
        try {
            return mService.getControlMode();
        } catch (RemoteException e) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(e));
            return ControlMode.UNKNOWN;
        }
    }

    public void setControlMode(int mode) {
        ensureService();
        try {
            mService.setControlMode(mode);
        } catch (RemoteException e) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(e));
        }
    }

    public String getBuildSerial() {
        ensureService();
        try {
            return mService.getBuildSerial();
        } catch (RemoteException e) {
            return null;
        }
    }

    public boolean isAutoAddBlackEnabled() {
        ensureService();
        try {
            return mService.isAutoAddBlackEnabled();
        } catch (RemoteException e) {
            return false;
        }
    }

    public void setAutoAddBlackEnable(boolean enable) {
        ensureService();
        try {
            mService.setAutoAddBlackEnable(enable);
        } catch (RemoteException ignored) {

        }
    }

    public void forceReloadPackages() {
        ensureService();
        try {
            mService.forceReloadPackages();
        } catch (RemoteException e) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(e));
        }
    }

    public void setPermissionControlEnabled(boolean enabled) {
        ensureService();
        try {
            mService.setPermissionControlEnabled(enabled);
        } catch (RemoteException e) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(e));
        }
    }

    public boolean isPermissionControlEnabled() {
        ensureService();
        try {
            return mService.isPermissionControlEnabled();
        } catch (RemoteException e) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(e));
            return false;
        }
    }

    public int getPermissionControlBlockModeForUid(int code, String pkg) {
        ensureService();
        try {
            return mService.getPermissionControlBlockModeForUid(code, pkg);
        } catch (RemoteException e) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(e));
            return PackageManager.PERMISSION_GRANTED;
        }
    }

    public void setPermissionControlBlockModeForUid(int code, String pkg, int mode) {
        ensureService();
        try {
            mService.setPermissionControlBlockModeForUid(code, pkg, mode);
        } catch (RemoteException e) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(e));
        }
    }

    public void setAndroidId(String id) {
        ensureService();
        try {
            mService.setAndroidId(id);
        } catch (RemoteException e) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(e));
        }
    }

    public void setDeviceId(String id) {
        ensureService();
        try {
            mService.setDeviceId(id);
        } catch (RemoteException e) {
            Logger.e("XAshmanManager remote: " + Logger.getStackTraceString(e));
        }
    }
}
