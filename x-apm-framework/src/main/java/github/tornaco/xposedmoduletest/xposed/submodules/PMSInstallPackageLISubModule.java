package github.tornaco.xposedmoduletest.xposed.submodules;

import android.content.pm.PackageManager;
import android.util.Log;

import java.util.Arrays;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.xposed.XAppBuildVar;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

class PMSInstallPackageLISubModule extends AndroidSubModule {

    @Override
    public String needBuildVar() {
        return XAppBuildVar.APP_PACKAGE_INSTALL_VERIFY;
    }

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        super.handleLoadingPackage(pkg, lpparam);
        hookInstallPackageLI(lpparam);
    }

    private void hookInstallPackageLI(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.boot(XposedLog.PREFIX_PM + "hookInstallPackageLI...");
        try {
            Class clz = XposedHelpers.findClass("com.android.server.pm.PackageManagerService", lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(clz,
                    "installPackageLI", new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            XposedLog.verbose(XposedLog.PREFIX_PM + "installPackageLI: " + Arrays.toString(param.args));
                            Object installArgsObj = param.args[0];
                            Object res = param.args[1];
                            if (res != null && !getBridge().checkInstallApk(installArgsObj)) {
                                XposedLog.verbose(XposedLog.PREFIX_PM + "installPackageLI INSTALL_FAILED_ABORTED");
                                XposedHelpers.callMethod(res, "setError", PackageManager.INSTALL_FAILED_ABORTED, "installPackageLI");
                                param.setResult(null);
                            }
                        }
                    });
            XposedLog.boot(XposedLog.PREFIX_PM + "hookInstallPackageLI OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.wtf(XposedLog.PREFIX_PM + "Fail hookInstallPackageLI:" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
