package github.tornaco.xposedmoduletest.xposed.submodules;

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

@Deprecated
class PMSSubModule3 extends AndroidSubModule {

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookScanPackageDirtyLI(lpparam);
    }

    private void hookScanPackageDirtyLI(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("hookScanPackageDirtyLI...");
        try {
            Class clz = XposedHelpers.findClass("com.android.server.pm.PackageManagerService",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(clz,
                    "scanPackageDirtyLI", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            try {
                                Object pkgObj = param.getResult();
                                String pkgName = (String) XposedHelpers.getObjectField(pkgObj, "packageName");
                                XposedLog.debug("scanPackageDirtyLI: " + pkgName);
//                                if (BuildConfig.APPLICATION_ID.equals(pkgName)) {
//                                    XposedLog.wtf("Set our app to core app.");
//                                    XposedHelpers.setObjectField(pkgObj, "coreApp", true);
//                                }
                            } catch (Throwable e) {
                                XposedLog.wtf("scanPackageDirtyLI: " + Log.getStackTraceString(e));
                            }
                        }
                    });
            XposedLog.verbose("hookScanPackageDirtyLI OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hookScanPackageDirtyLI:" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
