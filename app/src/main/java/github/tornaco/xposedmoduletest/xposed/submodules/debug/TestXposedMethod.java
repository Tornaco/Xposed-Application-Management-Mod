package github.tornaco.xposedmoduletest.xposed.submodules.debug;

import java.lang.reflect.Method;

import de.robv.android.xposed.XposedHelpers;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by Tornaco on 2018/4/28 20:45.
 * God bless no bug!
 */
public class TestXposedMethod extends SuperClassTest {

    public void main() {
        Object testXpMethodObj = this;

        for (Method m : getClass().getDeclaredMethods()) {
            XposedLog.wtf("DEBUG-TEST: " + m);
        }

        XposedHelpers.callMethod(testXpMethodObj, "sayHello");

    }
}
