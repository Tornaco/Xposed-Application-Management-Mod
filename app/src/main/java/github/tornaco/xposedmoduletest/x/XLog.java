package github.tornaco.xposedmoduletest.x;

import de.robv.android.xposed.XposedBridge;
import github.tornaco.xposedmoduletest.BuildConfig;

/**
 * Created by guohao4 on 2017/10/25.
 * Email: Tornaco@163.com
 */

abstract class XLog {

    private static final String TAG = "XAppGuard-";

    private static final boolean DEBUG_V = BuildConfig.DEBUG;
    private static final boolean DEBUG_D = BuildConfig.DEBUG;

    static void logV(Object log) {
        if (DEBUG_V) XposedBridge.log(TAG + String.valueOf(log));
    }

    static void logD(Object log) {
        if (DEBUG_D) XposedBridge.log(TAG + String.valueOf(log));
    }

    static void logW(Object log) {
        XposedBridge.log(TAG + String.valueOf(log));
    }
}
