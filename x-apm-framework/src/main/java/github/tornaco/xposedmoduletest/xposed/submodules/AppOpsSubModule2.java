package github.tornaco.xposedmoduletest.xposed.submodules;

import android.app.AppOpsManager;
import android.util.Log;

import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.xposed.XAppBuildVar;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

// Hook hookCheckOp settings.
class AppOpsSubModule2 extends AndroidSubModule {
    @Override
    public String needBuildVar() {
        return XAppBuildVar.APP_OPS;
    }

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookCheckOp(lpparam);
    }

    private void hookCheckOp(XC_LoadPackage.LoadPackageParam lpparam) {
        logOnBootStage("AppOpsSubModule2 hookCheckOp...");
        try {
            Class ams = XposedHelpers.findClass("com.android.server.AppOpsService",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(ams, "startOperation", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);

                    boolean permCtrlEnabled = getBridge().isPermissionControlEnabled();
                    if (!permCtrlEnabled) {
                        return;
                    }

                    int code = (int) param.args[1];
                    int uid = (int) param.args[2];
                    String pkgName = (String) param.args[3];
                    int mode = getBridge().checkOperation(code, uid, pkgName, null);
                    if (mode == AppOpsManager.MODE_IGNORED) {
                        param.setResult(AppOpsManager.MODE_IGNORED);
                    }
                }
            });
            logOnBootStage("AppOpsSubModule2 hookCheckOp OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            logOnBootStage("AppOpsSubModule2 Fail hookCheckOp: " + Log.getStackTraceString(e));
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
