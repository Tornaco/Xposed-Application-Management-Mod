package github.tornaco.xposedmoduletest.xposed.service.dpm;

import android.content.Context;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.view.IWindowManager;

import com.android.internal.widget.LockPatternUtils;

import github.tornaco.xposedmoduletest.xposed.service.InvokeTargetProxy;

/**
 * Created by guohao4 on 2018/1/30.
 * Email: Tornaco@163.com
 */
@Deprecated
public class DevicePolicyManagerServiceProxy extends InvokeTargetProxy<Object> {

    public DevicePolicyManagerServiceProxy(Object host) {
        super(host);
    }

    public void lockNowUnchecked() {
        invokeMethod("lockNowUnchecked");
    }

    public IWindowManager getWindowManager() {
        return IWindowManager.Stub.asInterface(ServiceManager.getService(Context.WINDOW_SERVICE));
    }

    public void lockNow(Context context) throws RemoteException {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (pm != null) {
            pm.goToSleep(SystemClock.uptimeMillis(), PowerManager.GO_TO_SLEEP_REASON_DEVICE_ADMIN,
                    0);
        }
        new LockPatternUtils(context)
                .requireStrongAuth(LockPatternUtils.
                                StrongAuthTracker.STRONG_AUTH_REQUIRED_AFTER_DPM_LOCK_NOW,
                        UserHandle.USER_ALL);
        getWindowManager().lockNow(null);

    }
}
