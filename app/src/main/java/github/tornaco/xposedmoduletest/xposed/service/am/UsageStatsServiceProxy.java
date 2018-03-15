package github.tornaco.xposedmoduletest.xposed.service.am;

import github.tornaco.xposedmoduletest.xposed.service.InvokeTargetProxy;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by Tornaco on 2018/3/15 9:49.
 */

@InvokeTargetProxy.Target("UsageStatsService")
public class UsageStatsServiceProxy extends InvokeTargetProxy<Object> {

    public UsageStatsServiceProxy(Object host) {
        super(host);
        XposedLog.boot("UsageStatsServiceProxy init, host: " + host);
    }

    public void setAppIdle(String pkg, boolean idle, int userId) {
        invokeMethod("setAppIdle", pkg, idle, userId);
    }
}
