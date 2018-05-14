package github.tornaco.xposedmoduletest.xposed.submodules;

import android.os.Build;
import android.util.Log;

import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.xposed.XAppBuildVar;
import github.tornaco.xposedmoduletest.xposed.service.doze.DeviceIdleControllerProxy;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

// Hook hookConstructor settings.
class DeviceIdleControllerSubModule extends AndroidSubModule {
    @Override
    public String needBuildVar() {
        return XAppBuildVar.APP_DOZE;
    }

    @Override
    public int needMinSdk() {
        return Build.VERSION_CODES.M;
    }

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookConstructor(lpparam);
    }

    private void hookConstructor(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("hookConstructor...");
        try {
            Class c = XposedHelpers.findClass("com.android.server.DeviceIdleController",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllConstructors(c, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    Object idleController = param.thisObject;
                    if (idleController != null) {
                        DeviceIdleControllerProxy proxy = new DeviceIdleControllerProxy(idleController);
                        getBridge().attachDeviceIdleController(proxy);
                    }
                }
            });
            XposedLog.verbose("hookConstructor OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hookConstructor: " + Log.getStackTraceString(e));
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
