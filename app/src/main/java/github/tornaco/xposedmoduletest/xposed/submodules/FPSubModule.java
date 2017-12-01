package github.tornaco.xposedmoduletest.xposed.submodules;

import android.util.Log;

import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.xposed.app.XAppGuardManager;
import github.tornaco.xposedmoduletest.xposed.util.XPosedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

class FPSubModule extends AppGuardAndroidSubModule {
    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookFPUtil(lpparam);
    }

    /**
     * @deprecated No need anymore, our verifier mush be declared as an Activity.
     */
    // http://androidxref.com/7.0.0_r1/xref/frameworks/base/services/core/java/com/android/server/fingerprint/FingerprintService.java
    // http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/fingerprint/FingerprintService.java
    @Deprecated
    private void hookFPService(XC_LoadPackage.LoadPackageParam lpparam) {
        XPosedLog.verbose("hookFPService...");
        try {
            Set unHooks = XposedBridge.hookAllMethods(
                    XposedHelpers.findClass("com.android.server.fingerprint.FingerprintService",
                            lpparam.classLoader),
                    "canUseFingerprint", new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            Object pkg = param.args[0];
                            if (BuildConfig.APPLICATION_ID.equals(pkg)) {
                                param.setResult(true);
                            }
                        }
                    });
            XPosedLog.verbose("hookFPService OK:" + unHooks);
            getBridge().publishFeature(XAppGuardManager.Feature.FP);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XPosedLog.verbose("Fail hookFPService" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }

    private void hookFPUtil(XC_LoadPackage.LoadPackageParam lpparam) {
        XPosedLog.verbose("hookFPUtil ...");

        try {
            Set unHooks = XposedBridge.hookAllMethods(
                    XposedHelpers.findClass("com.android.server.fingerprint.FingerprintUtils",
                            lpparam.classLoader),
                    "vibrateFingerprintSuccess", new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            if (getAppGuardBridge().interruptFPSuccessVibrate()) {
                                param.setResult(null);
                                XPosedLog.verbose("vibrateFingerprintSuccess blocked.");
                            }
                        }
                    });
            XPosedLog.verbose("hookFPUtil @SUCCESS OK:" + unHooks);
            getBridge().publishFeature(XAppGuardManager.Feature.FP);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XPosedLog.verbose("Fail hookFPUtil @SUCCESS: " + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }

        try {
            Set unHooks = XposedBridge.hookAllMethods(
                    XposedHelpers.findClass("com.android.server.fingerprint.FingerprintUtils",
                            lpparam.classLoader),
                    "vibrateFingerprintError", new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            if (getAppGuardBridge().interruptFPErrorVibrate()) {
                                param.setResult(null);
                                XPosedLog.verbose("vibrateFingerprintError blocked.");
                            }
                        }
                    });
            XPosedLog.verbose("hookFPUtil @ERROR OK:" + unHooks);
            getBridge().publishFeature(XAppGuardManager.Feature.FP);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XPosedLog.verbose("Fail hookFPUtil @ERROR: " + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
