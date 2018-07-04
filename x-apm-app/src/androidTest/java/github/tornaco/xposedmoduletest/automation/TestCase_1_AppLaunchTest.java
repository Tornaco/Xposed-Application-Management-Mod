package github.tornaco.xposedmoduletest.automation;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.Until;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;


/**
 * Created by Tornaco on 2018/7/4 17:28.
 * This file is writen for project X-APM at host guohao4.
 */
@RunWith(AndroidJUnit4.class)
public class TestCase_1_AppLaunchTest {

    private final static long BENCH_MAX_ACCEPTABLE_LAUNCH_TIME = TimeUnit.SECONDS.toMillis(12);

    @Test
    public void testLaunchApp() {
        String pkgName = github.tornaco.xposedmoduletest.BuildConfig.APPLICATION_ID;
        Context context = InstrumentationRegistry.getContext();
        PackageManager pm = context.getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(pkgName);
        Assert.assertNotNull("Could not find launch intent", intent);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

        // Wait util app launch.
        // If it take too much time, fail it.
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
                .wait(Until.hasObject(By.pkg(pkgName).depth(0)),
                        BENCH_MAX_ACCEPTABLE_LAUNCH_TIME);

    }
}
