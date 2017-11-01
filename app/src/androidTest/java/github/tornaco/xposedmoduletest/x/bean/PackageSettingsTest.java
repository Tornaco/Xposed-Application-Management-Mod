package github.tornaco.xposedmoduletest.x.bean;

import android.content.ComponentName;
import android.support.test.espresso.core.deps.guava.collect.Lists;

import org.junit.Assert;
import org.junit.Test;

import java.util.logging.Logger;

/**
 * Created by guohao4 on 2017/11/1.
 * Email: Tornaco@163.com
 */
public class PackageSettingsTest {
    @Test
    public void toJsonString() throws Exception {
        PackageSettings packageSettings = new PackageSettings();
        packageSettings.setPkgName("xxxxx.xxx.xxx");
        packageSettings.setVerify(true);
        packageSettings.setVerifyComponents(Lists.newArrayList(new ComponentName("aaa", "bbb"), new ComponentName("rrr", "ccc")));
        packageSettings.setVerifyPolicy(9);

        String js = packageSettings.toJsonString();
        packageSettings = PackageSettings.fromJsonString(js);
        Assert.assertTrue(packageSettings.getPkgName().equals("xxxxx.xxx.xxx"));
    }

}