package github.tornaco.xposedmoduletest.xposed.submodules;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.AndroidAppHelper;
import android.content.ComponentName;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.Set;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.util.OSUtil;
import github.tornaco.xposedmoduletest.xposed.XAppBuildVar;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.app.XAppLockManager;
import github.tornaco.xposedmoduletest.xposed.bean.BlurTask;
import github.tornaco.xposedmoduletest.xposed.repo.RepoProxy;
import github.tornaco.xposedmoduletest.xposed.util.BlurTaskCache;
import github.tornaco.xposedmoduletest.xposed.util.XBitmapUtil;
import github.tornaco.xposedmoduletest.xposed.util.XStopWatch;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

public class ScreenshotApplicationsSubModule extends AndroidSubModule {

    private static final String ENABLE_TASK_SNAPSHOTS_PROP = "persist.enable_task_snapshots";

    private final static BitmapFactory.Options sBitmapOptions;

    static {
        sBitmapOptions = new BitmapFactory.Options();
        sBitmapOptions.inMutable = true;
        sBitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565;
    }

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

        if (OSUtil.isOOrAbove() && !OSUtil.isPOrAbove()) {
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
                        if (ENABLE_TASK_SNAPSHOTS_PROP.equals(param.args[0])) {

                            boolean blurEnabledOreo = RepoProxy.hasFileIndicator("blur_indicator");
                            XposedLog.boot("blur_indicator " + blurEnabledOreo);
                            if (blurEnabledOreo) {
                                XposedLog.boot("BLUR hookSystemProp_ENABLE_TASK_SNAPSHOTS caller:"
                                        + AndroidAppHelper.currentPackageName());
                                param.setResult(false);
                                XposedLog.boot("BLUR hookSystemProp_ENABLE_TASK_SNAPSHOTS returning false");
                            }
                        }
                    } catch (Throwable e) {
                        XposedLog.boot("Fail handle SystemProp_ENABLE_TASK_SNAPSHOTS: " + e);
                    }
                }
            });
            XposedLog.boot("BLUR hookSystemProp_ENABLE_TASK_SNAPSHOTS OK: " + unhooksToStatus(unHooks));
            final boolean ENABLE_TASK_SNAPSHOT = ActivityManager.ENABLE_TASK_SNAPSHOTS;
            XposedLog.boot("BLUR hookSystemProp_ENABLE_TASK_SNAPSHOTS AFTER: " + ENABLE_TASK_SNAPSHOT);
        } catch (Exception e) {
            XposedLog.boot("BLUR Fail hookSystemProp_ENABLE_TASK_SNAPSHOTS: " + e);
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

                            // Bench.
                            XStopWatch xStopWatch = null;
                            if (BuildConfig.DEBUG) {
                                xStopWatch = XStopWatch.start("BLUR");
                            }

                            try {

                                if (!XAPMManager.get().isServiceAvailable()
                                        || !XAppLockManager.get().isBlurEnabled()) {
                                    return;
                                }

                                String caller = AndroidAppHelper.currentPackageName();
                                int taskId = (int) param.args[0];
                                String pkg = XAPMManager.get().packageForTaskId(taskId);
                                XposedLog.verbose("BLUR getTaskThumbnail caller %s task %s", caller, pkg);

                                if (pkg == null) {
                                    return;
                                }

                                boolean blur = XAppLockManager.get().isBlurEnabledForPackage(pkg);
                                if (!blur) {
                                    XposedLog.verbose("BLUR isBlurEnabledForPackage is false");
                                    return;
                                }

                                // Query from cache.
                                BlurTaskCache cache = BlurTaskCache.getInstance();
                                BlurTask cachedTask = cache.get(pkg);
                                if (BuildConfig.DEBUG) {
                                    XposedLog.verbose("BLUR getTaskThumbnail cachedTask: " + cachedTask);
                                }

                                ActivityManager.TaskThumbnail tt = (ActivityManager.TaskThumbnail) param.getResult();
                                if (tt == null) {
                                    XposedLog.verbose("BLUR TaskThumbnail is null");
                                    return;
                                }

                                if (cachedTask != null) {
                                    tt.mainThumbnail = cachedTask.bitmap;
                                    param.setResult(tt);
                                    XposedLog.verbose("BLUR getTaskThumbnail using cached: " + cachedTask);
                                    return;
                                }

                                if (BuildConfig.DEBUG) {
                                    XposedLog.verbose("BLUR getTaskThumbnail mainThumbnail: " + tt.mainThumbnail);
                                    XposedLog.verbose("BLUR getTaskThumbnail thumbnailFileDescriptor: " + tt.thumbnailFileDescriptor);
                                    XposedLog.verbose("BLUR getTaskThumbnail thumbnailInfo: " + tt.thumbnailInfo);
                                }

                                int br = XAppLockManager.get().getBlurRadius();
                                Bitmap source = tt.mainThumbnail;
                                if (source == null) {
                                    XposedLog.verbose("BLUR getTaskThumbnail source is null, Try decode with fd.");
                                    if (tt.thumbnailFileDescriptor != null) {
                                        XposedLog.verbose("BLUR getTaskThumbnail source is null, Try decode with op: " + sBitmapOptions);
                                        source = BitmapFactory.decodeFileDescriptor(
                                                tt.thumbnailFileDescriptor.getFileDescriptor(),
                                                null, sBitmapOptions);
                                        if (source != null) {
                                            XposedLog.verbose("BLUR getTaskThumbnail source is null, Got decoded: " + source);
                                            tt.thumbnailFileDescriptor = null;
                                        }
                                    }
                                }
                                tt.mainThumbnail = XBitmapUtil.createBlurredBitmap(source, br, XBitmapUtil.BITMAP_SCALE);
                                // Save to cache.
                                cache.put(pkg, BlurTask.from(pkg, tt.mainThumbnail));
                                param.setResult(tt);
                                XposedLog.verbose("BLUR getTaskThumbnail Thumb replaced!");
                            } catch (Throwable e) {
                                XposedLog.wtf("BLUR Fail replace thumb: " + Log.getStackTraceString(e));
                            } finally {
                                if (xStopWatch != null) {
                                    xStopWatch.stop();
                                }
                            }
                        }
                    });
            XposedLog.boot("BLUR hookGetThumbForOreo OK: " + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.boot("BLUR Fail hookGetThumbForOreo: " + e);
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
                        XposedLog.boot("BLUR WindowManagerService method: " + m);
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
                                XposedLog.boot("BLUR Fail onScreenshotApplicationsNAndBelow: " + e);
                            }
                        }
                    });
            XposedLog.boot("BLUR hookScreenshotApplicationsForNAndBelow OK: " + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.boot("BLUR Fail hookScreenshotApplicationsForNAndBelow: " + e);
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

        XposedLog.verbose("BLUR onScreenshotApplicationsNAndBelow: " + pkgName);
        if (getBridge().isBlurForPkg(pkgName)
                && param.getResult() != null) {

            // Query from cache.
            BlurTaskCache cache = BlurTaskCache.getInstance();
            BlurTask cachedTask = cache.get(pkgName);
            if (BuildConfig.DEBUG) {
                XposedLog.verbose("BLUR onScreenshotApplicationsNAndBelow cachedTask: " + cachedTask);
            }

            if (cachedTask != null) {
                param.setResult(cachedTask.bitmap);
                XposedLog.verbose("BLUR onScreenshotApplicationsNAndBelow using cached: " + cachedTask);
                return;
            }

            Bitmap res = (Bitmap) param.getResult();
            XposedLog.verbose("BLUR onScreenshotApplicationsNAndBelow. res: " + res);
            int radius = getBridge().getBlurRadius();
            float scale = getBridge().getBlurScale();
            XposedLog.verbose("BLUR onScreenshotApplicationsNAndBelow, bluring, r and s: " + radius + "-" + scale);
            Bitmap blured = (XBitmapUtil.createBlurredBitmap(res, radius, scale));
            if (blured != null) {
                param.setResult(blured);
                cache.put(pkgName, BlurTask.from(pkgName, blured));
            }
        } else {
            XposedLog.verbose("BLUR onScreenshotApplicationsNAndBelow, blur is disabled...");
        }
    }
}
