package github.tornaco.xposedmoduletest.xposed.submodules;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
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

// Hook hookSetFocusStackUnchecked settings.
// Only for Oreo.
//  void setFocusStackUnchecked(String reason, ActivityStack focusCandidate)
class ActivityStackSupervisorSetFocusedStackSubModule extends AndroidSubModule {

    @Override
    public int needMinSdk() {
        return Build.VERSION_CODES.O;
    }

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookSetFocusStackUnchecked(lpparam);
    }

    private void hookSetFocusStackUnchecked(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("setFocusStackUnchecked...");
        try {
            Class ams = XposedHelpers.findClass("com.android.server.am.ActivityStackSupervisor",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(ams, "setFocusStackUnchecked", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    String reason = (String) param.args[0];
                    Object activityStack = param.args[1];
                    Object taskRecord = XposedHelpers.callMethod(activityStack, "topTask");
                    Object realActivityObj = XposedHelpers.getObjectField(taskRecord, "realActivity");
                    if (realActivityObj != null) {
                        ComponentName componentName = (ComponentName) realActivityObj;
                        XposedLog.verbose("setFocusStackUnchecked:" + componentName);
                        Intent intent = new Intent();
                        intent.setComponent(componentName);
                        getBridge().onPackageMoveToFront(intent);
                    } else {
                        XposedLog.verbose("setFocusStackUnchecked, no realActivity obj");
                    }
                }
            });
            logOnBootStage("setFocusStackUnchecked OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            logOnBootStage("Fail setFocusStackUnchecked: " + Log.getStackTraceString(e));
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
