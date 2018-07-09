package github.tornaco.xposedmoduletest.xposed.submodules;

import android.app.AlarmManager;
import android.app.AndroidAppHelper;
import android.util.Log;

import java.util.Set;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.compat.os.XAppOpsManager;
import github.tornaco.xposedmoduletest.xposed.XAppBuildVar;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

// FIXME Check method def for L M N O!!!!!!!!!!!!!!!!!!!!
class AlarmManagerSubModule extends AndroidSubModule {
    @Override
    public String needBuildVar() {
        return XAppBuildVar.APP_OPS;
    }

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
        super.initZygote(startupParam);
        hookSetAlarm();
    }

    private void hookSetAlarm() {
        XposedLog.verbose("hookSetAlarm...");
        try {
            Class clz = XposedHelpers.findClass("android.app.AlarmManager", null);
            Set unHooks = XposedBridge.hookAllMethods(clz,
                    "setImpl", new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);

                            boolean permControlEnabled = XAPMManager.get().isServiceAvailable() && XAPMManager.get().isPermissionControlEnabled();
                            if (!permControlEnabled) {
                                return;
                            }

//                            @IntDef(prefix = { "RTC", "ELAPSED" }, value = {
//                                    RTC_WAKEUP,
//                                    RTC,
//                                    ELAPSED_REALTIME_WAKEUP,
//                                    ELAPSED_REALTIME,
//                            })

                            int type = (int) param.args[0];
                            // We do not interrupt the alram that will not wakeup device.
                            if (type == AlarmManager.RTC || type == AlarmManager.ELAPSED_REALTIME) {
                                return;
                            }

                            String pkgName = AndroidAppHelper.currentPackageName();

                            if (BuildConfig.DEBUG) {
                                Log.d(XposedLog.TAG, "set alarm: " + pkgName);
                            }

                            if ("android".equals(pkgName)) return;

                            // Check Greening.
                            boolean greening = XAPMManager.get().isServiceAvailable()
                                    && XAPMManager.get().isPackageGreening(pkgName);
                            if (BuildConfig.DEBUG) {
                                Log.d(XposedLog.TAG, "set alarm: "
                                        + pkgName
                                        + ", greening: " + greening);
                            }
                            if (greening) {
                                param.setResult(null);
                                return;
                            }

                            // Check OP.
                            if (XAPMManager.get().isServiceAvailable()) {
                                int mode = XAPMManager.get().getPermissionControlBlockModeForPkg(
                                        XAppOpsManager.OP_SET_ALARM, pkgName, true);
                                if (mode == XAppOpsManager.MODE_IGNORED) {
                                    if (BuildConfig.DEBUG) {
                                        Log.d(XposedLog.TAG, "set alarm, MODE_IGNORED returning...");
                                    }
                                    param.setResult(null);
                                }
                            }
                        }
                    });
            XposedLog.verbose("hookSetAlarm OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hookSetAlarm:" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
