package github.tornaco.xposedmoduletest.x;

import android.os.Build;
import android.support.annotation.Keep;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by guohao4 on 2017/10/19.
 * Email: Tornaco@163.com
 */
@Keep
public class XModuleDelegate extends XModule {

    private XModule mImpl;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        mImpl.handleLoadPackage(lpparam);
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        mImpl.initZygote(startupParam);
    }

    public XModuleDelegate() {
        int sdkInt = Build.VERSION.SDK_INT;
        XposedBridge.log("Init XModuleDelegate with SDK:" + sdkInt);
        if (sdkInt == Build.VERSION_CODES.N) {
            mImpl = new XModuleImpl24();
        } else if (sdkInt == Build.VERSION_CODES.M) {
            mImpl = new XModuleImpl23();
        } else {
            mImpl = new XModuleNotSupport();
        }
    }
}
