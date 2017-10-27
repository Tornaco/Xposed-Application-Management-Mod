package github.tornaco.xposedmoduletest.x;

import android.app.ActivityManagerNative;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.KeyEvent;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.apigen.GithubCommitSha;
import github.tornaco.xposedmoduletest.BuildConfig;

/**
 * Created by guohao4 on 2017/10/19.
 * Email: Tornaco@163.com
 */
@GithubCommitSha
class XModule implements IXposedHookLoadPackage, IXposedHookZygoteInit, IXposedHookInitPackageResources {

    XStatus xStatus = XStatus.UNKNOWN;

    XAppGuardServiceAbs mAppGuardService = new XAppGuardServiceDelegate();

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if ("android".equals(lpparam.packageName)) {
            onLoadingAndroid(lpparam);
        }
    }

    void onLoadingAndroid(XC_LoadPackage.LoadPackageParam lpparam) {
        XStopWatch stopWatch = XStopWatch.start("Hooking init");

        xStatus = XStatus.GOOD;

        hookAMSStart(lpparam);
        hookSystemServiceRegister(lpparam);
        hookAMSSystemReady(lpparam);
        hookAMSShutdown(lpparam);
        hookFPService(lpparam);
        hookScreenshotApplications(lpparam);
        hookActivityLifecycle(lpparam);
        hookAMSActivityDestroy(lpparam);
        hookPWM(lpparam);

        stopWatch.stop();
    }

    private void hookAMSShutdown(XC_LoadPackage.LoadPackageParam lpparam) {
        XLog.logV("hookAMSShutdown...");
        try {
            Class ams = XposedHelpers.findClass("com.android.server.am.ActivityManagerService",
                    lpparam.classLoader);
            XposedBridge.hookAllMethods(ams, "shutdown", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    mAppGuardService.shutdown();
                }
            });
            XLog.logV("hookAMSShutdown OK");
        } catch (Exception e) {
            XLog.logV("Fail hookAMSShutdown");
            xStatus = XStatus.ERROR;
        }
    }

    private void hookSystemServiceRegister(XC_LoadPackage.LoadPackageParam lpparam) {
        XLog.logV("hookSystemServiceRegister...");
        XLog.logV("hookSystemServiceRegister OK");
    }

    private void hookAMSSystemReady(XC_LoadPackage.LoadPackageParam lpparam) {
        XLog.logV("hookAMSSystemReady...");
        try {
            Class ams = XposedHelpers.findClass("com.android.server.am.ActivityManagerService",
                    lpparam.classLoader);
            XposedBridge.hookAllMethods(ams, "systemReady", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    mAppGuardService.systemReady();
                    mAppGuardService.setStatus(xStatus);
                }
            });
            XLog.logV("hookAMSSystemReady OK");
        } catch (Exception e) {
            XLog.logV("Fail hookAMSSystemReady");
            xStatus = XStatus.ERROR;
        }
    }

    private void hookAMSStart(XC_LoadPackage.LoadPackageParam lpparam) {
        XLog.logV("hookAMSStart...");
        try {
            Class ams = XposedHelpers.findClass("com.android.server.am.ActivityManagerService",
                    lpparam.classLoader);
            XposedBridge.hookAllMethods(ams, "start", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    Context context = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                    mAppGuardService.attachContext(context);
                    mAppGuardService.publish();
                }
            });
            XLog.logV("hookAMSStart OK");
        } catch (Exception e) {
            XLog.logV("Fail hook hookAMSStart");
            xStatus = XStatus.ERROR;
        }
    }

    /**
     * @deprecated No need anymore, our verifier mush be declared as an Activity.
     */
    // http://androidxref.com/7.0.0_r1/xref/frameworks/base/services/core/java/com/android/server/fingerprint/FingerprintService.java
    // http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/fingerprint/FingerprintService.java
    @Deprecated
    private void hookFPService(XC_LoadPackage.LoadPackageParam lpparam) {
        XLog.logV("hookFPService...");
        try {
            XposedBridge.hookAllMethods(
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
            XLog.logV("hookFPService OK");
            mAppGuardService.publishFeature(XAppGuardManager.Feature.FP);
        } catch (Exception e) {
            XLog.logV("Fail hookFPService" + e);
            if (xStatus != XStatus.ERROR) xStatus = XStatus.WITH_WARN;
        }
    }

    /**
     * Not work, we can not receive any call through this.
     */
    @Deprecated
    private void hookTaskRecordSetLastThumbnail(XC_LoadPackage.LoadPackageParam lpparam) {
        XLog.logV("hookTaskRecordSetLastThumbnail...");
        try {
            Class clz = XposedHelpers.findClass("com.android.server.am.TaskRecord",
                    lpparam.classLoader);
            XposedBridge.hookAllMethods(clz, "setLastThumbnail", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    // Retrieve package name first.
                    Object me = param.thisObject;
                    // FIXME Using aff instead of PKG.
                    final String affinity = (String) XposedHelpers.getObjectField(me, "affinity");
                    final int effectiveUid = (int) XposedHelpers.getObjectField(me, "effectiveUid");
                    XLog.logV("affinity:" + affinity + ", effectiveUid:" + effectiveUid);
                    if (mAppGuardService.isBlurForPkg(affinity)
                            && param.getResult() != null) {
                        Bitmap res = (Bitmap) param.args[0];
                        XLog.logV("Blur bitmap start");
                        Bitmap blured = (XBitmapUtil.createBlurredBitmap(res,
                                mAppGuardService.getBlurRadius(), mAppGuardService.getBlurScale()));
                        if (blured != null) param.args[0] = blured;
                        XLog.logV("Blur bitmap end");
                    }
                }
            });
            XLog.logV("hookTaskRecordSetLastThumbnail OK");
        } catch (Exception e) {
            XLog.logV("Fail hookTaskRecordSetLastThumbnail:" + e);
        }
    }

    /**
     * @see #onScreenshotApplications(XC_MethodHook.MethodHookParam)
     */
    private void hookScreenshotApplications(XC_LoadPackage.LoadPackageParam lpparam) {
        XLog.logV("hookScreenshotApplications...");
        try {
            Class clz = XposedHelpers.findClass("com.android.server.wm.WindowManagerService",
                    lpparam.classLoader);
            XposedBridge.hookAllMethods(clz,
                    "screenshotApplications", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            try {
                                onScreenshotApplications(param);
                            } catch (Exception e) {
                                XLog.logV("Fail onScreenshotApplications:" + e);
                            }
                        }
                    });
            XLog.logV("hookScreenshotApplications OK");
            mAppGuardService.publishFeature(XAppGuardManager.Feature.BLUR);
        } catch (Exception e) {
            XLog.logV("Fail hookScreenshotApplications:" + e);
        }
    }

    private void onScreenshotApplications(XC_MethodHook.MethodHookParam param) throws RemoteException {
        IBinder token = (IBinder) param.args[0];
        ComponentName activityClassForToken = ActivityManagerNative.getDefault().getActivityClassForToken(token);
        XStopWatch stopWatch = XStopWatch.start("onScreenshotApplications");
        String pkgName = activityClassForToken == null ? null : activityClassForToken.getPackageName();
        if (TextUtils.isEmpty(pkgName)) {
            return;
        }
        if (mAppGuardService.isBlurForPkg(pkgName)
                && param.getResult() != null) {
            Bitmap res = (Bitmap) param.getResult();
            stopWatch.split("Blur bitmap start");
            Bitmap blured = (XBitmapUtil.createBlurredBitmap(res,
                    mAppGuardService.getBlurRadius(), mAppGuardService.getBlurScale()));
            if (blured != null)
                param.setResult(blured);
            stopWatch.split("Blur bitmap end");
        }
        stopWatch.stop();
    }


    private void hookActivityLifecycle(XC_LoadPackage.LoadPackageParam lpparam) {

    }

    private void hookAMSActivityDestroy(XC_LoadPackage.LoadPackageParam lpparam) {

    }

    private void hookPWM(XC_LoadPackage.LoadPackageParam lpparam) {
        XLog.logV("hookPWM...");
        try {
            Class clz = XposedHelpers.findClass("com.android.server.policy.PhoneWindowManager",
                    lpparam.classLoader);
            XposedBridge.hookAllMethods(clz,
                    "interruptKeyBeforeQueueing", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            KeyEvent keyEvent = (KeyEvent) param.args[0];
                            if (keyEvent.getAction() == KeyEvent.ACTION_UP && keyEvent.getKeyCode() == KeyEvent.KEYCODE_HOME) {
                                mAppGuardService.onHome();
                            }
                        }
                    });
            XLog.logV("hookPWM OK");
            mAppGuardService.publishFeature(XAppGuardManager.Feature.BLUR);
        } catch (Exception e) {
            XLog.logV("Fail hookPWM:" + e);
        }
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        XLog.logV("initZygote...");
    }

    boolean isLauncherIntent(Intent intent) {
        return intent != null
                && intent.getCategories() != null
                && intent.getCategories().contains("android.intent.category.HOME");
    }

    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) throws Throwable {

    }
}
