package github.tornaco.xposedmoduletest.x.submodules;

import java.util.Set;

import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.x.service.XAppGuardServiceAbs;

/**
 * Created by guohao4 on 2017/10/30.
 * Email: Tornaco@163.com
 */

public interface SubModule {

    void onAppGuardServiceCreate(XAppGuardServiceAbs service);

    void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam);

    Set<String> getInterestedPackages();

    SubModuleStatus getStatus();

    String getErrorMessage();

    String name();

    enum SubModuleStatus {
        ERROR,
        READY
    }
}
