package github.tornaco.xposedmoduletest.xposed.service;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import de.robv.android.xposed.XposedBridge;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/27.
 * Email: Tornaco@163.com
 */

class XAppGuardServiceImplDev extends XAppGuardServiceImpl {

    private interface Call {
        void onCall() throws Throwable;
    }

    @Override
    public void attachContext(final Context context) {
        makeSafeCall(new XAppGuardServiceImplDev.Call() {
            @Override
            public void onCall() throws Throwable {
                XAppGuardServiceImplDev.super.attachContext(context);
            }
        });
    }

    @Override
    public void publish() {
        makeSafeCall(new XAppGuardServiceImplDev.Call() {
            @Override
            public void onCall() throws Throwable {
                XAppGuardServiceImplDev.super.publish();
            }
        });
    }

    @Override
    public void systemReady() {
        makeSafeCall(new XAppGuardServiceImplDev.Call() {
            @Override
            public void onCall() throws Throwable {
                XAppGuardServiceImplDev.super.systemReady();
            }
        });
    }

    @Override
    public void retrieveSettings() {
        makeSafeCall(new XAppGuardServiceImplDev.Call() {
            @Override
            public void onCall() throws Throwable {
                XAppGuardServiceImplDev.super.retrieveSettings();
            }
        });
    }

    @Override
    public void publishFeature(final String f) {
        makeSafeCall(new XAppGuardServiceImplDev.Call() {
            @Override
            public void onCall() throws Throwable {
                XAppGuardServiceImplDev.super.publishFeature(f);
            }
        });
    }

    @Override
    public void shutdown() {
        makeSafeCall(new XAppGuardServiceImplDev.Call() {
            @Override
            public void onCall() throws Throwable {
                XAppGuardServiceImplDev.super.shutdown();
            }
        });
    }

    @Override
    protected void enforceCallingPermissions() {
        // super.enforceCallingPermissions();
        XposedLog.verbose("Skip enforce permission on DEV version!");
    }

    @Override
    protected Handler onCreateServiceHandler() {
        final Handler impl = super.onCreateServiceHandler();
        return new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(final Message msg) {
                return makeSafeCall(new Call() {
                    @Override
                    public void onCall() throws Throwable {
                        impl.handleMessage(msg);
                    }
                });
            }
        });
    }

    private boolean makeSafeCall(Call call) {
        try {
            call.onCall();
            return true;
        } catch (Throwable e) {
            onException(e);
            return false;
        }
    }

    private void onException(Throwable e) {
        String logMsg = "XAppGuard-ERROR:"
                + String.valueOf(e) + "\n"
                + Log.getStackTraceString(e);
        XposedBridge.log(logMsg);
        XposedLog.debug(logMsg);
    }
}
