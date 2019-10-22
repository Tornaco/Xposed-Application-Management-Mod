package github.tornaco.xposedmoduletest.xposed.submodules;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.renderscript.RenderScriptCacheDir;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.util.BitmapUtil;
import github.tornaco.xposedmoduletest.util.OSUtil;
import github.tornaco.xposedmoduletest.xposed.XAppBuildVar;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.app.XAppLockManager;
import github.tornaco.xposedmoduletest.xposed.bean.BlurTask;
import github.tornaco.xposedmoduletest.xposed.repo.RepoProxy;
import github.tornaco.xposedmoduletest.xposed.util.BlurTaskCache;
import github.tornaco.xposedmoduletest.xposed.util.XBitmapUtil;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

public class RecentBlurSubModule extends AndroidSubModule {
    private static final long REPORT_TOO_LONG_TO_BLUR_IF_TIME_LONGER_THAN = 100;
    private static final boolean RS_BLUR_ENABLED = false;
    private static final Executor BLUR_EXE = Executors.newCachedThreadPool();

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
        // O P here.
        if (OSUtil.isOOrAbove()) {
            hookTaskSnapshotController(lpparam);
            hookGetTaskSnapshot(lpparam);
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
        if (getBridge().isBlurForPkg(pkgName) && param.getResult() != null) {

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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void hookTaskSnapshotController(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.boot("BLUR hookTaskSnapshotController...");

        try {
            Class clz = XposedHelpers.findClass("com.android.server.wm.TaskSnapshotController",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(clz, "snapshotTask", new XC_MethodHook() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    onSnapshotTask(param);
                }
            });
            XposedLog.boot("BLUR hookTaskSnapshotController OK: " + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.boot("BLUR Fail hookTaskSnapshotController: " + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void hookGetTaskSnapshot(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.boot("BLUR hookGetTaskSnapshot...");

        try {
            Class clz = XposedHelpers.findClass("com.android.server.am.ActivityManagerService",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(clz, "getTaskSnapshot", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    ActivityManager.TaskSnapshot orig = (ActivityManager.TaskSnapshot) param.getResult();
                    ActivityManager.TaskSnapshot taskSnapshot = onGetTaskSnapshot(orig, (Integer) param.args[0]);
                    if (taskSnapshot != null) {
                        param.setResult(taskSnapshot);
                    }
                }
            });
            XposedLog.boot("BLUR hookGetTaskSnapshot OK: " + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.boot("BLUR Fail hookGetTaskSnapshot: " + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void blurAndCacheAsync(XC_MethodHook.MethodHookParam param) {
        Object taskObj = param.args[0];
        XposedLog.verbose("BLUR onSnapshotTask, taskObj: " + taskObj);
        int taskId = (int) XposedHelpers.getObjectField(taskObj, "mTaskId");
        XposedLog.verbose("BLUR onSnapshotTask, taskId: " + taskId);
        long startTimeMills = System.currentTimeMillis();
        if (XAPMManager.get().isServiceAvailable()) {
            ComponentName name = XAPMManager.get().componentNameForTaskId(taskId);
            XposedLog.verbose("BLUR onSnapshotTask, name: " + name);
            if (name == null) return;
            String pkgName = name.getPackageName();
            if (getBridge().isBlurForPkg(pkgName)) {
                ActivityManager.TaskSnapshot snapshot = (ActivityManager.TaskSnapshot) param.getResult();
                if (snapshot == null) {
                    return;
                }
                try {
                    Pair<Integer, Integer> screenSize = getBridge().getScreenSize();
                    if (BuildConfig.DEBUG) {
                        XposedLog.verbose("BLUR onSnapshotTask, screenSize: " + screenSize);
                    }
                    if (screenSize == null) {
                        XposedLog.verbose("BLUR onSnapshotTask, no screen size");
                        return;
                    }

                    Bitmap hwBitmap = Bitmap.createHardwareBitmap(snapshot.getSnapshot());
                    XposedLog.verbose("BLUR onSnapshotTask, hwBitmap: " + hwBitmap);
                    if (hwBitmap != null) {
                        BlurTask cachedTask = BlurTask.from(pkgName, blurBitmap(hwBitmap, screenSize));
                        BlurTaskCache.getInstance().put(pkgName, cachedTask);
                        XposedLog.verbose("BLUR onSnapshotTask, bitmap: " + cachedTask.bitmap);
                        XposedHelpers.setObjectField(snapshot, "mSnapshot", cachedTask.bitmap.createGraphicBufferHandle());
                        XposedLog.verbose("BLUR onSnapshotTask, mSnapshot: " + snapshot);

                        long timeTaken = System.currentTimeMillis() - startTimeMills;
                        reportBlurTimeIfNeed(timeTaken);
                    }


                } catch (Exception e) {
                    XposedLog.wtf("Error TaskSnapshotBuilder " + Log.getStackTraceString(e));
                }
            }
        }
    }

    private ActivityManager.TaskSnapshot onGetTaskSnapshot(ActivityManager.TaskSnapshot orig, int taskId) {
        ComponentName name = XAPMManager.get().componentNameForTaskId(taskId);
        XposedLog.verbose("BLUR onGetTaskSnapshot, name: " + name);
        String pkgName = name.getPackageName();
        BlurTask cache = BlurTaskCache.getInstance().get(pkgName);
        if (cache == null || cache.bitmap == null) {
            return null;
        }
        XposedHelpers.setObjectField(orig, "mSnapshot", cache.bitmap.createGraphicBufferHandle());
        return orig;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void onSnapshotTask(final XC_MethodHook.MethodHookParam param) {
        BLUR_EXE.execute(() -> {
            try {
                blurAndCacheAsync(param);
            } catch (Throwable e) {
                XposedLog.wtf("Error occur @BLUR_EXE, while call blurAndCacheAsync: " + Log.getStackTraceString(e));
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Bitmap blurBitmap(Bitmap hwBitmap, Pair<Integer, Integer> screenSize) {
        if (RS_BLUR_ENABLED) {
            return rsBlur(hwBitmap, screenSize);
        } else {
            return jBlur(hwBitmap, screenSize);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Deprecated
    private Bitmap rsBlur(Bitmap hwBitmap, Pair<Integer, Integer> screenSize) {
        setupRsCache(getBridge().getContext());
        Bitmap swBitmap = hwBitmap.copy(Bitmap.Config.ARGB_8888, false);
        XposedLog.verbose("BLUR rsBlur, copy done");
        swBitmap = BitmapUtil.createScaledBitmap(swBitmap, screenSize.first / 2, screenSize.second / 2);
        XposedLog.verbose("BLUR rsBlur, scale down done");
        int br = XAppLockManager.get().getBlurRadius();
        swBitmap = XBitmapUtil.rsBlur(getBridge().getContext(), swBitmap, br);
        XposedLog.verbose("BLUR rsBlur, blur done");
        swBitmap = BitmapUtil.createScaledBitmap(swBitmap, screenSize.first, screenSize.second);
        XposedLog.verbose("BLUR rsBlur, scale up done");
        Bitmap newHwBitmap = swBitmap.copy(Bitmap.Config.HARDWARE, false);
        XposedLog.verbose("BLUR rsBlur, copy done");
        swBitmap.recycle();
        hwBitmap.recycle();
        return newHwBitmap;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Bitmap jBlur(Bitmap hwBitmap, Pair<Integer, Integer> screenSize) {
        setupRsCache(getBridge().getContext());
        Bitmap swBitmap = hwBitmap.copy(Bitmap.Config.ARGB_8888, false);
        XposedLog.verbose("BLUR jBlur, copy done");
        int br = XAppLockManager.get().getBlurRadius();
        swBitmap = XBitmapUtil.createBlurredBitmap(swBitmap, br, XBitmapUtil.BITMAP_SCALE);
        XposedLog.verbose("BLUR jBlur, blur done");
        swBitmap = BitmapUtil.createScaledBitmap(swBitmap, screenSize.first, screenSize.second);
        XposedLog.verbose("BLUR jBlur, scale done");
        Bitmap newHwBitmap = swBitmap.copy(Bitmap.Config.HARDWARE, false);
        XposedLog.verbose("BLUR jBlur, copy done");
        swBitmap.recycle();
        hwBitmap.recycle();
        return newHwBitmap;
    }

    private void reportBlurTimeIfNeed(long timeMills) {
        XposedLog.verbose("BLUR reportBlurTimeIfNeed, time taken: " + timeMills);
    }

    private void setupRsCache(Context context) {
        RenderScriptCacheDir.setupDiskCache(RepoProxy.getRsCacheDir());
    }
}
