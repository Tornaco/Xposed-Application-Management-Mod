package github.tornaco.xposedmoduletest.xposed.submodules;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageParser;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;
import android.util.Log;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.xposed.service.opt.gcm.GCMFCMHelper;
import github.tornaco.xposedmoduletest.xposed.service.opt.gcm.TGPushNotificationHandler;
import github.tornaco.xposedmoduletest.xposed.util.ObjectToStringUtil;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

class AMSActivityIntentResolverSubModule extends AndroidSubModule {

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
//        hookBuildResolveList(lpparam);
//        hookIsFilterStopped(lpparam);
//        hookQueryIntentReceiversInternal(lpparam);
//        hookBroadcastRecord(lpparam);
        hookBroadcastRecordPerformReceive(lpparam);
    }

    private void hookBuildResolveList(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("hookBuildResolveList...");
        try {
            Class ams = XposedHelpers.findClass("com.android.server.IntentResolver",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(ams, "buildResolveList",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            if (GCMFCMHelper.isGcmIntent((Intent) param.args[0])) {
                                PackageParser.ActivityIntentInfo[] src = (PackageParser.ActivityIntentInfo[]) param.args[param.args.length - 3];
                                XposedLog.verbose("PushNotificationHandler buildResolveList: %s %s", Arrays.toString(src),
                                        Log.getStackTraceString(new Throwable()));
                            }
                        }
                    });
            XposedLog.verbose("hookBuildResolveList OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hookBuildResolveList: " + Log.getStackTraceString(e));
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }

    private void hookIsFilterStopped(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("hookIsFilterStopped...");
        try {
            Class ams = XposedHelpers.findClass("com.android.server.pm.PackageManagerService$ActivityIntentResolver",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(ams, "isFilterStopped",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            XposedLog.verbose("PushNotificationHandler isFilterStopped: " + param.args[0]);
                        }
                    });
            XposedLog.verbose("hookIsFilterStopped OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hookIsFilterStopped: " + Log.getStackTraceString(e));
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }

    private void hookQueryIntentReceiversInternal(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("hookQueryIntentReceiversInternal...");
        try {
            Class ams = XposedHelpers.findClass("com.android.server.pm.PackageManagerService",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(ams, "queryIntentReceiversInternal",
                    new XC_MethodHook() {
                        @SuppressWarnings("unchecked")
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            List<ResolveInfo> resolveInfos = (List<ResolveInfo>) param.getResult();
                            XposedLog.verbose("PushNotificationHandler queryIntentReceiversInternal: "
                                    + ObjectToStringUtil.intentToString((Intent) param.args[0])
                                    + ", res: " + TextUtils.join(", ", resolveInfos));
                        }
                    });
            XposedLog.verbose("hookQueryIntentReceiversInternal OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hookQueryIntentReceiversInternal: " + Log.getStackTraceString(e));
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }

    private void hookBroadcastRecord(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("hookBroadcastRecord...");
        try {
            Class ams = XposedHelpers.findClass("com.android.server.am.BroadcastRecord",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllConstructors(ams,
                    new XC_MethodHook() {
                        @SuppressWarnings("unchecked")
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            Intent intent = (Intent) XposedHelpers
                                    .getObjectField(param.thisObject, "intent");
                            if (intent != null && TGPushNotificationHandler.TG_PKG_NAME.equals(intent.getPackage())) {
                                XposedLog.verbose("BroadcastRecord TG: %s, intent %s", param.thisObject, intent);

                            }
                        }
                    });
            XposedLog.verbose("hookBroadcastRecord OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hookBroadcastRecord: " + Log.getStackTraceString(e));
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }

    //FIXME  Has not check from other, current is SDK24.
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
