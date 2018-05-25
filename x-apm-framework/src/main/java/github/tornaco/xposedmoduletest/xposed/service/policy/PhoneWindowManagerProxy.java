package github.tornaco.xposedmoduletest.xposed.service.policy;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManagerPolicy;

import de.robv.android.xposed.XposedHelpers;
import github.tornaco.xposedmoduletest.ISettingsChangeListener;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.repo.SettingsProvider;
import github.tornaco.xposedmoduletest.xposed.service.InvokeTargetProxy;
import github.tornaco.xposedmoduletest.xposed.service.policy.wm.SystemGesturesPointerEventListener;
import github.tornaco.xposedmoduletest.xposed.service.policy.wm.SystemGesturesPointerEventListenerCallbackImpl;
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
    private boolean haveEnableThreeFingerGesture, haveEnablePGesture, settingsListenerRegistered;

    @Getter
    @Setter
    private Context context;

    @Getter
    @Setter
    private WindowManagerPolicy.WindowManagerFuncs windowManagerFuncs;

    private OPGesturesListener mOPGestures;
    private SystemGesturesPointerEventListener mSystemGesturesListener;

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

    private void initThreeFingerGesture(Context context) {
        if (context != null) {
            mOPGestures = new OPGesturesListener(context, () -> {
                XposedLog.verbose("PhoneWindowManagerProxy onSwipeThreeFinger");
                takeScreenshot(0);
            });
        }
        registerSettingsListener();
    }

    private void initPGesture(Context context) {
        if (context != null) {
            mSystemGesturesListener = new SystemGesturesPointerEventListener(context,
                    new SystemGesturesPointerEventListenerCallbackImpl(context));
        }
        registerSettingsListener();
    }

    private void registerSettingsListener() {
        if (settingsListenerRegistered) {
            return;
        }
        // Register listener.
        settingsListenerRegistered = SettingsProvider.get().registerSettingsChangeListener(new ISettingsChangeListener.Stub() {
            @Override
            public void onChange(String name) {
                XposedLog.verbose("PhoneWindowManagerProxy onChange: " + name);
                if (XAPMManager.OPT.THREE_FINGER_GESTURE.name().equals(name)) {
                    boolean enable = SettingsProvider.get().getBoolean(name, false);
                    enableSwipeThreeFingerGesture(enable);
                } else if (XAPMManager.OPT.P_GESTURE.name().equals(name)) {
                    boolean enable = SettingsProvider.get().getBoolean(name, false);
                    enablePGesture(enable);
                }
            }
        });
    }

    public void enablePGesture(boolean enable) {
        if (getContext() == null) {
            XposedLog.wtf("PhoneWindowManagerProxy enablePGesture called while getContext() is null");
            return;
        }

        if (mSystemGesturesListener == null) {
            initPGesture(context);
        }

        retrieveWindowManagerFuncs();
        if (getWindowManagerFuncs() != null) {
            if (enable) {
                if (haveEnablePGesture) return;
                haveEnablePGesture = true;
                getWindowManagerFuncs().registerPointerEventListener(mSystemGesturesListener);
            } else {
                if (!haveEnablePGesture) return;
                haveEnablePGesture = false;
                getWindowManagerFuncs().unregisterPointerEventListener(mSystemGesturesListener);
            }
            XposedLog.verbose("PhoneWindowManagerProxy enablePGesture ok: " + enable);
        } else {
            XposedLog.wtf("PhoneWindowManagerProxy enablePGesture called while getWindowManagerFuncs() is null");
        }
    }

    public void enableSwipeThreeFingerGesture(boolean enable) {
        if (getContext() == null) {
            XposedLog.wtf("PhoneWindowManagerProxy enableSwipeThreeFingerGesture called while getContext() is null");
            return;
        }

        if (mOPGestures == null) {
            initThreeFingerGesture(context);
        }

        retrieveWindowManagerFuncs();
        if (getWindowManagerFuncs() != null) {
            if (enable) {
                if (haveEnableThreeFingerGesture) return;
                haveEnableThreeFingerGesture = true;
                getWindowManagerFuncs().registerPointerEventListener(mOPGestures);
            } else {
                if (!haveEnableThreeFingerGesture) return;
                haveEnableThreeFingerGesture = false;
                getWindowManagerFuncs().unregisterPointerEventListener(mOPGestures);
            }
            XposedLog.verbose("PhoneWindowManagerProxy enableSwipeThreeFingerGesture ok: " + enable);
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
