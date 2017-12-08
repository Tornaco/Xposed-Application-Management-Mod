package github.tornaco.xposedmoduletest.xposed.submodules;

import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;

import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/11/9.
 * Email: Tornaco@163.com
 */

public class IFWSubModule extends IntentFirewallAndroidSubModule {
    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookIntentFireWall(lpparam);
    }

    private void hookIntentFireWall(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("hookIntentFireWall...");
        try {
            Class ams = XposedHelpers.findClass("com.android.server.firewall.IntentFirewall",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(ams, "checkService", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
//                    if (XposedLog.isVerboseLoggable()) {
//                        try {
//                            Intent intent = (Intent) param.args[1];
//                            XposedLog.verbose("checkService@ intent: " + intent + "extra: " + intent.getExtras()
//                                    + new IntentFormatter().format(intent));
//                        } catch (Exception ignored) {
//                        }
//                    }
                    ComponentName componentName = (ComponentName) param.args[0];
                    int callerID = (int) param.args[2];
                    param.setResult(getIntentFirewallBridge().checkService(componentName, callerID));
                }
            });

            Set unHooks2 = XposedBridge.hookAllMethods(ams, "checkBroadcast", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    int callerUid = (int) param.args[1];
                    int recUid = (int) param.args[4];
                    Intent intent = (Intent) param.args[0];
                    String action = intent == null ? null : intent.getAction();
                    param.setResult(getIntentFirewallBridge().checkBroadcast(action, recUid, callerUid));
                }
            });
            XposedLog.verbose("hookIntentFireWall OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
            setStatus(unhooksToStatus(unHooks2));
        } catch (Exception e) {
            XposedLog.verbose("Fail hook hookIntentFireWall");
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
