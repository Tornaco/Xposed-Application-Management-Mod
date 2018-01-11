package github.tornaco.xposedmoduletest.xposed.submodules;

import android.content.Intent;
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

// https://github.com/LineageOS/android_frameworks_base/blob/cm-14.1/services/core/java/com/android/server/am/ActiveServices.java
// Hook hookRetrieveService settings.

@Deprecated
class ActiveServiceSubModule2 extends AndroidSubModule {

//    14.1
//    private ServiceLookupResult retrieveServiceLocked(Intent service,
//                                                      String resolvedType, String callingPackage, int callingPid, int callingUid, int userId,
//                                                      boolean createIfNeeded, boolean callingFromFg, boolean isBindExternal) {
//    }

//    13.0 12.1
//    private ServiceLookupResult retrieveServiceLocked(Intent service,
//                                                      String resolvedType, String callingPackage, int callingPid, int callingUid, int userId,
//                                                      boolean createIfNeeded, boolean callingFromFg) {


    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookRetrieveService(lpparam);
    }

    private void hookRetrieveService(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("hookRetrieveService...");
        try {
            Class ams = XposedHelpers.findClass("com.android.server.am.ActiveServices",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(ams, "retrieveServiceLocked",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            Intent service = (Intent) param.args[0];
                            String callingPackage = (String) param.args[2];
                            int callingPid = (int) param.args[3];
                            int callingUid = (int) param.args[4];
                            boolean callingFromFg = (boolean) param.args[7];
                            boolean allow = getBridge()
                                    .checkService(service, callingPackage, callingPid, callingUid, callingFromFg);
                            if (!allow) {
                                param.setResult(null);
                            }
                        }
                    });
            XposedLog.verbose("hookRetrieveService OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hookRetrieveService: " + Log.getStackTraceString(e));
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
