package github.tornaco.xposedmoduletest.x.service;

import lombok.experimental.Delegate;

/**
 * Created by guohao4 on 2017/10/27.
 * Email: Tornaco@163.com
 */

public class XAppGuardServiceDelegate implements IModuleBridge {
    @Delegate
    private final XAppGuardServiceAbs mImpl = new XAppGuardServiceImplDev();

}
