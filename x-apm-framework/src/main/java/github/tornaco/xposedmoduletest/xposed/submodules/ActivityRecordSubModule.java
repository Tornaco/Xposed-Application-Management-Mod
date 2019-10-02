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

class ActivityRecordSubModule extends AndroidSubModule {

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookStartLaunchTickingLocked(lpparam);
    }

    private void hookStartLaunchTickingLocked(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("hookStartLaunchTickingLocked...");
        try {
            String clazzName = OSUtil.isQOrAbove()
                    ? "com.android.server.wm.ActivityRecord"
                    : "com.android.server.am.ActivityRecord";
            Class c = XposedHelpers.findClass(clazzName, lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(c, "startLaunchTickingLocked", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    Object ar = param.thisObject;
                    Intent intent = (Intent) XposedHelpers.getObjectField(ar, "intent");
                    if (intent != null) {
                        getBridge().reportActivityLaunching(intent, "startLaunchTickingLocked");
                    }
                }
            });
            XposedLog.verbose("hookStartLaunchTickingLocked OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hookStartLaunchTickingLocked: " + Log.getStackTraceString(e));
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
