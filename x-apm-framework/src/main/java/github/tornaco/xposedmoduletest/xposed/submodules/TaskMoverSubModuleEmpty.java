package github.tornaco.xposedmoduletest.xposed.submodules;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

class TaskMoverSubModuleEmpty extends AndroidSubModule {
    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        // Nothing.
        setStatus(SubModuleStatus.ERROR);
        setErrorMessage("Empty impl");
    }
}
