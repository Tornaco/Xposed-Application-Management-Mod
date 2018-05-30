package github.tornaco.xposedmoduletest.xposed.submodules.debug;

import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.Method;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import github.tornaco.xposedmoduletest.xposed.submodules.AndroidSubModule;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

// To fix that when bitmap is too large, an app is FC.
public class DisplayListCanvasSubModule extends AndroidSubModule {

    @Override
    public int needMinSdk() {
        return Build.VERSION_CODES.N;
    }

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
        super.initZygote(startupParam);
        hookThrowIfCannotDraw();
    }

    private void hookThrowIfCannotDraw() {
        XposedLog.verbose("DisplayListCanvasSubModule hookThrowIfCannotDraw...");
        try {
            Class clz = XposedHelpers.findClass("android.view.DisplayListCanvas", null);
            @SuppressWarnings("unchecked") Method setContentViewInt = clz.getDeclaredMethod("throwIfCannotDraw", Bitmap.class);
            XC_MethodHook.Unhook unhook = XposedBridge.hookMethod(setContentViewInt, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param)
                        throws Throwable {
                    super.afterHookedMethod(param);
                    Throwable e = param.getThrowable();
                    if (e != null) {
                        Log.w(XposedLog.TAG, "DisplayListCanvasSubModule got throwIfCannotDraw, clearing err");
                        param.setThrowable(null);
                        // Replace the bitmap.
                        Bitmap replacement = null;
                        param.args[0] = replacement;
                    }
                }
            });
            XposedLog.verbose("DisplayListCanvasSubModule hookThrowIfCannotDraw OK:" + unhook);
            setStatus(unhookToStatus(unhook));
        } catch (Exception e) {
            XposedLog.verbose("DisplayListCanvasSubModule Fail hookThrowIfCannotDraw:" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
