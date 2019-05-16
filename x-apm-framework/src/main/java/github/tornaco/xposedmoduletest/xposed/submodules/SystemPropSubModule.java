package github.tornaco.xposedmoduletest.xposed.submodules;

import android.app.AndroidAppHelper;
import android.text.TextUtils;
import android.util.Log;

import java.util.Set;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.xposed.GlobalWhiteList;
import github.tornaco.xposedmoduletest.xposed.XAppBuildVar;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.bean.SystemProp;
import github.tornaco.xposedmoduletest.xposed.bean.SystemPropProfile;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

public class SystemPropSubModule extends AndroidSubModule {

    @Override
    public String needBuildVar() {
        return XAppBuildVar.APP_PROP;
    }

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
        super.initZygote(startupParam);
        hookGet();
    }

    private void hookGet() {
        XposedLog.boot("SystemProp hookGet...");
        try {
            Class clz = XposedHelpers.findClass("android.os.SystemProperties", null);
            Set unHooks = XposedBridge.hookAllMethods(clz, "get", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);

                    // Keep system etc clear!
                    // Dangerous!!!
                    // This code cause the system dead...
                    // Native code is not raady.
//                    if (PkgUtil.isSystemOrPhoneOrShell(Binder.getCallingUid())) {
//                        return;
//                    }

                    String callerPackage = AndroidAppHelper.currentPackageName();

                    if (TextUtils.isEmpty(callerPackage) || GlobalWhiteList.isInGlobalWhiteList(callerPackage)) {
                        return;
                    }

                    if (BuildConfig.DEBUG) {
                        Log.d(XposedLog.TAG, "SystemProp get: " + param.args[0] + ", caller: " + callerPackage);
                    }

                    String propKey = (String) param.args[0];

                    // No need.
                    if (propKey == null) {
                        return;
                    }

                    boolean checkProfile = SystemProp.FIELDS.contains(propKey);
                    if (checkProfile) {
                        boolean isSystemPropEnabled = XAPMManager.get()
                                .isServiceAvailable() && XAPMManager.get()
                                .isSystemPropEnabled();
                        if (BuildConfig.DEBUG) {
                            Log.d(XposedLog.TAG,
                                    "SystemProp get, will check for key: " + propKey
                                            + ", isSystemPropEnabled: " + isSystemPropEnabled);
                        }
                        if (isSystemPropEnabled) {
                            // Check if should apply this this package.
                            boolean enabledForThisPackage =
                                    !GlobalWhiteList.isInGlobalWhiteList(callerPackage)
                                            && XAPMManager.get().isSystemPropProfileApplyApp(callerPackage);
                            if (BuildConfig.DEBUG) {
                                Log.d(XposedLog.TAG,
                                        "SystemProp get, enabledForThisPackage: " + enabledForThisPackage);
                            }
                            if (enabledForThisPackage) {
                                SystemPropProfile activeProfile = XAPMManager.get().getActiveSystemPropProfile();
                                if (BuildConfig.DEBUG) {
                                    Log.d(XposedLog.TAG,
                                            "SystemProp get, activeProfile: " + activeProfile);
                                }
                                if (activeProfile != null && activeProfile.getSystemProp() != null) {
                                    String value = SystemProp.getProp(activeProfile.getSystemProp(), propKey);
                                    if (BuildConfig.DEBUG) {
                                        Log.d(XposedLog.TAG,
                                                "SystemProp get, replace with value: " + value);
                                    }
                                    param.setResult(value);
                                }
                            }
                        }
                    }
                }
            });
            setStatus(unhooksToStatus(unHooks));
            XposedLog.boot("SystemProp hookGet OK: " + unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.boot("SystemProp Fail hookGet: " + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
