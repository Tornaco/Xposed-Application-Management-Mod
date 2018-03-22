package github.tornaco.xposedmoduletest;

/**
 * Created by Tornaco on 2018/3/22 13:53.
 * God bless no bug!
 */

public class JavaTestSamePkg {

    public void testMethodCall() {
        new JavaTest().setup();
        new JavaTest().setupPkg();
    }
}
