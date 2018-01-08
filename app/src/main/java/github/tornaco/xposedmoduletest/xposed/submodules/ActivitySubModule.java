package github.tornaco.xposedmoduletest.xposed.submodules;

import android.util.Log;

import com.google.common.collect.Sets;

import java.util.Set;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

class ActivitySubModule extends IntentFirewallAndroidSubModule {

    @Override
    public String needBuildVar() {
        return super.needBuildVar();
    }

    @Override
    public int needMinSdk() {
        return super.needMinSdk();
    }

    @Override
    public Set<String> getInterestedPackages() {
        return Sets.newHashSet("*");
    }

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        super.handleLoadingPackage(pkg, lpparam);

        if ("com.coolapk.market".equals(lpparam.packageName)) {
            XposedLog.danger("LOADING COOLAPK");
        }
    }

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
        super.initZygote(startupParam);
        hookOnBackPressed();
    }

    private void hookOnBackPressed() {
        XposedLog.verbose("hookOnBackPressed...");
        try {
            Class clz = XposedHelpers.findClass("android.app.Activity", null);
            Set unHooks = XposedBridge.hookAllMethods(clz,
                    "onKeyUp", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            XposedLog.verbose("onKeyUp: " + param.thisObject);
                        }
                    });
            XposedLog.verbose("hookOnBackPressed OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hookOnBackPressed:" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
