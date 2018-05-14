package github.tornaco.xposedmoduletest.xposed.service.opt.gcm;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Created by Tornaco on 2018/4/29 12:32.
 * God bless no bug!
 */
public class GCMFCMHelperTest {

    @Test
    public void isGcmIntent() {
    }

    @Test
    public void isFcmIntent() {
    }

    @Test
    public void isGcmOrFcmIntent() {
    }

    @Test
    public void isHandlingGcmIntent() throws InterruptedException {
        GCMFCMHelper.onGcmIntentReceived("abcde");

        Assert.assertTrue(GCMFCMHelper.isHandlingGcmIntent("abcde"));
        Assert.assertFalse(GCMFCMHelper.isHandlingGcmIntent("abcdefffff"));
        Thread.sleep(20 * 1000);
        Assert.assertTrue(GCMFCMHelper.isHandlingGcmIntent("abcde"));
        Assert.assertFalse(GCMFCMHelper.isHandlingGcmIntent("abcdefffff"));
        Thread.sleep(10 * 1000);
        Assert.assertFalse(GCMFCMHelper.isHandlingGcmIntent("abcde"));
    }

    @Test
    public void onGcmIntentReceived() {
    }
}