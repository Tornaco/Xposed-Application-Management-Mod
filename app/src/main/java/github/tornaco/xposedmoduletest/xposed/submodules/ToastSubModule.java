package github.tornaco.xposedmoduletest.xposed.submodules;

import android.app.AndroidAppHelper;
import android.content.Context;
import android.os.Looper;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.Arrays;

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

class ToastSubModule extends AndroidSubModule {

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
        super.initZygote(startupParam);
        hookMakeToast();
        if (OSUtil.isOOrAbove()) {
            hookMakeToastOreoAddon();
        }
    }

    private void hookMakeToastOreoAddon() {
        XposedLog.verbose("hookMakeToastOreoAddon...");
        try {
            Class clz = XposedHelpers.findClass("android.widget.Toast", null);
            @SuppressWarnings("unchecked") Method m = clz.getDeclaredMethod("makeText", Context.class, Looper.class, CharSequence.class, int.class);
            XC_MethodHook.Unhook unHooks = XposedBridge.hookMethod(m,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            if (isOptEnabled()) {
                                XposedLog.verbose("makeText: " + Arrays.toString(param.args));
                                String appLabel = String.valueOf(PkgUtil.loadNameByPkgName((Context) param.args[0], AndroidAppHelper.currentPackageName()));
                                String newText = "@" + appLabel + "\t" + param.args[2];
                                param.args[2] = newText;
                            }
                        }
                    });
            XposedLog.verbose("hookMakeToastOreoAddon OK:" + unHooks);
            setStatus(unhookToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hookMakeToastOreoAddon:" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }

    private boolean isOptEnabled() {
        return XAshmanManager.get().isServiceAvailable() && XAshmanManager.get()
                .isOptFeatureEnabled(XAshmanManager.OPT.TOAST.name());
    }

    private void hookMakeToast() {
        XposedLog.verbose("hookMakeToast...");
        try {
            Class clz = XposedHelpers.findClass("android.widget.Toast", null);
            @SuppressWarnings("unchecked") Method m = clz.getDeclaredMethod("makeText",
                    Context.class, CharSequence.class, int.class);
            XC_MethodHook.Unhook unHooks = XposedBridge.hookMethod(m,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            if (isOptEnabled()) {
                                XposedLog.verbose("makeText: " + Arrays.toString(param.args));
                                String appLabel = String.valueOf(PkgUtil.loadNameByPkgName((Context) param.args[0], AndroidAppHelper.currentPackageName()));
                                String newText = "@" + appLabel + "\t" + param.args[1];
                                param.args[1] = newText;
                            }
                        }
                    });
            XposedLog.verbose("hookMakeToast OK:" + unHooks);
            setStatus(unhookToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hookMakeToast:" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
