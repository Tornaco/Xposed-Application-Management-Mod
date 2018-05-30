package github.tornaco.xposedmoduletest.xposed.submodules.debug;

import android.util.Log;

import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.xposed.submodules.AndroidSubModule;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by Tornaco on 2018/5/4 16:38.
 * God bless no bug!
 */
public class PointerEventDispatcherSubModule extends AndroidSubModule {

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookOnInputEvent(lpparam);
    }

    private void hookOnInputEvent(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("PointerEventDispatcherSubModule hookOnInputEvent...");
        try {
            Class ams = XposedHelpers.findClass("com.android.server.wm.PointerEventDispatcher",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(ams, "onInputEvent",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            getBridge().onInputEvent(param.args[0]);
                        }
                    });
            XposedLog.verbose("PointerEventDispatcherSubModule hookOnInputEvent OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hookOnInputEvent: " + Log.getStackTraceString(e));
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
