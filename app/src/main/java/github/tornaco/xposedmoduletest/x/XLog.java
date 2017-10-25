package github.tornaco.xposedmoduletest.x;

import de.robv.android.xposed.XposedBridge;

/**
 * Created by guohao4 on 2017/10/25.
 * Email: Tornaco@163.com
 */

abstract class XLog {

    private static final String TAG = "XAppGuard-";

    private static final boolean DEBUG_V = true;
    private static final boolean DEBUG_D = true;

    static void logV(Object log) {
        if (DEBUG_V) XposedBridge.log(TAG + String.valueOf(log));
    }

    static void logD(Object log) {
        if (DEBUG_D) XposedBridge.log(TAG + String.valueOf(log));
    }
}
