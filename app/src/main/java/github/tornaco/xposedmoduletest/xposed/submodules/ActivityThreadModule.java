package github.tornaco.xposedmoduletest.xposed.submodules;

import android.app.ActivityThread;
import android.util.Log;

import com.google.common.collect.Sets;

import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/11/7.
 * Email: Tornaco@163.com
 */

public class ActivityThreadModule extends AndroidSubModule {

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        // XposedLog.verbose("ActivityModule handleLoadingPackage@" + lpparam.packageName);
        // hookActivityThreadForApp(lpparam.packageName);
    }

    private void hookActivityThreadForApp(final String pkg) {
        XposedLog.verbose("hookActivityThreadForApp: " + pkg);
        try {
            Set unHooks = XposedBridge.hookAllMethods(ActivityThread.class, "handleResumeActivity",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            XposedLog.verbose("handleResumeActivity: " + param.args[0]);
                        }
                    });
            setStatus(unhooksToStatus(unHooks));
            XposedLog.verbose("hookActivityThreadForApp OK:" + unHooks);
        } catch (Throwable e) {
            XposedLog.verbose("Fail hookActivityThreadForApp: " + pkg + ", error:" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }

    //
    @Override
    public Set<String> getInterestedPackages() {
        return Sets.newHashSet("*");
    }
}
