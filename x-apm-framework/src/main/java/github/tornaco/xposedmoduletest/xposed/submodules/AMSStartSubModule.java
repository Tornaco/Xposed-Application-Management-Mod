package github.tornaco.xposedmoduletest.xposed.submodules;

import android.content.Context;
import android.util.Log;

import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.xposed.service.am.AMSProxy;
import github.tornaco.xposedmoduletest.xposed.service.am.ActiveServicesProxy;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

class AMSStartSubModule extends AndroidSubModule {

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookAMSStart(lpparam);
    }

    private void hookAMSStart(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("hookAMSStart...");
        try {
            Class ams = XposedHelpers.findClass("com.android.server.am.ActivityManagerService",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(ams, "start", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    Context context = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                    getBridge().attachContext(context);
                    getBridge().publish();

                    // Construct proxy.
                    AMSProxy proxy = new AMSProxy(param.thisObject);
                    getBridge().attachAMS(proxy);

                    Object mServices = XposedHelpers.getObjectField(param.thisObject, "mServices");
                    ActiveServicesProxy activeServicesProxy = new ActiveServicesProxy(mServices);
                    getBridge().attachActiveServices(activeServicesProxy);
                }
            });
            XposedLog.verbose("hookAMSStart OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hook hookAMSStart");
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }

    @Override
    public boolean isCoreModule() {
        return true;
    }


    @Override
    public Priority priority() {
        return Priority.High;
    }
}
