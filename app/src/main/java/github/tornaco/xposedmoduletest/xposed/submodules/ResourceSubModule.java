package github.tornaco.xposedmoduletest.xposed.submodules;

import android.app.AndroidAppHelper;
import android.content.res.Configuration;
import android.os.Build;
import android.text.TextUtils;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

class ResourceSubModule extends AppGuardAndroidSubModule {

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
        super.initZygote(startupParam);
        hookRes();
    }

    private void hookRes() {

        Class clz = XposedHelpers.findClass(
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ?
                        "android.content.res.ResourcesImpl"
                        : "android.content.res.Resources", null);

        XposedLog.verbose("ResourceSubModule initZygote :" + clz);

        Object unhooks = XposedBridge.hookAllMethods(clz, "updateConfiguration", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                Configuration configuration = (Configuration) param.args[0];
                if (configuration == null) return;
                String packageName = AndroidAppHelper.currentPackageName();
                if (TextUtils.isEmpty(packageName)) return;

                XposedLog.verbose("ResourceSubModule packageName " + packageName);
                int densityDpi = configuration.densityDpi;
                XposedLog.verbose("ResourceSubModule orig densityDpi " + densityDpi);

                configuration.densityDpi = densityDpi;
                configuration.fontScale = configuration.fontScale;
                configuration.orientation = configuration.orientation;
                configuration.screenHeightDp = configuration.screenHeightDp;
                configuration.screenWidthDp = configuration.screenWidthDp;
            }
        });

        XposedLog.verbose("ResourceSubModule hook Res done: " + unhooks);
    }

}
