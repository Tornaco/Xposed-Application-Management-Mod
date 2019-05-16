package github.tornaco.xposedmoduletest.xposed.submodules;

import android.app.ActivityManager;
import android.os.Binder;
import android.util.Log;

import java.util.List;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.compat.os.XAppOpsManager;
import github.tornaco.xposedmoduletest.xposed.XAppBuildVar;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.service.DeprecatedSince;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

// Hook hookGetRunningAppProcess settings.
@DeprecatedSince("4.9.3")
class AMSGetRunningAppsSubModule extends AndroidSubModule {

    @Override
    public String needBuildVar() {
        return XAppBuildVar.APP_OPS;
    }

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        // No need to check ops. Now 3-rd app can only get self process.
        // so we break here, only hook it for debug.
        if (BuildConfig.DEBUG) {
            hookGetRunningAppProcess(lpparam);
        }
    }

    private void hookGetRunningAppProcess(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("hookGetRunningAppProcess...");
        try {
            Class ams = XposedHelpers.findClass("com.android.server.am.ActivityManagerService",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(ams, "getRunningAppProcesses",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);

                            boolean permControlEnabled = XAPMManager.get().isServiceAvailable()
                                    && XAPMManager.get().isPermissionControlEnabled();
                            if (!permControlEnabled) {
                                return;
                            }

                            int callingUid = Binder.getCallingUid();

                            if (PkgUtil.isSystemOrPhoneOrShell(callingUid)) return;
                            // Check op.
                            XAPMManager xAshmanManager = XAPMManager.get();
                            if (xAshmanManager.isServiceAvailable()) {
                                int mode = xAshmanManager.getPermissionControlBlockModeForUid(
                                        XAppOpsManager.OP_GET_RUNNING_TASKS, callingUid,
                                        true);
                                if (mode == XAppOpsManager.MODE_IGNORED) {
                                    try {
                                        @SuppressWarnings("unchecked") List<ActivityManager.RunningAppProcessInfo> empty
                                                = (List<ActivityManager.RunningAppProcessInfo>) param.getResult();
                                        ActivityManager.RunningAppProcessInfo selfInfo = null;
                                        if (empty != null) {
                                            for (int i = 0; i < empty.size(); i++) {
                                                ActivityManager.RunningAppProcessInfo info = empty.get(i);
                                                int uid = info.uid;
                                                if (BuildConfig.DEBUG) {
                                                    XposedLog.verbose("getRunningAppProcesses items uid %s caller %s", uid, callingUid);
                                                }
                                                if (uid == callingUid) {
                                                    selfInfo = info;
                                                    break;
                                                }
                                            }
                                            empty.clear();
                                            empty.add(selfInfo);
                                            param.setResult(empty);
                                            XposedLog.verbose("getRunningAppProcesses, MODE_IGNORED returning empty for :"
                                                    + callingUid);
                                        }
                                    } catch (Exception e) {
                                        param.setResult(null);
                                        XposedLog.wtf("Fail get empty ArrayList:" + e);
                                    }
                                }
                            }
                        }
                    });
            XposedLog.verbose("hookGetRunningAppProcess OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hookGetRunningAppProcess: " + Log.getStackTraceString(e));
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
