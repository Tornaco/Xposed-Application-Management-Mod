package github.tornaco.xposedmoduletest.xposed.submodules;

import android.util.Log;

import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.xposed.XAppBuildVar;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

class PMSClearDataSubModule extends AndroidSubModule {

    @Override
    public String needBuildVar() {
        return XAppBuildVar.APP_DATA_CLEAR;
    }

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookClearApplicationUserData(lpparam);
    }

    private void hookClearApplicationUserData(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("hookClearApplicationUserData...");
        try {
            Class clz = XposedHelpers.findClass("com.android.server.pm.PackageManagerService",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(clz,
                    "clearApplicationUserData", new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            try {
                                String pkgName = (String) param.args[0];
                                XposedLog.debug("clearApplicationUserData: " + pkgName);
                                boolean interrupt = getBridge().interruptPackageDataClear(pkgName);
                                if (interrupt) {
                                    param.setResult(null);
                                    XposedLog.verbose("clearApplicationUserData interrupt");
                                    getBridge().notifyPackageDataClearInterrupt(pkgName);
                                }
                            } catch (Throwable e) {
                                XposedLog.wtf("scanPackageDirtyLI: " + Log.getStackTraceString(e));
                            }
                        }
                    });
            XposedLog.verbose("hookClearApplicationUserData OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hookClearApplicationUserData:" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
