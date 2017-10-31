package github.tornaco.xposedmoduletest.x;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import github.tornaco.xposedmoduletest.x.app.XAppGuardManager;

import static java.lang.Thread.sleep;

/**
 * Created by guohao4 on 2017/10/25.
 * Email: Tornaco@163.com
 */
public class XAppGuardManagerTest {

    @Before
    public void init() throws Exception {
        XAppGuardManager.init();
    }

    @Test
    public void from() throws Exception {
        Assert.assertNotNull(XAppGuardManager.from());
    }

    @Test
    public void isServiceConnected() throws Exception {
        Assert.assertTrue(XAppGuardManager.from().isServiceConnected());
    }

    @Test
    public void isEnabled() throws Exception {
        XAppGuardManager.from().isEnabled();
    }

    @Test
    public void setEnabled() throws Exception {
        XAppGuardManager.from().setEnabled(true);
        sleep(1000);
        Assert.assertTrue(XAppGuardManager.from().isEnabled());
        XAppGuardManager.from().setEnabled(false);
        sleep(1000);
        Assert.assertFalse(XAppGuardManager.from().isEnabled());
    }

    @Test
    public void setResult() throws Exception {

    }

    @Test
    public void testUI() throws Exception {

    }

    @Test
    public void addPackages() throws Exception {
        XAppGuardManager.from().addPackages(new String[]{"XXXX"});
        try {
            XAppGuardManager.from().addPackages(null);
            Assert.fail("Null should be ignored.");
        } catch (NullPointerException ignored) {

        }
    }

    @Test
    public void removePackages() throws Exception {

    }

    @Test
    public void watch() throws Exception {

    }

    @Test
    public void forceWriteState() throws Exception {

    }

    @Test
    public void forceReadState() throws Exception {

    }

    @Test
    public void getPackages() throws Exception {

    }

    @Test
    public void getStatus() throws Exception {
        Assert.assertTrue(XAppGuardManager.from().getStatus() == XStatus.GOOD.ordinal());
    }

    @Test
    public void isBlur() throws Exception {

    }

    @Test
    public void setBlur() throws Exception {
        XAppGuardManager.from().setBlur(true);
        sleep(1000);
        Assert.assertTrue(XAppGuardManager.from().isBlur());
        XAppGuardManager.from().setBlur(false);
        sleep(1000);
        Assert.assertFalse(XAppGuardManager.from().isBlur());
    }

    @Test
    public void ignore() throws Exception {

    }

    @Test
    public void pass() throws Exception {

    }

    @Test
    public void setBlurPolicy() throws Exception {
        XAppGuardManager.from().setBlurPolicy(XAppGuardManager.BlurPolicy.BLUR_ALL);
        sleep(1000);
        Assert.assertTrue(XAppGuardManager.from().getBlurPolicy() == XAppGuardManager.BlurPolicy.BLUR_ALL);

        XAppGuardManager.from().setBlurPolicy(XAppGuardManager.BlurPolicy.BLUR_WATCHED);
        sleep(1000);
        Assert.assertTrue(XAppGuardManager.from().getBlurPolicy() == XAppGuardManager.BlurPolicy.BLUR_WATCHED);
    }

    @Test
    public void getBlurPolicy() throws Exception {

    }

    @Test
    public void setBlurRadius() throws Exception {
        for (int i = 1; i < 25; i++) {
            XAppGuardManager.from().setBlurRadius(i);
            sleep(1000);
            Assert.assertTrue(XAppGuardManager.from().getBlurRadius() == i);
        }
    }

    @Test
    public void getBlurRadius() throws Exception {

    }

    @Test
    public void setBlurScale() throws Exception {
        XAppGuardManager.from().setBlurScale(0.3f);
        sleep(1000);
        Assert.assertTrue(XAppGuardManager.from().getBlurScale() == 0.3f);
        XAppGuardManager.from().setBlurScale(0.22f);
        sleep(1000);
        Assert.assertTrue(XAppGuardManager.from().getBlurScale() == 0.22f);
    }

    @Test
    public void getBlurScale() throws Exception {

    }

    @Test
    public void hasFeature() throws Exception {
        Assert.assertTrue(XAppGuardManager.from().hasFeature(XAppGuardManager.Feature.BASE));
        Assert.assertTrue(XAppGuardManager.from().hasFeature(XAppGuardManager.Feature.BLUR));
        Assert.assertTrue(XAppGuardManager.from().hasFeature(XAppGuardManager.Feature.FP));
    }

}