package github.tornaco.xposedmoduletest.xposed.submodules;

import android.app.AndroidAppHelper;
import android.os.Binder;
import android.util.Log;

import java.util.Set;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import github.tornaco.xposedmoduletest.util.OSUtil;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

// Hook hookUnCaughtErr settings.
class RuntimeInitSubModule extends AndroidSubModule {

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
        super.initZygote(startupParam);
        if (!OSUtil.isMIUI()) {
            hookUnCaughtErr();
        } else {
            XposedLog.boot("Skip hookUnCaughtErr for MIUI ");
        }
    }

    private void hookUnCaughtErr() {
        XposedLog.verbose("hookUnCaughtErr...");
        try {
            final Class c = XposedHelpers.findClass(
                    OSUtil.isOOrAbove() ?
                            "com.android.internal.os.RuntimeInit$KillApplicationHandler"
                            : "com.android.internal.os.RuntimeInit$UncaughtHandler",
                    null);
            Set unHooks = XposedBridge.hookAllMethods(c, "uncaughtException",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);

                            if (PkgUtil.isSystemOrPhoneOrShell(Binder.getCallingUid())) {
                                return;
                            }

                            // Now report to ash man.
                            XAshmanManager xAshmanManager = XAshmanManager.get();
                            if (xAshmanManager.isServiceAvailable()) {
                                // Wrap err log to xp log.
                                Thread t = (Thread) param.args[0];
                                Throwable e = (Throwable) param.args[1];
                                String currentPackage = AndroidAppHelper.currentPackageName();
                                String trace = Log.getStackTraceString(e);

                                boolean shouldInterruptCrash =
                                        xAshmanManager.onApplicationUncaughtException(currentPackage,
                                                t.getName(), e.getClass().getName(), trace);
                                if (shouldInterruptCrash) {
                                    param.setResult(null);
                                }
                            }
                        }
                    });
            XposedLog.verbose("hookUnCaughtErr OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hookUnCaughtErr: " + Log.getStackTraceString(e));
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
