package github.tornaco.xposedmoduletest.x;

import android.app.ActivityManager;
import android.app.IApplicationThread;
import android.app.ProfilerInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.UserHandle;
import android.util.Log;

import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by guohao4 on 2017/10/19.
 * Email: Tornaco@163.com
 */

class XModuleImpl22 extends XModule {

    @Override
    void onLoadingAndroid(XC_LoadPackage.LoadPackageParam lpparam) {
        super.onLoadingAndroid(lpparam);
        if (xStatus == XStatus.ERROR) return;
        hookActivityManagerService(lpparam);
        hookTaskMover(lpparam);
    }

    private void hookTaskMover(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Class taskRecordClass = Class.forName("com.android.server.am.TaskRecord", false, lpparam.classLoader);
            final Method moveToFront = Class.forName("com.android.server.am.ActivityStackSupervisor",
                    false, lpparam.classLoader)
                    .getDeclaredMethod("findTaskToMoveToFrontLocked",
                            taskRecordClass, int.class, Bundle.class, String.class);
            XposedBridge.hookMethod(moveToFront, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);

                    XposedBridge.log(TAG + "findTaskToMoveToFrontLocked:" + param.args[0]);
                    // FIXME Using aff instead of PKG.
                    try {
                        final String affinity = (String) XposedHelpers.getObjectField(param.args[0], "affinity");

                        // Package has been passed.
                        if (mAppGuardService.passed(affinity)) return;

                        int callingUID = Binder.getCallingUid();
                        int callingPID = Binder.getCallingPid();

                        mAppGuardService.verify(null, affinity, callingUID, callingPID, new XAppGuardService.VerifyListener() {
                            @Override
                            public void onVerifyRes(String pkg, int uid, int pid, int res) {
                                if (res == XMode.MODE_ALLOWED) try {
                                    XposedBridge.invokeOriginalMethod(moveToFront,
                                            param.thisObject, param.args);
                                } catch (Exception e) {
                                    XposedBridge.log(TAG
                                            + "Error@"
                                            + Log.getStackTraceString(e));
                                }
                            }
                        });

                        param.setResult(null);

                    } catch (Exception e) {
                        XposedBridge.log(TAG + "findTaskToMoveToFrontLocked" + Log.getStackTraceString(e));
                    }
                }
            });
        } catch (Exception e) {
            XposedBridge.log(TAG + "hookTaskMover" + Log.getStackTraceString(e));
            xStatus = XStatus.ERROR;
        }
    }

    private void hookActivityManagerService(XC_LoadPackage.LoadPackageParam lpparam) {

        try {

            final Method startActivity =
                    Class.forName("com.android.server.am.ActivityManagerService", false, lpparam.classLoader)
                            .getDeclaredMethod("startActivity", IApplicationThread.class, String.class,
                                    Intent.class, String.class, IBinder.class, String.class,
                                    int.class, int.class, ProfilerInfo.class, Bundle.class);

            final Method startActivityAsUser =
                    Class.forName("com.android.server.am.ActivityManagerService", false, lpparam.classLoader)
                            .getDeclaredMethod("startActivityAsUser", IApplicationThread.class, String.class,
                                    Intent.class, String.class, IBinder.class, String.class,
                                    int.class, int.class, ProfilerInfo.class, Bundle.class,
                                    int.class);

            XposedBridge.hookMethod(startActivity,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);

                            try {
                                Intent intent = (Intent) param.args[2];
                                if (intent == null) return;

                                ComponentName componentName = intent.getComponent();
                                if (componentName == null) return;
                                final String pkgName = componentName.getPackageName();

                                XposedBridge.log(TAG + "HOOKING startActivity:" + intent);

                                // Package has been passed.
                                if (mAppGuardService.passed(pkgName)) {
                                    return;
                                }

                                boolean isHomeIntent = isLauncherIntent(intent);
                                if (isHomeIntent) {
                                    return;
                                }

                                int callingUID = Binder.getCallingUid();
                                int callingPID = Binder.getCallingPid();

                                int callingUserId = UserHandle.getCallingUserId();
                                final Object[] args = new Object[param.args.length + 1];
                                System.arraycopy(param.args, 0, args, 0, param.args.length);
                                args[args.length - 1] = callingUserId;

                                Bundle bnds = (Bundle) param.args[9];
                                if (DEBUG_V) {
                                    XposedBridge.log(TAG + "bnds:" + bnds);
                                }

                                mAppGuardService.verify(bnds, pkgName, callingUID, callingPID, new XAppGuardService.VerifyListener() {
                                    @Override
                                    public void onVerifyRes(String pkg, int uid, int pid, int res) {
                                        if (res == XMode.MODE_ALLOWED) try {
                                            XposedBridge.invokeOriginalMethod(startActivityAsUser, param.thisObject, args);
                                        } catch (Exception e) {
                                            XposedBridge.log(TAG
                                                    + "Error@"
                                                    + Log.getStackTraceString(e));
                                        }
                                    }
                                });
                                param.setResult(ActivityManager.START_SUCCESS);
                            } catch (Exception e) {
                                // replacing did not work.. but no reason to crash the VM! Log the error and go on.
                                XposedBridge.log(TAG + Log.getStackTraceString(e));
                            }
                        }
                    });
        } catch (Exception e) {
            XposedBridge.log(TAG + "hookActivityManagerService" + Log.getStackTraceString(e));
            xStatus = XStatus.ERROR;
        }
    }
}
