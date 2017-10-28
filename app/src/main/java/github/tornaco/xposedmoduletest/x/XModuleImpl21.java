package github.tornaco.xposedmoduletest.x;

import android.annotation.SuppressLint;
import android.os.Bundle;

import java.lang.reflect.Method;

import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by guohao4 on 2017/10/28.
 * Email: Tornaco@163.com
 */

// http://androidxref.com/5.0.0_r2/xref/frameworks/base/services/core/java/com/android/server/am/ActivityStackSupervisor.java
// void findTaskToMoveToFrontLocked(TaskRecord task, int flags, Bundle options)
public class XModuleImpl21 extends XModuleImpl {
    @SuppressLint("PrivateApi")
    @Override
    Method methodForTaskMover(XC_LoadPackage.LoadPackageParam lpparam) throws ClassNotFoundException, NoSuchMethodException {
        @SuppressLint("PrivateApi") Class taskRecordClass = Class.forName("com.android.server.am.TaskRecord", false, lpparam.classLoader);
        return Class.forName("com.android.server.am.ActivityStackSupervisor",
                false, lpparam.classLoader)
                .getDeclaredMethod("findTaskToMoveToFrontLocked",
                        taskRecordClass, int.class, Bundle.class);
    }

    @Override
    Class clzForStartActivityMayWait(XC_LoadPackage.LoadPackageParam lpparam) throws ClassNotFoundException {
        return XposedHelpers.findClass("com.android.server.am.ActivityStackSupervisor",
                lpparam.classLoader);
    }
}


