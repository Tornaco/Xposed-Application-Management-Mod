package github.tornaco.xposedmoduletest.xposed.app;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.RemoteException;
import android.os.ServiceManager;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;

import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.IAshmanService;
import github.tornaco.xposedmoduletest.IAshmanWatcher;
import github.tornaco.xposedmoduletest.IPackageUninstallCallback;
import github.tornaco.xposedmoduletest.ITopPackageChangeListener;
import github.tornaco.xposedmoduletest.compat.os.AppOpsManagerCompat;
import github.tornaco.xposedmoduletest.util.Singleton;
import github.tornaco.xposedmoduletest.xposed.bean.BlockRecord2;

/**
 * Created by guohao4 on 2017/11/9.
 * Email: Tornaco@163.com
 */

@SuppressWarnings("EmptyCatchBlock")
public class XAshmanManager {

    public static final int FLAG_SHOW_SYSTEM_APP = 0X100;
    public static final int FLAG_SHOW_SYSTEM_APP_WITHOUT_CORE_APP = 0X200;
    public static final int FLAG_NONE = 0;

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
        } catch (RemoteException ignored) {
        }
    }

    public void setBootBlockEnabled(boolean enabled) {
        ensureService();
        try {
            mService.setBootBlockEnabled(enabled);
        } catch (RemoteException e) {
        }
    }

    public boolean isBlockBlockEnabled() {
        ensureService();
        try {
            return mService.isBlockBlockEnabled();
        } catch (RemoteException e) {
            return false;
        }
    }

    public void setStartBlockEnabled(boolean enabled) {
        ensureService();
        try {
            mService.setStartBlockEnabled(enabled);
        } catch (RemoteException e) {
        }
    }

    public boolean isStartBlockEnabled() {
        ensureService();
        try {
            return mService.isStartBlockEnabled();
        } catch (RemoteException e) {
            return false;
        }
    }

    public void setLockKillEnabled(boolean enabled) {
        ensureService();
        try {
            mService.setLockKillEnabled(enabled);
        } catch (RemoteException e) {
        }
    }

    public List<BlockRecord2> getBlockRecords() {
        ensureService();
        try {
            return mService.getBlockRecords();
        } catch (RemoteException e) {
            return new ArrayList<>(0);
        }
    }

    public void clearBlockRecords() {
        ensureService();
        try {
            mService.clearBlockRecords();
        } catch (RemoteException e) {
        }
    }

    public boolean isLockKillEnabled() {
        ensureService();
        try {
            return mService.isLockKillEnabled();
        } catch (RemoteException e) {
            return false;
        }
    }

    public void setLockKillDelay(long delay) {
        ensureService();
        try {
            mService.setLockKillDelay(delay);
        } catch (RemoteException e) {
        }
    }

    public long getLockKillDelay() {
        ensureService();
        try {
            return mService.getLockKillDelay();
        } catch (RemoteException e) {
            return 0L;
        }
    }

    public void setRFKillEnabled(boolean enabled) {
        try {
            mService.setRFKillEnabled(enabled);
        } catch (RemoteException e) {
        }
    }

    public boolean isRFKillEnabled() {
        try {
            return mService.isRFKillEnabled();
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean checkService(ComponentName servicePkgName, int callerUid) {
        ensureService();
        try {
            return mService.checkService(servicePkgName, callerUid);
        } catch (RemoteException ignored) {
            return true;
        }
    }

    public boolean checkBroadcast(String action, int receiverUid, int callerUid) {
        ensureService();
        try {
            return mService.checkBroadcast(action, receiverUid, callerUid);
        } catch (RemoteException ignored) {
            return true;
        }
    }

    public void setComponentEnabledSetting(ComponentName componentName, int newState, int flags) {
        ensureService();
        try {
            mService.setComponentEnabledSetting(componentName, newState, flags);
        } catch (RemoteException e) {
        }
    }

    public int getComponentEnabledSetting(ComponentName componentName) {
        ensureService();
        try {
            return mService.getComponentEnabledSetting(componentName);
        } catch (RemoteException e) {
            return Integer.MIN_VALUE + PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;
        }
    }

    public int getApplicationEnabledSetting(String packageName) {
        try {
            return mService.getApplicationEnabledSetting(packageName);
        } catch (RemoteException e) {
            return Integer.MIN_VALUE + PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;
        }
    }

    public void setApplicationEnabledSetting(String packageName, int newState, int flags) {
        try {
            mService.setApplicationEnabledSetting(packageName, newState, flags);
        } catch (RemoteException e) {
        }
    }

    public void watch(IAshmanWatcher w) {
        ensureService();
        try {
            mService.watch(w);
        } catch (RemoteException e) {
        }
    }

    public void unWatch(IAshmanWatcher w) {
        ensureService();
        try {
            mService.unWatch(w);
        } catch (RemoteException e) {
        }
    }

    public void setNetworkPolicyUidPolicy(int uid, int policy) {
        ensureService();
        try {
            mService.setNetworkPolicyUidPolicy(uid, policy);
        } catch (RemoteException e) {
        }
    }

    public void restart() {
        ensureService();
        try {
            mService.restart();
        } catch (RemoteException e) {
        }
    }

    public void setCompSettingBlockEnabled(boolean enabled) {
        ensureService();
        try {
            mService.setCompSettingBlockEnabled(enabled);
        } catch (RemoteException e) {
        }
    }

    public boolean isCompSettingBlockEnabledEnabled() {
        ensureService();
        try {
            return mService.isCompSettingBlockEnabledEnabled();
        } catch (RemoteException e) {
            return false;
        }
    }

    public String[] getWhiteListApps(int filterOptions) {
        ensureService();
        try {
            return mService.getWhiteListApps(filterOptions);
        } catch (RemoteException e) {
            return new String[0];
        }
    }

    public String[] getBootBlockApps(boolean block) {
        ensureService();
        try {
            return mService.getBootBlockApps(block);
        } catch (RemoteException e) {
            return new String[0];
        }
    }

    public void addOrRemoveBootBlockApps(String[] packages, int op) {
        ensureService();
        try {
            mService.addOrRemoveBootBlockApps(packages, op);
        } catch (RemoteException e) {
        }
    }

    public String[] getStartBlockApps(boolean block) {
        ensureService();
        try {
            return mService.getStartBlockApps(block);
        } catch (RemoteException e) {
            return new String[0];
        }
    }

    public void addOrRemoveStartBlockApps(String[] packages, int op) {
        ensureService();
        try {
            mService.addOrRemoveStartBlockApps(packages, op);
        } catch (RemoteException e) {
        }
    }

    public String[] getLKApps(boolean kill) {
        ensureService();
        try {
            return mService.getLKApps(kill);
        } catch (RemoteException e) {
            return new String[0];
        }
    }

    public void addOrRemoveLKApps(String[] packages, int op) {
        ensureService();
        try {
            mService.addOrRemoveLKApps(packages, op);
        } catch (RemoteException e) {
        }
    }

    public String[] getRFKApps(boolean kill) {
        ensureService();
        try {
            return mService.getRFKApps(kill);
        } catch (RemoteException e) {
            return new String[0];
        }
    }

    public void addOrRemoveRFKApps(String[] packages, int op) {
        ensureService();
        try {
            mService.addOrRemoveRFKApps(packages, op);
        } catch (RemoteException e) {
        }
    }

    public void setWhiteSysAppEnabled(boolean enabled) {
        ensureService();
        try {
            mService.setWhiteSysAppEnabled(enabled);
        } catch (RemoteException e) {
        }
    }

    public boolean isWhiteSysAppEnabled() {
        ensureService();
        try {
            return mService.isWhiteSysAppEnabled();
        } catch (RemoteException e) {
            return true;
        }
    }

    public void unInstallPackage(String pkg, IPackageUninstallCallback callback) {
        ensureService();
        try {
            mService.unInstallPackage(pkg, callback);
        } catch (RemoteException e) {
        }
    }

    public void restrictAppOnData(int uid, boolean restrict) {
        ensureService();
        try {
            mService.restrictAppOnData(uid, restrict);
        } catch (RemoteException e) {
        }
    }

    public void restrictAppOnWifi(int uid, boolean restrict) {
        ensureService();
        try {
            mService.restrictAppOnWifi(uid, restrict);
        } catch (RemoteException e) {
        }
    }

    public boolean isRestrictOnData(int uid) {
        ensureService();
        try {
            return mService.isRestrictOnData(uid);
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean isRestrictOnWifi(int uid) {
        ensureService();
        try {
            return mService.isRestrictOnWifi(uid);
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean isLockKillDoNotKillAudioEnabled() {
        ensureService();
        try {
            return mService.isLockKillDoNotKillAudioEnabled();
        } catch (RemoteException e) {
            return false;
        }
    }

    public void setLockKillDoNotKillAudioEnabled(boolean enabled) {
        ensureService();
        try {
            mService.setLockKillDoNotKillAudioEnabled(enabled);
        } catch (RemoteException e) {
        }
    }

    public int getControlMode() {
        try {
            return mService.getControlMode();
        } catch (RemoteException e) {
            return ControlMode.UNKNOWN;
        }
    }

    public void setControlMode(int mode) {
        ensureService();
        try {
            mService.setControlMode(mode);
        } catch (RemoteException e) {
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
        }
    }

    public void setPermissionControlEnabled(boolean enabled) {
        ensureService();
        try {
            mService.setPermissionControlEnabled(enabled);
        } catch (RemoteException e) {
        }
    }

    public boolean isPermissionControlEnabled() {
        ensureService();
        try {
            return mService.isPermissionControlEnabled();
        } catch (RemoteException e) {
            return false;
        }
    }

    public int getPermissionControlBlockModeForPkg(int code, String pkg) {
        ensureService();
        try {
            return mService.getPermissionControlBlockModeForPkg(code, pkg);
        } catch (RemoteException e) {
            return AppOpsManagerCompat.MODE_ALLOWED;
        }
    }

    public int getPermissionControlBlockModeForUid(int code, int uid) {
        ensureService();
        try {
            return mService.getPermissionControlBlockModeForUid(code, uid);
        } catch (RemoteException e) {
            return AppOpsManagerCompat.MODE_ALLOWED;
        }
    }

    public void setPermissionControlBlockModeForPkg(int code, String pkg, int mode) {
        ensureService();
        try {
            mService.setPermissionControlBlockModeForPkg(code, pkg, mode);
        } catch (RemoteException ignored) {
        }
    }

    public void setUserDefinedAndroidId(String id) {
        ensureService();
        try {
            mService.setUserDefinedAndroidId(id);
        } catch (RemoteException ignored) {
        }
    }

    public void setUserDefinedDeviceId(String id) {
        ensureService();
        try {
            mService.setUserDefinedDeviceId(id);
        } catch (RemoteException ignored) {
        }
    }

    public void setUserDefinedLine1Number(String id) {
        ensureService();
        try {
            mService.setUserDefinedLine1Number(id);
        } catch (RemoteException ignored) {

        }
    }

    public String getAndroidId() {
        ensureService();
        try {
            return mService.getAndroidId();
        } catch (RemoteException e) {
            return null;
        }
    }

    public String getDeviceId() {
        ensureService();
        try {
            return mService.getDeviceId();
        } catch (RemoteException ignored) {
            return null;
        }
    }

    public String getLine1Number() {
        ensureService();
        try {
            return mService.getLine1Number();
        } catch (RemoteException ignored) {
            return null;
        }
    }

    public String getUserDefinedLine1Number() {
        ensureService();
        try {
            return mService.getUserDefinedLine1Number();
        } catch (RemoteException ignored) {
            return null;
        }
    }

    public String getUserDefinedDeviceId() {
        ensureService();
        try {
            return mService.getUserDefinedDeviceId();
        } catch (RemoteException e) {
            return null;
        }
    }

    public String getUserDefinedAndroidId() {
        ensureService();
        try {
            return mService.getUserDefinedAndroidId();
        } catch (RemoteException ignored) {
            return null;
        }
    }

    public boolean showFocusedActivityInfoEnabled() {
        ensureService();
        try {
            return mService.showFocusedActivityInfoEnabled();
        } catch (RemoteException e) {
            return false;
        }
    }

    public void setShowFocusedActivityInfoEnabled(boolean enabled) {
        ensureService();
        try {
            mService.setShowFocusedActivityInfoEnabled(enabled);
        } catch (RemoteException ignored) {

        }
    }

    public boolean isPackageInPrivacyList(String pkg) {
        ensureService();
        if (BuildConfig.DEBUG) try {
            return mService.isPackageInPrivacyList(pkg);
        } catch (RemoteException e) {
            return false;
        }
        return false;
    }

    public boolean isUidInPrivacyList(int uid) {
        ensureService();
        if (BuildConfig.DEBUG) try {
            return mService.isUidInPrivacyList(uid);
        } catch (RemoteException e) {
            return false;
        }
        return false;
    }

    public String[] getPrivacyList() {
        ensureService();
        try {
            return mService.getPrivacyList();
        } catch (RemoteException ignored) {
            return new String[0];
        }
    }

    public void addOrRemoveFromPrivacyList(String pkg, int op) {
        ensureService();
        try {
            mService.addOrRemoveFromPrivacyList(pkg, op);
        } catch (RemoteException ignored) {
        }
    }

    public String[] getGreeningApps(boolean greening) {
        ensureService();
        try {
            return mService.getGreeningApps(greening);
        } catch (RemoteException e) {
            return new String[0];
        }
    }

    public void addOrRemoveGreeningApps(String[] packages, int op) {
        ensureService();
        try {
            mService.addOrRemoveGreeningApps(packages, op);
        } catch (RemoteException ignored) {
        }
    }

    public void setGreeningEnabled(boolean enabled) {
        ensureService();
        try {
            mService.setGreeningEnabled(enabled);
        } catch (RemoteException ignored) {
        }
    }

    public boolean isGreeningEnabled() {
        ensureService();
        try {
            return mService.isGreeningEnabled();
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean isPackageGreening(String packageName) {
        ensureService();
        if (BuildConfig.DEBUG) try {
            return mService.isPackageGreening(packageName);
        } catch (RemoteException e) {
            return false;
        }
        return false;
    }

    public boolean isUidGreening(int uid) {
        ensureService();
        try {
            return mService.isUidGreening(uid);
        } catch (RemoteException e) {
            return false;
        }
    }

    public String[] getInstalledApps(int filterOptions) {
        ensureService();
        try {
            return mService.getInstalledApps(filterOptions);
        } catch (RemoteException e) {
            return new String[0];
        }
    }

    public void restoreDefaultSettings() {
        ensureService();
        try {
            mService.restoreDefaultSettings();
        } catch (RemoteException ignored) {
        }
    }

    public List<ActivityManager.RunningServiceInfo> getRunningServices(int max) {
        ensureService();
        try {
            return mService.getRunningServices(max);
        } catch (RemoteException e) {
            return new ArrayList<>(0);
        }
    }

    public List<ActivityManager.RunningAppProcessInfo> getRunningAppProcesses() {
        try {
            return mService.getRunningAppProcesses();
        } catch (RemoteException e) {
            return new ArrayList<>(0);
        }
    }

    public void writeSystemSettings(String key, String value) {
        ensureService();
        try {
            mService.writeSystemSettings(key, value);
        } catch (RemoteException ignored) {
        }
    }

    public String getSystemSettings(String key) {
        ensureService();
        try {
            return mService.getSystemSettings(key);
        } catch (RemoteException e) {
            return null;
        }
    }

    public long[] getProcessPss(int[] pids) {
        ensureService();
        try {
            return mService.getProcessPss(pids);
        } catch (RemoteException e) {
            return new long[0];
        }
    }

    public boolean onApplicationUncaughtException(String packageName, String thread, String exception, String trace) {
        ensureService();
        try {
            return mService.onApplicationUncaughtException(packageName, thread, exception, trace);
        } catch (RemoteException ignored) {
            return false;
        }
    }

    public boolean isAppCrashDumpEnabled() {
        try {
            return mService.isAppCrashDumpEnabled();
        } catch (RemoteException e) {
            return false;
        }
    }

    public void setAppCrashDumpEnabled(boolean enabled) {
        try {
            mService.setAppCrashDumpEnabled(enabled);
        } catch (RemoteException ignored) {

        }
    }

    public void registerOnTopPackageChangeListener(ITopPackageChangeListener listener) {
        try {
            mService.registerOnTopPackageChangeListener(listener);
        } catch (RemoteException ignored) {
        }
    }

    public void unRegisterOnTopPackageChangeListener(ITopPackageChangeListener listener) {
        try {
            mService.unRegisterOnTopPackageChangeListener(listener);
        } catch (RemoteException ignored) {

        }
    }

    public boolean isLazyModeEnabled() {
        try {
            return mService.isLazyModeEnabled();
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean isLazyModeEnabledForPackage(String pkg) {
        try {
            return mService.isLazyModeEnabledForPackage(pkg);
        } catch (RemoteException e) {
            return false;
        }
    }

    public void setLazyModeEnabled(boolean enabled) {
        try {
            mService.setLazyModeEnabled(enabled);
        } catch (RemoteException ignored) {
        }
    }

    public String[] getLazyApps(boolean lazy) {
        try {
            return mService.getLazyApps(lazy);
        } catch (RemoteException e) {
            return new String[0];
        }
    }

    public void addOrRemoveLazyApps(String[] packages, int op) {
        try {
            mService.addOrRemoveLazyApps(packages, op);
        } catch (RemoteException ignored) {
        }
    }

    public void setLPBKEnabled(boolean enabled) {
        try {
            mService.setLPBKEnabled(enabled);
        } catch (RemoteException ignored) {

        }
    }

    public boolean isLPBKEnabled() {
        try {
            return mService.isLPBKEnabled();
        } catch (RemoteException ignored) {
            return false;
        }
    }

    public void onTaskRemoving(int callingUid, int taskId) {
        try {
            mService.onTaskRemoving(callingUid, taskId);
        } catch (RemoteException ignored) {

        }
    }

    public void addOrRemoveAppFocusAction(String pkg, String[] actions, boolean add) {
        try {
            mService.addOrRemoveAppFocusAction(pkg, actions, add);
        } catch (RemoteException e) {

        }
    }

    public void addOrRemoveAppUnFocusAction(String pkg, String[] actions, boolean add) {
        try {
            mService.addOrRemoveAppUnFocusAction(pkg, actions, add);
        } catch (RemoteException e) {

        }
    }

    public String[] getAppFocusActionPackages() {
        try {
            return mService.getAppFocusActionPackages();
        } catch (RemoteException e) {
            return new String[0];
        }
    }

    public String[] getAppFocusActions(String pkg) {
        try {
            return mService.getAppFocusActions(pkg);
        } catch (RemoteException e) {
            return new String[0];
        }
    }

    public String[] getAppUnFocusActionPackages() {
        try {
            return mService.getAppUnFocusActionPackages();
        } catch (RemoteException e) {
            return new String[0];
        }
    }

    public String[] getAppUnFocusActions(String pkg) {
        try {
            return mService.getAppUnFocusActions(pkg);
        } catch (RemoteException e) {
            return new String[0];
        }
    }

    public long getLastDozeEnterTimeMills() {
        try {
            return mService.getLastDozeEnterTimeMills();
        } catch (RemoteException e) {
            return -1;
        }
    }
}
