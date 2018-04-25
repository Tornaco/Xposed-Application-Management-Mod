package github.tornaco.xposedmoduletest.xposed.submodules;

import android.content.Intent;
import android.util.Log;

import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.util.OSUtil;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

// Hook hookSetFocusedActivityLocked settings.
// See {@#ActivityStackSupervisorSetFocusedStackSubModule}
@Deprecated
class AMSSetFocusedActivitySubModule extends AndroidSubModule {

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        // This only works on N and M.
        if (!(OSUtil.isOOrAbove() || OSUtil.isLOrBelow())) {
            hookSetFocusedActivityLocked(lpparam);
        }
    }

    private void hookSetFocusedActivityLocked(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("hookSetFocusedActivityLocked...");
        try {
            Class ams = XposedHelpers.findClass("com.android.server.am.ActivityManagerService",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(ams, "setFocusedActivityLocked", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    Object ar = param.args[0];
                    if (ar == null) return;
                    Intent intent = (Intent) XposedHelpers.getObjectField(ar, "intent");
                    if (intent == null) return;
                    getBridge().onPackageMoveToFront(intent);
                }
            });
            logOnBootStage("hookSetFocusedActivityLocked OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            logOnBootStage("Fail hookSetFocusedActivityLocked: " + Log.getStackTraceString(e));
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
