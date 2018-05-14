package github.tornaco.xposedmoduletest.xposed.submodules;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.util.Set;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

// Hook hookStaticGetService settings.
// https://github.com/LineageOS/android_system_sepolicy/blob/4a2af220a1b1e2e868123c91b27bdc2cacf0b9a8/service_contexts
class ServiceManagerSubModule extends AndroidSubModule {

    @Override
    public int needMinSdk() {
        return Build.VERSION_CODES.O;
    }

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
        super.initZygote(startupParam);
        hookStaticGetService();
    }

    private void hookStaticGetService() {
        XposedLog.verbose("hookStaticGetService...");
        try {
            Class smg = XposedHelpers.findClass("android.os.ServiceManager", null);
            Set unHooks = XposedBridge.hookAllMethods(smg, "addService", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    if (BuildConfig.DEBUG) {
                        XposedLog.verbose("****************** addService: " + param.args[0] + " *****************");
                    }
                    boolean isTVInput = Context.TV_INPUT_SERVICE.equals(param.args[0]);
                    if (isTVInput) {
                        XposedLog.wtf("Adding TV input service @" + Log.getStackTraceString(new Throwable()));
                    }
                }
            });
            logOnBootStage("hookStaticGetService OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            logOnBootStage("Fail hookStaticGetService: " + Log.getStackTraceString(e));
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
