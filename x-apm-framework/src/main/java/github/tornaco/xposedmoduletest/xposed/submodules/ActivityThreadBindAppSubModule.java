package github.tornaco.xposedmoduletest.xposed.submodules;

import android.content.pm.ApplicationInfo;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.util.Log;

import java.util.Set;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/11/7.
 * Email: Tornaco@163.com
 */

public class ActivityThreadBindAppSubModule extends AndroidSubModule {

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
        super.initZygote(startupParam);
        hookHandleBindApplication();
    }

    private void hookHandleBindApplication() {
        XposedLog.verbose("hookHandleBindApplication");
        try {
            Class clz = XposedHelpers.findClass("android.app.ActivityThread", null);
            Set unHooks = XposedBridge.hookAllMethods(clz, "handleBindApplication",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            Object appBindData = param.args[0];
                            if (appBindData == null) return;
                            Configuration configuration = (Configuration) XposedHelpers.getObjectField(appBindData, "config");
                            ApplicationInfo applicationInfo = (ApplicationInfo) XposedHelpers.getObjectField(appBindData, "appInfo");
                            if (configuration == null || applicationInfo == null) return;
                            // Do not hook android config for safety.
                            if ("android".equals(applicationInfo.packageName)) {
                                return;
                            }
                            // Check
                            CompatibilityInfo compatibilityInfo
                                    = (CompatibilityInfo) XposedHelpers.getObjectField(appBindData, "compatInfo");
                            if (XAPMManager.get().isServiceAvailable()) {
                                int densityDpi = XAPMManager.get().getAppConfigOverlayIntSetting(applicationInfo.packageName, "densityDpi");
                                if (BuildConfig.DEBUG) {
                                    Log.d(XposedLog.TAG, "handleBindApplication: "
                                            + applicationInfo.packageName + "-" + configuration.densityDpi + "-" + densityDpi
                                            + "-" + compatibilityInfo);
                                }
                                if (densityDpi != XAPMManager.ConfigOverlays.NONE) {
                                    configuration.densityDpi = densityDpi;
                                    if (compatibilityInfo != null) {
                                        XposedHelpers.setObjectField(compatibilityInfo, "applicationDensity", densityDpi);
                                        if (BuildConfig.DEBUG) {
                                            XposedLog.verbose("Change compatibilityInfo to: " + compatibilityInfo);
                                        }
                                    }
                                }
                            }
                        }
                    });
            setStatus(unhooksToStatus(unHooks));
            XposedLog.verbose("hookHandleBindApplication OK:" + unHooks);
        } catch (Throwable e) {
            XposedLog.verbose("Fail hookHandleBindApplication" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }

}
