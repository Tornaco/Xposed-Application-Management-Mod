package github.tornaco.xposedmoduletest.xposed.submodules;

import android.util.Log;

import java.util.Set;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

// For no-recent feature.
class PackageParserSubModule extends AndroidSubModule {

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
        super.initZygote(startupParam);
        hookParseActivity();
    }

    private void hookParseActivity() {
        XposedLog.verbose("hookParseActivity...");
        try {
            Class clz = XposedHelpers.findClass("android.content.pm.PackageParser", null);
            Set unHooks = XposedBridge.hookAllMethods(clz,
                    "parseActivity", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            XposedLog.verbose("parseActivity: " + param.getResult());
                        }
                    });
            XposedLog.verbose("hookParseActivity OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hookParseActivity:" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
