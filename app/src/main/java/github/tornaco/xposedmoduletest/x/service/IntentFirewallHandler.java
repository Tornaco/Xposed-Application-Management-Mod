package github.tornaco.xposedmoduletest.x.service;

import github.tornaco.apigen.CreateMessageIdWithMethods;

import static github.tornaco.xposedmoduletest.x.service.AppGuardServiceHandler.BASE;

/**
 * Created by guohao4 on 2017/11/1.
 * Email: Tornaco@163.com
 */
@CreateMessageIdWithMethods(base = BASE, fallbackMessageDecode = "UNKNOWN")
interface IntentFirewallHandler {
    int BASE = 0x1;

    void setEnabled(boolean enabled);

    void clearProcessOnScreenOff();
}
