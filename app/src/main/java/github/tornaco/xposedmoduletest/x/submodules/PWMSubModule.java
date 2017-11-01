package github.tornaco.xposedmoduletest.x.submodules;

import android.util.Log;
import android.view.KeyEvent;

import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.x.app.XAppGuardManager;
import github.tornaco.xposedmoduletest.x.util.XLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

class PWMSubModule extends AndroidSubModuleModule {
    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookPWM(lpparam);
    }

    private void hookPWM(final XC_LoadPackage.LoadPackageParam lpparam) {
        XLog.logV("hookPWM...");
        try {
            Class clz = XposedHelpers.findClass("com.android.server.policy.PhoneWindowManager",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(clz,
                    "interceptKeyBeforeQueueing", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            KeyEvent keyEvent = (KeyEvent) param.args[0];
                            if (keyEvent.getAction() == KeyEvent.ACTION_UP
                                    && (keyEvent.getKeyCode() == KeyEvent.KEYCODE_HOME
                                    || keyEvent.getKeyCode() == KeyEvent.KEYCODE_APP_SWITCH)) {
                                XLog.logV("dispatchUnhandledKey: HOME/APP_SWITCH");
                                getBridge().onUserLeaving("HOME");
                            }
                        }
                    });
            XLog.logV("hookPWM OK:" + unHooks);
            getBridge().publishFeature(XAppGuardManager.Feature.HOME);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XLog.logV("Fail hookPWM:" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
