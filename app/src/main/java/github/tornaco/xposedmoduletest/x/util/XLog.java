package github.tornaco.xposedmoduletest.x.util;

import de.robv.android.xposed.XposedBridge;

/**
 * Created by guohao4 on 2017/10/25.
 * Email: Tornaco@163.com
 */

public abstract class XLog {

    private static final String TAG = "XAppGuard-";

    private static final boolean DEBUG_V = true;
    private static final boolean DEBUG_D = true;

    public static void logV(Object log) {
        if (DEBUG_V) XposedBridge.log(TAG + String.valueOf(log));
    }

    public static void logD(Object log) {
        if (DEBUG_D) XposedBridge.log(TAG + String.valueOf(log));
    }

    public static void logF(Object log) {
        XposedBridge.log(TAG + String.valueOf(log));
    }
}
