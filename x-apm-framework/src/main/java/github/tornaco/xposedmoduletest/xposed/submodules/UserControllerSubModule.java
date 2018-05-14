package github.tornaco.xposedmoduletest.xposed.submodules;

import android.os.Binder;
import android.util.Log;

import java.util.Arrays;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.xposed.AppGlobals;
import github.tornaco.xposedmoduletest.xposed.service.DebugOnly;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

// Hook hookHandleIncomingUser settings.
@DebugOnly
class UserControllerSubModule extends AndroidSubModule {

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookHandleIncomingUser(lpparam);
    }

    private void hookHandleIncomingUser(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("hookHandleIncomingUser...");
        try {
            Class ams = XposedHelpers.findClass("com.android.server.am.UserController",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(ams, "handleIncomingUser",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            int callingUid = Binder.getCallingUid();
                            if (XposedLog.isVerboseLoggable()) {
                                XposedLog.verbose("beforeHookedMethod handleIncomingUser: " + Arrays.toString(param.args) + ", caller: "
                                        + callingUid);
                            }
                            if (callingUid == AppGlobals.getXAPMCUid()) {
                            }
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            int callingUid = Binder.getCallingUid();
                            if (XposedLog.isVerboseLoggable()) {
                                XposedLog.verbose("afterHookedMethod handleIncomingUser: " + Arrays.toString(param.args) + ", caller: "
                                        + callingUid + ", res: " + param.getResult());
                            }
                        }
                    });
            XposedLog.verbose("hookHandleIncomingUser OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hookHandleIncomingUser: " + Log.getStackTraceString(e));
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
