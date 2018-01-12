package github.tornaco.xposedmoduletest.xposed.app;

import android.content.ComponentName;
import android.os.RemoteException;
import android.os.ServiceManager;

import com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.Map;

import github.tornaco.xposedmoduletest.IAshmanService;
import github.tornaco.xposedmoduletest.util.Singleton;
import github.tornaco.xposedmoduletest.xposed.bean.BlurSettings;
import github.tornaco.xposedmoduletest.xposed.bean.VerifySettings;

/**
 * Created by guohao4 on 2017/10/23.
 * Email: Tornaco@163.com
 */

@SuppressWarnings({"WeakerAccess", "EmptyCatchBlock"})
public class XAppGuardManager {

    public static final String META_DATA_KEY_APP_GUARD_VERIFY_DISPLAYER = "app_guard_verify_displayer";

    public static final String ACTION_APP_GUARD_VERIFY_DISPLAYER
            = "github.tornaco.xpose.app.interruptPackageRemoval.action.verify.displayer";

    public static final String SERVICE_NAME = XAshmanManager.SERVICE_NAME;

    public static final String EXTRA_PKG_NAME = "extra.pkg";

    public static final String EXTRA_TRANS_ID = "extra.tid";
    public static final String EXTRA_INJECT_HOME_WHEN_FAIL_ID = "extra.inject_home_on_fail";

    @SuppressWarnings("WeakerAccess")
    public interface BlurPolicy {
        int BLUR_WATCHED = 0x1;
        int BLUR_ALL = 0x2;
        int BLUR_POLICY_UNKNOWN = -1;

        class Checker {
            public static boolean valid(int p) {
                return p == BLUR_ALL || p == BLUR_WATCHED;
            }
        }
    }

    public interface FPEvent {
        int SUCCESS = 0x1;
        int ERROR = 0x2;
    }

    private static final Singleton<XAppGuardManager> sManager =
            new Singleton<XAppGuardManager>() {
                @Override
                protected XAppGuardManager create() {
                    return new XAppGuardManager();
                }
            };

    private final IAshmanService mService;

    private XAppGuardManager() {
        mService = IAshmanService.Stub.asInterface(ServiceManager.getService(SERVICE_NAME));
    }

    public static XAppGuardManager get() {
        return sManager.get();
    }

    public boolean isServiceAvailable() {
        return mService != null;
    }

    private void ensureService() {
        Preconditions.checkNotNull(mService, "Service not available");
    }

    public boolean isEnabled() {
        ensureService();
        try {
            return mService.isAppLockEnabled();
        } catch (RemoteException e) {

        }
        return false;
    }

    public void setEnabled(boolean enabled) {
        ensureService();
        try {
            mService.setAppLockEnabled(enabled);
        } catch (RemoteException e) {

        }
    }


    public void setVerifySettings(VerifySettings settings) {
        ensureService();
        try {
            mService.setVerifySettings(settings);
        } catch (RemoteException e) {

        }
    }

    public VerifySettings getVerifySettings() {
        ensureService();
        try {
            return mService.getVerifySettings();
        } catch (RemoteException e) {

            return null;
        }
    }

    public void setResult(int transactionID, int res) {
        ensureService();
        try {
            mService.setResult(transactionID, res);
        } catch (RemoteException e) {

        }
    }

    public boolean isTransactionValid(int transactionID) {
        ensureService();
        try {
            return mService.isTransactionValid(transactionID);
        } catch (RemoteException e) {

            return false;
        }
    }

    public void mockCrash() {
        ensureService();
        try {
            mService.mockCrash();
        } catch (RemoteException e) {

        }
    }

    public boolean isUninstallInterruptEnabled() {
        ensureService();
        try {
            return mService.isUninstallInterruptEnabled();
        } catch (RemoteException e) {

        }
        return false;
    }

    public void setUninstallInterruptEnabled(boolean enabled) {
        ensureService();
        try {
            mService.setUninstallInterruptEnabled(enabled);
        } catch (RemoteException e) {

        }
    }

