// ICallback.aidl
package github.tornaco.xposedmoduletest;

// Declare any non-default types here with import statements

interface IWatcher {
    void onServiceException(String trace);
    void onUserLeaving(String reason);
}
