package github.tornaco.xposedmoduletest.xposed.submodules;

import java.util.Set;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.xposed.service.IModuleBridge;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

abstract class AbsSubModule implements SubModule {
    @Getter
    private IModuleBridge bridge;

    @Getter
    @Setter
    private SubModuleStatus status = SubModuleStatus.UNKNOWN;

    @Getter
    @Setter
    private String errorMessage;

    @Override
    public String needBuildVar() {
        return null;
    }

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
        // Empty.
    }

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        // Empty.
    }

    @Override
    public void onBridgeCreate(IModuleBridge bridge) {
        this.bridge = bridge;
        XposedLog.verbose("onBridgeCreate@" + bridge.serial() + ", assign to: " + getClass().getName());
    }

    @Override
    public String name() {
        return getClass().getSimpleName();
    }

    static SubModuleStatus unhooksToStatus(Set unHooks) {
        if (unHooks == null || unHooks.size() == 0) return SubModuleStatus.ERROR;
        return SubModuleStatus.READY;
    }

    static SubModuleStatus unhookToStatus(XC_MethodHook.Unhook unHooks) {
        if (unHooks == null) return SubModuleStatus.ERROR;
        return SubModuleStatus.READY;
    }
}
