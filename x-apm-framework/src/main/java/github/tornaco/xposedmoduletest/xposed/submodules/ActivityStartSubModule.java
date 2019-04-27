package github.tornaco.xposedmoduletest.xposed.submodules;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.util.Log;

import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.xposed.app.XAppVerifyMode;
import github.tornaco.xposedmoduletest.xposed.service.VerifyListener;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

class ActivityStartSubModule extends AndroidSubModule {
    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookStartActivityMayWait(lpparam);
    }

    private void hookStartActivityMayWait(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("hookStartActivityMayWait...");
        try {
            Class clz = clzForStartActivityMayWait(lpparam);

            // Search method.
            String targetMethodName = "startActivityMayWait";
            int matchCount = 0;
            int activityOptsIndex = -1;
            int intentIndex = -1;
            Method startActivityMayWaitMethod = null;
            if (clz != null) {
                for (Method m : clz.getDeclaredMethods()) {
                    if (m.getName().equals(targetMethodName)) {
                        startActivityMayWaitMethod = m;
                        startActivityMayWaitMethod.setAccessible(true);
                        matchCount++;

                        Class[] classes = m.getParameterTypes();
                        for (int i = 0; i < classes.length; i++) {
                            if (Bundle.class == classes[i]) {
                                activityOptsIndex = i;
                            } else if (Intent.class == classes[i]) {
                                intentIndex = i;
                            }
                        }

                        if (activityOptsIndex >= 0 && intentIndex >= 0) {
                            break;
                        }
                    }
                }
            }

            if (startActivityMayWaitMethod == null) {
                XposedLog.wtf("*** FATAL can not find startActivityMayWait method ***");
                setStatus(SubModuleStatus.ERROR);
                setErrorMessage("*** FATAL can not find startActivityMayWait method ***");
                return;
            }

            if (matchCount > 1) {
                XposedLog.wtf("*** FATAL more than 1 startActivityMayWait method ***");
                setStatus(SubModuleStatus.ERROR);
                setErrorMessage("*** FATAL more than 1 startActivityMayWait method ***");
                // return;
            }

            if (intentIndex < 0) {
                XposedLog.wtf("*** FATAL can not find intentIndex ***");
                setStatus(SubModuleStatus.ERROR);
                setErrorMessage("*** FATAL can not find intentIndex ***");
                return;
            }

            XposedLog.wtf("startActivityMayWait method:" + startActivityMayWaitMethod);
            XposedLog.wtf("intentIndex index:" + intentIndex);
            XposedLog.wtf("activityOptsIndex index:" + activityOptsIndex);

            final int finalActivityOptsIndex = activityOptsIndex;
            final int finalIntentIndex = intentIndex;

            final Method finalStartActivityMayWaitMethod = startActivityMayWaitMethod;
            XC_MethodHook.Unhook unhook = XposedBridge.hookMethod(startActivityMayWaitMethod, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    try {
                        Intent intent =
                                finalIntentIndex > 0 ?
                                        (Intent) param.args[finalIntentIndex]
                                        : null;
                        if (intent == null) {
                            return;
                        }

                        // Use checked Intent instead of previous one.
                        Intent checkedIntent = getBridge().checkIntent(intent);
                        if (checkedIntent != null) {
                            intent = checkedIntent;
                            param.args[finalIntentIndex] = intent;
                            Binder.restoreCallingIdentity(getBridge()
                                    .wrapCallingUidForIntent(Binder.clearCallingIdentity(), intent));
                        } else {
                            param.setResult(ActivityManager.START_SUCCESS);
                            return;
                        }

                        ComponentName componentName = intent.getComponent();
                        if (componentName == null) {
                            return;
                        }

                        // Incas the component is disabled.
                        boolean itrp = getBridge()
                                .isActivityStartShouldBeInterrupted(componentName);
                        if (itrp) {
                            param.setResult(ActivityManager.START_SUCCESS);
                            return;
                        }

                        final String pkgName = componentName.getPackageName();

                        boolean isHomeIntent = PkgUtil.isHomeIntent(intent);
                        if (isHomeIntent) {
                            return;
                        }

                        // Package has been passed.
                        if (!getBridge().onEarlyVerifyConfirm(pkgName, "startActivityMayWait")) {
                            getBridge().reportActivityLaunching(intent, "startActivityMayWait onEarlyVerifyConfirm");
                            return;
                        }

                        Bundle options =
                                finalActivityOptsIndex > 0 ?
                                        (Bundle) param.args[finalActivityOptsIndex]
                                        : null;

                        Intent finalIntent = intent;
                        getBridge().verify(options, pkgName, componentName, 0, 0,
                                (pkg, uid, pid, res) -> {
                                    if (res == XAppVerifyMode.MODE_ALLOWED) {
                                        try {
                                            getBridge().reportActivityLaunching(finalIntent, "startActivityMayWait, MODE_ALLOWED");
                                            XposedBridge.invokeOriginalMethod(finalStartActivityMayWaitMethod,
                                                    param.thisObject, param.args);
                                        } catch (Exception e) {
                                            XposedLog.wtf("Error@" + Log.getStackTraceString(e));
                                        }
                                    }
                                });
                        param.setResult(ActivityManager.START_SUCCESS);
                    } catch (Throwable e) {
                        // replacing did not work.. but no reason to crash the VM! Log the error and go on.
                        XposedLog.wtf("Error@startActivityMayWaitMethod:" + Log.getStackTraceString(e));
                    } finally {
                    }
                }
            });
            XposedLog.wtf("hookStartActivityMayWait OK: " + unhook);
            setStatus(unhookToStatus(unhook));
        } catch (Exception e) {
            XposedLog.wtf("Fail hookStartActivityMayWait:" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }

    Class clzForStartActivityMayWait(XC_LoadPackage.LoadPackageParam lpparam) throws ClassNotFoundException {
        throw new IllegalStateException("Need impl here");
    }
}
