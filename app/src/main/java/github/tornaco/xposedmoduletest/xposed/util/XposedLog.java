package github.tornaco.xposedmoduletest.xposed.util;

import java.util.concurrent.ExecutorService;

import de.robv.android.xposed.XposedBridge;
import github.tornaco.xposedmoduletest.BuildConfig;

/**
 * Created by guohao4 on 2017/10/25.
 * Email: Tornaco@163.com
 */

public abstract class XposedLog {

    public enum LogLevel {
        ALL,
        VERBOSE,
        INFO,
        DEBUG,
        WARN,
        ERROR,
        NONE;
    }

    private static final boolean FORCE_DEBUG = false;

    public static final String TAG_PREFIX = "X-APM-S-";
    public static final String TAG_DANGER = "X-APM-DANGER-";
    public static final String TAG_LIST = "X-APM-LIST-";
    public static final String TAG_LAZY = "X-APM-LAZY-";

    private static LogLevel sLevel = BuildConfig.DEBUG ? LogLevel.ALL : LogLevel.WARN;

    public static void setLogLevel(LogLevel level) {
        XposedLog.sLevel = level;
    }

    private static boolean isLoggable(LogLevel logLevel) {
        return FORCE_DEBUG || logLevel.ordinal() >= sLevel.ordinal();
    }

    public static boolean isVerboseLoggable() {
        return isLoggable(LogLevel.VERBOSE);
    }

    public static void verbose(Object log) {
        if (isLoggable(LogLevel.VERBOSE)) XposedBridge.log(TAG_PREFIX
                + LogLevel.VERBOSE.name()
                + "-"
                + String.valueOf(log));
    }

    public static void verbose(String format, Object... args) {
        if (isLoggable(LogLevel.VERBOSE)) XposedBridge.log(TAG_PREFIX
                + LogLevel.VERBOSE.name()
                + "-"
                + String.format(format, args));
    }

    public static void verboseOn(final Object log,
                                 ExecutorService executorService) {
        if (isLoggable(LogLevel.VERBOSE)) executorService.execute(new Runnable() {
            @Override
            public void run() {
                XposedBridge.log(TAG_PREFIX
                        + LogLevel.VERBOSE.name()
                        + "-"
                        + String.valueOf(log));
            }
        });
    }

    public static void debug(Object log) {
        if (isLoggable(LogLevel.DEBUG)) XposedBridge.log(TAG_PREFIX
                + LogLevel.DEBUG.name()
                + "-"
                + String.valueOf(log));
    }

    /**
     * Log anyway, fuck it.
     */
    public static void wtf(Object log) {
        if (isLoggable(LogLevel.WARN)) XposedBridge.log(TAG_PREFIX
                + LogLevel.WARN.name()
                + "-"
                + String.valueOf(log));
    }

    public static void danger(Object log) {
        if (isLoggable(LogLevel.WARN)) XposedBridge.log(TAG_DANGER
                + "-"
                + String.valueOf(log));
    }
}
