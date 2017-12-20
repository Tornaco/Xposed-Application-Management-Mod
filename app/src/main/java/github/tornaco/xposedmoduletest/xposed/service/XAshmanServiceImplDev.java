package github.tornaco.xposedmoduletest.xposed.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.RemoteException;
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
    public boolean checkRestartService(String packageName, ComponentName componentName) throws RemoteException {
        return super.checkRestartService(packageName, componentName);
    }

    @Override
    public boolean checkService(ComponentName serviceComp, int callerUid) {
        return super.checkService(serviceComp, callerUid);
    }

    @Override
    public boolean checkBroadcast(String action, int receiverUid, int callerUid) {
        return super.checkBroadcast(action, receiverUid, callerUid);
    }

    @Override
    public boolean checkComponentSetting(ComponentName componentName, int newState, int flags, int callingUid) {
        try {
            return super.checkComponentSetting(componentName, newState, flags, callingUid);
        } catch (Exception e) {
            onException(e);
            return true;
        }
    }

    @Override
    public void onActivityDestroy(Intent intent, String reason) {
        super.onActivityDestroy(intent, reason);
    }

    @Override
    protected Handler onCreateServiceHandler() {
        // Wrap the default handler into a new looper thread
        // this can decrease the performance drain of our service.
        HandlerThread hr = new HandlerThread("ASHMAN-H");
        hr.start();
        final Handler impl = super.onCreateServiceHandler();
        return new Handler(hr.getLooper(), new Handler.Callback() {
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
        // Wrap the default handler into a new looper thread
        // this can decrease the performance drain of our service.
        HandlerThread hr = new HandlerThread("ASHMAN-LAZY-H");
        hr.start();
        final Handler lazy = super.onCreateLazyHandler();
        return new Handler(hr.getLooper(), new Handler.Callback() {
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
