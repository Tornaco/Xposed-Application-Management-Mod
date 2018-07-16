package github.tornaco.xposedmoduletest.xposed.submodules;

import java.util.Set;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.xposed.service.IModuleBridge;

/**
 * Created by guohao4 on 2017/10/30.
 * Email: Tornaco@163.com
 */

public interface SubModule extends Comparable<SubModule> {

    void onBridgeCreate(IModuleBridge bridge);

    void onBridgeChange(IModuleBridge bridge);

    void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam);

    void initZygote(IXposedHookZygoteInit.StartupParam startupParam);

    Set<String> getInterestedPackages();

    SubModuleStatus getStatus();

    String getErrorMessage();

    String name();

    String needBuildVar();

    int needMinSdk();

    boolean isCoreModule();

    Priority priority();

    enum SubModuleStatus {
        UNKNOWN,
        ERROR,
        READY;

        public static SubModuleStatus valueOf(int i) {
            for (SubModuleStatus s : values()) {
                if (i == s.ordinal()) return s;
            }
            return UNKNOWN;
        }
    }

    enum Priority {
        Low, Normal, High
    }
}
