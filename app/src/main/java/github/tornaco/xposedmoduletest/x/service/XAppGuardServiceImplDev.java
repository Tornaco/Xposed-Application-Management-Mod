package github.tornaco.xposedmoduletest.x.service;

import android.util.Log;

import de.robv.android.xposed.XposedBridge;
import github.tornaco.xposedmoduletest.x.util.XLog;

/**
 * Created by guohao4 on 2017/10/27.
 * Email: Tornaco@163.com
 */

class XAppGuardServiceImplDev extends XAppGuardServiceImpl {

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
}
