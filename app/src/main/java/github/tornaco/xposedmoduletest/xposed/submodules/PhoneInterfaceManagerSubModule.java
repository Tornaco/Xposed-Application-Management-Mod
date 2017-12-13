package github.tornaco.xposedmoduletest.xposed.submodules;

import android.util.Log;

import com.google.common.collect.Sets;

import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

// Hook hookGetDeviceID settings.
class PhoneInterfaceManagerSubModule extends IntentFirewallAndroidSubModule {

    @Override
    public Set<String> getInterestedPackages() {
        return Sets.newHashSet("com.android.phone");
    }

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookGetDeviceID(lpparam);
    }

    private void hookGetDeviceID(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("PhoneInterfaceManagerSubModule hookGetDeviceID...");
        try {
            Class ams = XposedHelpers.findClass("com.android.phone.PhoneInterfaceManager",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(ams, "getDeviceId",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            String callPackageName = (String) param.args[0];
                            XposedLog.verbose("getDeviceId: " + callPackageName
                                    + "-" + param.getResult());
                        }
                    });
            XposedLog.verbose("PhoneInterfaceManagerSubModule hookGetDeviceID OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("PhoneInterfaceManagerSubModule Fail hookGetDeviceID: " + Log.getStackTraceString(e));
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
