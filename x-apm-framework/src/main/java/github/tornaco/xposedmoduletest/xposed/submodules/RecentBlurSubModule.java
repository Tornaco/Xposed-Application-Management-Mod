package github.tornaco.xposedmoduletest.xposed.submodules;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.AndroidAppHelper;
import android.content.ComponentName;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.GraphicBuffer;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.android.internal.graphics.palette.Palette;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.util.BitmapUtil;
import github.tornaco.xposedmoduletest.util.DeviceUtils;
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

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;
import static android.graphics.GraphicBuffer.USAGE_HW_TEXTURE;
import static android.graphics.GraphicBuffer.USAGE_SW_READ_RARELY;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

public class RecentBlurSubModule extends AndroidSubModule {

    private static final String ENABLE_TASK_SNAPSHOTS_PROP = "persist.enable_task_snapshots";
    private static final int MASK_COLOR = Color.WHITE;

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
        // P here.
        if (OSUtil.isPOrAbove()) {
            hookTaskSnapshotController(lpparam);
        }
    }

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
        super.initZygote(startupParam);
        // For O.
        if (OSUtil.isO()) {
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

    private void hookTaskSnapshotController(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.boot("BLUR hookTaskSnapshotController...");

        try {
            Class clz = XposedHelpers.findClass("com.android.server.wm.TaskSnapshotController",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(clz, "snapshotTask", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
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

    private void onSnapshotTask(XC_MethodHook.MethodHookParam param) {
        Object taskObj = param.args[0];
        XposedLog.verbose("BLUR onSnapshotTask, taskObj: " + taskObj);
        int taskId = (int) XposedHelpers.getObjectField(taskObj, "mTaskId");
        XposedLog.verbose("BLUR onSnapshotTask, taskId: " + taskId);
        if (XAPMManager.get().isServiceAvailable()) {
            ComponentName name = XAPMManager.get().componentNameForTaskId(taskId);
            XposedLog.verbose("BLUR onSnapshotTask, name: " + name);
            String pkgName = name.getPackageName();
            if (getBridge().isBlurForPkg(pkgName)) {
                try {
                    Pair<Integer, Integer> screenSize = getBridge().getScreenSize();
                    if (BuildConfig.DEBUG) {
                        XposedLog.verbose("BLUR onSnapshotTask, screenSize: " + screenSize);
                    }
                    if (screenSize == null) {
                        XposedLog.verbose("BLUR onSnapshotTask, no screen size");
                        return;
                    }
                    BlurTask cachedTask = BlurTaskCache.getInstance().get(pkgName);
                    if (cachedTask == null) {
                        Bitmap icon = getBridge().getAppIconBitmap(pkgName);
                        if (icon != null) {
                            icon = BitmapUtil.createScaledBitmap(icon, screenSize.first / 6, screenSize.first / 6);
                            int dominant = DeviceUtils.isFastDevice() ? getDominantColor(icon) : MASK_COLOR;
                            cachedTask = BlurTask.from(pkgName, icon, dominant);
                            BlurTaskCache.getInstance().put(pkgName, cachedTask);
                        }
                    }
                    if (cachedTask == null || cachedTask.bitmap == null) {
                        XposedLog.verbose("BLUR onSnapshotTask, cachedTask.bitmap");
                        return;
                    }
                    XposedLog.verbose("BLUR onSnapshotTask, icon bitmap: " + cachedTask.bitmap);
                    ActivityManager.TaskSnapshot snapshot = new TaskSnapshotBuilder()
                            .setRect(new Rect(0, 0, screenSize.first, screenSize.second))
                            .setScreenSize(screenSize)
                            .setDominantColor((Integer) cachedTask.obj)
                            .setBitmap(cachedTask.bitmap)
                            .build();
                    param.setResult(snapshot);
                } catch (Exception e) {
                    XposedLog.wtf("Error TaskSnapshotBuilder " + Log.getStackTraceString(e));
                }
            }
        }
    }

    private int getDominantColor(Bitmap source) {
        try {
            Palette palette = Palette.from(source).generate();
            int dominant = palette.getDominantColor(MASK_COLOR);
            String dominantStr = String.format("#%06X", 0xFFFFFF & dominant);
            return Color.parseColor(dominantStr);
        } catch (Throwable e) {
            return MASK_COLOR;
        }
    }

    /**
     * Builds a TaskSnapshot, only used for Android P.
     */
    @TargetApi(28)
    static class TaskSnapshotBuilder {
        private static final Paint ICON_PAINT = new Paint();

        private float mScale = 1f;
        private boolean mIsRealSnapshot = true;
        private boolean mIsTranslucent = false;
        // WINDOWING_MODE_FULLSCREEN = 1;
        private int mWindowingMode = 1;
        private int mSystemUiVisibility = 0;
        private Rect mRect;
        private Pair<Integer, Integer> mScreenSize;
        private Bitmap mBitmap;
        private int mDominantColor;

        public TaskSnapshotBuilder setDominantColor(int dominantColor) {
            this.mDominantColor = dominantColor;
            return this;
        }

        public TaskSnapshotBuilder setBitmap(Bitmap bitmap) {
            this.mBitmap = bitmap;
            return this;
        }

        public TaskSnapshotBuilder setScreenSize(Pair<Integer, Integer> screenSize) {
            this.mScreenSize = screenSize;
            return this;
        }

        public TaskSnapshotBuilder setRect(Rect rect) {
            this.mRect = rect;
            return this;
        }

        public TaskSnapshotBuilder setScale(float scale) {
            mScale = scale;
            return this;
        }

        public TaskSnapshotBuilder setIsRealSnapshot(boolean isRealSnapshot) {
            mIsRealSnapshot = isRealSnapshot;
            return this;
        }

        public TaskSnapshotBuilder setIsTranslucent(boolean isTranslucent) {
            mIsTranslucent = isTranslucent;
            return this;
        }

        public TaskSnapshotBuilder setWindowingMode(int windowingMode) {
            mWindowingMode = windowingMode;
            return this;
        }

        public TaskSnapshotBuilder setSystemUiVisibility(int systemUiVisibility) {
            mSystemUiVisibility = systemUiVisibility;
            return this;
        }

        public ActivityManager.TaskSnapshot build() throws Exception {
            @SuppressWarnings("PointlessBitwiseExpression") final GraphicBuffer buffer = GraphicBuffer.create(
                    mScreenSize.first,
                    mScreenSize.second,
                    PixelFormat.RGBA_8888,
                    USAGE_HW_TEXTURE | USAGE_SW_READ_RARELY);
            Canvas c = buffer.lockCanvas();
            c.drawColor(mDominantColor);
            // Insert icon.
            float iconW = mBitmap.getWidth();
            float iconH = mBitmap.getHeight();
            float left = ((float) mScreenSize.first - iconW) / 2.0f;
            float top = ((float) mScreenSize.second - iconH) / 2.0f;
            c.drawBitmap(mBitmap, left, top, ICON_PAINT);
            buffer.unlockCanvasAndPost(c);
            if (OSUtil.isPOrAbove()) {
                return newSnapshotP(buffer, ORIENTATION_PORTRAIT, mRect,
                        mScale < 1f /* reducedResolution */, mScale, mIsRealSnapshot, mWindowingMode,
                        mSystemUiVisibility, mIsTranslucent);
            }
            if (OSUtil.isO()) {
                return new ActivityManager.TaskSnapshot(buffer, ORIENTATION_PORTRAIT, mRect, mScale < 1f, mScale);
            }
            return null;
        }

        @SuppressWarnings("SameParameterValue")
        private ActivityManager.TaskSnapshot newSnapshotP(
                GraphicBuffer snapshot,
                int orientation,
                Rect contentInsets,
                boolean reducedResolution,
                float scale,
                boolean isRealSnapshot,
                int windowingMode,
                int systemUiVisibility,
                boolean isTranslucent) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
            Class<ActivityManager.TaskSnapshot> snapshotClass = ActivityManager.TaskSnapshot.class;
            @SuppressWarnings("JavaReflectionMemberAccess")
            Constructor<ActivityManager.TaskSnapshot> constructor = snapshotClass.getConstructor(
                    GraphicBuffer.class,
                    int.class,
                    Rect.class,
                    boolean.class,
                    float.class,
                    boolean.class,
                    int.class,
                    int.class,
                    boolean.class);
            if (constructor != null) {
                return constructor.newInstance(snapshot, orientation, contentInsets,
                        reducedResolution, scale, isRealSnapshot, windowingMode,
                        systemUiVisibility, isTranslucent);
            }

            return null;
        }
    }
}
