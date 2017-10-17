package github.tornaco.xposedmoduletest.license;

import org.junit.Test;
import org.newstand.logger.Logger;

/**
 * Created by guohao4 on 2017/10/17.
 * Email: Tornaco@163.com
 */
public class LicenseServiceTest {
    @Test
    public void all() throws Exception {
        LicenseService licenseService = LicenseService.Factory.create();
        Logger.d(licenseService.all().execute().body());
    }

}