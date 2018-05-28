package github.tornaco.xposedmoduletest.xposed.service.am;

import android.content.pm.ApplicationInfo;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XposedHelpers;
import github.tornaco.xposedmoduletest.util.OSUtil;
import github.tornaco.xposedmoduletest.xposed.service.InvokeTargetProxy;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;
import lombok.Getter;

/**
 * Created by guohao4 on 2018/1/23.
 * Email: Tornaco@163.com
 */
@Getter
public class AMSProxy extends InvokeTargetProxy<Object> {

    private Object mStackSupervisor;

    public AMSProxy(Object host) {
        super(host);
    }

    // L: As N.
    // M: As N.
    // N: final addAppLocked(ApplicationInfo applicationInfo, boolean isolated, String abiOverride) {}
    // O: final ProcessRecord addAppLocked(ApplicationInfo info, String customProcess, boolean isolated,String abiOverride) {}
    public Object addAppLocked(ApplicationInfo applicationInfo, boolean isolated, String abiOverride) {
        if (applicationInfo == null) return null;
        XposedLog.verbose("addAppLocked: " + applicationInfo.packageName);
        if (OSUtil.isOOrAbove()) {
            return invokeMethod("addAppLocked", applicationInfo, null, isolated, abiOverride);
        }
        return invokeMethod("addAppLocked", applicationInfo, isolated, abiOverride);
    }

    public void dumpTopActivity() {
        List<Object> activities = getDumpActivitiesLocked("top");
        if (activities.size() > 0) {
            Object activityRecord = activities.get(0);
            IBinder appToken = (IBinder) XposedHelpers.getObjectField(activityRecord, "appToken");
            XposedLog.verbose("AMSProxy appToken: " + appToken);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Object> getDumpActivitiesLocked(String name) {
        try {
            synchronized (this) {
                if (mStackSupervisor == null) {
                    mStackSupervisor = XposedHelpers
                            .getObjectField(getHost(), "mStackSupervisor");
                    XposedLog.verbose("AMSProxy getDumpActivitiesLocked mStackSupervisor= " + mStackSupervisor);
                }
            }
            return (List<Object>) XposedHelpers.callMethod(mStackSupervisor, "getDumpActivitiesLocked", name);
        } catch (Throwable e) {
            XposedLog.wtf("AMSProxy Fail getDumpActivitiesLocked: " + Log.getStackTraceString(e));
            return new ArrayList<>(0);
        }
    }
}
