package github.tornaco.xposedmoduletest.xposed.submodules;

import android.app.ActivityManager;
import android.os.Binder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.compat.os.AppOpsManagerCompat;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

// Hook hookGetRunningAppProcess settings.
class AMSSubModule9 extends IntentFirewallAndroidSubModule {

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookGetRunningAppProcess(lpparam);
    }

    private void hookGetRunningAppProcess(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("hookGetRunningAppProcess...");
        try {
            Class ams = XposedHelpers.findClass("com.android.server.am.ActivityManagerService",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(ams, "getRunningAppProcesses",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            int callingUid = Binder.getCallingUid();
                            if (PkgUtil.isSystemOrPhoneOrShell(callingUid)) return;
                            // Check op.
                            XAshmanManager xAshmanManager = XAshmanManager.get();
                            if (xAshmanManager.isServiceAvailable()) {
                                int mode = xAshmanManager.getPermissionControlBlockModeForUid(
                                        AppOpsManagerCompat.OP_GET_RUNNING_TASKS, callingUid);
                                if (mode == AppOpsManagerCompat.MODE_IGNORED) {
                                    XposedLog.verbose("getRunningAppProcesses, MODE_IGNORED returning empty for :"
                                            + callingUid);
                                    try {
                                        List<ActivityManager.RunningAppProcessInfo> empty = new ArrayList<>(0);
                                        param.setResult(empty);
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
