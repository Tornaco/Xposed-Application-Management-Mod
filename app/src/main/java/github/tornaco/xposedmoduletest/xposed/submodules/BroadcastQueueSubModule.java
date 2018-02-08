package github.tornaco.xposedmoduletest.xposed.submodules;

import android.content.Intent;
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

// https://github.com/LineageOS/android_frameworks_base/blob/cm-14.1/services/core/java/com/android/server/am/ActiveServices.java
// Hook hookDeliverToRegisteredReceiverLocked settings.

@Deprecated
class BroadcastQueueSubModule extends AndroidSubModule {

//    12.0 12.1 13.0
//    private final void deliverToRegisteredReceiverLocked(BroadcastRecord r,
//                                                         BroadcastFilter filter, boolean ordered) {}

//    14.0 14.1
//    private void deliverToRegisteredReceiverLocked(BroadcastRecord r,
//                                                   BroadcastFilter filter, boolean ordered, int index) {}

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookDeliverToRegisteredReceiverLocked(lpparam);
    }

    private void hookDeliverToRegisteredReceiverLocked(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("hookDeliverToRegisteredReceiverLocked...");
        try {
            Class ams = XposedHelpers.findClass("com.android.server.am.BroadcastQueue",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(ams, "deliverToRegisteredReceiverLocked",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            Object broadcastRecord = param.args[0];
                            if (broadcastRecord == null) return;
                            // the original intent that generated us
                            Intent intent = (Intent) XposedHelpers.getObjectField(broadcastRecord, "intent");
                            if (intent == null) return;
                            // who sent this
                            String callerPackage = (String) XposedHelpers.getObjectField(broadcastRecord, "callerPackage");
                            if (callerPackage == null) return;
                            boolean allow = getBridge().checkBroadcastDeliver(intent, callerPackage, -1, -1);
                            if (!allow) {
                                param.setResult(null);
                            }
                        }
                    });
            XposedLog.verbose("hookDeliverToRegisteredReceiverLocked OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hookDeliverToRegisteredReceiverLocked: " + Log.getStackTraceString(e));
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
