package github.tornaco.xposedmoduletest.xposed.submodules;

import android.util.Log;
import android.view.KeyEvent;

import java.util.Set;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.xposed.XAppBuildVar;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */
public class InputManagerInjectInputSubModule extends AndroidSubModule {

    public static final String EVENT_SOURCE = "InputManagerInjectInputSubModule";
    public static final String EVENT_SOURCE_NATIVE = "InputManagerSubModuleNative";

    @Override
    public String needBuildVar() {
        return XAppBuildVar.APP_RFK;
    }

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
        super.initZygote(startupParam);
        hookInjectInputEvent();
    }

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        super.handleLoadingPackage(pkg, lpparam);
        hookNativeInjectInputEvent(lpparam);
    }

    private void hookInjectInputEvent() {
        logOnBootStage("hookInjectInputEvent...");
        try {
            Class clz = XposedHelpers.findClass("android.hardware.input.InputManager", null);
            Set unHooks = XposedBridge.hookAllMethods(clz,
                    "injectInputEvent", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param)
                                throws Throwable {
                            super.afterHookedMethod(param);

                            Throwable e = param.getThrowable();
                            if (e != null) {
                                // This event will not be handle.
                                return;
                            }

                            KeyEvent keyEvent = (KeyEvent) param.args[0];
                            getBridge().onKeyEvent(keyEvent, EVENT_SOURCE);
                        }
                    });
            logOnBootStage("hookInjectInputEvent OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            logOnBootStage("Fail hookInjectInputEvent:" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }

    private void hookNativeInjectInputEvent(XC_LoadPackage.LoadPackageParam lpparam) {
        logOnBootStage("hookNativeInjectInputEvent...");
        try {
            Class clz = XposedHelpers.findClass("com.android.server.input.InputManagerService",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(clz,
                    "nativeInjectInputEvent", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param)
                                throws Throwable {
                            super.afterHookedMethod(param);

                            Throwable e = param.getThrowable();
                            if (e != null) {
                                // This event will not be handle.
                                return;
                            }
                            try {

                                KeyEvent keyEvent = (KeyEvent) param.args[1];
                                getBridge().onKeyEvent(keyEvent, EVENT_SOURCE_NATIVE);
                            } catch (Throwable err) {
                                XposedLog.verbose("nativeInjectInputEvent: " + e);
                            }
                        }
                    });
            logOnBootStage("hookNativeInjectInputEvent OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            logOnBootStage("Fail hookNativeInjectInputEvent:" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
