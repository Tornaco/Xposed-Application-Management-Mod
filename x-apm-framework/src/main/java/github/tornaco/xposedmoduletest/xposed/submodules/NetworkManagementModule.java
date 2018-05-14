package github.tornaco.xposedmoduletest.xposed.submodules;

import android.util.Log;

import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.xposed.XAppBuildVar;
import github.tornaco.xposedmoduletest.xposed.service.NativeDaemonConnector;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

// Hook hookNMSSystemReady settings.
class NetworkManagementModule extends AndroidSubModule {

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookNMSSystemReady(lpparam);
    }

    private void hookNMSSystemReady(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("hookNMSSystemReady...");
        try {
            Class ams = XposedHelpers.findClass("com.android.server.NetworkManagementService",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(ams, "systemReady", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    Object connectorObj = XposedHelpers.getObjectField(param.thisObject, "mConnector");
                    NativeDaemonConnector connector = new NativeDaemonConnector(connectorObj);
                    getBridge().onNetWorkManagementServiceReady(connector);
                }
            });
            XposedLog.verbose("hookNMSSystemReady OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hookNMSSystemReady: " + Log.getStackTraceString(e));
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }

    @Override
    public String needBuildVar() {
        return XAppBuildVar.APP_FIREWALL;
    }
}
