package github.tornaco.xposedmoduletest.xposed;

import android.os.Build;
import android.support.annotation.Keep;

import de.robv.android.xposed.XposedBridge;
import github.tornaco.xposedmoduletest.BuildConfig;
import lombok.experimental.Delegate;

/**
 * Created by guohao4 on 2017/10/19.
 * Email: Tornaco@163.com
 */
@Keep
public class XModuleDelegate extends XModuleAbs {
    @Delegate
    private XModuleAbs mImpl;

    public XModuleDelegate() {
        try {
            dump();
        } catch (Exception e) {
            XposedBridge.log(e);
        }
        mImpl = new XModuleImplSeparable();
        XposedBridge.log(String.format("Init XModuleDelegate with impl %s: ", mImpl));
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    private static void dump() throws Exception {
        StringBuilder log = new StringBuilder("====== X-APM MODULE DELEGATE DUMP START ======").append("\n");
        log.append("APM VERSION CODE: ").append(BuildConfig.VERSION_NAME).append("\n");
        log.append("APM BUILD DATE: ").append(XAppBuildHostInfo.BUILD_DATE).append("\n");
        log.append("APM BUILD BY: ").append(XAppBuildHostInfo.BUILD_HOST_NAME).append("\n");
        log.append("DEVICE SDK: ").append(Build.VERSION.SDK_INT).append("\n");
        log.append("DEVICE CODENAME: ").append(Build.VERSION.CODENAME).append("\n");
        log.append("DEVICE RELEASE: ").append(Build.VERSION.RELEASE).append("\n");
        log.append("DEVICE FP: ").append(Build.FINGERPRINT).append("\n");
        log.append("DEVICE MAN: ").append(Build.MANUFACTURER).append("\n");
        log.append("DEVICE BRAND: ").append(Build.BRAND).append("\n");
        log.append("DEVICE MODEL: ").append(Build.MODEL).append("\n");
        log.append("====== X-APM MODULE DELEGATE DUMP END ======");
        XposedBridge.log(log.toString());
    }
}
