package github.tornaco.xposedmoduletest.xposed.submodules;

import android.util.Log;

import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.xposed.service.am.UsageStatsServiceProxy;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */
class UsageStatsSubModule extends AndroidSubModule {

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookUsageStatsServiceInit(lpparam);
    }

    private void hookUsageStatsServiceInit(final XC_LoadPackage.LoadPackageParam lpparam) {
        logOnBootStage("hookUsageStatsServiceInit...");
        try {
            Class clz = XposedHelpers.findClass("com.android.server.usage.UsageStatsService",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllConstructors(clz, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    logOnBootStage("UsageStatsService constructing...");
                    UsageStatsServiceProxy usageStatsServiceProxy = new UsageStatsServiceProxy(param.thisObject);
                    getBridge().attachUsageStatsService(usageStatsServiceProxy);
                }
            });
            logOnBootStage("hookUsageStatsServiceInit OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            logOnBootStage("Fail hookUsageStatsServiceInit:" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
