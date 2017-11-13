package github.tornaco.xposedmoduletest.xposed.submodules;

import github.tornaco.xposedmoduletest.xposed.service.IIntentFirewallBridge;
import github.tornaco.xposedmoduletest.xposed.service.IModuleBridge;
import lombok.Getter;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

abstract class IntentFirewallAndroidSubModule extends AndroidSubModuleModule {

    @Getter
    private IIntentFirewallBridge intentFirewallBridge;

    @Override
    public void onBridgeCreate(IModuleBridge bridge) {
        super.onBridgeCreate(bridge);
        this.intentFirewallBridge = (IIntentFirewallBridge) bridge;
    }
}
