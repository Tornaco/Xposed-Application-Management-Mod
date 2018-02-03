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

class AMSSystemReadySubModule extends AndroidSubModule {
    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookAMSSystemReady(lpparam);
    }

    private void hookAMSSystemReady(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("hookAMSSystemReady...");
        try {
            Class ams = XposedHelpers.findClass("com.android.server.am.ActivityManagerService",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(ams, "systemReady", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    getBridge().systemReady();
                }
            });
            XposedLog.verbose("hookAMSSystemReady OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hookAMSSystemReady");
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }

    @Override
    public boolean isCoreModule() {
        return true;
    }
}
