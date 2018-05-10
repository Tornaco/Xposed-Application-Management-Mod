package github.tornaco.xposedmoduletest.xposed.service.power;

import github.tornaco.xposedmoduletest.xposed.service.InvokeTargetProxy;

/**
 * Created by Tornaco on 2018/5/10 12:07.
 * God bless no bug!
 */
public class PowerManagerServiceProxy extends InvokeTargetProxy<Object> {

    public PowerManagerServiceProxy(Object host) {
        super(host);
    }
}
