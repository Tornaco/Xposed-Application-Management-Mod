package github.tornaco.xposedmoduletest.xposed.submodules;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
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

    private static final Map<Integer, String> TASK_ID_MAP = new HashMap<>();

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        if (OSUtil.isOOrAbove()) {
            hookNotifyTaskSnapshotChanged(lpparam);
            hookNotifyTaskCreated(lpparam);
        }
    }

    private void hookNotifyTaskCreated(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.boot("hookNotifyTaskCreated...");

        try {
            Class clz = XposedHelpers.findClass("com.android.server.am.TaskChangeNotificationController",
                    lpparam.classLoader);

            Set unHooks = XposedBridge.hookAllMethods(clz,
                    "notifyTaskCreated", new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            int taskId = (int) param.args[0];
                            ComponentName componentName = (ComponentName) param.args[1];
                            XposedLog.verbose("notifyTaskCreated task %s componentName %s", taskId, componentName);
                            TASK_ID_MAP.put(taskId, componentName.getPackageName());
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

    private void hookNotifyTaskSnapshotChanged(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.boot("hookNotifyTaskSnapshotChanged...");

        try {
            Class clz = XposedHelpers.findClass("com.android.server.am.TaskChangeNotificationController",
                    lpparam.classLoader);

            Set unHooks = XposedBridge.hookAllMethods(clz,
                    "notifyTaskSnapshotChanged", new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            int taskId = (int) param.args[0];
                            ActivityManager.TaskSnapshot snapshot = (ActivityManager.TaskSnapshot) param.args[1];
                            if (snapshot == null) return;
                            String pkgName = TASK_ID_MAP.get(taskId);
                            if (XposedLog.isVerboseLoggable()) {
                                XposedLog.verbose("notifyTaskSnapshotChanged task %s pkg %s", taskId, pkgName);
                            }
                            boolean blur = getBridge().isBlurForPkg(pkgName);
                            if (blur) {
                                XposedHelpers.setObjectField(snapshot, "mSnapshot", null);
                                XposedLog.verbose("Set mSnapshot for blur package");
                            } else {
                                XposedLog.verbose("Blur is not enabled for this one");
                            }
                        }
                    });
            XposedLog.boot("hookNotifyTaskSnapshotChanged OK: " + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.boot("Fail hookNotifyTaskSnapshotChanged: " + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
