package github.tornaco.xposedmoduletest;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.UiDevice;

import org.junit.Test;

import github.tornaco.xposedmoduletest.xposed.app.XAppLockManager;
import github.tornaco.xposedmoduletest.xposed.bean.VerifySettings;

/**
 * Created by guohao4 on 2018/2/6.
 * Email: Tornaco@163.com
 */

public class AppLockStablityTest {

    @Test
    public void testAppLockVerifyLoop() throws InterruptedException {
        while (true) {
            testAppLockVerify();
            Thread.sleep(200);

            // Go home.
            UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
                    .pressBack();
        }
    }


    @Test
    public void testAppLockVerify() {
        String targetPackage = "com.yinheng.passthecarexam";

        // 1. Setup verify mode.
        VerifySettings vf = new VerifySettings(true, true, true);
        XAppLockManager.get().setVerifySettings(vf);
        XAppLockManager.get().addOrRemoveLockApps(new String[]{targetPackage}, true);
        XAppLockManager.get().setEnabled(true);

        // 2. Launch an app with applock.
        Intent launchIntent = InstrumentationRegistry.getContext()
                .getPackageManager().getLaunchIntentForPackage(targetPackage);
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        InstrumentationRegistry.getTargetContext().startActivity(launchIntent);
    }
}
