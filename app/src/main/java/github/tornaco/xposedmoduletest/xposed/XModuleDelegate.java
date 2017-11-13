package github.tornaco.xposedmoduletest.xposed;

import android.support.annotation.Keep;

import de.robv.android.xposed.XposedBridge;
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
        mImpl = new XModuleImplSeparable();
        XposedBridge.log(String.format("Init XModuleDelegate with impl %s:", mImpl));
    }
}
