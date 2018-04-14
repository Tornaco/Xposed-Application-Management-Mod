package github.tornaco.xposedmoduletest.xposed.submodules;

import android.app.ActivityThread;
import android.app.Application;
import android.content.Intent;
import android.util.Log;

import java.util.Set;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.xposed.util.ObjectToStringUtil;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

class ActivityThreadSubModule extends AndroidSubModule {

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
        super.initZygote(startupParam);
        hookHandlerReceiver();
    }

    private void hookHandlerReceiver() {
        XposedLog.verbose("hookHandlerReceiver...");
        try {
            Class clz = XposedHelpers.findClass("android.app.ActivityThread", null);
            Set unHooks = XposedBridge.hookAllMethods(clz,
                    "handleReceiver", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            if (BuildConfig.DEBUG) {
                                Object receiverData = param.args[0];
                                XposedLog.verbose("handleReceiver: %s, data: %s", param.thisObject, receiverData);

                                Intent intent = (Intent) XposedHelpers.getObjectField(receiverData, "intent");

                                ActivityThread activityThread = (ActivityThread) param.thisObject;
                                Application application = activityThread.getApplication();
                                XposedLog.verbose("handleReceiver: app: %s, intent: %s", application, ObjectToStringUtil.intentToString(intent));
                            }
                        }
                    });
            XposedLog.verbose("hookHandlerReceiver OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hookHandlerReceiver:" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
