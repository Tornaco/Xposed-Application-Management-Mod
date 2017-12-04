// IPackageUninstallCallback.aidl
package github.tornaco.xposedmoduletest;

interface IPackageUninstallCallback {
    void onSuccess();
    void onFail(int reason);
}
