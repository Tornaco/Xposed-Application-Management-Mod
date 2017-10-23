package github.tornaco.xposedmoduletest.x;

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Binder;
import android.util.Log;

import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by guohao4 on 2017/10/13.
 * Email: Tornaco@163.com
 */

class XModuleImpl24 extends XModule {

    @Override
    void onLoadingAndroid(XC_LoadPackage.LoadPackageParam lpparam) {
        super.onLoadingAndroid(lpparam);
        if (xStatus == XStatus.ERROR) return;
        hookActivityStarter(lpparam);
        hookTaskMover(lpparam);
    }

    private void hookTaskMover(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Class taskRecordClass = Class.forName("com.android.server.am.TaskRecord", false, lpparam.classLoader);
            final Method moveToFront = Class.forName("com.android.server.am.ActivityStackSupervisor",
                    false, lpparam.classLoader)
                    .getDeclaredMethod("findTaskToMoveToFrontLocked",
                            taskRecordClass, int.class, ActivityOptions.class,
                            String.class, boolean.class);
            XposedBridge.hookMethod(moveToFront, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(final MethodHookParam param)
                        throws Throwable {
                    super.beforeHookedMethod(param);

                    if (DEBUG_V)
                        XposedBridge.log(TAG + "findTaskToMoveToFrontLocked:" + param.args[0]);
                    // FIXME Using aff instead of PKG.
                    try {
                        final String affinity = (String) XposedHelpers.getObjectField(param.args[0], "affinity");

                        if (mAppGuardService.passed(affinity)) {
                            return;
                        }

                        int callingUID = Binder.getCallingUid();
                        int callingPID = Binder.getCallingPid();

                        mAppGuardService.verify(affinity, callingUID, callingPID,
                                new XAppGuardService.VerifyListener() {
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

    private void hookActivityStarter(XC_LoadPackage.LoadPackageParam lpparam) {

        try {

            Method startActivityLockedExact = null;
            int matchCount = 0;
            for (Method method : Class.forName("com.android.server.am.ActivityStarter",
                    false, lpparam.classLoader).getDeclaredMethods()) {
                if (method.getName().equals("startActivityLocked")) {
                    startActivityLockedExact = method;
                    startActivityLockedExact.setAccessible(true);
                    matchCount++;
                }
            }

            if (startActivityLockedExact == null) {
                XposedBridge.log(TAG + "*** FATAL can not find starter method ***");
                return;
            }

            if (matchCount > 1) {
                XposedBridge.log(TAG + "*** FATAL more than 1 starter method ***");
                return;
            }

            XposedBridge.log(TAG + "startActivityLocked method:" + startActivityLockedExact);
            final Method finalStartActivityLockedExact = startActivityLockedExact;
            XposedBridge.hookMethod(startActivityLockedExact,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);

                            try {
                                Intent intent = (Intent) param.args[1];
                                if (intent == null) return;

                                if (DEBUG_V) {
                                    XposedBridge.log(TAG + "HOOKING intent: " + intent);
                                    XposedBridge.log(TAG + "HOOKING is home: " + isLauncherIntent(intent));
                                }

                                ComponentName componentName = intent.getComponent();
                                if (componentName == null) return;
                                final String pkgName = componentName.getPackageName();

                                if (DEBUG_V)
                                    XposedBridge.log(TAG + "HOOKING startActivityLocked:" + pkgName);

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

                                mAppGuardService.verify(pkgName, callingUID, callingPID, new XAppGuardService.VerifyListener() {
                                    @Override
                                    public void onVerifyRes(String pkg, int uid, int pid, int res) {
                                        if (res == XMode.MODE_ALLOWED) try {
                                            XposedBridge.invokeOriginalMethod(finalStartActivityLockedExact,
                                                    param.thisObject, param.args);
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
            XposedBridge.log(TAG + "hookActivityStarter" + Log.getStackTraceString(e));
            xStatus = XStatus.ERROR;
        }
    }
}
