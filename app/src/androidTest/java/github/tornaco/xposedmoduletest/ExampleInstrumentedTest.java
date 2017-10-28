package github.tornaco.xposedmoduletest;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiSelector;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.newstand.logger.Logger;
import org.newstand.logger.Settings;

import static org.junit.Assert.assertEquals;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        Logger.config(Settings.builder().tag("XAppGuard")
                .logLevel(Logger.LogLevel.ALL)
                .build());
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("github.tornaco.xposedmoduletest", appContext.getPackageName());

        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        UiObject uiObject = device.findObject(new UiSelector().resourceId("com.tencent.mm:id/iy"));
        String clzName = uiObject.getClassName();
        Logger.i(clzName);
        Logger.i(String.valueOf(uiObject.getChildCount()));
    }
}
