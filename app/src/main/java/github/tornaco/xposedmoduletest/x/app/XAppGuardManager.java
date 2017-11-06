package github.tornaco.xposedmoduletest.x.app;

import android.os.RemoteException;
import android.os.ServiceManager;

import com.google.common.base.Preconditions;

import org.newstand.logger.Logger;

import github.tornaco.xposedmoduletest.IAppGuardService;
import github.tornaco.xposedmoduletest.IWatcher;
import github.tornaco.xposedmoduletest.x.bean.BlurSettings;
import github.tornaco.xposedmoduletest.x.bean.VerifySettings;

/**
 * Created by guohao4 on 2017/10/23.
 * Email: Tornaco@163.com
 */

@SuppressWarnings("WeakerAccess")
public class XAppGuardManager {

    public static final String META_DATA_KEY_APP_GUARD_VERIFY_DISPLAYER = "app_guard_verify_displayer";

    public static final String ACTION_APP_GUARD_VERIFY_DISPLAYER
            = "github.tornaco.xpose.app.interruptPackageRemoval.action.verify.displayer";

    public static final String APP_GUARD_SERVICE = "user.appguard";

    public static final String EXTRA_PKG_NAME = "extra.pkg";
    public static final String EXTRA_TRANS_ID = "extra.tid";

    @SuppressWarnings("WeakerAccess")
    public interface Feature {
        String BASE = "feature.base";
        String START = "feature.start";
        String RECENT = "feature.recent";
        String FP = "feature.fp";
        String BLUR = "feature.blur";
        String HOME = "feature.home";
        int FEATURE_COUNT = 3;
    }

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

    private static XAppGuardManager sMe;

    private IAppGuardService mService;

    private XAppGuardManager() {
        mService = IAppGuardService.Stub.asInterface(ServiceManager.getService(APP_GUARD_SERVICE));
        Logger.d("XAppGuardManager, service: " + mService);
    }

    public static void init() {
        sMe = new XAppGuardManager();
    }

    public static XAppGuardManager from() {
        return sMe;
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
            return mService.isEnabled();
        } catch (RemoteException e) {
            Logger.e(Logger.getStackTraceString(e));
        }
        return false;
    }

    public void setEnabled(boolean enabled) {
        ensureService();
        try {
            mService.setEnabled(enabled);
        } catch (RemoteException e) {
            Logger.e(Logger.getStackTraceString(e));
        }
    }


    public void setVerifySettings(VerifySettings settings) {
        ensureService();
        try {
            mService.setVerifySettings(settings);
        } catch (RemoteException ignored) {

        }
    }

    public VerifySettings getVerifySettings() {
        ensureService();
        try {
            return mService.getVerifySettings();
        } catch (RemoteException ignored) {
            return null;
        }
    }

    public void setBlurSettings(BlurSettings settings) {
        ensureService();
        try {
            mService.setBlurSettings(settings);
        } catch (RemoteException ignored) {

        }
    }

    public BlurSettings getBlurSettings() {
        ensureService();
        try {
            return mService.getBlurSettings();
        } catch (RemoteException ignored) {
            return null;
        }
    }

    public void watch(IWatcher w) {
        ensureService();
        try {
            mService.watch(w);
        } catch (RemoteException ignored) {

        }
    }

    public void unWatch(IWatcher w) throws RemoteException {
        ensureService();
        try {
            mService.unWatch(w);
        } catch (RemoteException ignored) {

        }
    }

    public void setResult(int transactionID, int res) {
        ensureService();
        try {
            mService.setResult(transactionID, res);
        } catch (RemoteException e) {
            Logger.e(Logger.getStackTraceString(e));
        }
    }

    public void watch(XWatcherAdapter w) {
        ensureService();
        try {
            mService.watch(w);
        } catch (RemoteException e) {
            Logger.e(Logger.getStackTraceString(e));
        }
    }

    public void unWatch(XWatcherAdapter w) {
        ensureService();
        try {
            mService.unWatch(w);
        } catch (Exception e) {
            Logger.e(Logger.getStackTraceString(e));
        }
    }

    public void mockCrash() {
        ensureService();
        try {
            mService.mockCrash();
        } catch (RemoteException ignored) {
        }
    }

    public boolean isUninstallInterruptEnabled() {
        ensureService();
        try {
            return mService.isUninstallInterruptEnabled();
        } catch (RemoteException e) {
            Logger.e(Logger.getStackTraceString(e));
        }
        return false;
    }

    public void setUninstallInterruptEnabled(boolean enabled) {
        ensureService();
        try {
            mService.setUninstallInterruptEnabled(enabled);
        } catch (RemoteException e) {
            Logger.e(Logger.getStackTraceString(e));
        }
    }
}
