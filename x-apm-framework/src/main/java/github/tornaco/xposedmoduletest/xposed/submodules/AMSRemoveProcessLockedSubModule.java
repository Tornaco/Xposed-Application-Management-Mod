package github.tornaco.xposedmoduletest.xposed.submodules;

import android.content.pm.ApplicationInfo;
import android.util.Log;

import java.util.Arrays;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

// Listen for app process added.
class AMSRemoveProcessLockedSubModule extends AndroidSubModule {

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookRemoveProcessLocked(lpparam);
    }

    // LOS15.1
    //  boolean removeProcessLocked(ProcessRecord app,
    //            boolean callerWillRestart, boolean allowRestart, String reason) {}

    // LOS15.0
    //
    //    boolean removeProcessLocked(ProcessRecord app,
    //            boolean callerWillRestart, boolean allowRestart, String reason) {}

    // LOS14.1
    //  boolean removeProcessLocked(ProcessRecord app,
    //            boolean callerWillRestart, boolean allowRestart, String reason) {}

    // LOS14.0
    // boolean removeProcessLocked(ProcessRecord app,
    //            boolean callerWillRestart, boolean allowRestart, String reason) {}

    // LOS13
    //    private final boolean removeProcessLocked(ProcessRecord app,
    //            boolean callerWillRestart, boolean allowRestart, String reason) {}

    // LOS12.1
    //     private final boolean removeProcessLocked(ProcessRecord app,
    //            boolean callerWillRestart, boolean allowRestart, String reason) {}
    private void hookRemoveProcessLocked(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("hookRemoveProcessLocked...");
        try {
            Class ams = XposedHelpers.findClass("com.android.server.am.ActivityManagerService",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(ams, "removeProcessLocked",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);

                            if (BuildConfig.DEBUG) {
                                XposedLog.verbose("removeProcessLocked in ams: " + Arrays.toString(param.args));
                            }
                            Object processRecord = param.args[0];
                            boolean callerWillRestart = (boolean) param.args[1];
                            boolean allowRestart = (boolean) param.args[2];
                            String reason = (String) param.args[3];
                            if (processRecord != null) {
                                ApplicationInfo info = (ApplicationInfo) XposedHelpers.getObjectField(processRecord, "info");
                                if (BuildConfig.DEBUG) {
                                    XposedLog.verbose("removeProcessLocked in ams, info: " + info);
                                }
                                if (info != null) {
                                    getBridge().onRemoveProcessLocked(info, callerWillRestart, allowRestart, reason);
                                }
                            }
                        }
                    });
            XposedLog.verbose("hookRemoveProcessLocked OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hookRemoveProcessLocked: " + Log.getStackTraceString(e));
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
