package github.tornaco.xposedmoduletest.x;

import android.os.RemoteException;
import android.os.ServiceManager;

import org.newstand.logger.Logger;

import github.tornaco.xposedmoduletest.IAppGuardService;

/**
 * Created by guohao4 on 2017/10/23.
 * Email: Tornaco@163.com
 */

@SuppressWarnings("WeakerAccess")
public class XAppGuardManager {

    static final String ACTION_APP_GUARD_VERIFY_DISPLAYER
            = "github.tornaco.xpose.app.guard.action.verify.displayer";

    static final String APP_GUARD_SERVICE = "user.appguard";

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

    public static final long TRANSACTION_EXPIRE_TIME = 60 * 1000;

    private static XAppGuardManager sMe;

    private IAppGuardService mService;

    private XAppGuardManager() {
        mService = IAppGuardService.Stub.asInterface(ServiceManager.getService(APP_GUARD_SERVICE));
        Logger.d("XAppGuardManager, service: " + mService);
    }

    static void init() {
        sMe = new XAppGuardManager();
    }

    public static XAppGuardManager from() {
        return sMe;
    }

    public boolean isServiceConnected() {
        Logger.v("Service connected: " + mService);
        return mService != null;
    }

    public boolean isEnabled() {
        if (!isServiceConnected()) return false;
        try {
            return mService.isEnabled();
        } catch (RemoteException e) {
            Logger.e(Logger.getStackTraceString(e));
        }
        return false;
    }

    public void setEnabled(boolean enabled) {
        if (!isServiceConnected()) return;
        try {
            mService.setEnabled(enabled);
        } catch (RemoteException e) {
            Logger.e(Logger.getStackTraceString(e));
        }
    }

    public void setResult(int transactionID, int res) {
        if (!isServiceConnected()) return;
        try {
            mService.setResult(transactionID, res);
        } catch (RemoteException e) {
            Logger.e(Logger.getStackTraceString(e));
        }
    }

    public void testUI() {
        if (!isServiceConnected()) return;
        try {
            mService.testUI();
        } catch (RemoteException e) {
            Logger.e(Logger.getStackTraceString(e));
        }
    }

    public void addPackages(String[] pkgs) {
        if (!isServiceConnected()) return;
        try {
            mService.addPackages(pkgs);
        } catch (RemoteException e) {
            Logger.e(Logger.getStackTraceString(e));
        }
    }

    public void removePackages(String[] pkgs) {
        if (!isServiceConnected()) return;
        try {
            mService.removePackages(pkgs);
        } catch (RemoteException e) {
            Logger.e(Logger.getStackTraceString(e));
        }
    }

    public void watch(XWatcherAdapter w) {
        if (!isServiceConnected()) return;
        try {
            mService.watch(w);
        } catch (RemoteException e) {
            Logger.e(Logger.getStackTraceString(e));
        }
    }

    public void unWatch(XWatcherAdapter w) {
        if (!isServiceConnected()) return;
        try {
            mService.unWatch(w);
        } catch (Exception e) {
            Logger.e(Logger.getStackTraceString(e));
        }
    }

    public void forceWriteState() {
        if (!isServiceConnected()) return;
        try {
            mService.forceWriteState();
        } catch (RemoteException e) {
            Logger.e(Logger.getStackTraceString(e));
        }
    }

    public void forceReadState() {
        if (!isServiceConnected()) return;
        try {
            mService.forceReadState();
        } catch (RemoteException e) {
            Logger.e(Logger.getStackTraceString(e));
        }
    }

    public String[] getPackages() {
        if (!isServiceConnected()) return new String[0];
        try {
            return mService.getPackages();
        } catch (RemoteException e) {
            Logger.e(Logger.getStackTraceString(e));
        }
        return new String[0];
    }

    public int getStatus() {
        if (!isServiceConnected()) return XStatus.UNKNOWN.ordinal();
        try {
            return mService.getStatus();
        } catch (RemoteException e) {
            Logger.e(Logger.getStackTraceString(e));
        }
        return XStatus.UNKNOWN.ordinal();
    }

    public boolean isBlur() {
        if (!isServiceConnected()) return false;
        try {
            return mService.isBlur();
        } catch (RemoteException e) {
            Logger.e(Logger.getStackTraceString(e));
        }
        return false;
    }

    public void setBlur(boolean blur) {
        if (!isServiceConnected()) return;
        try {
            mService.setBlur(blur);
        } catch (RemoteException e) {
            Logger.e(Logger.getStackTraceString(e));
        }
    }

