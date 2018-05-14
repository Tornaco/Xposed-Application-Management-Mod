package github.tornaco.xposedmoduletest.xposed.submodules;

import android.content.ComponentName;
import android.os.Binder;
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

class PMSSetComponentEnabledSettingSubModule extends AndroidSubModule {

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookPackageManagerServiceCompSetting(lpparam);
    }

    private void hookPackageManagerServiceCompSetting(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("hookPackageManagerServiceCompSetting...");
        try {
            Class clz = XposedHelpers.findClass("com.android.server.pm.PackageManagerService",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(clz,
                    "setComponentEnabledSetting", new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            ComponentName componentName = (ComponentName) param.args[0];
                            int callerUid = Binder.getCallingUid();
                            int state = (int) param.args[1];
                            // FIXME Retrieve params.
                            if (!getBridge().checkComponentSetting(componentName,
                                    state, 0, callerUid)) {
                                param.setResult(null);
                            }
                        }
                    });
            XposedLog.verbose("hookPackageManagerServiceCompSetting OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hookPackageManagerServiceCompSetting:" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
