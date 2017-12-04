package github.tornaco.xposedmoduletest.xposed.service;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import de.robv.android.xposed.XposedBridge;
import github.tornaco.xposedmoduletest.xposed.util.XPosedLog;

/**
 * Created by guohao4 on 2017/11/9.
 * Email: Tornaco@163.com
 */

public class XAshmanServiceImplDev extends XAshmanServiceImpl {
    private interface Call {
        void onCall() throws Throwable;
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
        XPosedLog.debug(logMsg);
    }
}
