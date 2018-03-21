package github.tornaco.xposedmoduletest.xposed.submodules;

import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;

import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.xposed.util.ObjectToStringUtil;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/11/9.
 * Email: Tornaco@163.com
 */

public class IFWSubModule extends AndroidSubModule {
    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookIntentFireWall(lpparam);
    }

    private void hookIntentFireWall(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("hookIntentFireWall...");
        try {
            Class hookClass = XposedHelpers.findClass("com.android.server.firewall.IntentFirewall",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(hookClass, "checkService",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);

                            Intent intent = (Intent) param.args[1];
                            // Dump intent for debug build.
                            if (BuildConfig.DEBUG && XposedLog.isVerboseLoggable()) {
                                try {
                                    Log.d(XposedLog.TAG + XposedLog.PREFIX_SERVICE,
                                            "checkService@ intent: " + intent + "extra: " + intent.getExtras()
                                                    + ObjectToStringUtil.intentToString(intent));
                                } catch (Exception ignored) {
                                }
                            }

                            ComponentName componentName = (ComponentName) param.args[0];
                            int callerID = (int) param.args[2];
                            boolean res = getBridge().checkService(intent, componentName, callerID);
                            if (!res) {
                                param.setResult(false);
                            }
                        }
                    });

            XposedLog.boot("hookIntentFireWall checkService OK:" + unHooks);

            Set unHooks2 = XposedBridge.hookAllMethods(hookClass, "checkBroadcast", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    int callerUid = (int) param.args[1];
                    int recUid = (int) param.args[4];
                    Intent intent = (Intent) param.args[0];
                    if (intent == null) return;

                    // Dump intent for debug build.
                    if (BuildConfig.DEBUG && XposedLog.isVerboseLoggable()) {
                        try {
                            Log.d(XposedLog.TAG + XposedLog.PREFIX_BROADCAST,
                                    "checkBroadcast@ intent: " + intent + "extra: " + intent.getExtras()
                                            + ObjectToStringUtil.intentToString(intent));
                        } catch (Exception ignored) {
                        }
                    }

                    boolean res = getBridge().checkBroadcast(intent, recUid, callerUid);
                    if (!res) {
                        param.setResult(false);
                    }
                }
            });

            XposedLog.boot("hookIntentFireWall checkBroadcast OK:" + unHooks2);

            Set unHooks3 = XposedBridge.hookAllMethods(hookClass, "checkIntent",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            if (BuildConfig.DEBUG) {
                                // XposedLog.verbose("IFW checkIntent: " + Arrays.toString(param.args));
                            }
                        }
                    });
            XposedLog.boot("hookIntentFireWall checkIntent OK:" + unHooks3);


            setStatus(unhooksToStatus(unHooks));
            setStatus(unhooksToStatus(unHooks2));
            setStatus(unhooksToStatus(unHooks3));
        } catch (Exception e) {
            XposedLog.verbose("Fail hook hookIntentFireWall");
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
