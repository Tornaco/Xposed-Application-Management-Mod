package github.tornaco.xposedmoduletest.xposed.submodules;

import android.app.AndroidAppHelper;
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

public class ResourceManagerApplyConfigSubModule extends AndroidSubModule {

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
        super.initZygote(startupParam);
        hookApplyConfigurationToResourcesLocked();
    }

    private void hookApplyConfigurationToResourcesLocked() {
        XposedLog.verbose("hookApplyConfigurationToResourcesLocked");
        try {
            Class clz = XposedHelpers.findClass("android.app.ResourcesManager", null);
            Set unHooks = XposedBridge.hookAllMethods(clz, "applyConfigurationToResourcesLocked",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            Configuration configuration = (Configuration) param.args[0];
                            if (configuration == null) return;
                            String currentPackage = AndroidAppHelper.currentPackageName();
                            // Check
//                            CompatibilityInfo compatibilityInfo
//                                    = (CompatibilityInfo) param.args[1];
                            if (XAPMManager.get().isServiceAvailable()) {
                                int densityDpi = XAPMManager.get().getAppConfigOverlayIntSetting(currentPackage, "densityDpi");
                                if (BuildConfig.DEBUG) {
                                    Log.d(XposedLog.TAG, "handleBindApplication: "
                                                    + currentPackage + "-" + configuration.densityDpi + "-" + densityDpi
//                                            + "-" + compatibilityInfo
                                    );
                                }
                                if (densityDpi != XAPMManager.ConfigOverlays.NONE) {
                                    configuration.densityDpi = densityDpi;
//                                    if (compatibilityInfo != null) {
//                                        XposedHelpers.setObjectField(compatibilityInfo, "applicationDensity", densityDpi);
//                                        if (BuildConfig.DEBUG) {
//                                            XposedLog.verbose("handleBindApplication change compatibilityInfo to: " + compatibilityInfo);
//                                        }
//                                    }
                                }
                            }
                        }
                    });
            setStatus(unhooksToStatus(unHooks));
            XposedLog.verbose("hookApplyConfigurationToResourcesLocked OK:" + unHooks);
        } catch (Throwable e) {
            XposedLog.verbose("Fail hookApplyConfigurationToResourcesLocked" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }

}
