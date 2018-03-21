package github.tornaco.xposedmoduletest.xposed.service.am;

import android.content.pm.ApplicationInfo;

import github.tornaco.xposedmoduletest.xposed.service.InvokeTargetProxy;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;
import lombok.Getter;

/**
 * Created by guohao4 on 2018/1/23.
 * Email: Tornaco@163.com
 */
@Getter
public class AMSProxy extends InvokeTargetProxy<Object> {

    public AMSProxy(Object host) {
        super(host);
    }

    public Object addAppLocked(ApplicationInfo applicationInfo, boolean isolated, String abiOverride) {
        if (applicationInfo == null) return null;
        XposedLog.verbose("addAppLocked: " + applicationInfo.packageName);
        return invokeMethod("addAppLocked", applicationInfo, isolated, abiOverride);
    }
}
