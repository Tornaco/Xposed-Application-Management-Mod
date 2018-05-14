package github.tornaco.xposedmoduletest.util;

/**
 * Created by guohao4 on 2018/2/8.
 * Email: Tornaco@163.com
 */

public class WorkaroundFixer {

    // FIXME ANR ISSUES.
    public static boolean isThisDeviceVerifyDisplayerNeedDelayRes() {
        return OSUtil.isNubiaDevice();
    }
}
