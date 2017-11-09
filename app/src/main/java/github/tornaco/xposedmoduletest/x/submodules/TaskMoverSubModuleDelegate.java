package github.tornaco.xposedmoduletest.x.submodules;

import android.os.Build;

import lombok.experimental.Delegate;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

class TaskMoverSubModuleDelegate extends AppGuardAndroidSubModule {
    @Delegate
    private AppGuardAndroidSubModule taskMoverSubModule;

    TaskMoverSubModuleDelegate() {
        int sdkVersion = Build.VERSION.SDK_INT;
        switch (sdkVersion) {
            case 21:
                taskMoverSubModule = new TaskMoverSubModuleV21();
                break;
            case 22:
                taskMoverSubModule = new TaskMoverSubModuleV22();
                break;
            case 23:
                taskMoverSubModule = new TaskMoverSubModuleV23();
                break;
            case 24:
                taskMoverSubModule = new TaskMoverSubModuleV24();
                break;
            case 25:
                taskMoverSubModule = new TaskMoverSubModuleV25();
                break;
            case 26:
                taskMoverSubModule = new TaskMoverSubModuleV26();
                break;
            default:
                taskMoverSubModule = new TaskMoverSubModuleEmpty();
                break;

        }
    }
}
