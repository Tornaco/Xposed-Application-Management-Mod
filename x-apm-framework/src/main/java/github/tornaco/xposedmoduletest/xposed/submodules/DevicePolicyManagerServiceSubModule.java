package github.tornaco.xposedmoduletest.xposed.submodules;

import android.os.Binder;
import android.util.Log;

import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.xposed.service.dpm.DevicePolicyManagerServiceProxy;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

// FIXME Have not check M L O yet.
class DevicePolicyManagerServiceSubModule extends AndroidSubModule {

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookSystemReady(lpparam);
        hookGetActiveAdmin(lpparam);
    }

    private void hookSystemReady(final XC_LoadPackage.LoadPackageParam lpparam) {
        logOnBootStage("hookSystemReady...");
        try {
            Class clz = XposedHelpers.findClass("com.android.server.devicepolicy.DevicePolicyManagerService",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(clz,
                    "systemReady", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            Object service = param.thisObject;
                            if (service != null) {
                                DevicePolicyManagerServiceProxy proxy = new DevicePolicyManagerServiceProxy(service);
                                getBridge().attachDevicePolicyManagerService(proxy);
                            }
                        }
                    });
            logOnBootStage("hookSystemReady OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            logOnBootStage("Fail hookSystemReady:" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }

    private void hookGetActiveAdmin(final XC_LoadPackage.LoadPackageParam lpparam) {
        logOnBootStage("hookGetActiveAdmin...");
        try {
            Class clz = XposedHelpers.findClass("com.android.server.devicepolicy.DevicePolicyManagerService",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(clz,
                    "getActiveAdminForCallerLocked", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            int callingUid = Binder.getCallingUid();
                            if (PkgUtil.isSystemCall(callingUid)) {
                                boolean panicLockEnabled = getBridge().isPanicLockEnabled();
                                if (panicLockEnabled) {
                                    Throwable e = param.getThrowable();
                                    XposedLog.verbose("getActiveAdminForCallerLocked, Clear Throwable for DPM ADMIN CALLER:" + e);
                                    param.setThrowable(null);
                                }
                            }
                        }
                    });
            logOnBootStage("hookGetActiveAdmin OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            logOnBootStage("Fail hookGetActiveAdmin:" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
