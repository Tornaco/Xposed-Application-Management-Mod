package github.tornaco.xposedmoduletest.xposed.submodules;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.xposed.app.XAppGuardManager;
import github.tornaco.xposedmoduletest.xposed.app.XAppVerifyMode;
import github.tornaco.xposedmoduletest.xposed.service.VerifyListener;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

class TaskMoverSubModule extends AppGuardAndroidSubModule {
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
                        Object realActivityObj = XposedHelpers.getObjectField(param.args[0], "realActivity");
                        if (realActivityObj != null) {
                            ComponentName componentName = (ComponentName) realActivityObj;
                            pkgName = componentName.getPackageName();
                        } else {
                            // Using aff instead of PKG.
                            pkgName = (String) XposedHelpers.getObjectField(param.args[0], "affinity");
                        }

                        if (TextUtils.isEmpty(pkgName)) return;

                        XposedLog.wtf("findTaskToMoveToFrontLocked:" + pkgName);

                        // Package has been passed.
                        if (!getAppGuardBridge().onEarlyVerifyConfirm(pkgName)) return;

                        getAppGuardBridge().verify(null, pkgName, 0, 0,
                                new VerifyListener() {
                                    @Override
                                    public void onVerifyRes(String pkg, int uid, int pid, int res) {
                                        if (res == XAppVerifyMode.MODE_ALLOWED) try {
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
            getBridge().publishFeature(XAppGuardManager.Feature.RECENT);
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
