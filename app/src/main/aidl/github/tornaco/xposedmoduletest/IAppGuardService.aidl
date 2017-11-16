package github.tornaco.xposedmoduletest;

import github.tornaco.xposedmoduletest.IAppGuardWatcher;
import github.tornaco.xposedmoduletest.xposed.bean.BlurSettings;
import github.tornaco.xposedmoduletest.xposed.bean.PackageSettings;
import github.tornaco.xposedmoduletest.xposed.bean.VerifySettings;

interface IAppGuardService {

    boolean isEnabled();
    void setEnabled(boolean enabled);

    boolean isUninstallInterruptEnabled();
    void setUninstallInterruptEnabled(boolean enabled);

    void setVerifySettings(in VerifySettings settings);

    VerifySettings getVerifySettings();

    void setBlurSettings(in BlurSettings settings);
    BlurSettings getBlurSettings();

    void setResult(int transactionID, int res);

    boolean isTransactionValid(int transactionID);

    void watch(in IAppGuardWatcher w);
    void unWatch(in IAppGuardWatcher w);

    // For test only.
    void mockCrash();

    void setVerifierPackage(String pkg);

    void injectHomeEvent();

    void setDebug(boolean debug);

    boolean isDebug();

    void onActivityPackageResume(String pkg);

    String[] getSubModules();
    int getSubModuleStatus(String token);
}
