package github.tornaco.xposedmoduletest.xposed.submodules;

import java.util.HashSet;
import java.util.Set;

import github.tornaco.xposedmoduletest.BuildConfig;
import lombok.Synchronized;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

public class AppGuardSubModuleManager {

    private static AppGuardSubModuleManager sMe;

    private final Set<SubModule> SUBS = new HashSet<>();

    private AppGuardSubModuleManager() {
        SUBS.add(new ResourceSubModule());
        SUBS.add(new FPSubModule());
        SUBS.add(new ScreenshotApplicationsSubModule());
        SUBS.add(new PackageInstallerSubModule());
        SUBS.add(new PMSSubModule());
        SUBS.add(new TaskMoverSubModuleDelegate());
        SUBS.add(new ActivityStartSubModuleDelegate());
        SUBS.add(new AMSSubModule7());
        SUBS.add(new AMSSubModule5());
        SUBS.add(new AMSSubModule4());
        SUBS.add(new AMSSubModule3());
        SUBS.add(new AMSSubModule2());
        SUBS.add(new AMSSubModule());
        if (BuildConfig.DEBUG) {
            SUBS.add(new IconModule());
        }
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
