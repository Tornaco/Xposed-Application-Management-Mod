package github.tornaco.xposedmoduletest.xposed.submodules;

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

public class SystemPropSubModule extends AndroidSubModule {

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
        super.initZygote(startupParam);
        hookGet();
    }

    private void hookGet() {
        XposedLog.boot("SystemProp hookGet...");
        try {
            Class clz = XposedHelpers.findClass("android.os.SystemProperties", null);
            Set unHooks = XposedBridge.hookAllMethods(clz, "get", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    if (BuildConfig.DEBUG) {
                        Log.d(XposedLog.TAG, "SystemProp get: " + param.args[0]);
                    }
                }
            });
            setStatus(unhooksToStatus(unHooks));
            XposedLog.boot("SystemProp hookGet OK: " + unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.boot("SystemProp Fail hookGet: " + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
