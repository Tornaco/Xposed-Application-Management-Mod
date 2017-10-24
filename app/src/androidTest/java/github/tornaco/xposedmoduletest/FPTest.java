package github.tornaco.xposedmoduletest;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.app.ActivityCompat;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by guohao4 on 2017/10/24.
 * Email: Tornaco@163.com
 */
@RunWith(AndroidJUnit4.class)
public class FPTest {

    @TargetApi(Build.VERSION_CODES.M)
    @Test
    public void testHasFP() {

        Assert.assertTrue(InstrumentationRegistry.getTargetContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_FINGERPRINT));

        FingerprintManager fingerprintManager = (FingerprintManager) InstrumentationRegistry.getTargetContext()
                .getSystemService(Context.FINGERPRINT_SERVICE);
        Assert.assertNotNull(false);
        if (ActivityCompat.checkSelfPermission(InstrumentationRegistry.getTargetContext(),
                Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            Assert.fail("MISSING USE_FINGERPRINT");
            return;
        }
        Assert.assertTrue(fingerprintManager.isHardwareDetected());
    }
}
