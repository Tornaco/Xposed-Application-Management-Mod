package github.tornaco.xposedmoduletest.xposed.submodules;

import android.content.ComponentName;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.xposed.util.XPosedLog;

/**
 * ActivityStack move to front.
 */
@Deprecated
class ASFSubModule extends AndroidSubModuleModule {

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookActivityStack(lpparam);
    }

    private void hookActivityStack(XC_LoadPackage.LoadPackageParam lpparam) {
        XPosedLog.verbose("ASFSubModule hookActivityStack...");
        try {
            Class stackClass = XposedHelpers.findClass("com.android.server.am.ActivityStack",
                    lpparam.classLoader);

            @SuppressWarnings("unchecked")
            Method toHook = stackClass.getDeclaredMethod("moveToFront", String.class);
            XC_MethodHook.Unhook unHooks = XposedBridge.hookMethod(toHook, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    Object taskRecord = XposedHelpers.callMethod(param.thisObject, "topTask");
                    if (taskRecord == null) return;
                    String pkgName;
                    Object realActivityObj = XposedHelpers.getObjectField(taskRecord, "realActivity");
                    if (realActivityObj != null) {
                        ComponentName componentName = (ComponentName) realActivityObj;
                        pkgName = componentName.getPackageName();
                    } else {
                        // Using aff instead of PKG.
                        pkgName = (String) XposedHelpers.getObjectField(taskRecord, "affinity");
                    }

                    if (TextUtils.isEmpty(pkgName)) return;

                    getBridge().onPackageMoveToFront(pkgName);
                }
            });
            XPosedLog.verbose("ASFSubModule hookActivityStack OK:" + unHooks);
            setStatus(unhookToStatus(unHooks));
        } catch (Exception e) {
            XPosedLog.verbose("ASFSubModule Fail hook hookActivityStack" + Log.getStackTraceString(e));
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
