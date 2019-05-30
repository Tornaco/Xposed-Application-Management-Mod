package github.tornaco.xposedmoduletest.xposed.submodules;

import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.Arrays;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.util.OSUtil;
import github.tornaco.xposedmoduletest.xposed.service.notification.NotificationManagerServiceProxy;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

// FIXME Have not check M L O yet.
// https://github.com/LineageOS/android_frameworks_base/blob/cm-12.1/services/core/java/com/android/server/notification/NotificationManagerService.java
class NotificationManagerServiceSubModule extends AndroidSubModule {

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookOnStart(lpparam);

        // Listen for the notification post.
        hookNotificationListeners(lpparam);
        hookNotificationListenersRemove(lpparam);
    }

    private void hookOnStart(final XC_LoadPackage.LoadPackageParam lpparam) {
        logOnBootStage("hookOnStart...");
        try {
            Class clz = XposedHelpers.findClass("com.android.server.notification.NotificationManagerService",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(clz,
                    "onStart", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            Object service = param.thisObject;
                            if (service != null) {
                                NotificationManagerServiceProxy proxy = new NotificationManagerServiceProxy(service);
                                getBridge().attachNotificationService(proxy);
                            }
                        }
                    });
            logOnBootStage("hookOnStart OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            logOnBootStage("Fail hookOnStart:" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }

    private void hookNotificationListeners(final XC_LoadPackage.LoadPackageParam lpparam) {
        logOnBootStage("hookNotificationListeners...");
        try {
            Class clz = XposedHelpers.findClass("com.android.server.notification.NotificationManagerService$NotificationListeners",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(clz, "notifyPostedLocked", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    if (BuildConfig.DEBUG && XposedLog.isVerboseLoggable()) {
                        XposedLog.verbose("NotificationListeners, notifyPosted: " + Arrays.toString(param.args));
                    }
                    Object object = param.args[0];
                    if (OSUtil.isPOrAbove()) {
                        // FIXME Need a solution for Android P.
                        XposedLog.verbose("NotificationListeners No impl for android p");
                    } else if (object instanceof StatusBarNotification) {
                        StatusBarNotification sbn = (StatusBarNotification) param.args[0];
                        getBridge().onNotificationPosted(sbn);
                    }
                }
            });
            logOnBootStage("hookNotificationListeners OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            logOnBootStage("Fail hookNotificationListeners:" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }

    private void hookNotificationListenersRemove(final XC_LoadPackage.LoadPackageParam lpparam) {
        logOnBootStage("hookNotificationListenersRemove...");
        try {
            Class clz = XposedHelpers.findClass("com.android.server.notification.NotificationManagerService$NotificationListeners",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(clz, "notifyRemovedLocked", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    if (BuildConfig.DEBUG && XposedLog.isVerboseLoggable()) {
                        XposedLog.verbose("NotificationListeners, notifyRemoved: " + Arrays.toString(param.args));
                    }
                    Object object = param.args[0];
                    if (OSUtil.isPOrAbove()) {
                        XposedLog.wtf("NotificationListeners No impl for android p");
                    } else if (object instanceof StatusBarNotification) {
                        StatusBarNotification sbn = (StatusBarNotification) param.args[0];
                        getBridge().onNotificationRemoved(sbn);
                    }
                }
            });
            logOnBootStage("hookNotificationListenersRemove OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            logOnBootStage("Fail hookNotificationListenersRemove:" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}