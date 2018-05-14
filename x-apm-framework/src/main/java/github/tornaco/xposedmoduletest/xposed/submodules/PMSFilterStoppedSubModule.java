package github.tornaco.xposedmoduletest.xposed.submodules;

import android.content.pm.PackageParser;
import android.util.Log;

import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.xposed.service.DebugOnly;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */
@DebugOnly
class PMSFilterStoppedSubModule extends AndroidSubModule {

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookActivityFilterStopped(lpparam);
    }

    private void hookActivityFilterStopped(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("hookActivityFilterStopped...");
        try {
            Class clz = XposedHelpers.findClass("com.android.server.pm.PackageManagerService$ActivityIntentResolver",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(clz,
                    "isFilterStopped", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            PackageParser.ActivityIntentInfo intentInfo = (PackageParser.ActivityIntentInfo) param.args[0];
                            XposedLog.verbose("isFilterStopped: %s, result: %s", intentInfo, param.getResult());
                        }
                    });
            XposedLog.verbose("hookActivityFilterStopped OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hookActivityFilterStopped:" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
