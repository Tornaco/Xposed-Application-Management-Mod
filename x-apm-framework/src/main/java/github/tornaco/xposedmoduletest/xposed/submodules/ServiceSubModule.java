package github.tornaco.xposedmoduletest.xposed.submodules;

import android.app.AndroidAppHelper;
import android.app.Service;
import android.content.ComponentName;
import android.util.Log;

import java.util.Set;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import github.tornaco.xposedmoduletest.xposed.XAppBuildVar;
import github.tornaco.xposedmoduletest.xposed.app.IServiceControlAdapter;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */
class ServiceSubModule extends AndroidSubModule {

    @Override
    public String needBuildVar() {
        return XAppBuildVar.APP_LAZY;
    }

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
        super.initZygote(startupParam);
        hookTheFuckingServiceAttach();
    }

    private void hookTheFuckingServiceAttach() {
        XposedLog.verbose("hookTheFuckingServiceAttach...");
        try {
            Class clz = XposedHelpers.findClass("android.app.Service", null);
            Set unHooks = XposedBridge.hookAllMethods(clz,
                    "attach", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(final MethodHookParam param)
                                throws Throwable {
                            super.afterHookedMethod(param);

                            // Log.d(XposedLog.TAG_LAZY, "attach service: " + Arrays.toString(param.args));

                            final Service service = (Service) param.thisObject;

                            if (service == null) {
                                Log.d(XposedLog.TAG_LAZY, "We got null service for:"
                                        + AndroidAppHelper.currentPackageName());
                                return;
                            }

                            final String hostPackage = service.getPackageName();

                            if (hostPackage == null || "android".equals(hostPackage)) {
                                // Do not block any android service.
                                return;
                            }

                            if (XAPMManager.get().isServiceAvailable()
                                    && XAPMManager.get().isLazyModeEnabledForPackage(hostPackage)) {

                                // Retrieve class name.
                                String className = (String) XposedHelpers.getObjectField(service, "mClassName");
                                Log.d(XposedLog.TAG_LAZY,
                                        "Service attached: " + service
                                                + ", host: " + hostPackage
                                                + ", class: " + className);

                                if (className == null) {
                                    return;
                                }

                                // Register control.
                                ComponentName componentName = new ComponentName(hostPackage, className);
                                IServiceControlAdapter serviceControlAdapter = new IServiceControlAdapter(service, componentName);
                                Log.d(XposedLog.TAG_LAZY, "Registering serviceControlAdapter: " + serviceControlAdapter);
                                XAPMManager.get().registerController(serviceControlAdapter);
                            }
                        }
                    });
            XposedLog.verbose("hookTheFuckingServiceAttach OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hookTheFuckingServiceAttach:" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
