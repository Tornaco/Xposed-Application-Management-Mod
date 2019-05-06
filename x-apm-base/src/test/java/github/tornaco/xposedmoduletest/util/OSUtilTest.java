package github.tornaco.xposedmoduletest.util;

import org.junit.Assert;

public class OSUtilTest {

    @org.junit.Test
    public void isMIUI() {
        Assert.assertTrue(OSUtil.isMIUI());
    }
}