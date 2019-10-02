package github.tornaco.xposedmoduletest.xposed.submodules;

import android.util.Log;

import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.util.OSUtil;
import github.tornaco.xposedmoduletest.xposed.XAppBuildVar;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setIntField;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

class ActivityStackRealStartActivitySubModule extends AndroidSubModule {

    @Override
    public int needMinSdk() {
        return super.needMinSdk();
    }

    @Override
    public String needBuildVar() {
        return XAppBuildVar.APP_RESIDENT;
    }

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookRealStartActivityLocked(lpparam);
    }

    private void hookRealStartActivityLocked(XC_LoadPackage.LoadPackageParam lpparam) {
        logOnBootStage("hookRealStartActivityLocked...");
        try {
            String clazzName = OSUtil.isQOrAbove()
                    ? "com.android.server.wm.ActivityStackSupervisor"
                    : "com.android.server.am.ActivityStackSupervisor";
            Class clz = XposedHelpers.findClass(clazzName,
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(clz, "realStartActivityLocked",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            String pkgName = (String) getObjectField(param.args[0], "packageName");
                            if (pkgName == null) return;
                            XAPMManager ash = XAPMManager.get();
                            if (!ash.isServiceAvailable()) return;
                            boolean resident = ash.isResidentEnabled()
                                    && ash.isResidentEnabledForPackage(pkgName);
                            if (BuildConfig.DEBUG) {
                                XposedLog.verbose("realStartActivityLocked: " + pkgName
                                        + ", resident " + resident);
                            }
                            if (resident) {
                                XposedLog.verbose("resident adj app: " + pkgName);
                                int adj = -12; // LKM Android Will not kill this app.
                                Object proc = getObjectField(param.args[0], "app");
                                // Override the *Adj values if meant to be resident in memory
                                if (proc != null) {
                                    setIntField(proc, "maxAdj", adj);
                                    setIntField(proc, "curRawAdj", adj);
                                    setIntField(proc, "setRawAdj", adj);
                                    setIntField(proc, "curAdj", adj);
                                    setIntField(proc, "setAdj", adj);
                                }
                            }
                        }
                    });
            logOnBootStage("hookRealStartActivityLocked OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            logOnBootStage("Fail hookRealStartActivityLocked: " + Log.getStackTraceString(e));
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
