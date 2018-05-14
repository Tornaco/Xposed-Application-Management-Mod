// ITopPackageChangeListener.aidl
package github.tornaco.xposedmoduletest;

// Declare any non-default types here with import statements

interface ITopPackageChangeListener {
    void onChange(String from, String to);
    String hostPackageName();
}
