package github.tornaco.xposedmoduletest.x;

import android.content.Context;
import android.content.Intent;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.apigen.GithubCommitSha;
import github.tornaco.xposedmoduletest.BuildConfig;

/**
 * Created by guohao4 on 2017/10/19.
 * Email: Tornaco@163.com
 */
@GithubCommitSha
class XModule implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    static final boolean DEBUG_V = true;

    static final String TAG = "XAppGuard-";

    XStatus xStatus = XStatus.UNKNOWN;

    XAppGuardService mAppGuardService;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if ("android".equals(lpparam.packageName)) {
            onLoadingAndroid(lpparam);
        }
    }

    void onLoadingAndroid(XC_LoadPackage.LoadPackageParam lpparam) {
        xStatus = XStatus.RUNNING;
        hookAMSStart(lpparam);
        hookSystemServiceRegister(lpparam);
        hookAMSSystemReady(lpparam);
        hookAMSShutdown(lpparam);
        hookFPService(lpparam);
    }

    private void hookAMSShutdown(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Class ams = XposedHelpers.findClass("com.android.server.am.ActivityManagerService", lpparam.classLoader);
            XposedBridge.hookAllMethods(ams, "shutdown", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    if (mAppGuardService != null) {
                        mAppGuardService.shutdown();
                    }
                }
            });
        } catch (Exception e) {
            XposedBridge.log(TAG + "Fail hookAMSShutdown");
            xStatus = XStatus.ERROR;
        }
    }

    private void hookSystemServiceRegister(XC_LoadPackage.LoadPackageParam lpparam) {
    }

    private void hookAMSSystemReady(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Class ams = XposedHelpers.findClass("com.android.server.am.ActivityManagerService", lpparam.classLoader);
            XposedBridge.hookAllMethods(ams, "systemReady", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    if (mAppGuardService != null) {
                        mAppGuardService.systemReady();
                    }
                }
            });
        } catch (Exception e) {
            XposedBridge.log(TAG + "Fail hookAMSSystemReady");
            xStatus = XStatus.ERROR;
        }
    }

    private void hookAMSStart(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Class ams = XposedHelpers.findClass("com.android.server.am.ActivityManagerService",
                    lpparam.classLoader);
            XposedBridge.hookAllMethods(ams, "start", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    Context context = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                    mAppGuardService = new XAppGuardService(context);
                    mAppGuardService.publish();
                }
            });
        } catch (Exception e) {
            XposedBridge.log(TAG + "Fail hook hookAMSStart");
            xStatus = XStatus.ERROR;
        }
    }

    // http://androidxref.com/7.0.0_r1/xref/frameworks/base/services/core/java/com/android/server/fingerprint/FingerprintService.java
    // http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/fingerprint/FingerprintService.java
    private void hookFPService(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedBridge.hookAllMethods(
                    XposedHelpers.findClass("com.android.server.fingerprint.FingerprintService", lpparam.classLoader),
                    "canUseFingerprint", new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            Object pkg = param.args[0];
                            if (BuildConfig.APPLICATION_ID.equals(pkg)) {
                                param.setResult(true);
                                XposedBridge.log(TAG + "ALLOWING APPGUARD TO USE FP ANYWAY");
                            }
                        }
                    });
        } catch (Exception e) {
            XposedBridge.log(TAG + "Fail hookFPService" + e);
            if (xStatus != XStatus.ERROR) xStatus = XStatus.WARN;
        }
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {

    }

    boolean isLauncherIntent(Intent intent) {
        return intent != null
                && intent.getCategories() != null
                && intent.getCategories().contains("android.intent.category.HOME");
    }

}
