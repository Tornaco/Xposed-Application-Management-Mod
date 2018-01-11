package github.tornaco.xposedmoduletest.xposed.submodules;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
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

// Hook hookConstructor settings.
class ActivityRecordSubModule extends AndroidSubModule {

    @Override
    public String needBuildVar() {
        return "TORNACO@GMAIL.COM";
    }

    @Override
    public int needMinSdk() {
        return Build.VERSION_CODES.M;
    }

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookConstructor(lpparam);
    }

    private void hookConstructor(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("hookConstructor...");
        try {
            Class c = XposedHelpers.findClass("com.android.server.am.ActivityRecord",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllConstructors(c, new XC_MethodHook() {
                @SuppressLint("WrongConstant")
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    Object ar = param.thisObject;
                    if (ar != null) {
                        Intent intent = (Intent) XposedHelpers.getObjectField(ar, "intent");
                        int flag = intent.getFlags();
                        flag &= ActivityInfo.FLAG_EXCLUDE_FROM_RECENTS;
                        intent.setFlags(flag);
                    }
                }
            });
            XposedLog.verbose("hookConstructor OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hookConstructor: " + Log.getStackTraceString(e));
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
