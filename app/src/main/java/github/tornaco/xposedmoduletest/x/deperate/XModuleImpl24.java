package github.tornaco.xposedmoduletest.x.deperate;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;

import java.lang.reflect.Method;

import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by guohao4 on 2017/10/13.
 * Email: Tornaco@163.com
 */
// http://androidxref.com/7.1.1_r6/xref/frameworks/base/services/core/java/com/android/server/am/ActivityStackSupervisor.java
//  void findTaskToMoveToFrontLocked(TaskRecord task, int flags, ActivityOptions options, String reason, boolean forceNonResizeable)
class XModuleImpl24 extends XModuleImpl {
    @Override
    Method methodForTaskMover(XC_LoadPackage.LoadPackageParam lpparam) throws ClassNotFoundException, NoSuchMethodException {
        @SuppressLint("PrivateApi")
        Class taskRecordClass = Class.forName("com.android.server.am.TaskRecord", false, lpparam.classLoader);
        @SuppressLint("PrivateApi") final Method moveToFront
                = Class.forName("com.android.server.am.ActivityStackSupervisor",
                false, lpparam.classLoader)
                .getDeclaredMethod("findTaskToMoveToFrontLocked",
                        taskRecordClass, int.class, ActivityOptions.class,
                        String.class, boolean.class);
        return moveToFront;
    }

    @SuppressLint("PrivateApi")
    @Override
    Class clzForStartActivityMayWait(XC_LoadPackage.LoadPackageParam lpparam) throws ClassNotFoundException {
        return XposedHelpers.findClass("com.android.server.am.ActivityStarter", lpparam.classLoader);
    }
}
