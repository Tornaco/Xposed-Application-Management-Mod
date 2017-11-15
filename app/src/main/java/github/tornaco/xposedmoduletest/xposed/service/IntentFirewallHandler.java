package github.tornaco.xposedmoduletest.xposed.service;

import github.tornaco.apigen.CreateMessageIdWithMethods;

/**
 * Created by guohao4 on 2017/11/1.
 * Email: Tornaco@163.com
 */
@CreateMessageIdWithMethods(fallbackMessageDecode = "UNKNOWN")
interface IntentFirewallHandler {

    void setBootBlockEnabled(boolean enabled);

    void setStartBlockEnabled(boolean enabled);

    void setLockKillEnabled(boolean enabled);

    void clearProcess();

    void setLockKillDelay(long delay);

    void onScreenOff();

    void onScreenOn();
}
