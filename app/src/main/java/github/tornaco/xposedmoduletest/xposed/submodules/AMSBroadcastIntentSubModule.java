package github.tornaco.xposedmoduletest.xposed.submodules;

import android.app.ActivityManager;
import android.app.IApplicationThread;
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

// Hook hookBroadcastIntent.
class AMSBroadcastIntentSubModule extends AndroidSubModule {

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookBroadcastIntent(lpparam);
    }

    private void hookBroadcastIntent(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("hookBroadcastIntent...");
        try {
            Class ams = XposedHelpers.findClass("com.android.server.am.ActivityManagerService",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(ams, "broadcastIntent",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            IApplicationThread applicationThread = (IApplicationThread) param.args[0];
                            Intent intent = (Intent) param.args[1];
                            if (intent == null) return;
                            boolean allow = getBridge().checkBroadcastIntentSending(applicationThread, intent);
                            if (!allow) {
                                param.setResult(ActivityManager.BROADCAST_SUCCESS);
                                XposedLog.wtf("broadcastIntent set result to ActivityManager.BROADCAST_SUCCESS");
                            }
                        }
                    });
            XposedLog.verbose("hookBroadcastIntent OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hookBroadcastIntent: " + Log.getStackTraceString(e));
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
