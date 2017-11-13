package github.tornaco.xposedmoduletest;

// Declare any non-default types here with import statements

interface IAppGuardWatcher {
    void onServiceException(String trace);
    void onUserLeaving(String reason);
}
