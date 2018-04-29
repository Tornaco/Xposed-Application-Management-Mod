package github.tornaco.xposedmoduletest.xposed.submodules.debug;

import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by Tornaco on 2018/4/28 20:46.
 * God bless no bug!
 */
public class SuperClassTest {

    private void sayHello() {
        XposedLog.wtf("DEBUG-TEST Hello I am: " + getClass());
    }
}
