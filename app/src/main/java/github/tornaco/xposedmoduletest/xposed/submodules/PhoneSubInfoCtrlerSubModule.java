package github.tornaco.xposedmoduletest.xposed.submodules;

import android.util.Log;

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

// Hook hookGetIccSerialNumForSub settings.
class PhoneSubInfoCtrlerSubModule extends IntentFirewallAndroidSubModule {

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookGetIccSerialNumForSub(lpparam);
    }

    private void hookGetIccSerialNumForSub(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("PhoneSubInfoCtrlerSubModule hookGetIccSerialNumForSub...");
        try {
            Class ams = XposedHelpers.findClass("com.android.internal.telephony.PhoneSubInfoController",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(ams, "getIccSerialNumberForSubscriber",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            String callPackageName = (String) param.args[1];
                            XposedLog.verbose("getIccSerialNumberForSubscriber: " + callPackageName
                                    + "-" + param.getResult());
                        }
                    });
            XposedLog.verbose("PhoneSubInfoCtrlerSubModule hookGetIccSerialNumForSub OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("PhoneSubInfoCtrlerSubModule Fail hookGetIccSerialNumForSub: " + Log.getStackTraceString(e));
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
