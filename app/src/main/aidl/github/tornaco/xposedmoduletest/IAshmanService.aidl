package github.tornaco.xposedmoduletest;

import github.tornaco.xposedmoduletest.IProcessClearListener;
import github.tornaco.xposedmoduletest.IAshmanWatcher;
import github.tornaco.xposedmoduletest.xposed.bean.BlockRecord2;


interface IAshmanService {

    void clearProcess(in IProcessClearListener listener);

    void setLockKillDelay(long delay);
    long getLockKillDelay();

    void setBootBlockEnabled(boolean enabled);
    boolean isBlockBlockEnabled();

    void setStartBlockEnabled(boolean enabled);
    boolean isStartBlockEnabled();

    void setLockKillEnabled(boolean enabled);
    boolean isLockKillEnabled();

    // API For Firewall.
    boolean checkService(in ComponentName servicePkgName, int callerUid);

    boolean checkBroadcast(String action, int receiverUid, int callerUid);

    List<BlockRecord2> getBlockRecords();

    void clearBlockRecords();

    void setComponentEnabledSetting(in ComponentName componentName, int newState, int flags);

    int getComponentEnabledSetting(in ComponentName componentName);

    void watch(in IAshmanWatcher w);
    void unWatch(in IAshmanWatcher w);
}
