package github.tornaco.xposedmoduletest.x.service;

import android.content.Context;
import android.os.Bundle;

/**
 * Created by guohao4 on 2017/11/1.
 * Email: Tornaco@163.com
 */

public interface IModuleBridge {
    void attachContext(Context context);

    void publish();

    void systemReady();

    void publishFeature(String f);

    void shutdown();

    boolean interruptPackageRemoval(String pkg);

    boolean onEarlyVerifyConfirm(String pkg);

    void verify(Bundle options, String pkg, int uid, int pid, VerifyListener listener);

    void onUserLeaving(String reason);

    boolean isBlurForPkg(String pkg);
}
