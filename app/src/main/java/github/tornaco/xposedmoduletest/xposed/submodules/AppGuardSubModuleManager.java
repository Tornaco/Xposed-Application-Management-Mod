package github.tornaco.xposedmoduletest.xposed.submodules;

import java.util.HashSet;
import java.util.Set;

import github.tornaco.xposedmoduletest.xposed.XAppBuildVar;
import lombok.Synchronized;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

public class AppGuardSubModuleManager {

    private static AppGuardSubModuleManager sMe;

    private final Set<SubModule> SUBS = new HashSet<>();


    private void addToSubsChecked(SubModule subModule) {
        String var = subModule.needBuildVar();
        if (var == null || XAppBuildVar.BUILD_VARS.contains(var)) {
            SUBS.add(subModule);
        }
    }

    private AppGuardSubModuleManager() {
        addToSubsChecked(new FPSubModule());
        addToSubsChecked(new ScreenshotApplicationsSubModule());
        addToSubsChecked(new PackageInstallerSubModule());
        addToSubsChecked(new PMSSubModule());
        addToSubsChecked(new TaskMoverSubModuleDelegate());
        addToSubsChecked(new ActivityStartSubModuleDelegate());
        addToSubsChecked(new AMSSubModule5());
        addToSubsChecked(new AMSSubModule4());
        addToSubsChecked(new AMSSubModule3());
        addToSubsChecked(new AMSSubModule2());
        addToSubsChecked(new AMSSubModule());
    }

    @Synchronized
    public
    static AppGuardSubModuleManager getInstance() {
        if (sMe == null) sMe = new AppGuardSubModuleManager();
        return sMe;
    }

    public Set<SubModule> getAllSubModules() {
        return SUBS;
    }
}
