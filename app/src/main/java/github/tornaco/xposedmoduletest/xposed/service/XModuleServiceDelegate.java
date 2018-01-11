package github.tornaco.xposedmoduletest.xposed.service;

import lombok.AllArgsConstructor;
import lombok.experimental.Delegate;

/**
 * Created by guohao4 on 2017/10/27.
 * Email: Tornaco@163.com
 */
@AllArgsConstructor
public class XModuleServiceDelegate implements IModuleBridge {

    @Delegate
    private final XAshmanServiceAbs mImpl = new XAshmanServiceImplDev();
}
