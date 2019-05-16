package github.tornaco.xposedmoduletest.xposed.submodules;

import android.content.Intent;
import android.content.pm.UserInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.xposed.XAppBuildVar;
import github.tornaco.xposedmoduletest.xposed.app.XAppVerifyMode;
import github.tornaco.xposedmoduletest.xposed.service.multipleapps.MultipleAppsManager;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

// Hook hookStartShortcut settings.
@SuppressWarnings("unchecked")
class LauncherAppServiceSubModule extends AndroidSubModule {
    @Override
    public String needBuildVar() {
        return XAppBuildVar.APP_LOCK;
    }

    @Override
    public int needMinSdk() {
        return Build.VERSION_CODES.N;
    }

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookStartShortcut(lpparam);
        hookVerifyCallingPackage(lpparam);

        // For debug.
        if (BuildConfig.DEBUG) {
            hookIsEnabledProfileOf(lpparam);
        }
    }

    private void hookVerifyCallingPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("LauncherAppServiceSubModule hookVerifyCallingPackage...");
        try {
            Class clz = XposedHelpers.findClass("com.android.server.pm.LauncherAppsService$LauncherAppsImpl",
                    lpparam.classLoader);

            Method verifyMethod = null;

            for (Method m : clz.getDeclaredMethods()) {
                if ("verifyCallingPackage".equals(m.getName())) {
                    verifyMethod = m;
                }
            }

            XposedLog.boot("verifyCallingPackage method: " + verifyMethod);

            if (verifyMethod == null) {
                return;
            }

            Object unHooks = XposedBridge.hookMethod(verifyMethod, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    XposedLog.verbose("LauncherAppService verifyCallingPackage: " + param.args[0]);
                    param.setResult(null);
                }
            });
            XposedLog.verbose("LauncherAppServiceSubModule hookVerifyCallingPackage OK:" + unHooks);
            setStatus(unhookToStatus((XC_MethodHook.Unhook) unHooks));
        } catch (Exception e) {
            XposedLog.verbose("LauncherAppServiceSubModule Fail hookVerifyCallingPackage: " + Log.getStackTraceString(e));
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }

    private void hookStartShortcut(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("LauncherAppServiceSubModule hookStartShortcut...");
        try {
            Class clz = XposedHelpers.findClass("com.android.server.pm.LauncherAppsService$LauncherAppsImpl",
                    lpparam.classLoader);

            Method startShortcutMethod = null;

            for (Method m : clz.getDeclaredMethods()) {
                if ("startShortcut".equals(m.getName())) {
                    startShortcutMethod = m;
                }
            }

            XposedLog.boot("startShortcut method: " + startShortcutMethod);

            if (startShortcutMethod == null) {
                return;
            }

            final Method finalStartShortcutMethod = startShortcutMethod;
            Object unHooks = XposedBridge.hookMethod(startShortcutMethod, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    String pkgName = (String) param.args[1];
                    Bundle op = (Bundle) param.args[4];

                    if (BuildConfig.DEBUG) {
                        XposedLog.verbose("startShortcut: %s %s", pkgName, op);
                    }

                    if (pkgName == null) {
                        return;
                    }

                    Intent intent = new Intent();
                    intent.setPackage(pkgName);

                    // Package has been passed.
                    if (!getBridge().onEarlyVerifyConfirm(pkgName, "startShortcut")) {
                        getBridge().reportActivityLaunching(intent, "startShortcut onEarlyVerifyConfirm");
                        return;
                    }

                    getBridge().verify(op, pkgName, null, 0, 0, (pkg, uid, pid, res) -> {
                        if (res == XAppVerifyMode.MODE_ALLOWED) {
                            try {
                                getBridge().reportActivityLaunching(intent, "startShortcut MODE_ALLOWED");
                                XposedBridge.invokeOriginalMethod(finalStartShortcutMethod, param.thisObject, param.args);
                            } catch (Exception e) {
                                XposedLog.wtf("Error@" + Log.getStackTraceString(e));
                            }
                        }
                    });
                    param.setResult(true);
                }
            });
            XposedLog.verbose("LauncherAppServiceSubModule hookStartShortcut OK:" + unHooks);
            setStatus(unhookToStatus((XC_MethodHook.Unhook) unHooks));
        } catch (Exception e) {
            XposedLog.verbose("LauncherAppServiceSubModule Fail hookStartShortcut: " + Log.getStackTraceString(e));
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }

    private void hookIsEnabledProfileOf(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("LauncherAppServiceSubModule hookIsEnabledProfileOf...");
        try {
            Class clz = XposedHelpers.findClass("com.android.server.pm.LauncherAppsService$LauncherAppsImpl",
                    lpparam.classLoader);


            Set unHooks = XposedBridge.hookAllMethods(clz, "isEnabledProfileOf", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    UserHandle userHandle = (UserHandle) param.args[0];
                    UserManager um = (UserManager) XposedHelpers.getObjectField(param.thisObject, "mUm");
                    UserInfo userInfo = um.getUserInfo(userHandle.getIdentifier());
                    if (MultipleAppsManager.MULTIPLE_APPS_USER_NAME.equals(userInfo.name)) {
                        param.setResult(true);
                        XposedLog.verbose("LauncherAppServiceSubModule. isEnabledProfileOf MA!!!!");
                    }
                }
            });
            XposedLog.verbose("LauncherAppServiceSubModule hookIsEnabledProfileOf OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("LauncherAppServiceSubModule Fail hookIsEnabledProfileOf: " + Log.getStackTraceString(e));
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
