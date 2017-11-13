package github.tornaco.xposedmoduletest.xposed.submodules;

import android.app.ActivityManagerNative;
import android.content.ComponentName;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.xposed.app.XAppGuardManager;
import github.tornaco.xposedmoduletest.xposed.util.XBitmapUtil;
import github.tornaco.xposedmoduletest.xposed.util.XLog;
import github.tornaco.xposedmoduletest.xposed.util.XStopWatch;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

public class ScreenshotApplicationsSubModule extends AppGuardAndroidSubModule {
    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookScreenshotApplications(lpparam);
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
            getBridge().publishFeature(XAppGuardManager.Feature.BLUR);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XLog.logV("Fail hookScreenshotApplications:" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
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
        if (getAppGuardBridge().isBlurForPkg(pkgName)
                && param.getResult() != null) {
            Bitmap res = (Bitmap) param.getResult();
            stopWatch.split("Blur bitmap start");
            Bitmap blured = (XBitmapUtil.createBlurredBitmap(res,
                    XBitmapUtil.BLUR_RADIUS, XBitmapUtil.BITMAP_SCALE));
            if (blured != null)
                param.setResult(blured);
            stopWatch.split("Blur bitmap end");
        }
        stopWatch.stop();
    }
}
