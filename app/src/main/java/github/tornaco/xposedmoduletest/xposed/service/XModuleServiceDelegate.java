package github.tornaco.xposedmoduletest.xposed.service;

import github.tornaco.xposedmoduletest.xposed.repo.RepoProxy;
import github.tornaco.xposedmoduletest.xposed.submodules.SubModuleManager;
import lombok.AllArgsConstructor;
import lombok.experimental.Delegate;

/**
 * Created by guohao4 on 2017/10/27.
 * Email: Tornaco@163.com
 */
@AllArgsConstructor
public class XModuleServiceDelegate implements IModuleBridge {

    @Delegate
    private final XAshmanServiceAbs mImpl =
            RepoProxy.hasFileIndicator(SubModuleManager.REDEMPTION)
                    ? new XAshmanServiceImplRedemption()
                    : new XAshmanServiceImplDev();

    @Override
    public String toString() {
        return "XModuleServiceDelegate with impl: " + mImpl;
    }
}
