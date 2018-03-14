package github.tornaco.xposedmoduletest.xposed.submodules;

import android.app.AndroidAppHelper;
import android.util.Log;

import com.google.common.collect.Sets;

import java.util.Set;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

class AndroidProcessSubModule extends AndroidSubModule {

    private static final String TAG = "AndroidProcess-";

    @Override
    public String needBuildVar() {
        return "APP_DEBUG";
    }

    @Override
    public Set<String> getInterestedPackages() {
        return Sets.newHashSet("*");
    }

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
        super.initZygote(startupParam);
        hookKillProcess();
    }

    private void hookKillProcess() {
        XposedLog.verbose(TAG + "hookKillProcess...");
        try {
            Class clz = XposedHelpers.findClass("android.os.Process", null);
            Set unHooks = XposedBridge.hookAllMethods(clz,
                    "killProcessQuiet", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            int pid = (int) param.args[0];
                            String currentPkg = AndroidAppHelper.currentPackageName();
                            Log.d(XposedLog.TAG, TAG + "killProcess: " + pid + ", pkg: " + currentPkg);
                        }
                    });
            XposedLog.verbose(TAG + "hookKillProcess OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose(TAG + "Fail hookKillProcess:" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
