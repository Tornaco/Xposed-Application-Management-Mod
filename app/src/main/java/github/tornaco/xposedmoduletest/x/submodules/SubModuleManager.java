package github.tornaco.xposedmoduletest.x.submodules;

import java.util.HashSet;
import java.util.Set;

import lombok.Synchronized;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

public class SubModuleManager {

    private static SubModuleManager sMe;

    private final Set<SubModule> SUBS = new HashSet<>();

    private SubModuleManager() {
        SUBS.add(new ActivityModule());
        SUBS.add(new FPSubModule());
        SUBS.add(new ScreenshotApplicationsSubModule());
        SUBS.add(new PWMSubModule());
        SUBS.add(new PackageInstallerSubModule());
        SUBS.add(new PMSSubModule());
        SUBS.add(new TaskMoverSubModuleDelegate());
        SUBS.add(new ActivityStartSubModuleDelegate());
        SUBS.add(new AMSSubModule3());
        SUBS.add(new AMSSubModule2());
        SUBS.add(new AMSSubModule());

    }

    @Synchronized
    public
    static SubModuleManager getInstance() {
        if (sMe == null) sMe = new SubModuleManager();
        return sMe;
    }

    public Set<SubModule> getAllSubModules() {
        return SUBS;
    }
}
