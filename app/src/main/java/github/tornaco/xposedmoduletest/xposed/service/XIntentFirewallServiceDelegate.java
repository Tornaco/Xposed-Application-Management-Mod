package github.tornaco.xposedmoduletest.xposed.service;

import lombok.experimental.Delegate;

/**
 * Created by guohao4 on 2017/10/27.
 * Email: Tornaco@163.com
 */

public class XIntentFirewallServiceDelegate implements IIntentFirewallBridge {
    @Delegate
    private final XAshmanServiceAbs mImpl = new XAshmanServiceImpl();
}
