package github.tornaco.xposedmoduletest.xposed.submodules;

import android.util.Log;

import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */
@Deprecated
class SystemServiceRegistryModule extends AndroidSubModule {

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookSystemServiceRegistry(lpparam);
    }

    private void hookSystemServiceRegistry(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("hookSystemServiceRegistry...");
        try {
            Class reg = XposedHelpers.findClass("android.app.SystemServiceRegistry",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(reg, "registerService", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    XposedLog.verbose("registerService-" + param.args[0]);
                }
            });
            XposedLog.verbose("hookSystemServiceRegistry OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hook hookSystemServiceRegistry");
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
