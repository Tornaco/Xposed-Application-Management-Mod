package github.tornaco.xposedmoduletest.xposed.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.RemoteException;
import android.util.Log;

import github.tornaco.xposedmoduletest.xposed.service.doze.DeviceIdleControllerProxy;
import github.tornaco.xposedmoduletest.xposed.service.notification.NotificationManagerServiceProxy;
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
        makeSafeCall(() -> XAshmanServiceImplDev.super.attachContext(context));
    }

    @Override
    public void publish() {
        makeSafeCall(XAshmanServiceImplDev.super::publish);
    }

    @Override
    public void systemReady() {
        makeSafeCall(XAshmanServiceImplDev.super::systemReady);
    }

    @Override
    public void attachDeviceIdleController(final DeviceIdleControllerProxy proxy) {
        makeSafeCall(() -> XAshmanServiceImplDev.super.attachDeviceIdleController(proxy));
    }

    @Override
    public void attachNotificationService(final NotificationManagerServiceProxy proxy) {
        makeSafeCall(() -> XAshmanServiceImplDev.super.attachNotificationService(proxy));
    }

    @Override
    public void onNetWorkManagementServiceReady(final NativeDaemonConnector connector) {
        makeSafeCall(() -> XAshmanServiceImplDev.super.onNetWorkManagementServiceReady(connector));
    }

    @Override
    public void retrieveSettings() {
        makeSafeCall(XAshmanServiceImplDev.super::retrieveSettings);
    }


    @Override
    public void shutdown() {
        makeSafeCall(XAshmanServiceImplDev.super::shutdown);
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
    public boolean checkService(Intent intent, ComponentName serviceComp, int callerUid) {
        return super.checkService(intent, serviceComp, callerUid);
    }

    @Override
    public boolean checkBroadcast(Intent action, int receiverUid, int callerUid) {
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
        return new Handler(hr.getLooper(), msg -> makeSafeCall(() -> impl.handleMessage(msg)));
    }

    @Override
    protected Handler onCreateLazyHandler() {
        // Wrap the default handler into a new looper thread
        // this can decrease the performance drain of our service.
        HandlerThread hr = new HandlerThread("ASHMAN-LAZY-H");
        hr.start();
        final Handler lazy = super.onCreateLazyHandler();
        return new Handler(hr.getLooper(), msg -> makeSafeCall(() -> lazy.handleMessage(msg)));
    }

    @Override
    protected Handler onCreateDozeHandler() {
        HandlerThread hr = new HandlerThread("ASHMAN-DOZE-H");
        hr.start();
        final Handler doze = super.onCreateDozeHandler();
        return new Handler(hr.getLooper(), msg -> makeSafeCall(() -> doze.handleMessage(msg)));
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
        String logMsg = "X-APM-ERROR:"
                + String.valueOf(e) + "\n"
                + Log.getStackTraceString(e);
        XposedLog.wtf(logMsg);
        Log.e("X-APM-ERROR", logMsg);
    }
}
