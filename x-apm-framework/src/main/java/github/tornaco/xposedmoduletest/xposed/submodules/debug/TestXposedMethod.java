package github.tornaco.xposedmoduletest.xposed.submodules.debug;

import java.lang.reflect.Method;

import de.robv.android.xposed.XposedHelpers;
import github.tornaco.xposedmoduletest.util.ReflectionUtils;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by Tornaco on 2018/4/28 20:45.
 * God bless no bug!
 */
public class TestXposedMethod extends SuperClassTest {

    public void main() {
        try {
            Object testXpMethodObj = this;

            for (Method m : getClass().getDeclaredMethods()) {
                XposedLog.wtf("DEBUG-TEST: " + m);
            }


            try {
                XposedHelpers.callMethod(testXpMethodObj, "sayHello");
            } catch (Throwable e) {
                XposedLog.wtf("DEBUG-TEST 1@: " + e);
            }


            // 2.
            XposedLog.wtf("*****************************");

            Method hello = ReflectionUtils.findMethod(getClass(), "sayHello");
            ReflectionUtils.makeAccessible(hello);
            ReflectionUtils.invokeMethod(hello, testXpMethodObj);

        } catch (Throwable e) {
            XposedLog.wtf("DEBUG-TEST: " + e);
        }
    }
}
