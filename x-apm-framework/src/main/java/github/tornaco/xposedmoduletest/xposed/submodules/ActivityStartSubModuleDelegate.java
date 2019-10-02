package github.tornaco.xposedmoduletest.xposed.submodules;

import android.os.Build;

import lombok.experimental.Delegate;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

class ActivityStartSubModuleDelegate extends AndroidSubModule {

    ActivityStartSubModuleDelegate() {
        int sdkVersion = Build.VERSION.SDK_INT;
        switch (sdkVersion) {
            case 21:
                activityStartSubModuleImpl = new ActivityStartSubModuleV21();
                break;
            case 22:
                activityStartSubModuleImpl = new ActivityStartSubModuleV22();
                break;
            case 23:
                activityStartSubModuleImpl = new ActivityStartSubModuleV23();
                break;
            case 24:
                activityStartSubModuleImpl = new ActivityStartSubModuleV24();
                break;
            case 25:
                activityStartSubModuleImpl = new ActivityStartSubModuleV25();
                break;
            case 26:
                activityStartSubModuleImpl = new ActivityStartSubModuleV26();
                break;
            case 27:
                activityStartSubModuleImpl = new ActivityStartSubModuleV27();
                break;
            case 28:
            case 29:
                activityStartSubModuleImpl = new ActivityStartSubModuleV28();
                break;
            default:
                activityStartSubModuleImpl = new ActivityStartSubModuleEmpty();
                break;
        }
    }

    @Delegate
    private ActivityStartSubModule activityStartSubModuleImpl;

}
