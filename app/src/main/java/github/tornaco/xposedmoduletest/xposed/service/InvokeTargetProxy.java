package github.tornaco.xposedmoduletest.xposed.service;

import de.robv.android.xposed.XposedHelpers;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by guohao4 on 2018/1/23.
 * Email: Tornaco@163.com
 */
@AllArgsConstructor
@Getter
public class InvokeTargetProxy<T> {

    public @interface Target {
        String value();
    }

    private T host;

    @SuppressWarnings({"ConstantConditions", "unchecked", "SameParameterValue", "UnusedReturnValue"})
    protected <X> X invokeMethod(String methodName, Object... args) {
        if (host == null) {
            XposedLog.wtf("invokeMethod while host is null- " + getClass());
            return null;
        }
        try {
            Object res = XposedHelpers.callMethod(host, methodName, args);
            return (X) res;
        } catch (Throwable e) {
            XposedLog.wtf("invokeMethod fail: " + "method: " + methodName + " class" + getClass() + e);
            return null;
        }
    }
}
