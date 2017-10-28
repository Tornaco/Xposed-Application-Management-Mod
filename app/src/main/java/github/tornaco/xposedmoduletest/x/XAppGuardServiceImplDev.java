package github.tornaco.xposedmoduletest.x;

import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import de.robv.android.xposed.XposedBridge;
import github.tornaco.xposedmoduletest.IWatcher;

/**
 * Created by guohao4 on 2017/10/27.
 * Email: Tornaco@163.com
 */

public class XAppGuardServiceImplDev extends XAppGuardServiceImpl {

    private interface Call {
        void onCall() throws Throwable;
    }

    private void makeSafeCall(Call call) {
        try {
            call.onCall();
        } catch (Throwable e) {
            onException(e);
        }
    }

    private void onException(Throwable e) {
        String logMsg = "XAppGuard-ERROR:"
                + String.valueOf(e) + "\n"
                + Log.getStackTraceString(e);
        XposedBridge.log(logMsg);
        XLog.logD(logMsg);
    }

    @Override
    protected void enforceCallingPermissions() {
        // Skip permission check for DEV version.
        // super.enforceCallingPermissions();
        XLog.logF("Skip permission check for DEV version");
    }


    @Override
    protected void onMockCrash() {
        makeSafeCall(new Call() {
            @Override
            public void onCall() throws Throwable {
                XAppGuardServiceImplDev.super.onMockCrash();
            }
        });
    }

    @Override
    void verify(final Bundle options, final String pkg, final int uid, final int pid,
                final VerifyListener listener) {
        makeSafeCall(new Call() {
            @Override
            public void onCall() throws Throwable {
                XAppGuardServiceImplDev.super.verify(options, pkg, uid, pid, listener);
            }
        });
    }

    @Override
    public void setEnabled(final boolean enabled) throws RemoteException {
        makeSafeCall(new Call() {
            @Override
            public void onCall() throws Throwable {
                XAppGuardServiceImplDev.super.setEnabled(enabled);
            }
        });
    }

    @Override
    public void setVerifyOnScreenOff(final boolean ver) throws RemoteException {
        makeSafeCall(new Call() {
            @Override
            public void onCall() throws Throwable {
                XAppGuardServiceImplDev.super.setVerifyOnScreenOff(ver);
            }
        });
    }

    @Override
    public void setVerifyOnHome(final boolean ver) throws RemoteException {
        makeSafeCall(new Call() {
            @Override
            public void onCall() throws Throwable {
                XAppGuardServiceImplDev.super.setVerifyOnHome(ver);
            }
        });
    }

    @Override
    public void setBlur(final boolean blur) throws RemoteException {
        makeSafeCall(new Call() {
            @Override
            public void onCall() throws Throwable {
                XAppGuardServiceImplDev.super.setBlur(blur);
            }
        });
    }

    @Override
    public void setBlurPolicy(final int policy) throws RemoteException {
        makeSafeCall(new Call() {
            @Override
            public void onCall() throws Throwable {
                XAppGuardServiceImplDev.super.setBlurPolicy(policy);
            }
        });
    }

    @Override
    public void setBlurRadius(final int radius) throws RemoteException {
        makeSafeCall(new Call() {
            @Override
            public void onCall() throws Throwable {
                XAppGuardServiceImplDev.super.setBlurRadius(radius);
            }
        });
    }

    @Override
    public void setBlurScale(final float scale) throws RemoteException {
        makeSafeCall(new Call() {
            @Override
            public void onCall() throws Throwable {
                XAppGuardServiceImplDev.super.setBlurScale(scale);
            }
        });
    }

    @Override
    public void setAllow3rdVerifier(final boolean allow) throws RemoteException {
        makeSafeCall(new Call() {
            @Override
            public void onCall() throws Throwable {
                XAppGuardServiceImplDev.super.setAllow3rdVerifier(allow);
            }
        });
    }

    @Override
    public void setPasscode(final String passcode) throws RemoteException {
        makeSafeCall(new Call() {
            @Override
            public void onCall() throws Throwable {
                XAppGuardServiceImplDev.super.setPasscode(passcode);
            }
        });
    }

    @Override
    public void ignore(final String pkg) throws RemoteException {
        makeSafeCall(new Call() {
            @Override
            public void onCall() throws Throwable {
                XAppGuardServiceImplDev.super.ignore(pkg);
            }
        });
    }

    @Override
    public void pass(final String pkg) throws RemoteException {
        makeSafeCall(new Call() {
            @Override
            public void onCall() throws Throwable {
                XAppGuardServiceImplDev.super.pass(pkg);
            }
        });
    }

    @Override
    public void setResult(final int transactionID, final int res) {
        makeSafeCall(new Call() {
            @Override
            public void onCall() throws Throwable {
                XAppGuardServiceImplDev.super.setResult(transactionID, res);
            }
        });
    }

    @Override
    public void testUI() throws RemoteException {
        makeSafeCall(new Call() {
            @Override
            public void onCall() throws Throwable {
                XAppGuardServiceImplDev.super.testUI();
            }
        });
    }

    @Override
    public void addPackages(final String[] pkgs) throws RemoteException {
        makeSafeCall(new Call() {
            @Override
            public void onCall() throws Throwable {
                XAppGuardServiceImplDev.super.addPackages(pkgs);
            }
        });
    }

    @Override
    public void removePackages(final String[] pkgs) throws RemoteException {
        makeSafeCall(new Call() {
            @Override
            public void onCall() throws Throwable {
                XAppGuardServiceImplDev.super.removePackages(pkgs);
            }
        });
    }

    @Override
    public void watch(final IWatcher w) throws RemoteException {
        makeSafeCall(new Call() {
            @Override
            public void onCall() throws Throwable {
                XAppGuardServiceImplDev.super.watch(w);
            }
        });
    }

    @Override
    public void unWatch(final IWatcher w) throws RemoteException {
        makeSafeCall(new Call() {
            @Override
            public void onCall() throws Throwable {
                XAppGuardServiceImplDev.super.unWatch(w);
            }
        });
    }

    @Override
    public void forceWriteState() throws RemoteException {
        makeSafeCall(new Call() {
            @Override
            public void onCall() throws Throwable {
                XAppGuardServiceImplDev.super.forceWriteState();
            }
        });
    }

    @Override
    public void forceReadState() throws RemoteException {
        makeSafeCall(new Call() {
            @Override
            public void onCall() throws Throwable {
                XAppGuardServiceImplDev.super.forceReadState();
            }
        });
    }

    @Override
    void onHome() {
        makeSafeCall(new Call() {
            @Override
            public void onCall() throws Throwable {
                XAppGuardServiceImplDev.super.onHome();
            }
        });
    }
}
