package github.tornaco.xposedmoduletest.xposed.submodules;

import android.app.ActivityManagerNative;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
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

// Hook hookMoveActivityTaskToBack settings.
class AMSMoveTaskToBackSubModule extends AndroidSubModule {

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookMoveActivityTaskToBack(lpparam);
    }

    private void hookMoveActivityTaskToBack(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("hookMoveActivityTaskToBack...");
        try {
            Class ams = XposedHelpers.findClass("com.android.server.am.ActivityManagerService",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(ams, "moveActivityTaskToBack", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    IBinder token = (IBinder) param.args[0];
                    ComponentName comp = ActivityManagerNative.getDefault().getActivityClassForToken(token);
                    if (comp == null) return;
                    Intent intent = new Intent();
                    intent.setComponent(comp);
                    getBridge().onActivityDestroy(intent, "moveActivityTaskToBack");
                }
            });
            XposedLog.verbose("hookMoveActivityTaskToBack OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hookMoveActivityTaskToBack: " + Log.getStackTraceString(e));
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
