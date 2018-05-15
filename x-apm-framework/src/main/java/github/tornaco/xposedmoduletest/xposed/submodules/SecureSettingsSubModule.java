package github.tornaco.xposedmoduletest.xposed.submodules;

import android.app.AndroidAppHelper;
import android.provider.Settings;
import android.util.Log;

import java.util.Set;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.xposed.XAppBuildVar;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

// Hook hookGetStringForUser settings.
class SecureSettingsSubModule extends AndroidSubModule {
    @Override
    public String needBuildVar() {
        return XAppBuildVar.APP_PRIVACY;
    }

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
        super.initZygote(startupParam);
        hookGetStringForUser();
    }

    private void hookGetStringForUser() {
        XposedLog.verbose("hookGetStringForUser...");
        try {
            Class sceclass = XposedHelpers.findClass("android.provider.Settings$Secure",
                    null);
            Set unHooks = XposedBridge.hookAllMethods(sceclass, "getStringForUser",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            String name = String.valueOf(param.args[1]);
                            if (Settings.Secure.ANDROID_ID.equals(name)) {
                                // Use of defined id.
                                XAPMManager ash = XAPMManager.get();
                                if (ash.isServiceAvailable()) {
                                    String pkgName = AndroidAppHelper.currentPackageName();
                                    boolean priv =
                                            pkgName != null
                                                    && ash.isPrivacyEnabled()
                                                    && ash.isPackageInPrivacyList(pkgName);
                                    if (BuildConfig.DEBUG) {
                                        Log.d(XposedLog.TAG_DANGER,
                                                "get ANDROID_ID, pkg:" + pkgName);
                                    }
                                    if (priv) {
                                        String androidId = ash.getUserDefinedAndroidId();
                                        if (androidId != null) {
                                            Log.d(XposedLog.TAG_DANGER, "Using user defined ANDROID_ID!!! for: " + pkgName);
                                            param.setResult(androidId);
                                        }
                                    }
                                }
                            }
                        }
                    });
            XposedLog.verbose("hookGetStringForUser OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hookGetStringForUser: " + Log.getStackTraceString(e));
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
