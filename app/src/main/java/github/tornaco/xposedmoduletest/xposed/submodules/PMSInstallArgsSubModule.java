package github.tornaco.xposedmoduletest.xposed.submodules;

import android.content.pm.PackageManager;
import android.os.Build;
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

class PMSInstallArgsSubModule extends AndroidSubModule {

    @Override
    public int needMinSdk() {
        return Build.VERSION_CODES.M;
    }

    @Override
    public String needBuildVar() {
        return XAppBuildVar.APP_PACKAGE_INSTALL_VERIFY;
    }

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookFileInstallArgs(lpparam);
        hookMoveInstallArgs(lpparam);
        hookAescInstallArgs(lpparam);
    }

    private void hookFileInstallArgs(XC_LoadPackage.LoadPackageParam lpparam) {
        logOnBootStage("PMSInstallArgsSubModule hookFileInstallArgs...");
        try {
            final Class ams = XposedHelpers.findClass("com.android.server.pm.PackageManagerService$FileInstallArgs",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(ams, "copyApk", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    if (!onCopyApk(param.thisObject)) {
                        param.setResult(PackageManager.INSTALL_FAILED_ABORTED);
                    }
                }
            });
            logOnBootStage("PMSInstallArgsSubModule hookFileInstallArgs OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Throwable e) {
            logOnBootStage("PMSInstallArgsSubModule Fail hookFileInstallArgs: " + Log.getStackTraceString(e));
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }

    private void hookAescInstallArgs(XC_LoadPackage.LoadPackageParam lpparam) {
        logOnBootStage("PMSInstallArgsSubModule hookAescInstallArgs...");
        try {
            final Class ams = XposedHelpers.findClass("com.android.server.pm.PackageManagerService$AescInstallArgs",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(ams, "copyApk", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    if (!onCopyApk(param.thisObject)) {
                        param.setResult(PackageManager.INSTALL_FAILED_ABORTED);
                    }
                }
            });
            logOnBootStage("PMSInstallArgsSubModule hookAescInstallArgs OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Throwable e) {
            logOnBootStage("PMSInstallArgsSubModule Fail hookAescInstallArgs: " + Log.getStackTraceString(e));
            // It's OK.
        }
    }

    private void hookMoveInstallArgs(XC_LoadPackage.LoadPackageParam lpparam) {
        logOnBootStage("PMSInstallArgsSubModule hookMoveInstallArgs...");
        try {
            final Class ams = XposedHelpers.findClass("com.android.server.pm.PackageManagerService$MoveInstallArgs",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(ams, "copyApk", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    if (!onCopyApk(param.thisObject)) {
                        param.setResult(PackageManager.INSTALL_FAILED_ABORTED);
                    }
                }
            });
            logOnBootStage("PMSInstallArgsSubModule hookMoveInstallArgs OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Throwable e) {
            logOnBootStage("PMSInstallArgsSubModule Fail hookMoveInstallArgs: " + Log.getStackTraceString(e));
            // It's OK.
        }
    }

    private boolean onCopyApk(Object argsFrom) {
        try {
            return getBridge().checkInstallApk(argsFrom);
        } catch (Throwable e) {
            XposedLog.wtf("PMSInstallArgsSubModule fail call bridge checkInstallApk: " + Log.getStackTraceString(e));
            return true; // :(
        }
    }
}
