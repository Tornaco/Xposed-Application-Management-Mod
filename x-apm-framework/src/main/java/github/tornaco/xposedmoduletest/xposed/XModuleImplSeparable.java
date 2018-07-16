package github.tornaco.xposedmoduletest.xposed;

import android.util.Log;

import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.xposed.service.IModuleBridge;
import github.tornaco.xposedmoduletest.xposed.service.XModuleServiceDelegate;
import github.tornaco.xposedmoduletest.xposed.submodules.SubModule;
import github.tornaco.xposedmoduletest.xposed.submodules.SubModuleManager;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

class XModuleImplSeparable extends XModuleAbs {

    XModuleImplSeparable() {
        IModuleBridge bridge = new XModuleServiceDelegate();
        XposedLog.boot("Bridge created: " + bridge);
        for (SubModule s : SubModuleManager.getInstance().getAllSubModules()) {
            s.onBridgeCreate(bridge);
        }
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("handleLoadPackage: " + lpparam.packageName);
        for (SubModule s : SubModuleManager.getInstance().getAllSubModules()) {
            if (s.getInterestedPackages().contains(lpparam.packageName)
                    || s.getInterestedPackages().contains("*")) {
                try {
                    XposedLog.boot("Calling handle load pkg: " + s);
                    s.handleLoadingPackage(lpparam.packageName, lpparam);
                } catch (Throwable e) {
                    XposedLog.wtf("Error call handleLoadingPackage submodule:" + s
                            + " , trace: " + Log.getStackTraceString(e));
                }
            }
        }
    }

    @Override
    public void initZygote(StartupParam startupParam) {
        for (SubModule s : SubModuleManager.getInstance().getAllSubModules()) {
            try {
                XposedLog.boot("Calling init zygote: " + s);
                s.initZygote(startupParam);
            } catch (Throwable e) {
                XposedLog.wtf("Error call initZygote submodule:" + s
                        + " , trace: " + Log.getStackTraceString(e));
            }
        }
    }
}
