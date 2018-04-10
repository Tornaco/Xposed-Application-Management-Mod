package github.tornaco.xposedmoduletest.xposed.service.am;

import android.app.usage.IUsageStatsManager;
import android.content.Context;
import android.os.Binder;
import android.os.Build;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.support.annotation.RequiresApi;
import android.util.Log;

import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by Tornaco on 2018/3/14 10:08.
 * God bless no bug!
 */
// Only for 22+
public class InactiveAppIdler implements AppIdler {

    private UsageStatsServiceProxy proxy;
    private OnAppIdleListener listener;
    private IUsageStatsManager usm;

    public InactiveAppIdler(UsageStatsServiceProxy proxy) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            this.proxy = proxy;
            XposedLog.verbose("InactiveAppIdler init, proxy: " + proxy);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    @Override
    public void setAppIdle(String pkg) {
        XposedLog.verbose("setAppInactive, calling in: " + Binder.getCallingUid());
        int userId = UserHandle.USER_CURRENT;
        synchronized (this) {
            if (usm == null) {
                usm = IUsageStatsManager.Stub.asInterface(ServiceManager
                        .getService(Context.USAGE_STATS_SERVICE));
            }
            if (usm != null) {
                try {
                    usm.setAppInactive(pkg, true, userId);
                    listener.onAppIdle(pkg);
                } catch (Exception e) {
                    XposedLog.wtf("InactiveAppIdler, fail setAppInactive: " + Log.getStackTraceString(e));
                }
            }
        }

//        if (proxy != null) {
//            proxy.setAppIdle(pkg, true, UserHandle.USER_CURRENT);
//            listener.onAppIdle(pkg);
//            XposedLog.verbose("InactiveAppIdler, setAppIdle call end: " + pkg);
//        } else {
//            XposedLog.wtf("InactiveAppIdler, setAppIdle but proxy is null");
//        }
    }

    @Override
    public void setListener(OnAppIdleListener listener) {
        this.listener = listener;
    }
}
