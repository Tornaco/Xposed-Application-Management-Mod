package github.tornaco.xposedmoduletest.xposed.submodules;

import android.content.Intent;
import android.util.Log;

import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;
import github.tornaco.xposedmoduletest.xposed.util.XPosedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

// Hook hookSetFocusedActivityLocked settings.
class AMSSubModule5 extends AndroidSubModuleModule {

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookSetFocusedActivityLocked(lpparam);
    }

    private void hookSetFocusedActivityLocked(XC_LoadPackage.LoadPackageParam lpparam) {
        XPosedLog.verbose("hookSetFocusedActivityLocked...");
        try {
            Class ams = XposedHelpers.findClass("com.android.server.am.ActivityManagerService",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(ams, "setFocusedActivityLocked", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    Object ar = param.args[0];
                    if (ar==null) return;
                    Intent intent = (Intent) XposedHelpers.getObjectField(ar, "intent");
                    if (intent == null) return;
                    getBridge().onPackageMoveToFront(PkgUtil.packageNameOf(intent));
                }
            });
            XPosedLog.verbose("hookSetFocusedActivityLocked OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XPosedLog.verbose("Fail hookSetFocusedActivityLocked: " + Log.getStackTraceString(e));
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
