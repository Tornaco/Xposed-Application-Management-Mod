package github.tornaco.xposedmoduletest.xposed.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import de.robv.android.xposed.XposedBridge;
import github.tornaco.xposedmoduletest.BuildConfig;

/**
 * Created by guohao4 on 2017/10/25.
 * Email: Tornaco@163.com
 */

public abstract class XLog {

    private static final String TAG_D = "XAppGuard-DEBUG-";
    private static final String TAG_V = "XAppGuard-VERBOSE-";
    private static final String TAG_F = "XAppGuard-FUCK-";

    private static final AtomicBoolean DEBUG = new AtomicBoolean(BuildConfig.DEBUG);

    public static void setDebug(boolean debug) {
        XLog.DEBUG.set(debug);
    }

    public static boolean isDebug() {
        return DEBUG.get();
    }

    public static void logV(Object log) {
        if (DEBUG.get()) XposedBridge.log(TAG_V + String.valueOf(log));
    }

    public static void logVOnExecutor(final Object log,
                                      ExecutorService executorService) {
        if (DEBUG.get()) executorService.execute(new Runnable() {
            @Override
            public void run() {
                XposedBridge.log(TAG_V + String.valueOf(log));
            }
        });
    }

    public static void logD(Object log) {
        if (DEBUG.get()) XposedBridge.log(TAG_D + String.valueOf(log));
    }

    /**
     * Log anyway, fuck it.
     */
    public static void logF(Object log) {
        if (DEBUG.get()) XposedBridge.log(TAG_F + String.valueOf(log));
    }
}
