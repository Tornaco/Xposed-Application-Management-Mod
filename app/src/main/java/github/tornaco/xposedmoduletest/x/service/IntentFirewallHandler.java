package github.tornaco.xposedmoduletest.x.service;

import github.tornaco.apigen.CreateMessageIdWithMethods;

/**
 * Created by guohao4 on 2017/11/1.
 * Email: Tornaco@163.com
 */
@CreateMessageIdWithMethods(fallbackMessageDecode = "UNKNOWN")
interface IntentFirewallHandler {
    void setEnabled(boolean enabled);

    void clearProcess();

    void onScreenOff();

    void onScreenOn();
}
