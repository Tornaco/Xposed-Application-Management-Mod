package github.tornaco.xposedmoduletest.xposed.submodules;

import android.Manifest;
import android.app.AndroidAppHelper;
import android.os.Binder;
import android.os.UserHandle;
import android.util.Log;

import com.google.common.collect.Sets;

import java.util.Arrays;
import java.util.Set;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

class ContextImplSubModule extends AndroidSubModule {

    // These permissions is needed by X-APM app, but for system reason,
    // it can not acquire anyway.
    // So we hook it.
    private static final Set<String> APM_APP_NEEDED_PERMISSIONS = Sets.newHashSet();

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
                                Object permission = param.args[0];
                                if (isSystemCall(uid)) {
                                    Log.e(XposedLog.TAG, "ContextImpl enforce err@system:" + Arrays.toString(param.args));
                                    if (String.valueOf(permission).startsWith("android.permission")) {
                                        // This is really the permission param!
                                        boolean isIdlePermission = Manifest.permission.CHANGE_APP_IDLE_STATE
                                                .equals(permission);
                                        if (isIdlePermission) {
                                            Log.e(XposedLog.TAG, "ContextImpl clearing err@system:" + Arrays.toString(param.args));
                                            param.setThrowable(null);
                                        }
                                    }
                                } else if (isXAPMAppNeededButCanNotAccquiredPermission(String.valueOf(permission)) && isXAPMAppCall()) {
                                    Log.e(XposedLog.TAG, "ContextImpl enforce clearing err@x-apm app:" + Arrays.toString(param.args));
                                    param.setThrowable(null);
                                } else if (BuildConfig.DEBUG) {
                                    Log.e(XposedLog.TAG, "ContextImpl enforce err@?:" + Arrays.toString(param.args)
                                            + " - "
                                            + AndroidAppHelper.currentPackageName());
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

    private static boolean isXAPMAppNeededButCanNotAccquiredPermission(String perm) {
        return APM_APP_NEEDED_PERMISSIONS.contains(perm);
    }

    private static boolean isSystemCall(int uid) {
        return uid == 1000
                || (uid > UserHandle.PER_USER_RANGE && (uid % UserHandle.PER_USER_RANGE == 1000));
    }

    private static boolean isXAPMAppCall() {
        return BuildConfig.APPLICATION_ID.equals(AndroidAppHelper.currentPackageName());
    }
}
