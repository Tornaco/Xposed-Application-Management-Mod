package github.tornaco.xposedmoduletest;

import org.junit.Test;
import org.newstand.logger.Logger;

import java.util.Calendar;

/**
 * Created by Tornaco on 2018/3/22 13:52.
 * God bless no bug!
 */

public class JavaTest {

    @Test
    protected void setup() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(10000000);
        int h = c.get(Calendar.HOUR);
        Logger.d("H: " + h);
    }

    void setupPkg() {

    }
}
