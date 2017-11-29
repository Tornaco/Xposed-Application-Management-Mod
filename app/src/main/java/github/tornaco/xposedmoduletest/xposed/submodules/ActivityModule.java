package github.tornaco.xposedmoduletest.xposed.submodules;

import android.app.Activity;
import android.util.Log;

import com.google.common.collect.Sets;

import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.xposed.util.XPosedLog;

/**
 * Created by guohao4 on 2017/11/7.
 * Email: Tornaco@163.com
 */
@Deprecated
public class ActivityModule extends AndroidSubModuleModule {

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookActivityForApp(lpparam.packageName);
    }

    private void hookActivityForApp(final String pkg) {
        try {
            Set unHooks = XposedBridge.hookAllMethods(Activity.class, "onDestroy",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            // Make a binder call crossing process.
                            Activity activity = (Activity) param.thisObject;
                            String pkgName = activity.getPackageName();
                            XPosedLog.wtf("onDestroy: " + activity);
                        }
                    });
            setStatus(unhooksToStatus(unHooks));
        } catch (Throwable e) {
            XPosedLog.verbose("Fail hookActivityForApp: " + pkg + ", error:" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }

    @Override
    public Set<String> getInterestedPackages() {
        return Sets.newHashSet("*");
    }
}
