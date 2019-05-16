package github.tornaco.xposedmoduletest.xposed.service.bandwidth;

import android.text.TextUtils;
import android.util.Log;

import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.util.OSUtil;
import github.tornaco.xposedmoduletest.xposed.service.NativeDaemonConnector;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

// Try execute with aosp command format.
// LineageOS has changed it's command format.
// See https://github.com/LineageOS/android_system_netd/commit/2338babddfcf5f90c574b7a0d470f48dea8001ee#diff-cf1e8c14e54505f60aa10ceb8d5d8ab3

public class BandwidthCommandCompat {

    public static boolean restrictAppOnWifi(NativeDaemonConnector connector,
                                            int uid, boolean restrict,
                                            String ifaceName) {
        boolean los = OSUtil.isLOS();

        boolean success;

        final String action = restrict ? "add" : "remove";
        try {
            connector.execute("bandwidth", action + "restrictappsonwlan", uid);
            success = true;
        } catch (NativeDaemonConnector.NativeDaemonConnectorException e) {
            if (BuildConfig.DEBUG)
                XposedLog.verbose("Fail@aosp command restrictAppOnWifi: " + Log.getStackTraceString(e));
            success = false;
        }

        if (success) return true;

        if (TextUtils.isEmpty(ifaceName)) {
            XposedLog.wtf("ifaceName is empty!!!");
            return false;
        }

        try {
            connector.execute("bandwidth", action + "restrictappsonwlan", ifaceName, uid);
            success = true;
        } catch (NativeDaemonConnector.NativeDaemonConnectorException e) {
            if (BuildConfig.DEBUG)
                XposedLog.verbose("Fail@los command restrictAppOnWifi: " + Log.getStackTraceString(e));
            success = false;
        }

        return success;
    }

    public static boolean restrictAppOnData(NativeDaemonConnector connector,
                                            int uid, boolean restrict,
                                            String ifaceName) {
        boolean los = OSUtil.isLOS();

        boolean success;

        final String action = restrict ? "add" : "remove";
        try {
            connector.execute("bandwidth", action + "restrictappsondata", uid);
            success = true;
        } catch (NativeDaemonConnector.NativeDaemonConnectorException e) {
            XposedLog.verbose("Fail@aosp command restrictAppOnData: " + Log.getStackTraceString(e));
            success = false;
        }

        if (success) return true;


        if (TextUtils.isEmpty(ifaceName)) {
            XposedLog.wtf("ifaceName is empty!!!");
            return false;
        }

        try {
            connector.execute("bandwidth", action + "restrictappsondata", ifaceName, uid);
            success = true;
        } catch (NativeDaemonConnector.NativeDaemonConnectorException e) {
            XposedLog.verbose("Fail@los command restrictAppOnData: " + Log.getStackTraceString(e));
            success = false;
        }

        return success;
    }
}
