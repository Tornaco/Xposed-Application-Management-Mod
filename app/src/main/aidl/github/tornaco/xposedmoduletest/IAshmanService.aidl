package github.tornaco.xposedmoduletest;

import github.tornaco.xposedmoduletest.IProcessClearListener;

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
}
