package github.tornaco.xposedmoduletest.xposed.service.policy;

import android.view.WindowManagerPolicy;

import github.tornaco.xposedmoduletest.xposed.service.InvokeTargetProxy;

/**
 * Created by guohao4 on 2018/1/23.
 * Email: Tornaco@163.com
 */
@InvokeTargetProxy.Target("PhoneWindowManager")
public class PhoneWindowManagerProxy extends InvokeTargetProxy<Object> {

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
}
