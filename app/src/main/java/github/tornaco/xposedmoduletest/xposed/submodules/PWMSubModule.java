package github.tornaco.xposedmoduletest.xposed.submodules;

import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManagerPolicy;

import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.xposed.XAppBuildVar;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */
class PWMSubModule extends AndroidSubModuleModule {

    private static final String EVENT_SOURCE = "PWMSubModule";

    @Override
    public String needBuildVar() {
        return XAppBuildVar.APP_RFK;
    }

    private static final int ACTION_PASS_TO_USER = WindowManagerPolicy.ACTION_PASS_TO_USER;

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookInterceptKeyBeforeQueueing(lpparam);
    }

    private void hookInterceptKeyBeforeQueueing(final XC_LoadPackage.LoadPackageParam lpparam) {
        logOnBootStage("hookInterceptKeyBeforeQueueing...");
        try {
            Class clz = XposedHelpers.findClass("com.android.server.policy.PhoneWindowManager",
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
