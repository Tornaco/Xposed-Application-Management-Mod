package github.tornaco.xposedmoduletest.xposed.submodules;

import android.content.ComponentName;
import android.util.Log;

import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.util.OSUtil;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */
public class TaskChangeNotificationControllerSubModule extends AndroidSubModule {

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        if (OSUtil.isOOrAbove()) {
            hookNotifyTaskCreated(lpparam);
        }
    }

    private void hookNotifyTaskCreated(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.boot("hookNotifyTaskCreated...");

        try {
            String clazzName = OSUtil.isQOrAbove()
                    ? "com.android.server.wm.TaskChangeNotificationController"
                    : "com.android.server.am.TaskChangeNotificationController";
            Class clz = XposedHelpers.findClass(clazzName, lpparam.classLoader);

            Set unHooks = XposedBridge.hookAllMethods(clz,
                    "notifyTaskCreated", new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            int taskId = (int) param.args[0];
                            ComponentName componentName = (ComponentName) param.args[1];
                            XposedLog.verbose("notifyTaskCreated task %s componentName %s", taskId, componentName);
                            getBridge().notifyTaskCreated(taskId, componentName);
                        }
                    });
            XposedLog.boot("hookNotifyTaskCreated OK: " + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.boot("Fail hookNotifyTaskCreated: " + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
