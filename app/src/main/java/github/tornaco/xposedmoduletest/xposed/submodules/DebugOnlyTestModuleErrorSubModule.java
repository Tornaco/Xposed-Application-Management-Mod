package github.tornaco.xposedmoduletest.xposed.submodules;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

class DebugOnlyTestModuleErrorSubModule extends AndroidSubModule {

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        super.handleLoadingPackage(pkg, lpparam);
        setStatus(SubModuleStatus.ERROR);
    }

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
        super.initZygote(startupParam);
        setStatus(SubModuleStatus.ERROR);
    }
}
