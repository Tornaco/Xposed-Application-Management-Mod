package github.tornaco.xposedmoduletest.xposed.submodules;

import android.app.AndroidAppHelper;
import android.util.Log;

import java.util.Set;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.compat.os.AppOpsManagerCompat;
import github.tornaco.xposedmoduletest.xposed.XAppBuildVar;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

// Hook hookGetDeviceID settings.
class TelephonyManagerSubModule extends IntentFirewallAndroidSubModule {
    @Override
    public String needBuildVar() {
        return XAppBuildVar.APP_OPS;
    }

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
        super.initZygote(startupParam);
        hookTelephonyManagerGetDeviceId();
        hookTelephonyManagerGetLine1Number();
        hookTelephonyManagerGetSimSerial();
    }

    private void hookTelephonyManagerGetDeviceId() {
        XposedLog.verbose("TelephonyManagerSubModule hookTelephonyManagerGetDeviceId...");
        try {
            Class c = XposedHelpers.findClass("android.telephony.TelephonyManager",
                    null);
            Set unHooks = XposedBridge.hookAllMethods(c, "getDeviceId",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);

                            String callPackageName = AndroidAppHelper.currentPackageName();

                            if (BuildConfig.DEBUG) {
                                XposedLog.verbose("getDeviceId: " + callPackageName);
                            }

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
                                        AppOpsManagerCompat.OP_GET_DEVICE_ID, callPackageName);
                                if (mode == AppOpsManagerCompat.MODE_IGNORED) {
                                    XposedLog.verbose("getDeviceId, MODE_IGNORED returning null for :" + callPackageName);
                                    param.setResult(null);
                                } else if (BuildConfig.DEBUG) {
                                    // Check if is priv enabled for this app.
                                    boolean isPriv = xAshmanManager.isPackageInPrivacyList(callPackageName);
                                    if (isPriv) {
                                        String userSetId = xAshmanManager.getUserDefinedDeviceId();
                                        if (userSetId != null) {
                                            XposedLog.danger("getDeviceId, returning user device id :" + userSetId);
                                            param.setResult(userSetId);
                                        }
                                    }
                                }
                            }
                        }
                    });
            XposedLog.verbose("TelephonyManagerSubModule hookTelephonyManagerGetDeviceId OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("TelephonyManagerSubModule Fail hookTelephonyManagerGetDeviceId: " + Log.getStackTraceString(e));
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }

    private void hookTelephonyManagerGetLine1Number() {
        XposedLog.verbose("TelephonyManagerSubModule hookTelephonyManagerGetLine1Number...");
        try {
            Class c = XposedHelpers.findClass("android.telephony.TelephonyManager",
                    null);
            Set unHooks = XposedBridge.hookAllMethods(c, "getLine1Number",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);

                            String callPackageName = AndroidAppHelper.currentPackageName();

                            if (BuildConfig.DEBUG) {
                                XposedLog.verbose("getLine1Number: " + callPackageName);
                            }

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
                                    XposedLog.verbose("getLine1Number, MODE_IGNORED returning null for :" + callPackageName);
                                    param.setResult(null);
                                } else if (BuildConfig.DEBUG) {
                                    boolean isPriv = xAshmanManager.isPackageInPrivacyList(callPackageName);
                                    if (isPriv) {
                                        String userNumber = xAshmanManager.getUserDefinedLine1Number();
                                        if (userNumber != null) {
                                            XposedLog.danger("getLine1NumberForSubscriber, returning user defined num: " + userNumber);
                                            param.setResult(userNumber);
                                        }
                                    }
                                }
                            }
                        }
                    });
            XposedLog.verbose("TelephonyManagerSubModule hookTelephonyManagerGetLine1Number OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("TelephonyManagerSubModule Fail hookTelephonyManagerGetLine1Number: " + Log.getStackTraceString(e));
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }

    private void hookTelephonyManagerGetSimSerial() {
        XposedLog.verbose("TelephonyManagerSubModule hookTelephonyManagerGetSimSerial...");
        try {
            Class c = XposedHelpers.findClass("android.telephony.TelephonyManager",
                    null);
            Set unHooks = XposedBridge.hookAllMethods(c, "getSimSerialNumber",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);

                            String callPackageName = AndroidAppHelper.currentPackageName();

                            if (BuildConfig.DEBUG) {
                                XposedLog.verbose("getSimSerialNumber: " + callPackageName);
                            }

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
                                        AppOpsManagerCompat.OP_GET_SIM_SERIAL_NUMBER, callPackageName);
                                if (mode == AppOpsManagerCompat.MODE_IGNORED) {
                                    XposedLog.verbose("getSimSerialNumber, MODE_IGNORED returning null for :" + callPackageName);
                                    param.setResult(null);
                                }
                            }
                        }
                    });
            XposedLog.verbose("TelephonyManagerSubModule hookTelephonyManagerGetSimSerial OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("TelephonyManagerSubModule Fail hookTelephonyManagerGetSimSerial: " + Log.getStackTraceString(e));
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
