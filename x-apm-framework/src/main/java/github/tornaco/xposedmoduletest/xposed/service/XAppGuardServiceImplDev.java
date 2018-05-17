package github.tornaco.xposedmoduletest.xposed.service;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import de.robv.android.xposed.XposedBridge;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/27.
 * Email: Tornaco@163.com
 */

class XAppGuardServiceImplDev extends XAppGuardServiceImpl {

    public XAppGuardServiceImplDev(XAshmanServiceImpl mService) {
        super(mService);
    }

    private interface Call {
        void onCall() throws Throwable;
    }

    @Override
    public void attachContext(final Context context) {
        makeSafeCall(() -> XAppGuardServiceImplDev.super.attachContext(context));
    }

    @Override
    public void publish() {
        makeSafeCall(XAppGuardServiceImplDev.super::publish);
    }

    @Override
    public void systemReady() {
        makeSafeCall(XAppGuardServiceImplDev.super::systemReady);
    }

    @Override
    public void retrieveSettings() {
        makeSafeCall(XAppGuardServiceImplDev.super::retrieveSettings);
    }

    @Override
    public void shutdown() {
        makeSafeCall(XAppGuardServiceImplDev.super::shutdown);
    }

    @Override
    protected void enforceCallingPermissions() {
        // super.enforceCallingPermissions();
        XposedLog.verbose("Skip enforce permission on DEV version!");
    }

    @Override
    protected Handler onCreateServiceHandler() {
        // Wrap the default handler into a new looper thread
        // this can decrease the performance drain of our service.
        HandlerThread hr = new HandlerThread("APP-GUARD-H");
        hr.start();
        final Handler impl = super.onCreateServiceHandler();
        return new Handler(hr.getLooper(), msg -> makeSafeCall(() -> impl.handleMessage(msg)));
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
