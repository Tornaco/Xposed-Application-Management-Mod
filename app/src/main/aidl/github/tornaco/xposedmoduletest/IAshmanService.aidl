package github.tornaco.xposedmoduletest;

import github.tornaco.xposedmoduletest.IProcessClearListener;
import github.tornaco.xposedmoduletest.IPackageUninstallCallback;
import github.tornaco.xposedmoduletest.IAshmanWatcher;
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

    // API For Firewall.
    boolean checkService(in ComponentName servicePkgName, int callerUid);

    boolean checkBroadcast(String action, int receiverUid, int callerUid);

//    boolean isPackageStartBlockEnabled(String pkg);
//    boolean isPackageBootBlockEnabled(String pkg);
//    boolean isPackageLockKillEnabled(String pkg);
//    boolean isPackageRFKillEnabled(String pkg);
//
//    List<String> getWhiteListPackages();

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

    String[] getBootBlockApps(boolean block);
    void addOrRemoveBootBlockApps(in String[] packages, int op);

    String[] getStartBlockApps(boolean block);
    void addOrRemoveStartBlockApps(in String[] packages, int op);

    String[] getLKApps(boolean kill);
    void addOrRemoveLKApps(in String[] packages, int op);

    String[] getRFKApps(boolean kill);
    void addOrRemoveRFKApps(in String[] packages, int op);

    // PM API.
    void unInstallPackage(String pkg, in IPackageUninstallCallback callback);
}
