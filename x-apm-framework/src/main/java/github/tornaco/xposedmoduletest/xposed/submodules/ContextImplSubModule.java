package github.tornaco.xposedmoduletest.xposed.submodules;

import android.Manifest;
import android.os.Binder;
import android.os.UserHandle;
import android.util.Log;

import java.util.Arrays;
import java.util.Set;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

class ContextImplSubModule extends AndroidSubModule {

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
        super.initZygote(startupParam);
        hookEnforce();
    }

    private void hookEnforce() {
        XposedLog.verbose("ContextImplSubModule hookEnforce...");
        try {
            Class clz = XposedHelpers.findClass("android.app.ContextImpl", null);
            Set unHooks = XposedBridge.hookAllMethods(clz,
                    "enforce", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(final MethodHookParam param)
                                throws Throwable {
                            super.afterHookedMethod(param);
                            boolean hasErr = param.hasThrowable();
                            if (hasErr) {
                                int uid = Binder.getCallingUid();
                                if (isSystemCall(uid)) {
                                    Log.e(XposedLog.TAG, "ContextImpl enforce err@system:" + Arrays.toString(param.args));
                                    Object permission = param.args[0];
                                    if (String.valueOf(permission).startsWith("android.permission")) {
                                        // This is really the permission param!
                                        boolean isIdlePermission = Manifest.permission.CHANGE_APP_IDLE_STATE
                                                .equals(permission);
                                        if (isIdlePermission) {
                                            Log.e(XposedLog.TAG, "ContextImpl clearing err@system:" + Arrays.toString(param.args));
                                            param.setThrowable(null);
                                        }
                                    }
                                }
                            }
                        }
                    });
            XposedLog.verbose("ContextImplSubModule hookEnforce OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Throwable e) {
            XposedLog.verbose("ContextImplSubModule Fail hookEnforce:" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }

    private static boolean isSystemCall(int uid) {
        return uid == 1000
                || (uid > UserHandle.PER_USER_RANGE && (uid % UserHandle.PER_USER_RANGE == 1000));
    }
}
