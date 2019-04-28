// IProcessClearListener.aidl
package github.tornaco.xposedmoduletest;

// Declare any non-default types here with import statements

interface IProcessClearListener {
    oneway void onPrepareClearing();
    oneway void onStartClearing(int plan);
    oneway void onClearingPkg(String pkg);
    oneway void onClearedPkg(String pkg);
    oneway void onAllCleared(in String[] pkg);
    oneway void onIgnoredPkg(String pkg, String reason);
}
