package github.tornaco.xposedmoduletest.xposed.submodules;

import android.util.Log;

import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.xposed.service.notification.NotificationManagerServiceProxy;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

// FIXME Have not check M L O yet.
class NotificationManagerServiceSubModule extends IntentFirewallAndroidSubModule {

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookOnStart(lpparam);
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
                                getIntentFirewallBridge().attachNotificationService(proxy);
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
}
