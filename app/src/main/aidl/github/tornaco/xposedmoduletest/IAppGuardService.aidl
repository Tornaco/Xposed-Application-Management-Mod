// IAppGuardService.aidl
package github.tornaco.xposedmoduletest;

// Declare any non-default types here with import statements

import github.tornaco.xposedmoduletest.IWatcher;

interface IAppGuardService {
    boolean isEnabled();
    void setEnabled(boolean enabled);

    // Blur api.
    boolean isBlur();
    void setBlur(boolean blur);
    void setBlurPolicy(int policy);
    int getBlurPolicy();
    void setBlurRadius(int radius);
    int getBlurRadius();
    void setBlurScale(float scale);
    float getBlurScale();

    // Feature api.
    boolean hasFeature(String feature);

    void ignore(String pkg);
    void pass(String pkg);

    int getStatus();

    String[] getPackages();

    void setResult(int transactionID, int res);

    void testUI();

    void addPackages(in String[] pkgs);
    void removePackages(in String[] pkgs);

    void watch(in IWatcher w);

    void forceWriteState();
    void forceReadState();
}
