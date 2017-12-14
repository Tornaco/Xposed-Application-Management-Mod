package github.tornaco.xposedmoduletest.xposed.submodules;

import android.util.Log;

import com.google.common.collect.Sets;

import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.compat.os.AppOpsManagerCompat;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

// Hook hookGetLine1Number settings.
class PhoneInterfaceManagerSubModule extends IntentFirewallAndroidSubModule {

    @Override
    public Set<String> getInterestedPackages() {
        return Sets.newHashSet("com.android.phone");
    }

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookGetLine1Number(lpparam);
    }

    private void hookGetLine1Number(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("PhoneInterfaceManagerSubModule hookGetLine1Number...");
        try {
            Class ams = XposedHelpers.findClass("com.android.phone.PhoneInterfaceManager",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(ams, "getLine1NumberForDisplay",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            String callPackageName = (String) param.args[1];
                            if (callPackageName == null) return;
                            if ("com.android.phone".equals(callPackageName)
                                    || "android".equals(callPackageName)
                                    || "com.android.server.telecom".equals(callPackageName)) {
                                return;
                            }
                            // Check op.
                            XAshmanManager xAshmanManager = XAshmanManager.get();
                            if (xAshmanManager.isServiceAvailable()) {
                                int mode = xAshmanManager.getPermissionControlBlockModeForPkg(
                                        AppOpsManagerCompat.OP_GET_LINE1_NUMBER, callPackageName);
                                if (mode == AppOpsManagerCompat.MODE_IGNORED) {
                                    XposedLog.verbose("getLine1NumberForDisplay, MODE_IGNORED returning null for :"
                                            + callPackageName);
                                    param.setResult(null);
                                } else if (BuildConfig.DEBUG) {
                                    String userNumber = xAshmanManager.getUserDefinedLine1Number();
                                    if (userNumber != null) {
                                        XposedLog.verbose("getLine1NumberForDisplay, returning user defined num: " + userNumber);
                                        param.setResult(userNumber);
                                    }
                                }
                            }
                        }
                    });
            XposedLog.verbose("PhoneInterfaceManagerSubModule hookGetLine1Number OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("PhoneInterfaceManagerSubModule Fail hookGetLine1Number: " + Log.getStackTraceString(e));
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
