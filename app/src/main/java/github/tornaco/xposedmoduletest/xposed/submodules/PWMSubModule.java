package github.tornaco.xposedmoduletest.xposed.submodules;

import android.util.Log;
import android.view.KeyEvent;

import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.xposed.util.XPosedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */
@Deprecated
class PWMSubModule extends AndroidSubModuleModule {

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookPWM(lpparam);
    }

    private void hookPWM(final XC_LoadPackage.LoadPackageParam lpparam) {
        XPosedLog.verbose("hookPWM...");
        try {
            Class clz = XposedHelpers.findClass("com.android.server.policy.PhoneWindowManager",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(clz,
                    "interceptKeyBeforeQueueing", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            KeyEvent keyEvent = (KeyEvent) param.args[0];
//                            getBridge().onKeyEvent(keyEvent);
                        }
                    });
            XPosedLog.verbose("hookPWM OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XPosedLog.verbose("Fail hookPWM:" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
