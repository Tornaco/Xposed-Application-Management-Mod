package github.tornaco.xposedmoduletest.x.submodules;

import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageDeleteObserver2;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.x.app.XAppGuardManager;
import github.tornaco.xposedmoduletest.x.util.XLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

class PMSSubModule extends AndroidSubModuleModule {
    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookPackageManagerService(lpparam);
    }

    private void hookPackageManagerService(XC_LoadPackage.LoadPackageParam lpparam) {
        XLog.logV("hookPackageManagerService...");
        try {
            Class clz = XposedHelpers.findClass("com.android.server.pm.PackageManagerService",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(clz,
                    "deletePackageAsUser", new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            try {
                                String pkgName = (String) param.args[0];
                                XLog.logV("PackageManagerService deletePackageAsUser pkg:" + pkgName);
                                boolean interrupt = interruptPackageRemoval(pkgName);
                                if (interrupt) {
                                    Object oo = param.args[1];
                                    XLog.logV("deletePackageAsUser ob:" + oo);
                                    if (oo instanceof IPackageDeleteObserver2) {
                                        IPackageDeleteObserver2 observer2 = (IPackageDeleteObserver2) oo;
                                        observer2.onPackageDeleted(pkgName, PackageManager.DELETE_FAILED_ABORTED, null);
                                    } else if (oo instanceof IPackageDeleteObserver) {
                                        IPackageDeleteObserver observer = (IPackageDeleteObserver) oo;
                                        observer.packageDeleted(pkgName, PackageManager.DELETE_FAILED_ABORTED);
                                    }
                                    param.setResult(null);
                                    XLog.logV("PackageManagerService interruptPackageRemoval");
                                }
                            } catch (Exception e) {
                                XLog.logV("Fail deletePackageAsUser:" + e);
                            }
                        }
                    });
            XLog.logV("hookPackageManagerService OK:" + unHooks);
            getService().publishFeature(XAppGuardManager.Feature.HOME);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XLog.logV("Fail hookPackageManagerService:" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }


    private boolean interruptPackageRemoval(String pkgName) {
        return getService().interruptPackageRemoval(pkgName);
    }
}
