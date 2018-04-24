package github.tornaco.xposedmoduletest.xposed.submodules.debug;

import android.content.pm.UserInfo;
import android.util.Log;

import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.xposed.service.multipleapps.MultipleAppsManager;
import github.tornaco.xposedmoduletest.xposed.submodules.AndroidSubModule;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by Tornaco on 2018/4/23 17:30.
 * God bless no bug!
 */
public class UserManagerServiceSubModule extends AndroidSubModule {
    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookIsProfileOf(lpparam);
    }

    private void hookIsProfileOf(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("hookIsProfileOf...");
        try {
            Class ams = XposedHelpers.findClass("com.android.server.pm.UserManagerService",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(ams, "isProfileOf",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            XposedLog.verbose("hookIsProfileOf: " + param.args[0]);
                            UserInfo userInfo = (UserInfo) param.args[0];
                            if (MultipleAppsManager.MULTIPLE_APPS_USER_NAME.equals(userInfo.name)) {
                                param.setResult(true);
                            }
                        }
                    });
            XposedLog.verbose("hookIsProfileOf OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hookIsProfileOf: " + Log.getStackTraceString(e));
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
