package github.tornaco.xposedmoduletest.xposed;

/**
 * Created by Tornaco on 2018/5/3 12:07.
 * God bless no bug!
 */
public class AppGlobals {

    private static int sXAPMCUid = -1;

    public static void setXAPMCUid(int sXAPMCUid) {
        AppGlobals.sXAPMCUid = sXAPMCUid;
    }

    public static int getXAPMCUid() {
        return sXAPMCUid;
    }
}
