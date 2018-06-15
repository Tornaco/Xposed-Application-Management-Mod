package github.tornaco.xposedmoduletest.xposed.service.doze;

import android.util.Log;

import de.robv.android.xposed.XposedHelpers;
import github.tornaco.xposedmoduletest.util.ArrayUtil;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;
import lombok.AllArgsConstructor;
import lombok.ToString;

/**
 * Created by guohao4 on 2018/1/2.
 * Email: Tornaco@163.com
 */
@AllArgsConstructor
@ToString
public class DeviceIdleControllerProxy {

    /**
     * Device is currently active.
     */
    public static final int STATE_ACTIVE = 0;
    /**
     * Device is inactive (screen off, no motion) and we are waiting to for idle.
     */
    public static final int STATE_INACTIVE = 1;
    /**
     * Device is past the initial inactive period, and waiting for the next idle period.
     */
    public static final int STATE_IDLE_PENDING = 2;
    /**
     * Device is currently sensing motion.
     */
    public static final int STATE_SENSING = 3;
    /**
     * Device is currently finding location (and may still be sensing).
     */
    public static final int STATE_LOCATING = 4;
    /**
     * Device is in the idle state, trying to stay asleep as much as possible.
     */
    public static final int STATE_IDLE = 5;
    /**
     * Device is in the idle state, but temporarily out of idle to do regular maintenance.
     */
    public static final int STATE_IDLE_MAINTENANCE = 6;

    /**
     * Device is in the idle state, but temporarily out of idle to do regular maintenance.
     */
    private static final int STATE_UNKNOWN = -1;

    public static String stateToString(int state) {
        switch (state) {
            case STATE_ACTIVE:
                return "ACTIVE";
            case STATE_INACTIVE:
                return "INACTIVE";
            case STATE_IDLE_PENDING:
                return "IDLE_PENDING";
            case STATE_SENSING:
                return "SENSING";
            case STATE_LOCATING:
                return "LOCATING";
            case STATE_IDLE:
                return "IDLE";
            case STATE_IDLE_MAINTENANCE:
                return "IDLE_MAINTENANCE";
            case STATE_UNKNOWN:
                return "STATE_UNKNOWN";
            default:
                return Integer.toString(state);
        }
    }

    /**
     * Device is currently active.
     */
    private static final int LIGHT_STATE_ACTIVE = 0;
    /**
     * Device is inactive (screen off) and we are waiting to for the first light idle.
     */
    private static final int LIGHT_STATE_INACTIVE = 1;
    /**
     * Device is about to go idle for the first time, wait for current work to complete.
     */
    private static final int LIGHT_STATE_PRE_IDLE = 3;
    /**
     * Device is in the light idle state, trying to stay asleep as much as possible.
     */
    private static final int LIGHT_STATE_IDLE = 4;
    /**
     * Device is in the light idle state, we want to go in to idle maintenance but are
     * waiting for network connectivity before doing so.
     */
    private static final int LIGHT_STATE_WAITING_FOR_NETWORK = 5;
    /**
     * Device is in the light idle state, but temporarily out of idle to do regular maintenance.
     */
    private static final int LIGHT_STATE_IDLE_MAINTENANCE = 6;
    /**
     * Device light idle state is overriden, now applying deep doze state.
     */
    private static final int LIGHT_STATE_OVERRIDE = 7;

    public static String lightStateToString(int state) {
        switch (state) {
            case LIGHT_STATE_ACTIVE:
                return "ACTIVE";
            case LIGHT_STATE_INACTIVE:
                return "INACTIVE";
            case LIGHT_STATE_PRE_IDLE:
                return "PRE_IDLE";
            case LIGHT_STATE_IDLE:
                return "IDLE";
            case LIGHT_STATE_WAITING_FOR_NETWORK:
                return "WAITING_FOR_NETWORK";
            case LIGHT_STATE_IDLE_MAINTENANCE:
                return "IDLE_MAINTENANCE";
            case LIGHT_STATE_OVERRIDE:
                return "OVERRIDE";
            case STATE_UNKNOWN:
                return "STATE_UNKNOWN";
            default:
                return Integer.toString(state);
        }
    }

    private Object deviceIdleController;

    public void startMonitoringMotionLocked() {
        XposedLog.verbose("startMonitoringMotionLocked");
        try {
            XposedHelpers.callMethod(deviceIdleController, "startMonitoringMotionLocked");
        } catch (Throwable e) {
            XposedLog.wtf("startMonitoringMotionLocked call fail: " + Log.getStackTraceString(e));
        }
    }

    public void stopMonitoringMotionLocked() {
        XposedLog.verbose("stopMonitoringMotionLocked");
        try {
            XposedHelpers.callMethod(deviceIdleController, "stopMonitoringMotionLocked");
        } catch (Throwable e) {
            XposedLog.wtf("stopMonitoringMotionLocked call fail: " + Log.getStackTraceString(e));
        }
    }

    public void setForceIdle(boolean force) {
        try {
            XposedHelpers.setObjectField(deviceIdleController, "mForceIdle", force);
        } catch (Throwable e) {
            XposedLog.wtf("mForceIdle call fail: " + Log.getStackTraceString(e));
        }
    }

