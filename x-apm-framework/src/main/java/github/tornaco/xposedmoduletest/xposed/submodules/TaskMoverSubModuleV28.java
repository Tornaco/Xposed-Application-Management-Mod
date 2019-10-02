package github.tornaco.xposedmoduletest.xposed.submodules;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;

import java.lang.reflect.Method;

import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.util.OSUtil;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

//void findTaskToMoveToFront(TaskRecord task, int flags, ActivityOptions options, String reason,
//        boolean forceNonResizeable) {
class TaskMoverSubModuleV28 extends TaskMoverSubModuleV27 {
    @SuppressLint("PrivateApi")
    @Override
    Method methodForTaskMover(XC_LoadPackage.LoadPackageParam lpparam) throws ClassNotFoundException, NoSuchMethodException {
        String clazzName = OSUtil.isQOrAbove() ? "com.android.server.wm.TaskRecord" : "com.android.server.am.TaskRecord";
        @SuppressLint("PrivateApi")
        Class taskRecordClass = Class.forName(clazzName, false, lpparam.classLoader);

        String superVisorClazzName = OSUtil.isQOrAbove()
                ? "com.android.server.wm.ActivityStackSupervisor"
                : "com.android.server.am.ActivityStackSupervisor";

        @SuppressLint("PrivateApi") final Method moveToFront
                = Class.forName(superVisorClazzName,
                false, lpparam.classLoader)
                .getDeclaredMethod("findTaskToMoveToFront",
                        taskRecordClass, int.class, ActivityOptions.class,
                        String.class, boolean.class);
        return moveToFront;
    }
}
