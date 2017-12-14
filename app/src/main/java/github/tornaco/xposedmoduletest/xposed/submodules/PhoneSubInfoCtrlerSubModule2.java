package github.tornaco.xposedmoduletest.xposed.submodules;

import android.util.Log;

import java.util.Set;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import github.tornaco.xposedmoduletest.compat.os.AppOpsManagerCompat;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

// Android will query line1 number if phoneInterface return null.
class PhoneSubInfoCtrlerSubModule2 extends IntentFirewallAndroidSubModule {

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
        super.initZygote(startupParam);

        try {
            Class sub = XposedHelpers.findClass("com.android.internal.telephony.PhoneSubInfoController",
                    null);
            XposedLog.verbose("sub class: " + sub);
            Set unHooks = XposedBridge.hookAllMethods(sub, "getLine1NumberForSubscriber",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            String callPackageName = (String) param.args[1];
                            if (callPackageName == null || "android".equals(callPackageName)
                                    || "com.android.phone".equals(callPackageName)
                                    || "com.android.server.telecom".equals(callPackageName)) {
                                return;
                            }
                            // Check op.
                            XAshmanManager ashmanManager = XAshmanManager.get();
                            if (ashmanManager.isServiceAvailable()) {
                                int mode = ashmanManager.getPermissionControlBlockModeForPkg(
                                        AppOpsManagerCompat.OP_GET_LINE1_NUMBER, callPackageName);
                                if (mode == AppOpsManagerCompat.MODE_IGNORED) {
                                    XposedLog.verbose("PhoneSubInfoCtrlerSubModule2 " +
                                            "getLine1NumberForSubscriber, MODE_IGNORED returning null for :"
                                            + callPackageName);
                                    param.setResult(null);
                                }
                            }
                        }
                    });
            XposedLog.verbose("PhoneSubInfoCtrlerSubModule getLine1NumberForSubscriber OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("PhoneSubInfoCtrlerSubModule Fail getLine1NumberForSubscriber: " + Log.getStackTraceString(e));
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
