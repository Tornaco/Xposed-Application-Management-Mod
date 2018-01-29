package github.tornaco.xposedmoduletest.xposed.submodules;

import android.util.Log;

import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.util.OSUtil;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

// FIXME Need a porting for Oreo.
class FPSubModule extends AndroidSubModule {

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        if (OSUtil.isMOrAbove() && !OSUtil.isOOrAbove()) {
            // N.
            hookFPUtil(lpparam);
        }
    }

    /**
     * @deprecated No need anymore, our verifier mush be declared as an Activity.
     */
    // http://androidxref.com/7.0.0_r1/xref/frameworks/base/services/core/java/com/android/server/fingerprint/FingerprintService.java
    // http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/fingerprint/FingerprintService.java
    @Deprecated
    private void hookFPService(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("hookFPService...");
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
            XposedLog.verbose("hookFPService OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hookFPService" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }

    private void hookFPUtil(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("hookFPUtil ...");

        try {
            Set unHooks = XposedBridge.hookAllMethods(
                    XposedHelpers.findClass("com.android.server.fingerprint.FingerprintUtils",
                            lpparam.classLoader),
                    "vibrateFingerprintSuccess", new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            if (getBridge().interruptFPSuccessVibrate()) {
                                param.setResult(null);
                                XposedLog.verbose("vibrateFingerprintSuccess blocked.");
                            }
                        }
                    });
            XposedLog.verbose("hookFPUtil @SUCCESS OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hookFPUtil @SUCCESS: " + e);
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
                            if (getBridge().interruptFPErrorVibrate()) {
                                param.setResult(null);
                                XposedLog.verbose("vibrateFingerprintError blocked.");
                            }
                        }
                    });
            XposedLog.verbose("hookFPUtil @ERROR OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hookFPUtil @ERROR: " + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
