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
import github.tornaco.xposedmoduletest.util.OSUtil;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

// Listen for app process added.
class AMSStartProcessLockedSubModule extends AndroidSubModule {

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookStartProcessLocked(lpparam);
    }

    private void hookStartProcessLocked(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("hookStartProcessLocked...");
        try {
            Class ams = XposedHelpers.findClass("com.android.server.am.ActivityManagerService",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(ams, "startProcessLocked",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            Object processRecord = param.args[0];
                            // We only hook the method with ProcessRecord param class.
                            if (processRecord instanceof String) return;
                            if (!processRecord.getClass().getName().contains("ProcessRecord")) {
                                return;
                            }
                            // Check args.
                            // Multiple method exists.
                            // This is the fastest way to make it as compat on all platform.
                            // I have no time to check M/O/L, this is based on N.
                            // Sorry:(
                            if (param.args.length <= 4) {
                                return;
                            }
                            if (BuildConfig.DEBUG) {
                                XposedLog.verbose("startProcessLocked in ams, processRecord: "
                                        + Arrays.toString(param.args));
                            }
                            if (processRecord != null) {
                                ApplicationInfo info = (ApplicationInfo) XposedHelpers.getObjectField(processRecord, "info");
                                String hostType = (String) param.args[1];
                                String hostName = (String) param.args[2];
                                XposedLog.verbose("startProcessLocked in ams, info: %s, type: %s, name: %s", info, hostType, hostName);
                                if (info != null) {
                                    boolean res = getBridge().checkStartProcess(info, hostType, hostName);
                                    if (!res) {
                                        XposedLog.verbose("startProcessLocked BLOCKED!!!, info: " + info);
                                        // P require a boolean value
                                        // @return {@code true} if process start is successful, false otherwise.
                                        param.setResult(OSUtil.isPOrAbove() ? false : null);
                                    } else {
                                        getBridge().onStartProcessLocked(info);
                                    }
                                }
                            }
                        }
                    });
            XposedLog.verbose("hookStartProcessLocked OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hookStartProcessLocked: " + Log.getStackTraceString(e));
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
