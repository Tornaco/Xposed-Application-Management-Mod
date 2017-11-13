package github.tornaco.xposedmoduletest;

interface IAshmanService {
    void clearProcess();

    void setBootBlockEnabled(boolean enabled);
    boolean isBlockBlockEnabled();

    void setStartBlockEnabled(boolean enabled);
    boolean isStartBlockEnabled();

    void setLockKillEnabled(boolean enabled);
    boolean isLockKillEnabled();


}
