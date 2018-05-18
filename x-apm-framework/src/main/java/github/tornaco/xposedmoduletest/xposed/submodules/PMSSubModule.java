package github.tornaco.xposedmoduletest.xposed.submodules;

import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageDeleteObserver2;
import android.content.pm.PackageManager;
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

class PMSSubModule extends AndroidSubModule {
    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookPackageManagerService(lpparam);
    }

    private void hookPackageManagerService(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("hookPackageManagerService...");
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
                                XposedLog.verbose("PackageManagerService deletePackageAsUser pkg:" + pkgName);
                                boolean interrupt = interruptPackageRemoval(pkgName);
                                if (interrupt) {
                                    Object oo = param.args[OSUtil.isOOrAbove() ? 2 : 1];
                                    XposedLog.verbose("deletePackageAsUser ob:" + oo);
                                    if (oo instanceof IPackageDeleteObserver2) {
                                        IPackageDeleteObserver2 observer2 = (IPackageDeleteObserver2) oo;
                                        observer2.onPackageDeleted(pkgName, PackageManager.DELETE_FAILED_ABORTED, null);
                                    } else if (oo instanceof IPackageDeleteObserver) {
                                        IPackageDeleteObserver observer = (IPackageDeleteObserver) oo;
                                        observer.packageDeleted(pkgName, PackageManager.DELETE_FAILED_ABORTED);
                                    }
                                    param.setResult(null);
                                    XposedLog.verbose("PackageManagerService interruptPackageRemoval");

                                    getBridge().notifyPackageRemovalInterrupt(pkgName);
                                }
                            } catch (Exception e) {
                                XposedLog.verbose("Fail deletePackageAsUser:" + e);
                            }
                        }
                    });
            XposedLog.verbose("hookPackageManagerService OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hookPackageManagerService:" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }


    private boolean interruptPackageRemoval(String pkgName) {
        return getBridge().interruptPackageRemoval(pkgName);
    }
}
