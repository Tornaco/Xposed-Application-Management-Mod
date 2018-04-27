package github.tornaco.xposedmoduletest.xposed.service.am;

import android.os.Handler;

import de.robv.android.xposed.XposedHelpers;
import github.tornaco.xposedmoduletest.xposed.service.InvokeTargetProxy;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by Tornaco on 2018/3/15 9:49.
 */

@InvokeTargetProxy.Target("UsageStatsService")
public class UsageStatsServiceProxy extends InvokeTargetProxy<Object> {

    private Handler ussHandler;

    public UsageStatsServiceProxy(Object host) {
        super(host);
    }

    public Handler getUssHandler() {
        ensureHandler();
        return ussHandler;
    }

    private void ensureHandler() {
        if (this.ussHandler == null) try {
            this.ussHandler = (Handler) XposedHelpers.getObjectField(getHost(), "mHandler"); // Main looper!!! Same as USS.
            XposedLog.verbose("UsageStatsServiceProxy ensureHandler, host: " + getHost() + ", handler: " + ussHandler);
        } catch (Throwable e) {
            XposedLog.wtf("UsageStatsServiceProxy ensureHandler fail get main handler.");
        }
    }

    public void setAppIdle(String packageName, boolean idle, int userId) {
        invokeMethod("setAppIdle", packageName, idle, userId);
    }
}
