package github.tornaco.xposedmoduletest;

import github.tornaco.xposedmoduletest.IAppGuardWatcher;
import github.tornaco.xposedmoduletest.xposed.bean.BlurSettings;
import github.tornaco.xposedmoduletest.xposed.bean.PackageSettings;
import github.tornaco.xposedmoduletest.xposed.bean.VerifySettings;

import github.tornaco.xposedmoduletest.bean.CongfigurationSetting;

import android.content.ComponentName;
import java.util.Map;

interface IAppGuardService {

    boolean isEnabled();
    void setEnabled(boolean enabled);

    boolean isBlurEnabled();
    void setBlurEnabled(boolean enabled);

    int getBlurRadius();
    void setBlurRadius(int r);

    boolean isUninstallInterruptEnabled();
    void setUninstallInterruptEnabled(boolean enabled);

    void setVerifySettings(in VerifySettings settings);

    VerifySettings getVerifySettings();

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

    boolean isInterruptFPEventVBEnabled(int event);
    void setInterruptFPEventVBEnabled(int event, boolean enabled);

    void addOrRemoveComponentReplacement(in ComponentName from, in ComponentName to, boolean add);
    Map getComponentReplacements();

    void forceReloadPackages();

    String[] getLockApps(boolean lock);
    void addOrRemoveLockApps(in String[] packages, boolean add);

    String[] getBlurApps(boolean lock);
    void addOrRemoveBlurApps(in String[] packages, boolean blur);

    String[] getUPApps(boolean lock);
    void addOrRemoveUPApps(in String[] packages, boolean add);

    void restoreDefaultSettings();
}
