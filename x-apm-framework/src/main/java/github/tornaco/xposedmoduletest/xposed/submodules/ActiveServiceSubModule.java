package github.tornaco.xposedmoduletest.xposed.submodules;

import android.content.ComponentName;
import android.util.Log;

import java.util.Arrays;
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

// Hook hookRestartService settings.
class ActiveServiceSubModule extends AndroidSubModule {

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookRestartService(lpparam);
        hookActiveServicesConstructor(lpparam);
        hookBindService(lpparam);
    }

    private void hookBindService(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Class ams = XposedHelpers.findClass("com.android.server.am.ActiveServices",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(ams, "bindServiceLocked",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            int res = (int) param.getResult();
                            if (res < 0) {
                                XposedLog.boot("bindServiceLocked result < 0, we will fix it to 0.");
                                // Do not crash.
                                param.setResult(0);
                            }
                        }
                    });
            XposedLog.debug("bindServiceLocked, unhooks" + unHooks);
        } catch (Exception e) {
            XposedLog.wtf("bindServiceLocked error" + Log.getStackTraceString(e));
        }
    }

    private void hookRestartService(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("hookRestartService...");
        try {
            Class ams = XposedHelpers.findClass("com.android.server.am.ActiveServices",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(ams, "scheduleServiceRestartLocked",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            Object sr = param.args[0];
                            Object pkgNameObj = XposedHelpers.getObjectField(sr, "packageName");
                            if (pkgNameObj == null) return;
                            String pkgName = (String) pkgNameObj;
                            ComponentName componentName = (ComponentName) XposedHelpers.getObjectField(sr, "name");
                            if (!getBridge().checkRestartService(pkgName, componentName)) {
                                param.setResult(true);
                            }
                        }
                    });
            XposedLog.verbose("hookRestartService OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hookRestartService: " + Log.getStackTraceString(e));
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }

    private void hookActiveServicesConstructor(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("hookActiveServicesConstructor...");
        try {
            Class cla = XposedHelpers.findClass("com.android.server.am.ActiveServices",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllConstructors(cla, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    XposedLog.boot("ActiveServices construct: " + Arrays.toString(param.args));
                    Object activeServicesObj = param.thisObject;
//                    ActiveServicesProxy activeServicesProxy = new ActiveServicesProxy(activeServicesObj);
//                    getBridge().attachActiveServices(activeServicesProxy);
                }
            });
            XposedLog.verbose("hookActiveServicesConstructor OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hookActiveServicesConstructor: " + Log.getStackTraceString(e));
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
