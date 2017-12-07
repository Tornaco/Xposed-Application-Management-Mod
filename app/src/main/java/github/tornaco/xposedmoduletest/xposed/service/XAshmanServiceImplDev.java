package github.tornaco.xposedmoduletest.xposed.service;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import de.robv.android.xposed.XposedBridge;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/11/9.
 * Email: Tornaco@163.com
 */

public class XAshmanServiceImplDev extends XAshmanServiceImpl {

    private interface Call {
        void onCall() throws Throwable;
    }

    @Override
    public void attachContext(final Context context) {
        makeSafeCall(new Call() {
            @Override
            public void onCall() throws Throwable {
                XAshmanServiceImplDev.super.attachContext(context);
            }
        });
    }

    @Override
    public void publish() {
        makeSafeCall(new Call() {
            @Override
            public void onCall() throws Throwable {
                XAshmanServiceImplDev.super.publish();
            }
        });
    }

    @Override
    public void systemReady() {
        makeSafeCall(new Call() {
            @Override
            public void onCall() throws Throwable {
                XAshmanServiceImplDev.super.systemReady();
            }
        });
    }

    @Override
    public void onNetWorkManagementServiceReady(final NativeDaemonConnector connector) {
        makeSafeCall(new Call() {
            @Override
            public void onCall() throws Throwable {
                XAshmanServiceImplDev.super.onNetWorkManagementServiceReady(connector);
            }
        });
    }

    @Override
    public void retrieveSettings() {
        makeSafeCall(new Call() {
            @Override
            public void onCall() throws Throwable {
                XAshmanServiceImplDev.super.retrieveSettings();
            }
        });
    }

    @Override
    public void publishFeature(final String f) {
        makeSafeCall(new Call() {
            @Override
            public void onCall() throws Throwable {
                XAshmanServiceImplDev.super.publishFeature(f);
            }
        });
    }

    @Override
    public void shutdown() {
        makeSafeCall(new Call() {
            @Override
            public void onCall() throws Throwable {
                XAshmanServiceImplDev.super.shutdown();
            }
        });
    }

    @Override
    protected void enforceCallingPermissions() {
        // super.enforceCallingPermissions();
    }

    @Override
    protected Handler onCreateServiceHandler() {
        final Handler impl = super.onCreateServiceHandler();
        return new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(final Message msg) {
                return makeSafeCall(new XAshmanServiceImplDev.Call() {
                    @Override
                    public void onCall() throws Throwable {
                        impl.handleMessage(msg);
                    }
                });
            }
        });
    }

    @Override
    protected Handler onCreateLazyHandler() {
        final Handler lazy = super.onCreateLazyHandler();
        return new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(final Message msg) {
                return makeSafeCall(new Call() {
                    @Override
                    public void onCall() throws Throwable {
                        lazy.handleMessage(msg);
                    }
                });
            }
        });
    }

    private boolean makeSafeCall(XAshmanServiceImplDev.Call call) {
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
