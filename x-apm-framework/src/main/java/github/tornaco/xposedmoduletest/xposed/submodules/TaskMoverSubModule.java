package github.tornaco.xposedmoduletest.xposed.submodules;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.xposed.app.XAppVerifyMode;
import github.tornaco.xposedmoduletest.xposed.service.VerifyListener;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

class TaskMoverSubModule extends AndroidSubModule {
    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookTaskMover(lpparam);
    }

    private void hookTaskMover(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.wtf("hookTaskMover...");
        try {
            final Method moveToFront = methodForTaskMover(lpparam);
            XC_MethodHook.Unhook unhook = XposedBridge.hookMethod(moveToFront, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    try {
                        String pkgName;
                        ComponentName componentName = null;
                        Object realActivityObj = XposedHelpers.getObjectField(param.args[0], "realActivity");
                        if (realActivityObj != null) {
                            componentName = (ComponentName) realActivityObj;
                            pkgName = componentName.getPackageName();
                        } else {
                            // Using aff instead of PKG.
                            pkgName = (String) XposedHelpers.getObjectField(param.args[0], "affinity");
                        }

                        if (TextUtils.isEmpty(pkgName)) {
                            return;
                        }

                        XposedLog.verbose("findTaskToMoveToFrontLocked:" + pkgName);

                        Intent intent = new Intent();
                        intent.setComponent(componentName);
                        intent.setPackage(pkgName);

                        // Package has been passed.
                        if (!getBridge().onEarlyVerifyConfirm(pkgName, "findTaskToMoveToFrontLocked")) {
                            getBridge().reportActivityLaunching(intent, "findTaskToMoveToFrontLocked onEarlyVerifyConfirm");
                            return;
                        }

                        getBridge().verify(null, pkgName, componentName, 0, 0,
                                (pkg, uid, pid, res) -> {
                                    if (res == XAppVerifyMode.MODE_ALLOWED) {
                                        try {
                                            getBridge().reportActivityLaunching(intent, "findTaskToMoveToFrontLocked MODE_ALLOWED");
                                            XposedBridge.invokeOriginalMethod(moveToFront,
                                                    param.thisObject, param.args);
                                        } catch (Exception e) {
                                            XposedLog.debug("Error@"
                                                    + Log.getStackTraceString(e));
                                        }
                                    }
                                });

                        param.setResult(null);

                    } catch (Exception e) {
                        XposedLog.wtf("Error@hookTaskMover- findTaskToMoveToFrontLocked:" + Log.getStackTraceString(e));
                    } finally {
                    }
                }
            });
            XposedLog.wtf("hookTaskMover OK:" + unhook);
            setStatus(unhookToStatus(unhook));
        } catch (Exception e) {
            XposedLog.wtf("hookTaskMover" + Log.getStackTraceString(e));
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }

    @SuppressLint("PrivateApi")
    Method methodForTaskMover(XC_LoadPackage.LoadPackageParam lpparam) throws ClassNotFoundException, NoSuchMethodException {
        throw new IllegalStateException("Need impl here");
    }
}
