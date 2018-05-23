package github.tornaco.xposedmoduletest.xposed.service.am;

import android.app.usage.IUsageStatsManager;
import android.content.Context;
import android.os.Binder;
import android.os.Build;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.support.annotation.RequiresApi;
import android.util.Log;

import github.tornaco.xposedmoduletest.xposed.service.ErrorCatchRunnable;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;
import lombok.Getter;

/**
 * Created by Tornaco on 2018/3/14 10:08.
 * God bless no bug!
 */
// Only for 22+

//    04-27 14:19:16.490  6452 16085 I Xposed  : X-APM-S-WARN-InactiveAppIdler, fail setAppInactive: java.lang.SecurityException: No permission to change app idle state: uid 1000 does not have android.permission.CHANGE_APP_IDLE_STATE.
//            04-27 14:19:16.490  6452 16085 I Xposed  :      at android.app.ContextImpl.enforce(ContextImpl.java:1762)
//            04-27 14:19:16.490  6452 16085 I Xposed  :      at android.app.ContextImpl.enforceCallingPermission(ContextImpl.java:1784)
//            04-27 14:19:16.490  6452 16085 I Xposed  :      at com.android.server.usage.UsageStatsService$BinderService.setAppInactive(UsageStatsService.java:1483)
//            04-27 14:19:16.490  6452 16085 I Xposed  :      at github.tornaco.xposedmoduletest.xposed.service.am.InactiveAppIdler.setAppIdle(InactiveAppIdler.java:44)
//            04-27 14:19:16.490  6452 16085 I Xposed  :      at github.tornaco.xposedmoduletest.xposed.service.XAshmanServiceImpl$HandlerImpl$4.call(XAshmanServiceImpl.java:7464)
//            04-27 14:19:16.490  6452 16085 I Xposed  :      at github.tornaco.xposedmoduletest.xposed.service.XAshmanServiceImpl$HandlerImpl$4.call(XAshmanServiceImpl.java:7349)
//            04-27 14:19:16.490  6452 16085 I Xposed  :      at java.util.concurrent.FutureTask.run(FutureTask.java:266)
//            04-27 14:19:16.490  6452 16085 I Xposed  :      at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1162)
//            04-27 14:19:16.490  6452 16085 I Xposed  :      at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:636)
//            04-27 14:19:16.490  6452 16085 I Xposed  :      at java.lang.Thread.run(Thread.java:764)


public class InactiveAppIdler implements AppIdler {

    private OnAppIdleListener listener;
    private IUsageStatsManager usm;

    @Getter
    private final UsageStatsServiceProxy proxy;

    public InactiveAppIdler(UsageStatsServiceProxy proxy) {
        this.proxy = proxy;
        XposedLog.boot("InactiveAppIdler, bring up with proxy: " + proxy);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    @Override
    public void setAppIdle(String pkg) {
        if (proxy != null && proxy.getUssHandler() != null) {
            // Delegating to USS.
            ErrorCatchRunnable er = new ErrorCatchRunnable(() -> setAppIdleInternal(pkg), "setAppIdleInternal");
            proxy.getUssHandler().post(er);
        } else {
            XposedLog.wtf("InactiveAppIdler Fail setAppInactive, proxy or handler is null");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    private void setAppIdleInternal(String pkg) {
        int userId = UserHandle.getUserId(Binder.getCallingUid());
        if (ensureUSM()) {
            try {
                usm.setAppInactive(pkg, true, userId);
                listener.onAppIdle(pkg);
            } catch (Throwable e) {
                XposedLog.wtf("InactiveAppIdler, fail setAppInactive: " +
                        Log.getStackTraceString(e));
            }
        } else {
            XposedLog.wtf("InactiveAppIdler, fail setAppInactive: " +
                    Log.getStackTraceString(new NullPointerException("USM is null")));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    private boolean ensureUSM() {
        if (usm == null) {
            usm = IUsageStatsManager.Stub.asInterface(ServiceManager
                    .getService(Context.USAGE_STATS_SERVICE));
        }
        return usm != null;
    }

    @Override
    public void setListener(OnAppIdleListener listener) {
        this.listener = listener;
    }
}
