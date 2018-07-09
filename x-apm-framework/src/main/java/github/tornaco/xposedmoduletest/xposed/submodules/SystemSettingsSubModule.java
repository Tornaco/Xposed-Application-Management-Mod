package github.tornaco.xposedmoduletest.xposed.submodules;

import android.app.AndroidAppHelper;
import android.provider.Settings;
import android.util.Log;

import java.util.Set;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import github.tornaco.xposedmoduletest.compat.os.XAppOpsManager;
import github.tornaco.xposedmoduletest.xposed.GlobalWhiteList;
import github.tornaco.xposedmoduletest.xposed.XAppBuildVar;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

class SystemSettingsSubModule extends AndroidSubModule {
    @Override
    public String needBuildVar() {
        return XAppBuildVar.APP_OPS;
    }

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
        super.initZygote(startupParam);
        hookPutInt();
    }

    private void hookPutInt() {
        XposedLog.boot(XposedLog.PREFIX_OPS + "hookPutInt...");
        try {
            Class sceclass = XposedHelpers.findClass("android.provider.Settings$System",
                    null);
            Set unHooks = XposedBridge.hookAllMethods(sceclass, "putIntForUser",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);

                            boolean permControlEnabled = XAPMManager.get().isServiceAvailable()
                                    && XAPMManager.get().isPermissionControlEnabled();
                            if (!permControlEnabled) {
                                return;
                            }

                            String name = String.valueOf(param.args[1]);
                            if (Settings.System.SCREEN_BRIGHTNESS_MODE.equals(name)
                                    || Settings.System.SCREEN_BRIGHTNESS.equals(name)) {
                                XAPMManager ash = XAPMManager.get();
                                if (ash.isServiceAvailable()) {
                                    String pkgName = AndroidAppHelper.currentPackageName();

                                    if (pkgName == null || GlobalWhiteList.isInGlobalWhiteList(pkgName)) {
                                        Log.d(XposedLog.TAG_DANGER + XposedLog.PREFIX_OPS,
                                                "Do not block OP_CHANGE_BRIGHTNESS!!! for white-listed: " + pkgName);
                                        return;
                                    }

                                    int mode = ash.getPermissionControlBlockModeForPkg(
                                            XAppOpsManager.OP_CHANGE_BRIGHTNESS, pkgName,
                                            true);
                                    Log.d(XposedLog.TAG_DANGER + XposedLog.PREFIX_OPS,
                                            "set OP_CHANGE_BRIGHTNESS, pkg:" + pkgName + ", name: " + name);
                                    if (mode == XAppOpsManager.MODE_IGNORED) {
                                        Log.d(XposedLog.TAG_DANGER + XposedLog.PREFIX_OPS,
                                                "Block OP_CHANGE_BRIGHTNESS!!! for: " + pkgName);
                                        param.setResult(true);
                                    }
                                }
                            }
                        }
                    });
            XposedLog.boot(XposedLog.PREFIX_OPS + "hookPutInt OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.boot(XposedLog.PREFIX_OPS + "Fail hookPutInt: " + Log.getStackTraceString(e));
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
