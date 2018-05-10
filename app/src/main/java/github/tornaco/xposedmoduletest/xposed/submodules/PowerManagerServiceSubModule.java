package github.tornaco.xposedmoduletest.xposed.submodules;

import android.util.Log;

import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.xposed.XAppBuildVar;
import github.tornaco.xposedmoduletest.xposed.service.power.PowerManagerServiceProxy;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

class PowerManagerServiceSubModule extends AndroidSubModule {

    @Override
    public String needBuildVar() {
        return XAppBuildVar.APP_WAKELOCK_BLOCKER;
    }

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookConstructor(lpparam);
        hookAcuqire(lpparam);
    }

    private void hookConstructor(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("Power hookConstructor...");
        try {
            Class clz = XposedHelpers.findClass("com.android.server.power.PowerManagerService",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllConstructors(clz, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    Object power = param.thisObject;
                    PowerManagerServiceProxy powerManagerServiceProxy = new PowerManagerServiceProxy(power);
                    getBridge().attachPowerManagerServices(powerManagerServiceProxy);
                }
            });
            XposedLog.verbose("Power hookConstructor OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Power fail hookConstructor:" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }


    // https://github.com/LineageOS/android_frameworks_base/blob/cm-12.0/services/core/java/com/android/server/power/PowerManagerService.java
    // L
    //  private void acquireWakeLockInternal(IBinder lock, int flags, String tag, String packageName,
    //            WorkSource ws, String historyTag, int uid, int pid) {}

    // O
    // private void acquireWakeLockInternal(IBinder lock, int flags, String tag, String packageName,
    //            WorkSource ws, String historyTag, int uid, int pid) {}
    private void hookAcuqire(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("Power hookAcuqire...");
        try {
            Class clz = XposedHelpers.findClass("com.android.server.power.PowerManagerService",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(clz, "acquireWakeLockInternal", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    boolean allow = getBridge().checkAcquireWakeLockInternal(
                            (int) param.args[1],
                            (String) param.args[2],
                            (String) param.args[3]);
                    if (!allow) {
                        param.setResult(null);
                    }
                }
            });
            XposedLog.verbose("Power hookAcuqire OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Power fail hookAcuqire:" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