    public void setVerifierPackage(String pkg) {
        ensureService();
        try {
            mService.setVerifierPackage(pkg);
        } catch (RemoteException e) {

        }
    }

    @Deprecated
    public void injectHomeEvent() {
        ensureService();
        try {
            mService.injectHomeEvent();
        } catch (RemoteException e) {

        }
    }

    public void setDebug(boolean debug) {
        ensureService();
        try {
            mService.setDebug(debug);
        } catch (RemoteException e) {

        }
    }

    public boolean isDebug() {
        ensureService();
        try {
            return mService.isDebug();
        } catch (RemoteException e) {

        }
        return false;
    }

    @Deprecated
    public void onActivityPackageResume(String pkg) {
        ensureService();
        try {
            mService.onActivityPackageResume(pkg);
        } catch (RemoteException e) {

        }
    }

    public boolean isInterruptFPEventVBEnabled(int event) {
        ensureService();
        try {
            return mService.isInterruptFPEventVBEnabled(event);
        } catch (RemoteException e) {

            return false;
        }
    }

    public void setInterruptFPEventVBEnabled(int event, boolean enabled) {
        ensureService();
        try {
            mService.setInterruptFPEventVBEnabled(event, enabled);
        } catch (RemoteException e) {

        }
    }

    public void addOrRemoveComponentReplacement(ComponentName from, ComponentName to, boolean add) {
        if (from == null) return;
        ensureService();
        try {
            mService.addOrRemoveComponentReplacement(from, to, add);
        } catch (RemoteException e) {

        }
    }


    public Map getComponentReplacements() {
        ensureService();
        try {
            return mService.getComponentReplacements();
        } catch (RemoteException e) {
            return new HashMap(0);
        }
    }

    public void forceReloadPackages() {
        ensureService();
        try {
            mService.forceReloadPackages();
        } catch (RemoteException e) {

        }
    }

    public String[] getLockApps(boolean lock) {
        ensureService();
        try {
            return mService.getLockApps(lock);
        } catch (RemoteException e) {

            return new String[0];
        }
    }

    public void addOrRemoveLockApps(String[] packages, boolean add) {
        ensureService();
        try {
            mService.addOrRemoveLockApps(packages, add);
        } catch (RemoteException e) {

        }
    }

    public String[] getBlurApps(boolean lock) {
        ensureService();
        try {
            return mService.getBlurApps(lock);
        } catch (RemoteException e) {

            return new String[0];
        }
    }

    public void addOrRemoveBlurApps(String[] packages, boolean blur) {
        ensureService();
        try {
            mService.addOrRemoveBlurApps(packages, blur);
        } catch (RemoteException e) {

        }
    }

    // Uninstall pro.
    public String[] getUPApps(boolean lock) {
        ensureService();
        try {
            return mService.getUPApps(lock);
        } catch (RemoteException e) {

            return new String[0];
        }
    }

    public void addOrRemoveUPApps(String[] packages, boolean add) {
        ensureService();
        try {
            mService.addOrRemoveUPApps(packages, add);
        } catch (RemoteException e) {

        }
    }

    public boolean isBlurEnabled() {
        ensureService();
        try {
            return mService.isBlurEnabled();
        } catch (RemoteException e) {

            return false;
        }
    }

    public boolean isBlurEnabledForPackage(String who) {
        ensureService();
        try {
            return mService.isBlurEnabledForPackage(who);
        } catch (RemoteException e) {

            return false;
        }
    }

    public void setBlurEnabled(boolean enabled) {
        ensureService();
        try {
            mService.setBlurEnabled(enabled);
        } catch (RemoteException e) {

        }
    }

    public void restoreDefaultSettings() {
        ensureService();
        try {
            mService.restoreDefaultSettings();
        } catch (RemoteException e) {

        }
    }

    public int getBlurRadius() {
        ensureService();
        try {
            return mService.getBlurRadius();
        } catch (RemoteException e) {

            return BlurSettings.BLUR_RADIUS;
        }
    }

    public void setBlurRadius(int r) {
        ensureService();
        try {
            mService.setBlurRadius(r);
        } catch (RemoteException e) {

        }
    }
}
