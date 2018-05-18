package github.tornaco.xposedmoduletest.xposed.submodules;

import android.content.Context;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.WindowManagerPolicy;

import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.util.OSUtil;
import github.tornaco.xposedmoduletest.xposed.XAppBuildVar;
import github.tornaco.xposedmoduletest.xposed.service.policy.PhoneWindowManagerProxy;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */
class PWMInterceptKeySubModule extends AndroidSubModule {

    private static final String EVENT_SOURCE = "PWMInterceptKeySubModule";

    @Override
    public String needBuildVar() {
        return XAppBuildVar.APP_RFK;
    }

    private static final int ACTION_PASS_TO_USER = WindowManagerPolicy.ACTION_PASS_TO_USER;

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookPhoneWindowManagerConstruct(lpparam);
        hookInterceptKeyBeforeQueueing(lpparam);
        hookPhoneWindowManagerInit(lpparam);
        hookPhoneWindowManagerSetInitialSize(lpparam);
    }

    private void hookPhoneWindowManagerConstruct(final XC_LoadPackage.LoadPackageParam lpparam) {
        logOnBootStage("hookPhoneWindowManagerConstruct...");
        try {
            Class clz =
                    OSUtil.isMOrAbove() ?
                            XposedHelpers.findClass("com.android.server.policy.PhoneWindowManager",
                                    lpparam.classLoader)
                            : XposedHelpers.findClass("com.android.internal.policy.impl.PhoneWindowManager",
                            lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllConstructors(clz, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    logOnBootStage("PhoneWindowManager constructing...");
                    PhoneWindowManagerProxy phoneWindowManagerProxy = new PhoneWindowManagerProxy(param.thisObject);
                    getBridge().attachPhoneWindowManager(phoneWindowManagerProxy);
                }
            });
            logOnBootStage("hookPhoneWindowManagerConstruct OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Throwable e) {
            logOnBootStage("Fail hookPhoneWindowManagerConstruct:" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }

    private void hookPhoneWindowManagerInit(final XC_LoadPackage.LoadPackageParam lpparam) {
        logOnBootStage("hookPhoneWindowManagerInit...");
        try {
            Class clz =
                    OSUtil.isMOrAbove() ?
                            XposedHelpers.findClass("com.android.server.policy.PhoneWindowManager",
                                    lpparam.classLoader)
                            : XposedHelpers.findClass("com.android.internal.policy.impl.PhoneWindowManager",
                            lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(clz, "init", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    logOnBootStage("hookPhoneWindowManagerInit init...");
                    Context context = (Context) param.args[0];
                    WindowManagerPolicy.WindowManagerFuncs windowManagerFuncs = (WindowManagerPolicy.WindowManagerFuncs) param.args[2];
                    getBridge().initPhoneWindowManager(context, windowManagerFuncs);
                }
            });
            logOnBootStage("hookPhoneWindowManagerInit OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Throwable e) {
            logOnBootStage("Fail hookPhoneWindowManagerInit:" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }

    private void hookPhoneWindowManagerSetInitialSize(final XC_LoadPackage.LoadPackageParam lpparam) {
        logOnBootStage("hookPhoneWindowManagerSetInitialSize...");
        try {
            Class clz =
                    OSUtil.isMOrAbove() ?
                            XposedHelpers.findClass("com.android.server.policy.PhoneWindowManager",
                                    lpparam.classLoader)
                            : XposedHelpers.findClass("com.android.internal.policy.impl.PhoneWindowManager",
                            lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(clz, "setInitialDisplaySize",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            logOnBootStage("hookPhoneWindowManagerSetInitialSize setInitialDisplaySize...");
                            getBridge().onPhoneWindowManagerSetInitialDisplaySize((Display) param.args[0]);
                        }
                    });
            logOnBootStage("hookPhoneWindowManagerSetInitialSize OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Throwable e) {
            logOnBootStage("Fail hookPhoneWindowManagerSetInitialSize:" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }

    private void hookInterceptKeyBeforeQueueing(final XC_LoadPackage.LoadPackageParam lpparam) {
        logOnBootStage("hookInterceptKeyBeforeQueueing...");
        try {
            Class clz =
                    OSUtil.isMOrAbove() ?
                            XposedHelpers.findClass("com.android.server.policy.PhoneWindowManager",
                                    lpparam.classLoader)
                            : XposedHelpers.findClass("com.android.internal.policy.impl.PhoneWindowManager",
                            lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(clz,
                    "interceptKeyBeforeQueueing", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            KeyEvent keyEvent = (KeyEvent) param.args[0];
                            getBridge().onKeyEvent(keyEvent, EVENT_SOURCE);
                        }
                    });
            logOnBootStage("hookInterceptKeyBeforeQueueing OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            logOnBootStage("Fail hookInterceptKeyBeforeQueueing:" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
