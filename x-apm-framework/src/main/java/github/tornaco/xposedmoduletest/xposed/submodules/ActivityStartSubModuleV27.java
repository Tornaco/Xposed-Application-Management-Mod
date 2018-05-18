package github.tornaco.xposedmoduletest.xposed.submodules;

import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

class ActivityStartSubModuleV27 extends ActivityStartSubModule {
    @Override
    Class clzForStartActivityMayWait(XC_LoadPackage.LoadPackageParam lpparam)
            throws ClassNotFoundException {
        return XposedHelpers.findClass("com.android.server.am.ActivityStarter", lpparam.classLoader);
    }
}
