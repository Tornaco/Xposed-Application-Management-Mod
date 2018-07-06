package github.tornaco.xposedmoduletest.xposed.submodules;

import android.util.Log;

import java.util.Arrays;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.compat.os.XAppOpsManager;
import github.tornaco.xposedmoduletest.xposed.GlobalWhiteList;
import github.tornaco.xposedmoduletest.xposed.XAppBuildVar;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

class PowerManagerServiceLocalSubModule extends AndroidSubModule {

    @Override
    public String needBuildVar() {
        return XAppBuildVar.APP_OPS;
    }

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookLocalService(lpparam);
    }

    private void hookLocalService(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("hookLocalService...");
        try {
            Class clz = XposedHelpers.findClass("com.android.server.power.PowerManagerService$LocalService",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(clz,
                    "setScreenBrightnessOverrideFromWindowManager", new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);

                            XAPMManager ash = XAPMManager.get();
                            if (ash.isServiceAvailable()) {

                                boolean permCtrlEnabled = ash.isPermissionControlEnabled();
                                if (!permCtrlEnabled) {
                                    return;
                                }

                                String currentPkg = ash.getCurrentTopPackage();

                                if (currentPkg != null && !GlobalWhiteList.isInGlobalWhiteList(currentPkg)) {
                                    int mode = ash.getPermissionControlBlockModeForPkg(
                                            XAppOpsManager.OP_CHANGE_BRIGHTNESS,
                                            currentPkg,
                                            true,
                                            new String[]{Arrays.toString(param.args)});

                                    Log.d(XposedLog.TAG + XposedLog.PREFIX_OPS,
                                            String.format("setScreenBrightnessOverrideFromWindowManager: %s %s %s",
                                                    Arrays.toString(param.args), currentPkg, mode));

                                    if (mode == XAppOpsManager.MODE_IGNORED) {
                                        Log.d(XposedLog.TAG + XposedLog.PREFIX_OPS, "Block setScreenBrightnessOverrideFromWindowManager");
                                        param.setResult(null);
                                    }
                                }
                            }
                        }
                    });
            XposedLog.verbose("hookLocalService OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hookLocalService:" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
