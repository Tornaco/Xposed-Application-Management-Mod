package github.tornaco.xposedmoduletest.xposed.service.am;

import github.tornaco.xposedmoduletest.IServiceControl;

/**
 * Created by Tornaco on 2018/4/28 10:16.
 * God bless no bug!
 */
public interface AppServiceControlServiceStopper {
    boolean stopService(IServiceControl control);
}
