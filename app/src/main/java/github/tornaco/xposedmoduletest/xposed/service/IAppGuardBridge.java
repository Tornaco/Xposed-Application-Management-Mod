package github.tornaco.xposedmoduletest.xposed.service;

import android.content.ComponentName;
import android.os.Bundle;

/**
 * Created by guohao4 on 2017/11/1.
 * Email: Tornaco@163.com
 */

public interface IAppGuardBridge extends IModuleBridge {

    // API for AppGuard.
    boolean interruptPackageRemoval(String pkg);

    boolean onEarlyVerifyConfirm(String pkg);

    void verify(Bundle options, String pkg, int uid, int pid, VerifyListener listener);

    boolean isBlurForPkg(String pkg);

    int getBlurRadius();
    float getBlurScale();

    boolean interruptFPSuccessVibrate();

    boolean interruptFPErrorVibrate();

    boolean isActivityStartShouldBeInterrupted(ComponentName componentName);
}
