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
    }

    public static final String TAG = "X-APM-S-";

    public static final String TAG_DANGER = "X-APM-DANGER-";
    public static final String TAG_LAZY = "X-APM-LAZY-";
    public static final String TAG_VIEW = "X-APM-VIEW-";
    public static final String TAG_ME = "X-APM-ME-";

    public static final String PREFIX_USER = "USER-";
    public static final String PREFIX_KEY = "KEY-";
    public static final String PREFIX_DOZE = "DOZE-";
    public static final String PREFIX_PM = "PM-";
    public static final String PREFIX_OPS = "OPS-";

    public static final String PREFIX_SERVICE = "SERVICE-";
    public static final String PREFIX_BROADCAST = "BROADCAST-";

    // Default log level.
    private static LogLevel sLevel = BuildConfig.DEBUG ? LogLevel.ALL : LogLevel.WARN;

    public static void setLogLevel(LogLevel level) {
        XposedLog.sLevel = level;
    }

    private static boolean isLoggable(LogLevel logLevel) {
        return logLevel.ordinal() >= sLevel.ordinal();
    }

    public static boolean isVerboseLoggable() {
        return isLoggable(LogLevel.VERBOSE);
    }

    public static void verbose(Object log) {
        if (isLoggable(LogLevel.VERBOSE)) XposedBridge.log(TAG
                + LogLevel.VERBOSE.name()
                + "-"
                + String.valueOf(log));
    }

    public static void verbose(String format, Object... args) {
        if (isLoggable(LogLevel.VERBOSE)) XposedBridge.log(TAG
                + LogLevel.VERBOSE.name()
                + "-"
                + String.format(format, args));
    }

    public static void verboseOn(final Object log,
                                 ExecutorService executorService) {
        if (isLoggable(LogLevel.VERBOSE)) executorService.execute(new Runnable() {
            @Override
            public void run() {
                XposedBridge.log(TAG
                        + LogLevel.VERBOSE.name()
                        + "-"
                        + String.valueOf(log));
            }
        });
    }

    public static void debug(Object log) {
        if (isLoggable(LogLevel.DEBUG)) XposedBridge.log(TAG
                + LogLevel.DEBUG.name()
                + "-"
                + String.valueOf(log));
    }

    /**
     * Log anyway, fuck it.
     */
    public static void wtf(Object log) {
        if (isLoggable(LogLevel.WARN)) XposedBridge.log(TAG
                + LogLevel.WARN.name()
                + "-"
                + String.valueOf(log));
    }


    public static void boot(Object log) {
        XposedBridge.log(TAG
                + "BOOT_STAGE"
                + "-"
                + String.valueOf(log));
    }

    public static void danger(Object log) {
        if (isLoggable(LogLevel.WARN)) XposedBridge.log(TAG_DANGER
                + "-"
                + String.valueOf(log));
    }
}
