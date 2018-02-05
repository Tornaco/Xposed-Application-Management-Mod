package github.tornaco.xposedmoduletest.xposed.submodules;

import android.content.pm.PackageInstaller;
import android.os.Binder;
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

class PackageInstallerServiceSubModule extends AndroidSubModule {
    @Override
    public String needBuildVar() {
        return XAppBuildVar.APP_OPS;
    }

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookCreateSession(lpparam);
    }

    private void hookCreateSession(XC_LoadPackage.LoadPackageParam lpparam) {
        logOnBootStage("PackageInstallerServiceSubModule hookCreateSession...");
        try {
            final Class ams = XposedHelpers.findClass("com.android.server.pm.PackageInstallerService",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(ams, "createSession", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    int callingUid = Binder.getCallingUid();
                    XposedLog.verbose("createSession to install package, caller: " + callingUid);
                    PackageInstaller.SessionParams s = (PackageInstaller.SessionParams) param.args[0];
                    String appLabel = s.appPackageName;
                    XposedLog.verbose("createSession for label: " + appLabel);
                }
            });
            logOnBootStage("PackageInstallerServiceSubModule hookCreateSession OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            logOnBootStage("PackageInstallerServiceSubModule Fail hookCreateSession: " + Log.getStackTraceString(e));
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
