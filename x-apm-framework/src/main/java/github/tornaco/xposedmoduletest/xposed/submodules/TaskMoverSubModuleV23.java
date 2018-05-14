package github.tornaco.xposedmoduletest.xposed.submodules;

import android.annotation.SuppressLint;
import android.os.Bundle;

import java.lang.reflect.Method;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

class TaskMoverSubModuleV23 extends TaskMoverSubModule {
    @SuppressLint("PrivateApi")
    @Override
    Method methodForTaskMover(XC_LoadPackage.LoadPackageParam lpparam) throws ClassNotFoundException, NoSuchMethodException {
        @SuppressLint("PrivateApi") Class taskRecordClass = Class.forName("com.android.server.am.TaskRecord", false, lpparam.classLoader);
        return Class.forName("com.android.server.am.ActivityStackSupervisor",
                false, lpparam.classLoader)
                .getDeclaredMethod("findTaskToMoveToFrontLocked",
                        taskRecordClass, int.class, Bundle.class, String.class);
    }
}
