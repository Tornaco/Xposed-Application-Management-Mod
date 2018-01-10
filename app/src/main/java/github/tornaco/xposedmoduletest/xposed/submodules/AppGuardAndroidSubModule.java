package github.tornaco.xposedmoduletest.xposed.submodules;

import github.tornaco.xposedmoduletest.xposed.service.IAppGuardBridge;
import github.tornaco.xposedmoduletest.xposed.service.IModuleBridge;
import lombok.Getter;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

abstract class AppGuardAndroidSubModule extends AndroidSubModule {

    @Getter
    private IAppGuardBridge appGuardBridge;

    @Override
    public void onBridgeCreate(IModuleBridge bridge) {
        super.onBridgeCreate(bridge);
        this.appGuardBridge = (IAppGuardBridge) bridge;
    }
}
