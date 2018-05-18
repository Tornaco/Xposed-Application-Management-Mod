package github.tornaco.xposedmoduletest.xposed.submodules;

import android.app.AndroidAppHelper;
import android.content.res.Configuration;
import android.os.Build;
import android.util.Log;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */
class ResourceSubModule extends AndroidSubModule {

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
        super.initZygote(startupParam);
        hookRes();
    }

    private void hookRes() {
        try {
            Class clz = XposedHelpers.findClass(
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ?
                            "android.content.res.ResourcesImpl"
                            : "android.content.res.Resources", null);

            XposedLog.verbose("ResourceSubModule initZygote :" + clz);

            Object unhooks = XposedBridge.hookAllMethods(clz, "updateConfiguration",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            Configuration configuration = (Configuration) param.args[0];
                            if (configuration == null) return;
                            String currentPackage = AndroidAppHelper.currentPackageName();

                            if (XAPMManager.get().isServiceAvailable()) {
                                int densityDpi = XAPMManager.get().getAppConfigOverlayIntSetting(currentPackage, "densityDpi");
                                if (BuildConfig.DEBUG) {
                                    Log.d(XposedLog.TAG, "updateConfiguration: "
                                            + currentPackage + "-" + configuration.densityDpi + "-" + densityDpi);
                                }
                                if (densityDpi != XAPMManager.ConfigOverlays.NONE) {
                                    configuration.densityDpi = densityDpi;
                                }
                            }
                        }
                    });

            XposedLog.verbose("ResourceSubModule hook Res done: " + unhooks);
        } catch (Exception e) {
            XposedLog.wtf("Fail hook @ResourceSubModule: " + Log.getStackTraceString(e));
        }
    }

}
