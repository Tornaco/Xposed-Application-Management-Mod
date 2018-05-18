// IProcessClearListener.aidl
package github.tornaco.xposedmoduletest;

// Declare any non-default types here with import statements

interface IProcessClearListener {
    boolean doNotClearWhenIntervative();
    boolean onlyForThoseInList();
    void onPrepareClearing();
    void onStartClearing(int plan);
    void onClearingPkg(String pkg);
    void onClearedPkg(String pkg);
    void onAllCleared(in String[] pkg);
    void onIgnoredPkg(String pkg, String reason);
}
