package github.tornaco.xposedmoduletest.xposed.submodules;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.AndroidAppHelper;
import android.content.ComponentName;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Set;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.util.OSUtil;
import github.tornaco.xposedmoduletest.xposed.XAppBuildVar;
import github.tornaco.xposedmoduletest.xposed.app.XAppGuardManager;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;
import github.tornaco.xposedmoduletest.xposed.util.XBitmapUtil;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

public class ScreenshotApplicationsSubModule extends AndroidSubModule {

    @Override
    public String needBuildVar() {
        return XAppBuildVar.APP_BLUR;
    }

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        // For N and below android.
        if (!OSUtil.isOOrAbove()) {
            hookScreenshotApplicationsForNAndBelow(lpparam);
        }
    }

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
        super.initZygote(startupParam);

        if (OSUtil.isOOrAbove()) {
            hookSystemProp_ENABLE_TASK_SNAPSHOTS();
            hookGetThumbForOreo();
        }
    }

    private void hookSystemProp_ENABLE_TASK_SNAPSHOTS() {
        XposedLog.boot("hookSystemProp_ENABLE_TASK_SNAPSHOTS...");
        try {
            Class clz = XposedHelpers.findClass("android.os.SystemProperties", null);
            Set unHooks = XposedBridge.hookAllMethods(clz, "getBoolean", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);

                    try {
                        if ("persist.enable_task_snapshots".equals(param.args[0])) {

                            File systemFile = new File(Environment.getDataDirectory(), "system");
                            File dir = new File(systemFile, "tor_apm");
                            if (!dir.exists()) {
                                dir = new File(systemFile, "tor");
                            }
                            File indicatorFile = new File(dir, "blur_indicator");
                            boolean blurEnabledOreo = indicatorFile.exists();

                            XposedLog.boot("blur_indicator " + blurEnabledOreo);
                            if (!blurEnabledOreo) return;

                            XposedLog.boot("hookSystemProp_ENABLE_TASK_SNAPSHOTS caller:"
                                    + AndroidAppHelper.currentPackageName());
                            if (true) {
                                param.setResult(false);
                                XposedLog.boot("hookSystemProp_ENABLE_TASK_SNAPSHOTS returning false");
                            }
                        }
                    } catch (Throwable e) {
                        XposedLog.boot("Fail handle SystemProp_ENABLE_TASK_SNAPSHOTS: " + e);
                    }
                }
            });
            XposedLog.boot("hookSystemProp_ENABLE_TASK_SNAPSHOTS OK: " + unhooksToStatus(unHooks));
            final boolean ENABLE_TASK_SNAPSHOT = ActivityManager.ENABLE_TASK_SNAPSHOTS;
            XposedLog.boot("hookSystemProp_ENABLE_TASK_SNAPSHOTS AFTER: " + ENABLE_TASK_SNAPSHOT);
        } catch (Exception e) {
            XposedLog.boot("Fail hookSystemProp_ENABLE_TASK_SNAPSHOTS: " + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }

    private void hookGetThumbForOreo() {
        XposedLog.boot("hookGetThumbForOreo...");

        try {
            Class clz = XposedHelpers.findClass("android.app.ActivityManager",
                    null);

            Set unHooks = XposedBridge.hookAllMethods(clz,
                    "getTaskThumbnail", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            try {
                                if (!XAshmanManager.get().isServiceAvailable()
                                        || !XAppGuardManager.get().isBlurEnabled()) {
                                    return;
                                }

                                String caller = AndroidAppHelper.currentPackageName();
                                int taskId = (int) param.args[0];
                                String pkg = XAshmanManager.get().packageForTaskId(taskId);
                                XposedLog.verbose("getTaskThumbnail caller %s task %s", caller, pkg);

                                if (pkg == null) {
                                    return;
                                }

                                boolean blur = XAppGuardManager.get().isBlurEnabledForPackage(pkg);
                                if (!blur) {
                                    XposedLog.verbose("isBlurEnabledForPackage false");
                                    return;
                                }


                                ActivityManager.TaskThumbnail tt = (ActivityManager.TaskThumbnail) param.getResult();
                                int br = XAppGuardManager.get().getBlurRadius();
                                tt.mainThumbnail = XBitmapUtil.createBlurredBitmap(tt.mainThumbnail, br, XBitmapUtil.BITMAP_SCALE);
                                param.setResult(tt);
                                XposedLog.verbose("Thumb replaced!");
                            } catch (Throwable e) {
                                XposedLog.wtf("Fail replace thumb: " + Log.getStackTraceString(e));
                            }
                        }
                    });
            XposedLog.boot("hookGetThumbForOreo OK: " + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.boot("Fail hookGetThumbForOreo: " + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }

    /**
     * @see #onScreenshotApplicationsNAndBelow(XC_MethodHook.MethodHookParam)
     */
    private void hookScreenshotApplicationsForNAndBelow(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.boot("hookScreenshotApplicationsForNAndBelow...");

        try {
            Class clz = XposedHelpers.findClass("com.android.server.wm.WindowManagerService",
                    lpparam.classLoader);

            // Dump all methods in WMS.
            if (OSUtil.isMIUI()) {
                try {
                    for (Method m : clz.getDeclaredMethods()) {
                        XposedLog.boot("WindowManagerService method: " + m);
                    }
                } catch (Exception ignored) {

                }
            }

            Set unHooks = XposedBridge.hookAllMethods(clz,
                    "screenshotApplications", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            try {
                                onScreenshotApplicationsNAndBelow(param);
                            } catch (Exception e) {
                                XposedLog.boot("Fail onScreenshotApplicationsNAndBelow: " + e);
                            }
                        }
                    });
            XposedLog.boot("hookScreenshotApplicationsForNAndBelow OK: " + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.boot("Fail hookScreenshotApplicationsForNAndBelow: " + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }

    private void onScreenshotApplicationsNAndBelow(XC_MethodHook.MethodHookParam param) throws RemoteException {

        IBinder token = (IBinder) param.args[0];
        ComponentName activityClassForToken = ActivityManagerNative.getDefault().getActivityClassForToken(token);
        String pkgName = activityClassForToken == null ? null : activityClassForToken.getPackageName();

        if (TextUtils.isEmpty(pkgName)) {
            return;
        }

        XposedLog.verbose("onScreenshotApplicationsNAndBelow: " + pkgName);
        if (getBridge().isBlurForPkg(pkgName)
                && param.getResult() != null) {
            Bitmap res = (Bitmap) param.getResult();
            XposedLog.verbose("onScreenshotApplicationsNAndBelow. res: " + res);
            int radius = getBridge().getBlurRadius();
            float scale = getBridge().getBlurScale();
            XposedLog.verbose("onScreenshotApplicationsNAndBelow, bluring, r and s: " + radius + "-" + scale);
            Bitmap blured = (XBitmapUtil.createBlurredBitmap(res, radius, scale));
            if (blured != null)
                param.setResult(blured);
        } else {
            XposedLog.verbose("onScreenshotApplicationsNAndBelow, blur is disabled...");
        }
    }
}
