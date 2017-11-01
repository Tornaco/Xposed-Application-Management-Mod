package github.tornaco.xposedmoduletest.x.deperate;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.PackageDeleteObserver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageDeleteObserver2;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;

import java.lang.reflect.Method;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.apigen.GithubCommitSha;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.x.XModuleAbs;
import github.tornaco.xposedmoduletest.x.XStatus;
import github.tornaco.xposedmoduletest.x.app.XAppGuardManager;
import github.tornaco.xposedmoduletest.x.app.XMode;
import github.tornaco.xposedmoduletest.x.service.IModuleBridge;
import github.tornaco.xposedmoduletest.x.service.VerifyListener;
import github.tornaco.xposedmoduletest.x.service.XAppGuardServiceDelegate;
import github.tornaco.xposedmoduletest.x.util.XBitmapUtil;
import github.tornaco.xposedmoduletest.x.util.XLog;
import github.tornaco.xposedmoduletest.x.util.XStopWatch;

/**
 * Created by guohao4 on 2017/10/19.
 * Email: Tornaco@163.com
 */
@GithubCommitSha
class XModuleImpl extends XModuleAbs {

    private XStatus xStatus = XStatus.UNKNOWN;

    private IModuleBridge moduleBridge = new XAppGuardServiceDelegate();

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if ("android".equals(lpparam.packageName)) {
            onLoadingAndroid(lpparam);
        }
    }

    private void onLoadingAndroid(XC_LoadPackage.LoadPackageParam lpparam) {
        XStopWatch stopWatch = XStopWatch.start("Hooking init");

        xStatus = XStatus.GOOD;

        hookAMSStart(lpparam);
        hookAMSSystemReady(lpparam);
        hookStartActivityMayWait(lpparam);
        hookTaskMover(lpparam);
        hookAMSShutdown(lpparam);
        hookFPService(lpparam);
        hookScreenshotApplications(lpparam);
        hookPWM(lpparam);
        hookPackageInstaller(lpparam);
        hookPackageManagerService(lpparam);

        stopWatch.stop();
    }

    private void hookTaskMover(XC_LoadPackage.LoadPackageParam lpparam) {
        XLog.logV("hookTaskMover...");
        try {
            final Method moveToFront = methodForTaskMover(lpparam);
            XC_MethodHook.Unhook unhook = XposedBridge.hookMethod(moveToFront, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);

                    XStopWatch stopWatch = XStopWatch.start("hookTaskMover- findTaskToMoveToFrontLocked");
                    try {
                        String pkgName;
                        Object realActivityObj = XposedHelpers.getObjectField(param.args[0], "realActivity");
                        if (realActivityObj != null) {
                            ComponentName componentName = (ComponentName) realActivityObj;
                            pkgName = componentName.getPackageName();
                        } else {
                            // Using aff instead of PKG.
                            pkgName = (String) XposedHelpers.getObjectField(param.args[0], "affinity");
                        }

                        if (TextUtils.isEmpty(pkgName)) return;

                        XLog.logV("findTaskToMoveToFrontLocked:" + pkgName);

                        // Package has been passed.
                        if (!moduleBridge.onEarlyVerifyConfirm(pkgName)) return;

                        moduleBridge.verify(null, pkgName, 0, 0,
                                new VerifyListener() {
                                    @Override
                                    public void onVerifyRes(String pkg, int uid, int pid, int res) {
                                        if (res == XMode.MODE_ALLOWED) try {
                                            XposedBridge.invokeOriginalMethod(moveToFront,
                                                    param.thisObject, param.args);
                                        } catch (Exception e) {
                                            XLog.logD("Error@"
                                                    + Log.getStackTraceString(e));
                                        }
                                    }
                                });

                        param.setResult(null);

                    } catch (Exception e) {
                        XLog.logV("Error@hookTaskMover- findTaskToMoveToFrontLocked:" + Log.getStackTraceString(e));
                    } finally {
                        stopWatch.stop();
                    }
                }
            });
            XLog.logV("hookTaskMover OK:" + unhook);
            moduleBridge.publishFeature(XAppGuardManager.Feature.RECENT);
        } catch (Exception e) {
            XLog.logV("hookTaskMover" + Log.getStackTraceString(e));
            xStatus = XStatus.ERROR;
        }
    }

    @SuppressLint("PrivateApi")
    Method methodForTaskMover(XC_LoadPackage.LoadPackageParam lpparam) throws ClassNotFoundException, NoSuchMethodException {
        throw new IllegalStateException("Need impl here");
    }

    private void hookStartActivityMayWait(XC_LoadPackage.LoadPackageParam lpparam) {
        XLog.logV("hookStartActivityMayWait...");
        try {
            Class clz = clzForStartActivityMayWait(lpparam);

            // Search method.
            String targetMethodName = "startActivityMayWait";
            int matchCount = 0;
            int activityOptsIndex = -1;
            int intentIndex = -1;
            Method startActivityMayWaitMethod = null;
            if (clz != null) {
                for (Method m : clz.getDeclaredMethods()) {
                    if (m.getName().equals(targetMethodName)) {
                        startActivityMayWaitMethod = m;
                        startActivityMayWaitMethod.setAccessible(true);
                        matchCount++;

                        Class[] classes = m.getParameterTypes();
                        for (int i = 0; i < classes.length; i++) {
                            if (Bundle.class == classes[i]) {
                                activityOptsIndex = i;
                            } else if (Intent.class == classes[i]) {
                                intentIndex = i;
                            }
                        }
                    }
                }
            }

            if (startActivityMayWaitMethod == null) {
                XLog.logV("*** FATAL can not find startActivityMayWait method ***");
                xStatus = XStatus.ERROR;
                return;
            }

            if (matchCount > 1) {
                XLog.logV("*** FATAL more than 1 startActivityMayWait method ***");
                xStatus = XStatus.ERROR;
                return;
            }

            if (intentIndex < 0) {
                XLog.logV("*** FATAL can not find intentIndex ***");
                xStatus = XStatus.ERROR;
                return;
            }

            XLog.logV("startActivityMayWait method:" + startActivityMayWaitMethod);
            XLog.logV("intentIndex index:" + intentIndex);
            XLog.logV("activityOptsIndex index:" + activityOptsIndex);

            final int finalActivityOptsIndex = activityOptsIndex;
            final int finalIntentIndex = intentIndex;

            final Method finalStartActivityMayWaitMethod = startActivityMayWaitMethod;
            XC_MethodHook.Unhook unhook = XposedBridge.hookMethod(startActivityMayWaitMethod, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    XStopWatch stopWatch = XStopWatch.start("hookStartActivityMayWait- startActivityMayWaitMethod");
                    try {
                        Intent intent =
                                finalIntentIndex > 0 ?
                                        (Intent) param.args[finalIntentIndex]
                                        : null;
                        if (intent == null) return;

                        ComponentName componentName = intent.getComponent();
                        if (componentName == null) return;
                        final String pkgName = componentName.getPackageName();

                        boolean isHomeIntent = isLauncherIntent(intent);
                        if (isHomeIntent) {
                            return;
                        }

                        // Package has been passed.
                        if (!moduleBridge.onEarlyVerifyConfirm(pkgName)) {
                            return;
                        }

                        Bundle options =
                                finalActivityOptsIndex > 0 ?
                                        (Bundle) param.args[finalActivityOptsIndex]
                                        : null;

                        moduleBridge.verify(options, pkgName, 0, 0,
                                new VerifyListener() {
                                    @Override
                                    public void onVerifyRes(String pkg, int uid, int pid, int res) {
                                        if (res == XMode.MODE_ALLOWED) try {
                                            XposedBridge.invokeOriginalMethod(finalStartActivityMayWaitMethod, param.thisObject, param.args);
                                        } catch (Exception e) {
                                            XLog.logD("Error@" + Log.getStackTraceString(e));
                                        }
                                    }
                                });
                        param.setResult(ActivityManager.START_SUCCESS);
                    } catch (Throwable e) {
                        // replacing did not work.. but no reason to crash the VM! Log the error and go on.
                        XLog.logV("Error@startActivityMayWaitMethod:" + Log.getStackTraceString(e));
                    } finally {
                        stopWatch.stop();
                    }
                }
            });
            XLog.logV("hookStartActivityMayWait OK: " + unhook);
            moduleBridge.publishFeature(XAppGuardManager.Feature.START);
        } catch (Exception e) {
            XLog.logV("Fail hookStartActivityMayWait:" + e);
            xStatus = XStatus.ERROR;
        }
    }

    Class clzForStartActivityMayWait(XC_LoadPackage.LoadPackageParam lpparam) throws ClassNotFoundException {
        throw new IllegalStateException("Need impl here");
    }

    private void hookAMSShutdown(XC_LoadPackage.LoadPackageParam lpparam) {
        XLog.logV("hookAMSShutdown...");
        try {
            Class ams = XposedHelpers.findClass("com.android.server.am.ActivityManagerService",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(ams, "shutdown", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    moduleBridge.shutdown();
                }
            });
            XLog.logV("hookAMSShutdown OK:" + unHooks);
        } catch (Exception e) {
            XLog.logV("Fail hookAMSShutdown");
            xStatus = XStatus.ERROR;
        }
    }

    private void hookAMSSystemReady(XC_LoadPackage.LoadPackageParam lpparam) {
        XLog.logV("hookAMSSystemReady...");
        try {
            Class ams = XposedHelpers.findClass("com.android.server.am.ActivityManagerService",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(ams, "systemReady", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    moduleBridge.systemReady();
                }
            });
            XLog.logV("hookAMSSystemReady OK:" + unHooks);
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
            Set unHooks = XposedBridge.hookAllMethods(ams, "start", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    Context context = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                    moduleBridge.attachContext(context);
                    moduleBridge.publish();
                }
            });
            XLog.logV("hookAMSStart OK:" + unHooks);
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
            XLog.logV("hookFPService OK:" + unHooks);
            moduleBridge.publishFeature(XAppGuardManager.Feature.FP);
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
                    if (moduleBridge.isBlurForPkg(affinity)
                            && param.getResult() != null) {
                        Bitmap res = (Bitmap) param.args[0];
                        XLog.logV("Blur bitmap start");
                        Bitmap blured = (XBitmapUtil.createBlurredBitmap(res));
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
            Set unHooks = XposedBridge.hookAllMethods(clz,
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
            XLog.logV("hookScreenshotApplications OK:" + unHooks);
            moduleBridge.publishFeature(XAppGuardManager.Feature.BLUR);
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
        if (moduleBridge.isBlurForPkg(pkgName)
                && param.getResult() != null) {
            Bitmap res = (Bitmap) param.getResult();
            stopWatch.split("Blur bitmap start");
            Bitmap blured = (XBitmapUtil.createBlurredBitmap(res));
            if (blured != null)
                param.setResult(blured);
            stopWatch.split("Blur bitmap end");
        }
        stopWatch.stop();
    }

    private void hookPWM(final XC_LoadPackage.LoadPackageParam lpparam) {
        XLog.logV("hookPWM...");
        try {
            Class clz = XposedHelpers.findClass("com.android.server.policy.PhoneWindowManager",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(clz,
                    "interceptKeyBeforeQueueing", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            KeyEvent keyEvent = (KeyEvent) param.args[0];
                            if (keyEvent.getAction() == KeyEvent.ACTION_UP
                                    && (keyEvent.getKeyCode() == KeyEvent.KEYCODE_HOME
                                    || keyEvent.getKeyCode() == KeyEvent.KEYCODE_APP_SWITCH)) {
                                XLog.logV("dispatchUnhandledKey: HOME/APP_SWITCH");
                                moduleBridge.onUserLeaving("HOME");
                            }
                        }
                    });
            XLog.logV("hookPWM OK:" + unHooks);
            moduleBridge.publishFeature(XAppGuardManager.Feature.HOME);
        } catch (Exception e) {
            XLog.logV("Fail hookPWM:" + e);
        }
    }

    private void hookPackageInstaller(XC_LoadPackage.LoadPackageParam lpparam) {
        XLog.logV("hookPackageInstaller...");
        try {
            Class clz = XposedHelpers.findClass("com.android.server.pm.PackageInstallerService",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(clz,
                    "uninstall", new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            try {
                                String pkgName = (String) param.args[0];
                                XLog.logV("PackageInstallerService uninstall pkg:" + pkgName);
                                boolean interrupt = interruptPackageRemoval(pkgName);
                                if (interrupt) {

                                    // FIXME Test fail by adb.
                                    final Context context = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                                    String callingPkgName = (String) param.args[1];
                                    int userID = (int) param.args[4];
//                                    DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
//                                    boolean isDeviceOwner = dpm != null && dpm.isDeviceOwnerAppOnCallingUser(callingPkgName);
                                    IntentSender statusReceiver = (IntentSender) param.args[3];
                                    PackageDeleteObserverAdapter observerAdapter =
                                            new PackageDeleteObserverAdapter(context, statusReceiver, pkgName, userID);
                                    observerAdapter.onPackageDeleted(pkgName, PackageManager.DELETE_FAILED_ABORTED, null);

                                    param.setResult(null);
                                    XLog.logV("PackageInstallerService interruptPackageRemoval");
                                }
                            } catch (Exception e) {
                                XLog.logV("Fail uninstall:" + e);
                            }
                        }
                    });
            XLog.logV("hookPackageInstaller OK:" + unHooks);
            moduleBridge.publishFeature(XAppGuardManager.Feature.HOME);
        } catch (Exception e) {
            XLog.logV("Fail hookPackageInstaller:" + e);
        }
    }

    private void hookPackageManagerService(XC_LoadPackage.LoadPackageParam lpparam) {
        XLog.logV("hookPackageManagerService...");
        try {
            Class clz = XposedHelpers.findClass("com.android.server.pm.PackageManagerService",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(clz,
                    "deletePackageAsUser", new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            try {
                                String pkgName = (String) param.args[0];
                                XLog.logV("PackageManagerService deletePackageAsUser pkg:" + pkgName);
                                boolean interrupt = interruptPackageRemoval(pkgName);
                                if (interrupt) {
                                    Object oo = param.args[1];
                                    XLog.logV("deletePackageAsUser ob:" + oo);
                                    if (oo instanceof IPackageDeleteObserver2) {
                                        IPackageDeleteObserver2 observer2 = (IPackageDeleteObserver2) oo;
                                        observer2.onPackageDeleted(pkgName, PackageManager.DELETE_FAILED_ABORTED, null);
                                    } else if (oo instanceof IPackageDeleteObserver) {
                                        IPackageDeleteObserver observer = (IPackageDeleteObserver) oo;
                                        observer.packageDeleted(pkgName, PackageManager.DELETE_FAILED_ABORTED);
                                    }
                                    param.setResult(null);
                                    XLog.logV("PackageManagerService interruptPackageRemoval");
                                }
                            } catch (Exception e) {
                                XLog.logV("Fail deletePackageAsUser:" + e);
                            }
                        }
                    });
            XLog.logV("hookPackageManagerService OK:" + unHooks);
            moduleBridge.publishFeature(XAppGuardManager.Feature.HOME);
        } catch (Exception e) {
            XLog.logV("Fail hookPackageManagerService:" + e);
        }
    }

    private boolean interruptPackageRemoval(String pkgName) {
        return moduleBridge.interruptPackageRemoval(pkgName);
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        XLog.logV("initZygote...");
    }

    private boolean isLauncherIntent(Intent intent) {
        return intent != null
                && intent.getCategories() != null
                && intent.getCategories().contains("android.intent.category.HOME");
    }

    static class PackageDeleteObserverAdapter extends PackageDeleteObserver {

        private final Context mContext;
        private final IntentSender mTarget;
        private final String mPackageName;

        PackageDeleteObserverAdapter(Context context, IntentSender target,
                                     String packageName, int userId) {
            mContext = context;
            mTarget = target;
            mPackageName = packageName;
        }

        @Override
        public void onUserActionRequired(Intent intent) {
            final Intent fillIn = new Intent();
            fillIn.putExtra(PackageInstaller.EXTRA_PACKAGE_NAME, mPackageName);
            fillIn.putExtra(PackageInstaller.EXTRA_STATUS,
                    PackageInstaller.STATUS_PENDING_USER_ACTION);
            fillIn.putExtra(Intent.EXTRA_INTENT, intent);
            try {
                mTarget.sendIntent(mContext, 0, fillIn, null, null);
            } catch (IntentSender.SendIntentException ignored) {
            }
        }

        @Override
        public void onPackageDeleted(String basePackageName, int returnCode, String msg) {
            final Intent fillIn = new Intent();
            fillIn.putExtra(PackageInstaller.EXTRA_PACKAGE_NAME, mPackageName);
            fillIn.putExtra(PackageInstaller.EXTRA_STATUS,
                    PackageManager.deleteStatusToPublicStatus(returnCode));
            fillIn.putExtra(PackageInstaller.EXTRA_STATUS_MESSAGE,
                    PackageManager.deleteStatusToString(returnCode, msg));
            fillIn.putExtra(PackageInstaller.EXTRA_LEGACY_STATUS, returnCode);
            try {
                mTarget.sendIntent(mContext, 0, fillIn, null, null);
            } catch (IntentSender.SendIntentException ignored) {
                XLog.logF("SendIntentException:" + Log.getStackTraceString(ignored));
            }
        }
    }
}
