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

class PMSSubModule3 extends IntentFirewallAndroidSubModule {

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookGetPersistApps(lpparam);
    }

    private void hookGetPersistApps(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("hookGetPersistApps...");
        try {
            Class clz = XposedHelpers.findClass("com.android.server.pm.PackageManagerService",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(clz,
                    "getPersistentApplicationsInternal", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            XposedLog.verbose("getPersistentApplicationsInternal: " + param.getResult());
                        }
                    });
            XposedLog.verbose("hookGetPersistApps OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hookGetPersistApps:" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
