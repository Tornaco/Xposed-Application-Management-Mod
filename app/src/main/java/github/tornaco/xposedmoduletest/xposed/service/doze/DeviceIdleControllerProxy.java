package github.tornaco.xposedmoduletest.xposed.service.doze;

import android.util.Log;

import de.robv.android.xposed.XposedHelpers;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;
import lombok.AllArgsConstructor;

/**
 * Created by guohao4 on 2018/1/2.
 * Email: Tornaco@163.com
 */
@AllArgsConstructor
public class DeviceIdleControllerProxy {

    private Object deviceIdleController;

    public void stepIdleStateLocked() {
        try {
            XposedHelpers.callMethod(deviceIdleController, "stepIdleStateLocked", "s:shell");
        } catch (Throwable e) {
            XposedLog.wtf("deviceIdleController call fail: " + Log.getStackTraceString(e));
        }
    }
}
