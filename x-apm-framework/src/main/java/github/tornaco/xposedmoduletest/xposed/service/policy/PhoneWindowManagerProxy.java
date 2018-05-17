package github.tornaco.xposedmoduletest.xposed.service.policy;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManagerPolicy;

import de.robv.android.xposed.XposedHelpers;
import github.tornaco.xposedmoduletest.ISettingsChangeListener;
import github.tornaco.xposedmoduletest.xposed.repo.SettingsProvider;
import github.tornaco.xposedmoduletest.xposed.service.InvokeTargetProxy;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by guohao4 on 2018/1/23.
 * Email: Tornaco@163.com
 */
@InvokeTargetProxy.Target("PhoneWindowManager")
public class PhoneWindowManagerProxy extends InvokeTargetProxy<Object> {

    @Setter
    @Getter
    private boolean haveEnableGesture;

    @Getter
    @Setter
    private Context context;

    @Getter
    @Setter
    private WindowManagerPolicy.WindowManagerFuncs windowManagerFuncs;

    private OPGesturesListener mOPGestures;

    public PhoneWindowManagerProxy(Object host) {
        super(host);
    }

    public void enableKeyguard(boolean enabled) {
        invokeMethod("enableKeyguard", enabled);
    }

    public void exitKeyguardSecurely(WindowManagerPolicy.OnKeyguardExitResult result) {
        invokeMethod("exitKeyguardSecurely", result);
    }

    public void dismissKeyguardLw() {
        invokeMethod("dismissKeyguardLw");
    }

    public boolean isKeyguardLocked() {
        return invokeMethod("isKeyguardLocked");
    }

    private void retrieveWindowManagerFuncs() {
        synchronized (this) {
            if (getWindowManagerFuncs() == null) {
                try {
                    setWindowManagerFuncs((WindowManagerPolicy.WindowManagerFuncs)
                            XposedHelpers.getObjectField(getHost(), "mWindowManagerFuncs"));
                    XposedLog.verbose("PhoneWindowManagerProxy retrieveWindowManagerFuncs: " + getWindowManagerFuncs());
                } catch (Throwable e) {
                    XposedLog.wtf("PhoneWindowManagerProxy Fail retrieveWindowManagerFuncs: "
                            + Log.getStackTraceString(e));
                }
            }
        }
    }

    public void init(Context context) {
        mOPGestures = new OPGesturesListener(context, () -> {
            XposedLog.verbose("PhoneWindowManagerProxy onSwipeThreeFinger");
            takeScreenshot(0);
        });

        // Register listener.
        SettingsProvider.get().registerSettingsChangeListener(new ISettingsChangeListener.Stub() {
            @Override
            public void onChange(String name) {
                XposedLog.verbose("PhoneWindowManagerProxy onChange: " + name);
            }
        });
    }

    public void enableSwipeThreeFingerGesture(boolean enable) {
        if (getContext() == null) {
            XposedLog.wtf("PhoneWindowManagerProxy enableSwipeThreeFingerGesture called while getContext() is null");
            return;
        }

        if (mOPGestures == null) {
            init(context);
        }

        retrieveWindowManagerFuncs();
        if (getWindowManagerFuncs() != null) {
            if (enable) {
                if (haveEnableGesture) return;
                haveEnableGesture = true;
                getWindowManagerFuncs().registerPointerEventListener(mOPGestures);
            } else {
                if (!haveEnableGesture) return;
                haveEnableGesture = false;
                getWindowManagerFuncs().unregisterPointerEventListener(mOPGestures);
            }
        } else {
            XposedLog.wtf("PhoneWindowManagerProxy enableSwipeThreeFingerGesture called while getWindowManagerFuncs() is null");
        }
    }

    private void takeScreenshot(long delay) {
        try {
            Handler mHandler = (Handler) XposedHelpers.getObjectField(getHost(), "mHandler");
            XposedLog.verbose("PhoneWindowManagerProxy takeScreenshot, handler: " + mHandler);
            if (mHandler != null) {
                Runnable mScreenshotRunnable = (Runnable) XposedHelpers.getObjectField(getHost(), "mScreenshotRunnable");
                XposedLog.verbose("PhoneWindowManagerProxy takeScreenshot, mScreenshotRunnable: " + mScreenshotRunnable);
                if (mScreenshotRunnable != null) {
                    mHandler.postDelayed(mScreenshotRunnable, delay);
                }
            }
        } catch (Throwable e) {
            XposedLog.wtf("PhoneWindowManagerProxy fail takeScreenshot: " + Log.getStackTraceString(e));
        }
    }
}
