package github.tornaco.xposedmoduletest.xposed.submodules;

import android.os.IBinder;
import android.util.Log;

import java.util.Set;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

// Hook hookStaticGetService settings.
class ServiceManagerSubModule extends AndroidSubModule {

//    @Override
//    public int needMinSdk() {
//        return Build.VERSION_CODES.O;
//    }

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
        super.initZygote(startupParam);
        hookStaticGetService();
    }

    private void hookStaticGetService() {
        XposedLog.verbose("hookStaticGetService...");
        try {
            Class smg = XposedHelpers.findClass("android.os.ServiceManager", null);
            Set unHooks = XposedBridge.hookAllMethods(smg, "getService", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    XposedLog.verbose("****************** getService: " + param.args[0] + " *****************");
                    IBinder service = getBridge().onRetrieveBinderService((String) param.args[0]);
                    if (service != null) {
                        XposedLog.verbose("######## return service: " + service + "#######");
                        param.setResult(service);
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
