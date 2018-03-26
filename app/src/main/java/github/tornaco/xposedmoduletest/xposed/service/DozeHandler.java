package github.tornaco.xposedmoduletest.xposed.service;

import github.tornaco.apigen.CreateMessageIdWithMethods;
import github.tornaco.xposedmoduletest.xposed.service.doze.BatterState;

/**
 * Created by guohao4 on 2017/11/1.
 * Email: Tornaco@163.com
 */
@CreateMessageIdWithMethods(fallbackMessageDecode = "UNKNOWN")
interface DozeHandler {
    void enterIdleMode();

    void stepIdleStateLocked();

    void onScreenOff();

    void onBatteryStateChange(BatterState batterState);

    void setDozeDelayMills(long delayMills);

    void setDozeEnabled(boolean enable);

    void setForceDozeEnabled(boolean enable);

    void setDisableMotionEnabled(boolean enable);

    void updateDozeEndState();

    void onScreenOn();
}
