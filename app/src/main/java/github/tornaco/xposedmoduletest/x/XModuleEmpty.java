package github.tornaco.xposedmoduletest.x;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Empty hook module.
 */

class XModuleEmpty extends XModuleAbs {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {

    }
}
