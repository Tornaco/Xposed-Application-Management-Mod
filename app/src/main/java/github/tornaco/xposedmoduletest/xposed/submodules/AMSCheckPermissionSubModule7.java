package github.tornaco.xposedmoduletest.xposed.submodules;

import android.Manifest;
import android.content.pm.PackageManager;
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

// Hook hookCheckPermission settings.
@Deprecated
class AMSCheckPermissionSubModule7 extends AndroidSubModule {

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookCheckPermission(lpparam);
    }

    private void hookCheckPermission(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("hookCheckPermission...");
        try {
            Class ams = XposedHelpers.findClass("com.android.server.am.ActivityManagerService",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(ams, "checkPermission", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);

                    // FIXME, Ensure this is default launcher.
                    String permission = (String) param.args[0];
                    if (permission.equals(Manifest.permission.START_ANY_ACTIVITY)) {
                        param.setResult(PackageManager.PERMISSION_GRANTED);
                        if (XposedLog.isVerboseLoggable()) {
                            XposedLog.debug("START_ANY_ACTIVITY ALLOWED!!!");
                        }
                    }
                }
            });
            XposedLog.verbose("hookCheckPermission OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hookCheckPermission: " + Log.getStackTraceString(e));
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