    public void becomeInactiveIfAppropriateLocked() {
        try {
            XposedHelpers.callMethod(deviceIdleController, "becomeInactiveIfAppropriateLocked");
        } catch (Throwable e) {
            XposedLog.wtf("becameInactiveIfAppropriateLocked call fail: " + Log.getStackTraceString(e));
        }
    }

    public void setDeepIdle(boolean idle) {
        try {
            XposedHelpers.setObjectField(deviceIdleController, "mDeepEnabled", idle);
            XposedHelpers.setObjectField(deviceIdleController, "mLightEnabled", idle);
        } catch (Throwable e) {
            XposedLog.wtf("mDeepEnabled call fail: " + Log.getStackTraceString(e));
        }
    }

    public void exitForceIdleLocked() {
        try {
            XposedHelpers.callMethod(deviceIdleController, "exitForceIdleLocked");
        } catch (Throwable e) {
            XposedLog.wtf("exitForceIdleLocked call fail: " + Log.getStackTraceString(e));
        }
    }

    public void stepIdleStateLocked() {
        if (stepIdleStateLockedForM()
                || stepIdleStateLockedForN()) {
            XposedLog.debug("stepIdleStateLocked success");
        } else {
            XposedLog.debug("stepIdleStateLocked fail");
        }
    }

    private boolean stepIdleStateLockedForM() {
        XposedLog.verbose("stepIdleStateLocked for M");
        try {
            XposedHelpers.callMethod(deviceIdleController, "stepIdleStateLocked");
            return true;
        } catch (Throwable e) {
            XposedLog.wtf("stepIdleStateLocked call fail: " + e);
            return false;
        }
    }

    private boolean stepIdleStateLockedForN() {
        XposedLog.verbose("stepIdleStateLocked for N/O");
        try {
            XposedHelpers.callMethod(deviceIdleController, "stepIdleStateLocked", "s:shell");
            return true;
        } catch (Throwable e) {
            XposedLog.wtf("stepIdleStateLocked call fail: " + e);
            return false;
        }
    }

    public int getState() {
        try {
            return XposedHelpers.getIntField(deviceIdleController, "mState");
        } catch (Throwable e) {
            XposedLog.wtf("getState call fail: " + Log.getStackTraceString(e));
            return STATE_UNKNOWN;
        }
    }

    public boolean isForceIdle() {
        try {
            return XposedHelpers.getBooleanField(deviceIdleController, "mForceIdle");
        } catch (Throwable e) {
            XposedLog.wtf("isForceIdle call fail: " + Log.getStackTraceString(e));
            return false;
        }
    }

    public boolean addPowerSaveWhitelistAppInternal(String name) {
        XposedLog.verbose("addPowerSaveWhitelistAppInternal: " + name);
        try {
            Object res =
                    XposedHelpers.callMethod(deviceIdleController, "addPowerSaveWhitelistAppInternal", name);
            return (boolean) res;
        } catch (Throwable e) {
            XposedLog.wtf("addPowerSaveWhitelistAppInternal call fail: " + e);
            return false;
        }
    }

    public boolean removePowerSaveWhitelistAppInternal(String name) {
        XposedLog.verbose("removePowerSaveWhitelistAppInternal: " + name);
        try {
            Object res =
                    XposedHelpers.callMethod(deviceIdleController, "removePowerSaveWhitelistAppInternal", name);
            return (boolean) res;
        } catch (Throwable e) {
            XposedLog.wtf("removePowerSaveWhitelistAppInternal call fail: " + e);
            return false;
        }
    }

    public String[] getUserPowerWhitelistInternal() {
        XposedLog.verbose("getUserPowerWhitelistInternal");
        try {
            Object res =
                    XposedHelpers.callMethod(deviceIdleController, "getUserPowerWhitelistInternal");
            return (String[]) res;
        } catch (Throwable e) {
            XposedLog.wtf("getUserPowerWhitelistInternal call fail: " + e);
            return ArrayUtil.newEmptyStringArray();
        }
    }

    public String[] getFullPowerWhitelistInternal() {
        XposedLog.verbose("getFullPowerWhitelistInternal");
        try {
            Object res =
                    XposedHelpers.callMethod(deviceIdleController, "getFullPowerWhitelistInternal");
            return (String[]) res;
        } catch (Throwable e) {
            XposedLog.wtf("getFullPowerWhitelistInternal call fail: " + e);
            return ArrayUtil.newEmptyStringArray();
        }
    }

    public String[] getSystemPowerWhitelistInternal() {
        XposedLog.verbose("getSystemPowerWhitelistInternal");
        try {
            Object res =
                    XposedHelpers.callMethod(deviceIdleController, "getSystemPowerWhitelistInternal");
            return (String[]) res;
        } catch (Throwable e) {
            XposedLog.wtf("getSystemPowerWhitelistInternal call fail: " + e);
            return ArrayUtil.newEmptyStringArray();
        }
    }
}
