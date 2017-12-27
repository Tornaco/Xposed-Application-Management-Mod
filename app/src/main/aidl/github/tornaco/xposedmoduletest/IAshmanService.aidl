package github.tornaco.xposedmoduletest;

import github.tornaco.xposedmoduletest.IProcessClearListener;
import github.tornaco.xposedmoduletest.IPackageUninstallCallback;
import github.tornaco.xposedmoduletest.IAshmanWatcher;
import github.tornaco.xposedmoduletest.ITopPackageChangeListener;
import github.tornaco.xposedmoduletest.xposed.bean.BlockRecord2;


interface IAshmanService {

    void clearProcess(in IProcessClearListener listener);

    void setLockKillDelay(long delay);
    long getLockKillDelay();

    void setWhiteSysAppEnabled(boolean enabled);
    boolean isWhiteSysAppEnabled();

    void setBootBlockEnabled(boolean enabled);
    boolean isBlockBlockEnabled();

    void setStartBlockEnabled(boolean enabled);
    boolean isStartBlockEnabled();

    void setLockKillEnabled(boolean enabled);
    boolean isLockKillEnabled();

    void setRFKillEnabled(boolean enabled);
    boolean isRFKillEnabled();

    void setGreeningEnabled(boolean enabled);
    boolean isGreeningEnabled();

    // API For Firewall.
    boolean checkService(in ComponentName servicePkgName, int callerUid);

    boolean checkBroadcast(String action, int receiverUid, int callerUid);

    List<BlockRecord2> getBlockRecords();

    void clearBlockRecords();

    void setComponentEnabledSetting(in ComponentName componentName, int newState, int flags);

    int getComponentEnabledSetting(in ComponentName componentName);

    int getApplicationEnabledSetting(String packageName);

    void setApplicationEnabledSetting(String packageName, int newState, int flags);

    void watch(in IAshmanWatcher w);
    void unWatch(in IAshmanWatcher w);

    // Network policy API.
    void setNetworkPolicyUidPolicy(int uid, int policy);

    void restrictAppOnData(int uid, boolean restrict);
    void restrictAppOnWifi(int uid, boolean restrict);

    boolean isRestrictOnData(int uid);
    boolean isRestrictOnWifi(int uid);

    // Power API.
    void restart();

    // Extra API.
    void setCompSettingBlockEnabled(boolean enabled);
    boolean isCompSettingBlockEnabledEnabled();

    // Package loader API.
    String[] getWhiteListApps(int filterOptions);
    String[] getInstalledApps(int filterOptions);

    String[] getBootBlockApps(boolean block);
    void addOrRemoveBootBlockApps(in String[] packages, int op);

    String[] getStartBlockApps(boolean block);
    void addOrRemoveStartBlockApps(in String[] packages, int op);

    String[] getLKApps(boolean kill);
    void addOrRemoveLKApps(in String[] packages, int op);

    String[] getRFKApps(boolean kill);
    void addOrRemoveRFKApps(in String[] packages, int op);

    String[] getGreeningApps(boolean greening);
    void addOrRemoveGreeningApps(in String[] packages, int op);

    boolean isPackageGreening(String packageName);
    boolean isUidGreening(int uid);

    // PM API.
    void unInstallPackage(String pkg, in IPackageUninstallCallback callback);

    boolean isLockKillDoNotKillAudioEnabled();
    void setLockKillDoNotKillAudioEnabled(boolean enabled);

    int getControlMode();
    void setControlMode(int mode);

    String getBuildSerial();

    boolean isAutoAddBlackEnabled();
    void setAutoAddBlackEnable(boolean enable);

    void forceReloadPackages();

    void setPermissionControlEnabled(boolean enabled);
    boolean isPermissionControlEnabled();

    int getPermissionControlBlockModeForPkg(int code, String pkg);
    int getPermissionControlBlockModeForUid(int code, int uid);
    void setPermissionControlBlockModeForPkg(int code, String pkg, int mode);

    void setUserDefinedAndroidId(String id);
    void setUserDefinedDeviceId(String id);
    void setUserDefinedLine1Number(String id);

    String getAndroidId();
    String getDeviceId();
    String getLine1Number();

    String getUserDefinedLine1Number();
    String getUserDefinedDeviceId();
    String getUserDefinedAndroidId();

    boolean isPackageInPrivacyList(String pkg);
    boolean isUidInPrivacyList(int uid);
    String[] getPrivacyList();
    void addOrRemoveFromPrivacyList(String pkg, int op);

    boolean showFocusedActivityInfoEnabled();
    void setShowFocusedActivityInfoEnabled(boolean enabled);

    void restoreDefaultSettings();

    List<RunningServiceInfo> getRunningServices(int max);
    List<RunningAppProcessInfo> getRunningAppProcesses();

    void writeSystemSettings(String key, String value);
    String getSystemSettings(String key);

    long[] getProcessPss(in int[] pids);

    boolean onApplicationUncaughtException(String packageName, String thread, String exception, String trace);
    boolean isAppCrashDumpEnabled();
    void setAppCrashDumpEnabled(boolean enabled);

    void registerOnTopPackageChangeListener(in ITopPackageChangeListener listener);
    void unRegisterOnTopPackageChangeListener(in ITopPackageChangeListener listener);

    boolean isLazyModeEnabled();
    boolean isLazyModeEnabledForPackage(String pkg);
    void setLazyModeEnabled(boolean enabled);

    String[] getLazyApps(boolean lazy);
    void addOrRemoveLazyApps(in String[] packages, int op);

    // Long press back kill.
    void setLPBKEnabled(boolean enabled);
    boolean isLPBKEnabled();
}
