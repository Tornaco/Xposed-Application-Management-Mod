package github.tornaco.xposedmoduletest.x;

import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.x.service.IModuleBridge;
import github.tornaco.xposedmoduletest.x.service.XAppGuardServiceDelegate;
import github.tornaco.xposedmoduletest.x.submodules.SubModule;
import github.tornaco.xposedmoduletest.x.submodules.SubModuleManager;
import github.tornaco.xposedmoduletest.x.util.XLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

class XModuleImplSeparable extends XModuleAbs {

    XModuleImplSeparable() {
        IModuleBridge bridge = new XAppGuardServiceDelegate();
        for (SubModule s : SubModuleManager.getInstance().getAllSubModules()) {
            s.onBridgeCreate(bridge);
        }
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        for (SubModule s : SubModuleManager.getInstance().getAllSubModules()) {
            if (s.getInterestedPackages().contains(lpparam.packageName)) {
                try {
                    XLog.logF("Invoking submodule@handleLoadPackage: " + s.name());
                    s.handleLoadingPackage(lpparam.packageName, lpparam);
                } catch (Throwable e) {
                    XLog.logF("Error call handleLoadingPackage from submodule:" + s);
                }
            }
        }
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {

    }
}
