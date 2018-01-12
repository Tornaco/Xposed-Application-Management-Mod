package github.tornaco.xposedmoduletest.xposed.submodules;

import android.app.ActivityManagerNative;
import android.content.ComponentName;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.util.OSUtil;
import github.tornaco.xposedmoduletest.xposed.util.XBitmapUtil;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

public class ScreenshotApplicationsSubModule extends AndroidSubModule {
    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        if (!OSUtil.isOOrAbove()) {
            hookScreenshotApplicationsForNAndBelow(lpparam);
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
            Bitmap blured = (XBitmapUtil.createBlurredBitmap(res));
            if (blured != null)
                param.setResult(blured);
        } else {
            XposedLog.verbose("onScreenshotApplicationsNAndBelow, blur is disabled...");
        }
    }
}
