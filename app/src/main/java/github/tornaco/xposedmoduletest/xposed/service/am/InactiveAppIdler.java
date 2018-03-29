package github.tornaco.xposedmoduletest.xposed.service.am;

import android.os.Build;
import android.os.UserHandle;

import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by Tornaco on 2018/3/14 10:08.
 * God bless no bug!
 */
// Only for 22+
public class InactiveAppIdler implements AppIdler {

    private UsageStatsServiceProxy proxy;
    private OnAppIdleListener listener;

    public InactiveAppIdler(UsageStatsServiceProxy proxy) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            this.proxy = proxy;
            XposedLog.verbose("InactiveAppIdler init, proxy: " + proxy);
        }
    }

    @Override
    public void setAppIdle(String pkg) {
        if (proxy != null) {
            proxy.setAppIdle(pkg, true, UserHandle.USER_CURRENT);
            listener.onAppIdle(pkg);
            XposedLog.verbose("InactiveAppIdler, setAppIdle call end: " + pkg);
        } else {
            XposedLog.wtf("InactiveAppIdler, setAppIdle but proxy is null");
        }
    }

    @Override
    public void setListener(OnAppIdleListener listener) {
        this.listener = listener;
    }
}
