package github.tornaco.xposedmoduletest.xposed.service.am;

import android.content.ComponentName;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import github.tornaco.xposedmoduletest.IServiceControl;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by Tornaco on 2018/4/28 9:45.
 * God bless no bug!
 */
public class AppServiceController {

    private final RemoteCallbackList<IServiceControl> mServiceControls = new RemoteCallbackList<>();

    public void registerController(IServiceControl control) {
        if (isValidControl(control)) {
            try {
                mServiceControls.register(control);
                if (XposedLog.isVerboseLoggable()) {
                    XposedLog.verbose(XposedLog.TAG_LAZY + "AppServiceController register for: " + control.getServiceComponent());
                }
            } catch (Exception ignored) {
                // Bad service?
                XposedLog.wtf(XposedLog.TAG_LAZY + "AppServiceController Fail registerController: " + Log.getStackTraceString(ignored));
            }
        } else {
            XposedLog.wtf(XposedLog.TAG_LAZY + "AppServiceController Bad control, won't register: " + control);
        }
    }

    public void unRegisterController(IServiceControl control) {
        if (isValidControl(control)) {
            try {
                mServiceControls.unregister(control);
                if (XposedLog.isVerboseLoggable()) {
                    XposedLog.verbose(XposedLog.TAG_LAZY + "AppServiceController unregister: " + control.getServiceComponent());
                }
            } catch (Exception ignored) {
                // Bad service?
                XposedLog.wtf(XposedLog.TAG_LAZY + "AppServiceController Fail unRegisterController: " + Log.getStackTraceString(ignored));
            }
        } else {
            XposedLog.wtf(XposedLog.TAG_LAZY + "AppServiceController Bad control, won't un-register: " + control);
        }
    }

    public void stopAppService(String servicePackageName, AppServiceControlServiceStopper serviceStopper) {
        if (servicePackageName != null) {
            if (XposedLog.isVerboseLoggable()) {
                XposedLog.verbose(XposedLog.TAG_LAZY + "AppServiceController called @stopAppService: " + servicePackageName);
            }
            try {
                int itemCount = mServiceControls.beginBroadcast();
                for (int i = 0; i < itemCount; i++) {
                    try {
                        IServiceControl control = mServiceControls.getBroadcastItem(i);
                        ComponentName controlName = control.getServiceComponent();
                        String controlPackageName = controlName.getPackageName();
                        if (XposedLog.isVerboseLoggable()) {
                            XposedLog.verbose(XposedLog.TAG_LAZY + "AppServiceController @stopAppService checking: %s target: %s",
                                    controlName, servicePackageName);
                        }
                        if (servicePackageName.equals(controlPackageName)) {
                            if (XposedLog.isVerboseLoggable()) {
                                XposedLog.verbose(XposedLog.TAG_LAZY + "AppServiceController matched @stopService: %s target: %s",
                                        controlName, servicePackageName);
                            }
                            // UnRegister.
                            if (serviceStopper.stopService(control)) {
                                unRegisterController(control);
                            }
                        }
                    } catch (Throwable ignored) {
                        // We tried...
                    }
                }
            } catch (Throwable e) {
                XposedLog.wtf(XposedLog.TAG_LAZY + "AppServiceController stopAppService broadcast fail: " + Log.getStackTraceString(e));
            } finally {
                mServiceControls.finishBroadcast();
                // If dead, go dead!!!!!
            }
        }
    }

    private static boolean isValidControl(IServiceControl control) {
        try {
            return control != null && control.getServiceComponent() != null;
        } catch (RemoteException e) {
            return false;
        }
    }
}