    public void ignore(String pkg) {
        if (!isServiceConnected()) return;
        try {
            mService.ignore(pkg);
        } catch (RemoteException e) {
            Logger.e(Logger.getStackTraceString(e));
        }
    }

    public void pass(String pkg) {
        if (!isServiceConnected()) return;
        try {
            mService.pass(pkg);
        } catch (RemoteException e) {
            Logger.e(Logger.getStackTraceString(e));
        }
    }

    public void setBlurPolicy(int policy) {
        if (!isServiceConnected()) return;
        try {
            mService.setBlurPolicy(policy);
        } catch (RemoteException e) {
            Logger.e(Logger.getStackTraceString(e));
        }
    }

    public int getBlurPolicy() {
        if (!isServiceConnected()) return BlurPolicy.BLUR_POLICY_UNKNOWN;
        try {
            return mService.getBlurPolicy();
        } catch (RemoteException e) {
            Logger.e(Logger.getStackTraceString(e));
        }
        return BlurPolicy.BLUR_POLICY_UNKNOWN;
    }

    public void setBlurRadius(int radius) {
        if (!isServiceConnected()) return;
        try {
            mService.setBlurRadius(radius);
        } catch (RemoteException e) {
            Logger.e(Logger.getStackTraceString(e));
        }
    }

    public int getBlurRadius() {
        if (!isServiceConnected()) return -1;
        try {
            return mService.getBlurRadius();
        } catch (RemoteException e) {
            Logger.e(Logger.getStackTraceString(e));
        }
        return -1;
    }

    public void setBlurScale(float scale) {
        if (!isServiceConnected()) return;
        try {
            mService.setBlurScale(scale);
        } catch (RemoteException e) {
            Logger.e(Logger.getStackTraceString(e));
        }
    }

    public float getBlurScale() {
        if (!isServiceConnected()) return -1;
        try {
            return mService.getBlurScale();
        } catch (RemoteException e) {
            Logger.e(Logger.getStackTraceString(e));
        }
        return -1;
    }

    public boolean hasFeature(String feature) {
        if (!isServiceConnected()) return false;
        try {
            return mService.hasFeature(feature);
        } catch (RemoteException e) {
            Logger.e(Logger.getStackTraceString(e));
        }
        return false;
    }

    public void setAllow3rdVerifier(boolean allow) {
        if (!isServiceConnected()) return;
        try {
            mService.setAllow3rdVerifier(allow);
        } catch (RemoteException e) {
            Logger.e(Logger.getStackTraceString(e));
        }
    }

    public boolean isAllow3rdVerifier() {
        if (!isServiceConnected()) return false;
        try {
            return mService.isAllow3rdVerifier();
        } catch (RemoteException e) {
            Logger.e(Logger.getStackTraceString(e));
        }
        return false;
    }

    public void setPasscode(String passcode) {
        if (!isServiceConnected()) return;
        try {
            mService.setPasscode(passcode);
        } catch (RemoteException e) {
            Logger.e(Logger.getStackTraceString(e));
        }
    }

    public String getPasscode() {
        if (!isServiceConnected()) return null;
        try {
            return mService.getPasscode();
        } catch (RemoteException e) {
            Logger.e(Logger.getStackTraceString(e));
        }
        return null;
    }

    public void setVerifyOnScreenOff(boolean ver) {
        if (!isServiceConnected()) return;
        try {
            mService.setVerifyOnScreenOff(ver);
        } catch (RemoteException e) {
            Logger.e(Logger.getStackTraceString(e));
        }
    }

    public boolean isVerifyOnScreenOff() {
        if (!isServiceConnected()) return false;
        try {
            return mService.isVerifyOnScreenOff();
        } catch (RemoteException e) {
            Logger.e(Logger.getStackTraceString(e));
        }
        return false;
    }

    public void setVerifyOnHome(boolean ver) {
        if (!isServiceConnected()) return;
        try {
            mService.setVerifyOnHome(ver);
        } catch (RemoteException e) {
            Logger.e(Logger.getStackTraceString(e));
        }
    }

    public boolean isVerifyOnHome() {
        if (!isServiceConnected()) return false;
        try {
            return mService.isVerifyOnHome();
        } catch (RemoteException e) {
            Logger.e(Logger.getStackTraceString(e));
        }
        return false;
    }

    public void mockCrash() {
        if (!isServiceConnected()) return;
        try {
            mService.mockCrash();
        } catch (RemoteException ignored) {
        }
    }
}
