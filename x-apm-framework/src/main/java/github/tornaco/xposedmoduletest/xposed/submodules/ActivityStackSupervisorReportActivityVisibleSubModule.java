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

class ActivityStackSupervisorReportActivityVisibleSubModule extends AndroidSubModule {
    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        // To make patch for P.
        if (OSUtil.isPOrAbove()) {
            hookReportActivityVisible(lpparam);
        }
    }

    private void hookReportActivityVisible(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("hookReportActivityVisible...");
        try {
            String clazzName = OSUtil.isQOrAbove()
                    ? "com.android.server.wm.ActivityStackSupervisor"
                    : "com.android.server.am.ActivityStackSupervisor";
            Class ams = XposedHelpers.findClass(clazzName, lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(ams, "reportActivityVisibleLocked", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    Object ar = param.args[0];
                    if (ar == null) return;
                    Intent intent = (Intent) XposedHelpers.getObjectField(ar, "intent");
                    if (intent == null) return;
                    getBridge().reportActivityLaunching(intent, "reportActivityVisibleLocked");
                }
            });
            logOnBootStage("hookReportActivityVisible OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            logOnBootStage("Fail hookReportActivityVisible: " + Log.getStackTraceString(e));
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
