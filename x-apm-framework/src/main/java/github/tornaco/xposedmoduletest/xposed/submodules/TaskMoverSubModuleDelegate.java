package github.tornaco.xposedmoduletest.xposed.submodules;

import android.os.Build;

import github.tornaco.xposedmoduletest.xposed.util.XposedLog;
import lombok.experimental.Delegate;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

class TaskMoverSubModuleDelegate extends AndroidSubModule {
    @Delegate
    private AndroidSubModule taskMoverSubModule;

    TaskMoverSubModuleDelegate() {
        int sdkVersion = Build.VERSION.SDK_INT;
        XposedLog.boot("TaskMoverSubModuleDelegate SDK VERSION =" + sdkVersion);
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
            case 27:
                taskMoverSubModule = new TaskMoverSubModuleV27();
                break;
            case 28: // Fall
            case 29:
                taskMoverSubModule = new TaskMoverSubModuleV28();
                break;
            default:
                taskMoverSubModule = new TaskMoverSubModuleEmpty();
                break;

        }
    }
}
