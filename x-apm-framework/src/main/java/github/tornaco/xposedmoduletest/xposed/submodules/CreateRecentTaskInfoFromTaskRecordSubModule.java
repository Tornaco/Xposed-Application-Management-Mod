package github.tornaco.xposedmoduletest.xposed.submodules;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.util.OSUtil;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

class CreateRecentTaskInfoFromTaskRecordSubModule extends AndroidSubModule {

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookCreateRecentTaskInfoFromTaskRecord(lpparam);
    }

    private void hookCreateRecentTaskInfoFromTaskRecord(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.boot("hookCreateRecentTaskInfoFromTaskRecord...");
        try {
            boolean isPOrAbove = OSUtil.isPOrAbove();
            boolean isQOrAlove = OSUtil.isQOrAbove();
            Class clazz = XposedHelpers.findClass(isPOrAbove
                            ? (isQOrAlove ? "com.android.server.wm.RecentTasks" : "com.android.server.am.RecentTasks")
                            : "com.android.server.am.ActivityManagerService",
                    lpparam.classLoader);
            XposedLog.boot("hookCreateRecentTaskInfoFromTaskRecord...class:" + clazz);
            Set unHooks = XposedBridge.hookAllMethods(clazz,
                    isPOrAbove
                            ? "createRecentTaskInfo"
                            : "createRecentTaskInfoFromTaskRecord",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            ActivityManager.RecentTaskInfo recentTaskInfo = (ActivityManager.RecentTaskInfo) param.getResult();
                            if (recentTaskInfo == null) {
                                return;
                            }
                            Intent baseIntent = recentTaskInfo.baseIntent;
                            if (baseIntent == null) {
                                return;
                            }
                            ComponentName componentName = baseIntent.getComponent();
                            if (componentName == null) {
                                XposedLog.wtf("Null comp for base intent: " + baseIntent);
                                return;
                            }
                            int flags = baseIntent.getFlags();
                            boolean excludeRecent = (flags & Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS) == Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS;
                            String pkgName = componentName.getPackageName();
                            if (TextUtils.isEmpty(pkgName)) {
                                return;
                            }
                            if (BuildConfig.DEBUG) {
                                XposedLog.verbose("- createRecentTaskInfoFromTaskRecord exclude: %s comp: %s", excludeRecent, componentName);
                            }
                            int setting = getBridge().getRecentTaskExcludeSetting(componentName);
                            if (setting == XAPMManager.ExcludeRecentSetting.NONE) {
                                // Do nothing.
                            } else if (setting == XAPMManager.ExcludeRecentSetting.EXCLUDE && !excludeRecent) {
                                flags |= Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS;
                            } else if (setting == XAPMManager.ExcludeRecentSetting.INCLUDE && excludeRecent) {
                                flags &= Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS;
                            }
                            baseIntent.setFlags(flags);
                            recentTaskInfo.baseIntent = baseIntent;
                            param.setResult(recentTaskInfo);

                            if (BuildConfig.DEBUG) {
                                excludeRecent = (flags & Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS) == Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS;
                                XposedLog.verbose("-AFTER createRecentTaskInfoFromTaskRecord exclude: %s comp: %s", excludeRecent, componentName);
                            }
                        }
                    });
            XposedLog.verbose("hookCreateRecentTaskInfoFromTaskRecord OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hookCreateRecentTaskInfoFromTaskRecord: " + Log.getStackTraceString(e));
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
