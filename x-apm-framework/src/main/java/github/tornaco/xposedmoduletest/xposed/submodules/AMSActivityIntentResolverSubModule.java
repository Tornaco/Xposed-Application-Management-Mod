package github.tornaco.xposedmoduletest.xposed.submodules;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

class AMSActivityIntentResolverSubModule extends AndroidSubModule {

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookBroadcastRecordPerformReceive(lpparam);
    }

    // https://github.com/LineageOS/android_frameworks_base/blob/lineage-15.1/services/core/java/com/android/server/am/BroadcastQueue.java
    private void hookBroadcastRecordPerformReceive(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("hookBroadcastRecordPerformReceive...");
        try {
            Class ams = XposedHelpers.findClass("com.android.server.am.BroadcastQueue",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(ams,
                    "performReceiveLocked",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);

                            boolean hook = getBridge().beforeHookBroadcastPerformResult();
                            if (hook) {
                                Intent intent = (Intent) param.args[2];
                                int resultCode = (int) param.args[3];

                                int hookedCode = getBridge().onHookBroadcastPerformResult(intent, resultCode);
                                if (isValidResultCode(hookedCode) && resultCode != hookedCode) {
                                    param.args[3] = hookedCode;
                                    if (BuildConfig.DEBUG) {
                                        XposedLog.verbose("BroadcastRecord perform receive hooked res code to: " + hookedCode);
                                    }
                                }
                            }
                        }
                    });
            XposedLog.verbose("hookBroadcastRecordPerformReceive OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hookBroadcastRecordPerformReceive: " + Log.getStackTraceString(e));
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }

    // Only accept ok or canceled.
    private static boolean isValidResultCode(int code) {
        return code == Activity.RESULT_OK || code == Activity.RESULT_CANCELED;
    }
}
