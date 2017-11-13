package github.tornaco.xposedmoduletest.xposed.bean;

import org.junit.Test;
import org.newstand.logger.Logger;

/**
 * Created by guohao4 on 2017/11/4.
 * Email: Tornaco@163.com
 */
public class VerifySettingsTest {
    @Test
    public void from() throws Exception {
        VerifySettings verifySettings = new VerifySettings(true, false, true);
        Logger.d(verifySettings);
        verifySettings = VerifySettings.from("101");
        Logger.d(verifySettings);
        verifySettings = VerifySettings.from("111");
        Logger.d(verifySettings);
        verifySettings = VerifySettings.from("000");
        Logger.d(verifySettings);
    }

}