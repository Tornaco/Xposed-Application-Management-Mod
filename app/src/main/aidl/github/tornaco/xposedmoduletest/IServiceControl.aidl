// ITopPackageChangeListener.aidl
package github.tornaco.xposedmoduletest;

// Declare any non-default types here with import statements

import android.content.ComponentName;

interface IServiceControl {
    void stopService();
    ComponentName getServiceComponent();
}
