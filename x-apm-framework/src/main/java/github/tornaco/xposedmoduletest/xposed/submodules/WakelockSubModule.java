package github.tornaco.xposedmoduletest.xposed.submodules;

import android.app.AndroidAppHelper;
import android.util.Log;

import java.util.Set;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import github.tornaco.xposedmoduletest.compat.os.XAppOpsManager;
import github.tornaco.xposedmoduletest.xposed.XAppBuildVar;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

// FIXME Check method def for L M N O!!!!!!!!!!!!!!!!!!!!
class WakelockSubModule extends AndroidSubModule {
    @Override
    public String needBuildVar() {
        return XAppBuildVar.APP_OPS;
    }

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
        super.initZygote(startupParam);
        hookAcquireWakeLock();

    }

    private void hookAcquireWakeLock() {
        XposedLog.verbose("hookAcquireWakeLock...");
        try {
            Class clz = XposedHelpers.findClass("android.os.PowerManager$WakeLock", null);
            Set unHooks = XposedBridge.hookAllMethods(clz,
                    "acquire", new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);

                            boolean ashServiceAvailable = XAPMManager.get()
                                    .isServiceAvailable();
                            if (!ashServiceAvailable) {
                                Log.e(XposedLog.TAG, "WakeLock-Service not available.");
                                // Try retrieve service again.
                                XAPMManager.get().retrieveService();
                                return;
                            }

                            String pkgName = AndroidAppHelper.currentPackageName();

//                            if (BuildConfig.DEBUG) {
//                                Log.d(XposedLog.TAG, "acquire wakelock: " + pkgName);
//                            }

                            // Check Greening.
                            boolean greening = XAPMManager.get().isServiceAvailable()
                                    && XAPMManager.get().isPackageGreening(pkgName);
//                            if (BuildConfig.DEBUG) {
//                                Log.d(XposedLog.TAG, "acquire wake lock: "
//                                        + pkgName
//                                        + ", greening: " + greening);
//                            }
                            if (greening) {
                                param.setResult(null);
                                return;
                            }

                            // Check OP.
                            if (XAPMManager.get().isServiceAvailable()) {
                                boolean permControlEnabled = XAPMManager.get().isServiceAvailable()
                                        && XAPMManager.get().isPermissionControlEnabled();
                                int mode = permControlEnabled ?
                                        XAPMManager.get().getPermissionControlBlockModeForPkg(
                                                XAppOpsManager.OP_WAKE_LOCK, pkgName, true)
                                        : XAppOpsManager.MODE_ALLOWED;
                                if (mode == XAppOpsManager.MODE_IGNORED) {
//                                XposedLog.verbose("acquire wake lock, MODE_IGNORED returning...");
                                    param.setResult(null);
                                }
                            }

                            // Check blocker.
                        }
                    });
            XposedLog.verbose("hookAcquireWakeLock OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hookAcquireWakeLock:" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
