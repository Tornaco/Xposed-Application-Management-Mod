package github.tornaco.xposedmoduletest.xposed.submodules;

import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.util.OSUtil;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

class ActivityStartSubModuleV28 extends ActivityStartSubModule {
    @Override
    Class clzForStartActivityMayWait(XC_LoadPackage.LoadPackageParam lpparam)
            throws ClassNotFoundException {
        return XposedHelpers.findClass(
                OSUtil.isQOrAbove()
                        ? "com.android.server.wm.ActivityStarter"
                        : "com.android.server.am.ActivityStarter", lpparam.classLoader);
    }
}
