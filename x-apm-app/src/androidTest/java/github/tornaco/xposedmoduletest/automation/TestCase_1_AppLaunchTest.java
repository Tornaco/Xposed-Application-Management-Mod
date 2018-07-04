package github.tornaco.xposedmoduletest.automation;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * Created by Tornaco on 2018/7/4 17:28.
 * This file is writen for project X-APM at host guohao4.
 */
@RunWith(AndroidJUnit4.class)
public class TestCase_1_AppLaunchTest {

    @Test
    public void testLaunchApp() {
        String pkgName = github.tornaco.xposedmoduletest.BuildConfig.APPLICATION_ID;
        Context context = InstrumentationRegistry.getContext();
        PackageManager pm = context.getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(pkgName);
        Assert.assertNotNull("Could not find launch intent", intent);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
