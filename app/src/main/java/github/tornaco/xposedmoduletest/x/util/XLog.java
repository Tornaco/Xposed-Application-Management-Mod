package github.tornaco.xposedmoduletest.x.util;

import java.util.concurrent.atomic.AtomicBoolean;

import de.robv.android.xposed.XposedBridge;

/**
 * Created by guohao4 on 2017/10/25.
 * Email: Tornaco@163.com
 */

public abstract class XLog {

    private static final String TAG = "XAppGuard-";

    private static final AtomicBoolean DEBUG = new AtomicBoolean(true);

    public static void setDebug(boolean debug) {
        XLog.DEBUG.set(debug);
    }

    public static boolean isDebug() {
        return DEBUG.get();
    }

    public static void logV(Object log) {
        if (DEBUG.get()) XposedBridge.log(TAG + String.valueOf(log));
    }

    public static void logD(Object log) {
        if (DEBUG.get()) XposedBridge.log(TAG + String.valueOf(log));
    }

    public static void logF(Object log) {
        XposedBridge.log(TAG + String.valueOf(log));
    }
}
