package github.tornaco.xposedmoduletest.xposed.app;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.IAshmanService;
import github.tornaco.xposedmoduletest.IAshmanWatcher;
import github.tornaco.xposedmoduletest.IBackupAgent;
import github.tornaco.xposedmoduletest.IBooleanCallback1;
import github.tornaco.xposedmoduletest.IJsEvaluateListener;
import github.tornaco.xposedmoduletest.IPackageUninstallCallback;
import github.tornaco.xposedmoduletest.IServiceControl;
import github.tornaco.xposedmoduletest.ITaskRemoveListener;
import github.tornaco.xposedmoduletest.ITopPackageChangeListener;
import github.tornaco.xposedmoduletest.compat.os.XAppOpsManager;
import github.tornaco.xposedmoduletest.util.ArrayUtil;
import github.tornaco.xposedmoduletest.util.OSUtil;
import github.tornaco.xposedmoduletest.util.Singleton;
import github.tornaco.xposedmoduletest.xposed.bean.AppOpsTemplate;
import github.tornaco.xposedmoduletest.xposed.bean.AppSettings;
import github.tornaco.xposedmoduletest.xposed.bean.BlockRecord2;
import github.tornaco.xposedmoduletest.xposed.bean.DozeEvent;
import github.tornaco.xposedmoduletest.xposed.bean.JavaScript;
import github.tornaco.xposedmoduletest.xposed.bean.OpLog;
import github.tornaco.xposedmoduletest.xposed.bean.SystemPropProfile;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/11/9.
 * Email: Tornaco@163.com
 */

@SuppressWarnings("EmptyCatchBlock")
public class XAPMManager {

    // This means it is the app setting template edit settings.
    public static final String APPOPS_WORKAROUND_DUMMY_PACKAGE_NAME = "tornaco.github.apm.ops.dummy.template";

    public static final int FLAG_SHOW_SYSTEM_APP = 0X100;
    public static final int FLAG_SHOW_SYSTEM_APP_WITHOUT_CORE_APP = 0X200;
    public static final int FLAG_NONE = 0;

    /**
     * Reject application network traffic on wifi network
     **/
    public static final int POLICY_REJECT_ON_WIFI = 0x8000;

    /**
     * Reject application network traffic on cellular network
     **/
    public static final int POLICY_REJECT_ON_DATA = 0x10000;

    /**
     * Reject none.
     **/
    public static final int POLICY_REJECT_NONE = 0;

    public enum OPT {
        TOAST,
        TOAST_ICON,
        FOREGROUND_NOTIFICATION,
        THREE_FINGER_GESTURE,
        P_GESTURE,
        LAZY_APP_TIPS,
        PKG_INSTALL_VERIFY,
        OPT_BLUR_CACHE,
        APP_INACTIVE_POLICY_,
        REMOVE_TASK_ON_APP_IDLE,
        CAMERA_OPEN_NOTIFICATION,
    }

    public interface Op {
        int ADD = 0x1;
        int REMOVE = 0x2;
    }

    public interface ControlMode {
        int WHITE_LIST = 1;
        int BLACK_LIST = 2;
        int UNKNOWN = -1;
    }

    public interface AppInactivePolicy {
        int IDLE = 0x1;
        int FORCE_STOP = 0x2;
    }

    public interface AppLevel {
        int THIRD_PARTY = 0;
        int SYSTEM = 1;
        int MEDIA_UID = 2;
        int PHONE_UID = 3;
        int WEBVIEW_IMPL = 4;
        int SYSTEM_UID = 5;
    }

    public interface ExcludeRecentSetting {
        int NONE = 0;
        int EXCLUDE = 1;
        int INCLUDE = 2;
    }

    public interface AppServiceControlSolutions {

        int FLAG_APP = 0x00000001;
        int FLAG_FW = 0x00000002;

        static String decode(int flags) {
            StringBuilder sb = new StringBuilder();
            if (flags == FLAG_APP) {
                sb.append("FLAG_APP");
            }
            if (flags == FLAG_FW) {
                sb.append("FLAG_FW");
            }
            return sb.toString();
        }
    }

    public interface ConfigOverlays {
        int NONE = Integer.MIN_VALUE;
    }

    public static final String SERVICE_NAME =
            OSUtil.isOOrAbove() ? Context.TV_INPUT_SERVICE : "user.tor_ash";

    public static final String VERIFIER_CLASS_NAME = "github.tornaco.xposedmoduletest.ui.activity.ag.VerifyDisplayerActivity";

    private IAshmanService mService;

    private static final Singleton<XAPMManager> sManager
            = new Singleton<XAPMManager>() {
        @Override
        protected XAPMManager create() {
            return new XAPMManager();
        }
    };

    private XAPMManager() {
        retrieveService();
    }

    public static XAPMManager get() {
        return sManager.get();
    }

    public boolean isServiceAvailable() {
        return isServiceAvailable(false);
    }

    public boolean isServiceAvailable(boolean doubleConfirm) {
        boolean isRegistered = mService != null;
        if (!isRegistered) {
            return false;
        }
        if (!doubleConfirm) {
            return true;
        }
        try {
            String answer = mService.getBuildVersionName();
            return answer != null;
        } catch (Throwable e) {
            // Dead.
        }
        return false;
    }

    public void retrieveService() {
        mService = IAshmanService.Stub.asInterface(ServiceManager.getService(SERVICE_NAME));
    }

    private void ensureService() {
        if (mService == null) {
            Log.e(XposedLog.TAG, "Service is not available@\n" + Log.getStackTraceString(new Throwable()));
        }
    }

    public int getAppLevel(String pkg) {
        ensureService();
        try {
            return mService.getAppLevel(pkg);
        } catch (Exception e) {
            handleException(e);
            return AppLevel.THIRD_PARTY;
        }
    }

    public String packageForTaskId(int taskId) {
        ensureService();
        try {
            return mService.packageForTaskId(taskId);
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    public void clearProcess(IProcessClearListenerAdapter adapter, boolean doNotClearWhenIntervative, boolean onlyForThoseInList) {
        ensureService();
        try {
            mService.clearProcess(adapter, doNotClearWhenIntervative, onlyForThoseInList);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void setBootBlockEnabled(boolean enabled) {
        ensureService();
        try {
            mService.setBootBlockEnabled(enabled);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public boolean isBlockBlockEnabled() {
        ensureService();
        try {
            return mService.isBlockBlockEnabled();
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public void setStartBlockEnabled(boolean enabled) {
        ensureService();
        try {
            mService.setStartBlockEnabled(enabled);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public boolean isStartBlockEnabled() {
        ensureService();
        try {
            return mService.isStartBlockEnabled();
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public void setLockKillEnabled(boolean enabled) {
        ensureService();
        try {
            mService.setLockKillEnabled(enabled);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public List<BlockRecord2> getBlockRecords() {
        ensureService();
        try {
            return mService.getBlockRecords();
        } catch (Exception e) {
            handleException(e);
            return new ArrayList<>(0);
        }
    }

    public void clearBlockRecords() {
        ensureService();
        try {
            mService.clearBlockRecords();
        } catch (Exception e) {
            handleException(e);
        }
    }

    public boolean isLockKillEnabled() {
        ensureService();
        try {
            return mService.isLockKillEnabled();
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public void setLockKillDelay(long delay) {
        ensureService();
        try {
            mService.setLockKillDelay(delay);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public long getLockKillDelay() {
        ensureService();
        try {
            return mService.getLockKillDelay();
        } catch (Exception e) {
            handleException(e);
            return 0L;
        }
    }

    public void setRFKillEnabled(boolean enabled) {
        try {
            mService.setRFKillEnabled(enabled);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public boolean isRFKillEnabled() {
        try {
            return mService.isRFKillEnabled();
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public boolean checkService(Intent intent, ComponentName servicePkgName, int callerUid) {
        ensureService();
        try {
            return mService.checkService(intent, servicePkgName, callerUid);
        } catch (Exception e) {
            handleException(e);
            return true;
        }
    }

    public boolean checkBroadcast(Intent action, int receiverUid, int callerUid) {
        ensureService();
        try {
            return mService.checkBroadcast(action, receiverUid, callerUid);
        } catch (Exception e) {
            handleException(e);
            return true;
        }
    }

    public void setComponentEnabledSetting(ComponentName componentName, int newState, int flags) {
        ensureService();
        try {
            mService.setComponentEnabledSetting(componentName, newState, flags);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public int getComponentEnabledSetting(ComponentName componentName) {
        ensureService();
        try {
            return mService.getComponentEnabledSetting(componentName);
        } catch (Exception e) {
            handleException(e);
            return Integer.MIN_VALUE + PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;
        }
    }

    public int getApplicationEnabledSetting(String packageName) {
        ensureService();
        try {
            return mService.getApplicationEnabledSetting(packageName);
        } catch (Exception e) {
            handleException(e);
            return Integer.MIN_VALUE + PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;
        }
    }

    public void setApplicationEnabledSetting(String packageName, int newState, int flags, boolean tmp) {
        ensureService();
        try {
            mService.setApplicationEnabledSetting(packageName, newState, flags, tmp);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void addPendingDisableApps(String pkg) {
        ensureService();
        try {
            mService.addPendingDisableApps(pkg);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void addPendingDisableAppsTR(String pkg) {
        ensureService();
        try {
            mService.addPendingDisableAppsTR(pkg);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void watch(IAshmanWatcher w) {
        ensureService();
        try {
            mService.watch(w);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void unWatch(IAshmanWatcher w) {
        ensureService();
        try {
            mService.unWatch(w);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void setNetworkPolicyUidPolicy(int uid, int policy) {
        ensureService();
        try {
            mService.setNetworkPolicyUidPolicy(uid, policy);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void restart() {
        ensureService();
        try {
            mService.restart();
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void setCompSettingBlockEnabled(boolean enabled) {
        ensureService();
        try {
            mService.setCompSettingBlockEnabled(enabled);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public boolean isCompSettingBlockEnabledEnabled() {
        ensureService();
        try {
            return mService.isCompSettingBlockEnabledEnabled();
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public String[] getWhiteListApps(int filterOptions) {
        ensureService();
        try {
            return mService.getWhiteListApps(filterOptions);
        } catch (Exception e) {
            handleException(e);
            return new String[0];
        }
    }

    public String[] getBootBlockApps(boolean block) {
        ensureService();
        try {
            return mService.getBootBlockApps(block);
        } catch (Exception e) {
            handleException(e);
            return new String[0];
        }
    }

    public void addOrRemoveBootBlockApps(String[] packages, int op) {
        ensureService();
        try {
            mService.addOrRemoveBootBlockApps(packages, op);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public String[] getStartBlockApps(boolean block) {
        ensureService();
        try {
            return mService.getStartBlockApps(block);
        } catch (Exception e) {
            handleException(e);
            return new String[0];
        }
    }

    public void addOrRemoveStartBlockApps(String[] packages, int op) {
        ensureService();
        try {
            mService.addOrRemoveStartBlockApps(packages, op);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public String[] getLKApps(boolean kill) {
        ensureService();
        try {
            return mService.getLKApps(kill);
        } catch (Exception e) {
            handleException(e);
            return new String[0];
        }
    }

    public void addOrRemoveLKApps(String[] packages, int op) {
        ensureService();
        try {
            mService.addOrRemoveLKApps(packages, op);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public String[] getRFKApps(boolean kill) {
        ensureService();
        try {
            return mService.getRFKApps(kill);
        } catch (Exception e) {
            handleException(e);
            return new String[0];
        }
    }

    public void addOrRemoveRFKApps(String[] packages, int op) {
        ensureService();
        try {
            mService.addOrRemoveRFKApps(packages, op);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void setWhiteSysAppEnabled(boolean enabled) {
        ensureService();
        try {
            mService.setWhiteSysAppEnabled(enabled);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public boolean isWhiteSysAppEnabled() {
        ensureService();
        try {
            return mService.isWhiteSysAppEnabled();
        } catch (Exception e) {
            handleException(e);
            return true;
        }
    }

    public void unInstallPackage(String pkg, IPackageUninstallCallback callback) {
        ensureService();
        try {
            mService.unInstallPackage(pkg, callback);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void restrictAppOnData(int uid, boolean restrict) {
        ensureService();
        try {
            mService.restrictAppOnData(uid, restrict);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void restrictAppOnWifi(int uid, boolean restrict) {
        ensureService();
        try {
            mService.restrictAppOnWifi(uid, restrict);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public boolean isRestrictOnData(int uid) {
        ensureService();
        try {
            return mService.isRestrictOnData(uid);
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public boolean isRestrictOnWifi(int uid) {
        ensureService();
        try {
            return mService.isRestrictOnWifi(uid);
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public boolean isLockKillDoNotKillAudioEnabled() {
        ensureService();
        try {
            return mService.isLockKillDoNotKillAudioEnabled();
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public void setLockKillDoNotKillAudioEnabled(boolean enabled) {
        ensureService();
        try {
            mService.setLockKillDoNotKillAudioEnabled(enabled);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public int getControlMode() {
        try {
            return mService.getControlMode();
        } catch (Exception e) {
            handleException(e);
            return ControlMode.UNKNOWN;
        }
    }

    public void setControlMode(int mode) {
        ensureService();
        try {
            mService.setControlMode(mode);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public String getBuildSerial() {
        ensureService();
        try {
            return mService.getBuildSerial();
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    public String getBuildVersionName() {
        ensureService();
        try {
            return mService.getBuildVersionName();
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    public int getBuildVersionCode() {
        ensureService();
        try {
            return mService.getBuildVersionCode();
        } catch (Exception e) {
            handleException(e);
            return 0;
        }
    }

    public boolean isAutoAddBlackEnabled() {
        ensureService();
        try {
            return mService.isAutoAddBlackEnabled();
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public void setAutoAddBlackEnable(boolean enable) {
        ensureService();
        try {
            mService.setAutoAddBlackEnable(enable);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void forceReloadPackages() {
        ensureService();
        try {
            mService.forceReloadPackages();
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void setPermissionControlEnabled(boolean enabled) {
        ensureService();
        try {
            mService.setPermissionControlEnabled(enabled);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public boolean isPermissionControlEnabled() {
        ensureService();
        try {
            return mService.isPermissionControlEnabled();
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public int getPermissionControlBlockModeForPkg(int code, String pkg, boolean log, String[] payload) {
        ensureService();
        try {
            return mService.getPermissionControlBlockModeForPkg(code, pkg, log, payload);
        } catch (Exception e) {
            handleException(e);
            return XAppOpsManager.MODE_ALLOWED;
        }
    }

    public int getPermissionControlBlockModeForUid(int code, int uid, boolean log, String[] payload) {
        ensureService();
        try {
            return mService.getPermissionControlBlockModeForUid(code, uid, log, payload);
        } catch (Exception e) {
            handleException(e);
            return XAppOpsManager.MODE_ALLOWED;
        }
    }

    public int getPermissionControlBlockModeForPkg(int code, String pkg, boolean log) {
        ensureService();
        try {
            return mService.getPermissionControlBlockModeForPkg(code, pkg, log, null);
        } catch (Exception e) {
            handleException(e);
            return XAppOpsManager.MODE_ALLOWED;
        }
    }

    public int getPermissionControlBlockModeForUid(int code, int uid, boolean log) {
        ensureService();
        try {
            return mService.getPermissionControlBlockModeForUid(code, uid, log, null);
        } catch (Exception e) {
            handleException(e);
            return XAppOpsManager.MODE_ALLOWED;
        }
    }

    public void setPermissionControlBlockModeForPkg(int code, String pkg, int mode) {
        ensureService();
        try {
            mService.setPermissionControlBlockModeForPkg(code, pkg, mode);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void setUserDefinedAndroidId(String id) {
        ensureService();
        try {
            mService.setUserDefinedAndroidId(id);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void setUserDefinedDeviceId(String id) {
        ensureService();
        try {
            mService.setUserDefinedDeviceId(id);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void setUserDefinedLine1Number(String id) {
        ensureService();
        try {
            mService.setUserDefinedLine1Number(id);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public String getAndroidId() {
        ensureService();
        try {
            return mService.getAndroidId();
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    public String getDeviceId() {
        ensureService();
        try {
            return mService.getDeviceId();
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    public String getLine1Number() {
        ensureService();
        try {
            return mService.getLine1Number();
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    public String getUserDefinedLine1Number() {
        ensureService();
        try {
            return mService.getUserDefinedLine1Number();
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    public String getUserDefinedDeviceId() {
        ensureService();
        try {
            return mService.getUserDefinedDeviceId();
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    public String getUserDefinedAndroidId() {
        ensureService();
        try {
            return mService.getUserDefinedAndroidId();
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    public boolean showFocusedActivityInfoEnabled() {
        ensureService();
        try {
            return mService.showFocusedActivityInfoEnabled();
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public void setShowFocusedActivityInfoEnabled(boolean enabled) {
        ensureService();
        try {
            mService.setShowFocusedActivityInfoEnabled(enabled);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void setPrivacyEnabled(boolean enable) {
        ensureService();
        try {
            mService.setPrivacyEnabled(enable);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public boolean isPrivacyEnabled() {
        ensureService();
        try {
            return mService.isPrivacyEnabled();
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public boolean isPackageInPrivacyList(String pkg) {
        ensureService();
        try {
            return mService.isPackageInPrivacyList(pkg);
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public boolean isUidInPrivacyList(int uid) {
        ensureService();
        try {
            return mService.isUidInPrivacyList(uid);
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public String[] getPrivacyList(boolean priv) {
        ensureService();
        try {
            return mService.getPrivacyList(priv);
        } catch (Exception e) {
            handleException(e);
            return new String[0];
        }
    }

    public int getPrivacyAppsCount() {
        try {
            return mService.getPrivacyAppsCount();
        } catch (Exception e) {
            handleException(e);
            return 0;
        }
    }

    public void addOrRemoveFromPrivacyList(String pkg, int op) {
        ensureService();
        try {
            mService.addOrRemoveFromPrivacyList(pkg, op);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public String[] getGreeningApps(boolean greening) {
        ensureService();
        try {
            return mService.getGreeningApps(greening);
        } catch (Exception e) {
            handleException(e);
            return new String[0];
        }
    }

    public void addOrRemoveGreeningApps(String[] packages, int op) {
        ensureService();
        try {
            mService.addOrRemoveGreeningApps(packages, op);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void setGreeningEnabled(boolean enabled) {
        ensureService();
        try {
            mService.setGreeningEnabled(enabled);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public boolean isGreeningEnabled() {
        ensureService();
        try {
            return mService.isGreeningEnabled();
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public boolean isPackageGreening(String packageName) {
        ensureService();
        if (BuildConfig.DEBUG) try {
            return mService.isPackageGreening(packageName);
        } catch (Exception e) {
            handleException(e);
            return false;
        }
        return false;
    }

    public boolean isUidGreening(int uid) {
        ensureService();
        try {
            return mService.isUidGreening(uid);
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public String[] getInstalledApps(int filterOptions) {
        ensureService();
        try {
            return mService.getInstalledApps(filterOptions);
        } catch (Exception e) {
            handleException(e);
            return new String[0];
        }
    }

    public void restoreDefaultSettings() {
        ensureService();
        try {
            mService.restoreDefaultSettings();
        } catch (Exception e) {
            handleException(e);
        }
    }

    public List<ActivityManager.RunningServiceInfo> getRunningServices(int max) {
        ensureService();
        try {
            return mService.getRunningServices(max);
        } catch (Exception e) {
            handleException(e);
            return new ArrayList<>(0);
        }
    }

    public List<ActivityManager.RunningAppProcessInfo> getRunningAppProcesses() {
        try {
            return mService.getRunningAppProcesses();
        } catch (Exception e) {
            handleException(e);
            return new ArrayList<>(0);
        }
    }

    public void writeSystemSettings(String key, String value) {
        ensureService();
        try {
            mService.writeSystemSettings(key, value);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public String getSystemSettings(String key) {
        ensureService();
        try {
            return mService.getSystemSettings(key);
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    public long[] getProcessPss(int[] pids) {
        ensureService();
        try {
            return mService.getProcessPss(pids);
        } catch (Exception e) {
            handleException(e);
            return new long[0];
        }
    }

    public boolean onApplicationUncaughtException(String packageName, String thread, String exception, String trace) {
        ensureService();
        try {
            return mService.onApplicationUncaughtException(packageName, thread, exception, trace);
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public boolean isAppCrashDumpEnabled() {
        ensureService();
        try {
            return mService.isAppCrashDumpEnabled();
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public boolean isRedemptionModeEnabled() {
        ensureService();
        try {
            return mService.isRedemptionModeEnabled();
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public void setRedemptionModeEnabled(boolean enabled) {
        ensureService();
        try {
            mService.setRedemptionModeEnabled(enabled);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void setAppCrashDumpEnabled(boolean enabled) {
        ensureService();
        try {
            mService.setAppCrashDumpEnabled(enabled);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void registerOnTopPackageChangeListener(ITopPackageChangeListener listener) {
        ensureService();
        try {
            mService.registerOnTopPackageChangeListener(listener);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void unRegisterOnTopPackageChangeListener(ITopPackageChangeListener listener) {
        ensureService();
        try {
            mService.unRegisterOnTopPackageChangeListener(listener);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public boolean isLazyModeEnabled() {
        ensureService();
        try {
            return mService.isLazyModeEnabled();
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public boolean isLazyModeEnabledForPackage(String pkg) {
        ensureService();
        try {
            return mService.isLazyModeEnabledForPackage(pkg);
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public boolean hasNotificationForPackage(String pkg) {
        ensureService();
        try {
            return mService.hasNotificationForPackage(pkg);
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public void setLazyModeEnabled(boolean enabled) {
        ensureService();
        try {
            mService.setLazyModeEnabled(enabled);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public String[] getLazyApps(boolean lazy) {
        ensureService();
        try {
            return mService.getLazyApps(lazy);
        } catch (Exception e) {
            handleException(e);
            return new String[0];
        }
    }

    public void addOrRemoveLazyApps(String[] packages, int op) {
        ensureService();
        try {
            mService.addOrRemoveLazyApps(packages, op);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void setLPBKEnabled(boolean enabled) {
        ensureService();
        try {
            mService.setLPBKEnabled(enabled);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public boolean isLPBKEnabled() {
        ensureService();
        try {
            return mService.isLPBKEnabled();
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public void onTaskRemoving(int callingUid, int taskId) {
        ensureService();
        try {
            mService.onTaskRemoving(callingUid, taskId);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void addOrRemoveActivityFocusAction(ComponentName comp, String[] actions, boolean add) {
        ensureService();
        try {
            mService.addOrRemoveActivityFocusAction(comp, actions, add);
        } catch (RemoteException e) {
            handleException(e);
        }
    }

    public ComponentName[] getActivityFocusActionComponents() {
        ensureService();
        try {
            return mService.getActivityFocusActionComponents();
        } catch (RemoteException e) {
            handleException(e);
            return new ComponentName[0];
        }
    }

    public String[] getActivityFocusActions(ComponentName comp) {
        ensureService();
        try {
            return mService.getActivityFocusActions(comp);
        } catch (RemoteException e) {
            handleException(e);
            return new String[0];
        }
    }

    public void addOrRemoveActivityUnFocusAction(ComponentName comp, String[] actions, boolean add) {
        ensureService();
        try {
            mService.addOrRemoveActivityUnFocusAction(comp, actions, add);
        } catch (RemoteException e) {
            handleException(e);
        }
    }

    public ComponentName[] getActivityUnFocusActionComponents() {
        ensureService();
        try {
            return mService.getActivityUnFocusActionComponents();
        } catch (RemoteException e) {
            handleException(e);
            return new ComponentName[0];
        }
    }

    public String[] getActivityUnFocusActions(ComponentName comp) {
        ensureService();
        try {
            return mService.getActivityUnFocusActions(comp);
        } catch (RemoteException e) {
            handleException(e);
            return new String[0];
        }
    }

    public long getLastDozeEnterTimeMills() {
        ensureService();
        try {
            return mService.getLastDozeEnterTimeMills();
        } catch (Exception e) {
            handleException(e);
            return -1;
        }
    }

    // Nullable.
    public DozeEvent getLastDozeEvent() {
        ensureService();
        try {
            return mService.getLastDozeEvent();
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    public void setDozeEnabled(boolean enable) {
        ensureService();
        try {
            mService.setDozeEnabled(enable);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public boolean isDozeEnabled() {
        ensureService();
        try {
            return mService.isDozeEnabled();
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public long getDozeDelayMills() {
        ensureService();
        try {
            return mService.getDozeDelayMills();
        } catch (Exception e) {
            handleException(e);
            return -1; // FIXME Make a meaningful res.
        }
    }

    public void setDozeDelayMills(long delayMills) {
        ensureService();
        try {
            mService.setDozeDelayMills(delayMills);
        } catch (Exception e) {
            handleException(e);

        }
    }

    public void setDoNotKillSBNEnabled(boolean enable, String module) {
        ensureService();
        try {
            mService.setDoNotKillSBNEnabled(enable, module);
        } catch (Exception e) {
            handleException(e);

        }
    }

    public boolean isDoNotKillSBNEnabled(String module) {
        ensureService();
        try {
            return mService.isDoNotKillSBNEnabled(module);
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public void setTaskRemoveKillEnabled(boolean enable) {
        ensureService();
        try {
            mService.setTaskRemoveKillEnabled(enable);
        } catch (Exception e) {
            handleException(e);

        }
    }

    public boolean isTaskRemoveKillEnabled() {
        ensureService();
        try {
            return mService.isTaskRemoveKillEnabled();
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public String[] getTRKApps(boolean kill) {
        ensureService();
        try {
            return mService.getTRKApps(kill);
        } catch (Exception e) {
            handleException(e);
            return new String[0];
        }
    }

    public void addOrRemoveTRKApps(String[] packages, int op) {
        ensureService();
        try {
            mService.addOrRemoveTRKApps(packages, op);
        } catch (Exception e) {
            handleException(e);

        }
    }

    public List<DozeEvent> getDozeEventHistory() {
        ensureService();
        try {
            return mService.getDozeEventHistory();
        } catch (Exception e) {
            handleException(e);
            return new ArrayList<>(0);
        }
    }

    public void setForceDozeEnabled(boolean enable) {
        ensureService();
        try {
            mService.setForceDozeEnabled(enable);
        } catch (Exception e) {
            handleException(e);

        }
    }

    public boolean isForceDozeEnabled() {
        ensureService();
        try {
            return mService.isForceDozeEnabled();
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public AppSettings retrieveAppSettingsForPackage(String pkg) {
        ensureService();
        try {
            return mService.retrieveAppSettingsForPackage(pkg);
        } catch (Exception e) {
            handleException(e);
            return AppSettings.builder().pkgName(pkg).appName("NULL").build();
        }
    }

    public void applyAppSettingsForPackage(String pkg, AppSettings settings) {
        ensureService();
        try {
            mService.applyAppSettingsForPackage(pkg, settings);
        } catch (Exception e) {
            handleException(e);

        }
    }

    public void backupTo(String dir) {
        ensureService();
        try {
            mService.backupTo(dir);
        } catch (Exception e) {
            handleException(e);

        }
    }

    public void restoreFrom(String dir) {
        ensureService();
        try {
            mService.restoreFrom(dir);
        } catch (Exception e) {
            handleException(e);

        }
    }

    public String[] getRawPermSettings(int page, int countInPage) {
        ensureService();
        try {
            return mService.getRawPermSettings(page, countInPage);
        } catch (Exception e) {
            handleException(e);
            return new String[0];
        }
    }

    public void setAppInstalledAutoApplyTemplate(AppSettings settings) {
        ensureService();
        try {
            mService.setAppInstalledAutoApplyTemplate(settings);
        } catch (Exception e) {
            handleException(e);

        }
    }

    public AppSettings getAppInstalledAutoApplyTemplate() {
        ensureService();
        try {
            return mService.getAppInstalledAutoApplyTemplate();
        } catch (Exception e) {
            handleException(e);
            return AppSettings.fromJson("XXX");
        }
    }

    public List<OpLog> getOpLogForPackage(String packageName) {
        ensureService();
        try {
            return mService.getOpLogForPackage(packageName);
        } catch (Exception e) {
            handleException(e);
            return new ArrayList<>(0);
        }
    }

    public List<OpLog> getOpLogForOp(int code) {
        ensureService();
        try {
            return mService.getOpLogForOp(code);
        } catch (Exception e) {
            handleException(e);
            return new ArrayList<>(0);
        }
    }

    public void clearOpLogForPackage(String packageName) {
        ensureService();
        try {
            mService.clearOpLogForPackage(packageName);
        } catch (Exception e) {
            handleException(e);

        }
    }

    public void clearOpLogForOp(int cod) {
        ensureService();
        try {
            mService.clearOpLogForOp(cod);
        } catch (Exception e) {
            handleException(e);

        }
    }

    public String getUserName() {
        ensureService();
        try {
            return mService.getUserName();
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    public Bitmap getUserIcon() {
        ensureService();
        try {
            return mService.getUserIcon();
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    public void addPowerSaveWhitelistApp(String pkg) {
        ensureService();
        try {
            mService.addPowerSaveWhitelistApp(pkg);
        } catch (Exception e) {
            handleException(e);

        }
    }

    public void removePowerSaveWhitelistApp(String pkg) {
        ensureService();
        try {
            mService.removePowerSaveWhitelistApp(pkg);
        } catch (Exception e) {
            handleException(e);

        }
    }

    public String[] getFullPowerWhitelist() {
        ensureService();
        try {
            return mService.getFullPowerWhitelist();
        } catch (Exception e) {
            handleException(e);
            return ArrayUtil.newEmptyStringArray();
        }
    }

    public String[] getSystemPowerWhitelist() {
        ensureService();
        try {
            return mService.getSystemPowerWhitelist();
        } catch (Exception e) {
            handleException(e);
            return ArrayUtil.newEmptyStringArray();
        }
    }

    public String[] getUserPowerWhitelist() {
        ensureService();
        try {
            return mService.getUserPowerWhitelist();
        } catch (Exception e) {
            handleException(e);
            return ArrayUtil.newEmptyStringArray();
        }
    }

    public ActivityManager.MemoryInfo getMemoryInfo() {
        ensureService();
        try {
            return mService.getMemoryInfo();
        } catch (Exception e) {
            handleException(e);
            return new ActivityManager.MemoryInfo();
        }
    }

    public void enableKeyguard(boolean enabled) {
        ensureService();
        try {
            mService.enableKeyguard(enabled);
        } catch (Exception e) {
            handleException(e);

        }
    }

    public void exitKeyguardSecurely(IBooleanCallback1 result) {
        ensureService();
        try {
            mService.exitKeyguardSecurely(result);
        } catch (Exception e) {
            handleException(e);

        }
    }

    public void dismissKeyguardLw() {
        ensureService();
        try {
            mService.dismissKeyguardLw();
        } catch (Exception e) {
            handleException(e);

        }
    }

    public boolean isKeyguardLocked() {
        ensureService();
        try {
            return mService.isKeyguardLocked();
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public int getRunningProcessCount() {
        ensureService();
        try {
            return mService.getRunningProcessCount();
        } catch (Exception e) {
            handleException(e);
            return 0;
        }
    }

    public boolean hasModuleError() {
        ensureService();
        try {
            return mService.hasModuleError();
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public void setResidentEnabled(boolean enable) {
        ensureService();
        try {
            mService.setResidentEnabled(enable);
        } catch (Exception e) {
            handleException(e);

        }
    }

    public boolean isResidentEnabled() {
        ensureService();
        try {
            return mService.isResidentEnabled();
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public boolean isResidentEnabledForPackage(String who) {
        try {
            return mService.isResidentEnabledForPackage(who);
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public void addOrRemoveResidentApps(String app, boolean add) {
        ensureService();
        try {
            mService.addOrRemoveResidentApps(app, add);
        } catch (Exception e) {
            handleException(e);

        }
    }

    public String[] getResidentApps(boolean resident) {
        ensureService();
        try {
            return mService.getResidentApps(resident);
        } catch (Exception e) {
            handleException(e);
            return ArrayUtil.emptyStringArray();
        }
    }

    public boolean isPanicHomeEnabled() {
        ensureService();
        try {
            return mService.isPanicHomeEnabled();
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public void setPanicHomeEnabled(boolean enable) {
        ensureService();
        try {
            mService.setPanicHomeEnabled(enable);
        } catch (Exception e) {
            handleException(e);

        }
    }

    public boolean isPanicLockEnabled() {
        ensureService();
        try {
            return mService.isPanicLockEnabled();
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public void setPanicLockEnabled(boolean enable) {
        ensureService();
        try {
            mService.setPanicLockEnabled(enable);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void lockNow() {
        ensureService();
        try {
            mService.lockNow();
        } catch (Exception e) {
            handleException(e);

        }
    }

    public boolean isInRedemptionMode() {
        ensureService();
        try {
            return mService.isInRedemptionMode();
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public void leaveRedemptionMode() {
        ensureService();
        try {
            mService.leaveRedemptionMode();
        } catch (Exception e) {
            handleException(e);

        }
    }

    public void enterRedemptionMode() {
        ensureService();
        try {
            mService.enterRedemptionMode();
        } catch (Exception e) {
            handleException(e);

        }
    }

    public boolean isSELinuxEnabled() {
        ensureService();
        try {
            return mService.isSELinuxEnabled();
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public boolean isSELinuxEnforced() {
        ensureService();
        try {
            return mService.isSELinuxEnforced();
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public void setSelinuxEnforce(boolean enforce) {
        ensureService();
        try {
            mService.setSelinuxEnforce(enforce);
        } catch (Exception e) {
            handleException(e);

        }
    }

    public boolean isPowerSaveModeEnabled() {
        ensureService();
        try {
            return mService.isPowerSaveModeEnabled();
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public void setPowerSaveModeEnabled(boolean enable) {
        ensureService();
        try {
            mService.setPowerSaveModeEnabled(enable);
        } catch (Exception e) {
            handleException(e);

        }
    }

    public String[] getStartRules() {
        ensureService();
        try {
            return mService.getStartRules();
        } catch (Exception e) {
            handleException(e);
            return ArrayUtil.newEmptyStringArray();
        }
    }

    public boolean addOrRemoveStartRules(String rule, boolean add) {
        ensureService();
        try {
            return mService.addOrRemoveStartRules(rule, add);
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public boolean addOrRemoveLazyRules(String rule, boolean add) {
        ensureService();
        try {
            return mService.addOrRemoveLazyRules(rule, add);
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public String[] getLazyRules() {
        ensureService();
        try {
            return mService.getLazyRules();
        } catch (Exception e) {
            handleException(e);
            return ArrayUtil.newEmptyStringArray();
        }
    }

    public boolean isLazyRuleEnabled() {
        ensureService();
        try {
            return mService.isLazyRuleEnabled();
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public void setLazyRuleEnabled(boolean enable) {
        ensureService();
        try {
            mService.setLazyRuleEnabled(enable);
        } catch (Exception e) {
            handleException(e);

        }
    }

    public boolean hasSystemError() {
        ensureService();
        try {
            return mService.hasSystemError();
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public void cleanUpSystemErrorTraces() {
        ensureService();
        try {
            mService.cleanUpSystemErrorTraces();
        } catch (Exception e) {
            handleException(e);

        }
    }

    public void addAppLockWhiteListActivity(String[] activities) {
        ensureService();
        try {
            mService.addAppLockWhiteListActivity(activities);
        } catch (Exception e) {
            handleException(e);

        }
    }

    public boolean isAutoAddBlackNotificationEnabled() {
        ensureService();
        try {
            return mService.isAutoAddBlackNotificationEnabled();
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public void setAutoAddBlackNotificationEnabled(boolean value) {
        ensureService();
        try {
            mService.setAutoAddBlackNotificationEnabled(value);
        } catch (Exception e) {
            handleException(e);

        }
    }

    public boolean isOptFeatureEnabled(String tag) {
        ensureService();
        try {
            return mService.isOptFeatureEnabled(tag);
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public void setOptFeatureEnabled(String tag, boolean enable) {
        ensureService();
        try {
            mService.setOptFeatureEnabled(tag, enable);
        } catch (Exception e) {
            handleException(e);

        }
    }

    public int getRecentTaskExcludeSetting(ComponentName c) {
        ensureService();
        try {
            return mService.getRecentTaskExcludeSetting(c);
        } catch (Exception e) {
            handleException(e);
            return ExcludeRecentSetting.NONE;
        }
    }

    public void setRecentTaskExcludeSetting(ComponentName c, int setting) {
        ensureService();
        try {
            mService.setRecentTaskExcludeSetting(c, setting);
        } catch (Exception e) {
            handleException(e);

        }
    }

    public int getAppConfigOverlayIntSetting(String appPackageName, String tag) {
        ensureService();
        try {
            return mService.getAppConfigOverlayIntSetting(appPackageName, tag);
        } catch (Exception e) {
            handleException(e);
            return ConfigOverlays.NONE;
        }
    }

    public void setAppConfigOverlayIntSetting(String appPackageName, String tag, int value) {
        ensureService();
        try {
            mService.setAppConfigOverlayIntSetting(appPackageName, tag, value);
        } catch (Exception e) {
            handleException(e);

        }
    }

    public void injectPowerEvent() {
        ensureService();
        try {
            mService.injectPowerEvent();
        } catch (Exception e) {
            handleException(e);

        }
    }

    public String getServiceStarter(ComponentName service) {
        ensureService();
        try {
            return mService.getServiceStarter(service);
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    public boolean isInactiveAppInsteadOfKillPreferred() {
        ensureService();
        try {
            return mService.isInactiveAppInsteadOfKillPreferred();
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public void setInactiveAppInsteadOfKillPreferred(boolean prefer) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
            return;
        }
        ensureService();
        try {
            mService.setInactiveAppInsteadOfKillPreferred(prefer);
        } catch (Exception e) {
            handleException(e);

        }
    }

    public void mockSystemDead(long delay) {
        ensureService();
        try {
            mService.mockSystemDead(delay);
        } catch (Exception e) {
            handleException(e);

        }
    }

    public void clearModuleSettings(String moduleVar) {
        ensureService();
        try {
            mService.clearModuleSettings(moduleVar);
        } catch (Exception e) {
            handleException(e);

        }
    }

    public boolean isDisableMotionEnabled() {
        ensureService();
        try {
            return mService.isDisableMotionEnabled();
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public void setDisableMotionEnabled(boolean enable) {
        ensureService();
        try {
            mService.setDisableMotionEnabled(enable);
        } catch (Exception e) {
            handleException(e);

        }
    }

    public boolean isGCMSupportPackage(String pkg) {
        ensureService();
        try {
            return mService.isGCMSupportPackage(pkg);
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public boolean isMiPushSupportPackage(String pkg) {
        ensureService();
        try {
            return mService.isMiPushSupportPackage(pkg);
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public boolean isShowAppProcessUpdateNotificationEnabled() {
        ensureService();
        try {
            return mService.isShowAppProcessUpdateNotificationEnabled();
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public void setShowAppProcessUpdateNotificationEnabled(boolean enabled) {
        ensureService();
        try {
            mService.setShowAppProcessUpdateNotificationEnabled(enabled);
        } catch (Exception e) {
            handleException(e);

        }
    }

    public boolean isStartRuleEnabled() {
        ensureService();
        try {
            return mService.isStartRuleEnabled();
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public void setStartRuleEnabled(boolean enabled) {
        ensureService();
        try {
            mService.setStartRuleEnabled(enabled);
        } catch (Exception e) {
            handleException(e);

        }
    }

    public boolean isPushMessageHandlerEnabled(String pkg) {
        ensureService();
        try {
            return mService.isPushMessageHandlerEnabled(pkg);
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public void setPushMessageHandlerEnabled(String pkg, boolean enabled) {
        ensureService();
        try {
            mService.setPushMessageHandlerEnabled(pkg, enabled);
        } catch (Exception e) {
            handleException(e);

        }
    }

    public boolean isPushMessageHandlerShowContentEnabled(String pkg) {
        ensureService();
        try {
            return mService.isPushMessageHandlerShowContentEnabled(pkg);
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public void setPushMessageHandlerShowContentEnabled(String pkg, boolean enabled) {
        ensureService();
        try {
            mService.setPushMessageHandlerShowContentEnabled(pkg, enabled);
        } catch (Exception e) {
            handleException(e);

        }
    }

    public boolean isPushMessageHandleEnabled() {
        ensureService();
        try {
            return mService.isPushMessageHandleEnabled();
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public void setPushMessageHandleEnabled(boolean enabled) {
        ensureService();
        try {
            mService.setPushMessageHandleEnabled(enabled);
        } catch (Exception e) {
            handleException(e);

        }
    }

    public boolean isPushMessageHandlerNotificationSoundEnabled(String pkg) {
        ensureService();
        try {
            return mService.isPushMessageHandlerNotificationSoundEnabled(pkg);
        } catch (Exception e) {
            handleException(e);
            return true;
        }
    }

    public void setPushMessageHandlerNotificationSoundEnabled(String pkg, boolean enabled) {
        ensureService();
        try {
            mService.setPushMessageHandlerNotificationSoundEnabled(pkg, enabled);
        } catch (Exception e) {
            handleException(e);

        }
    }

    public boolean isPushMessageHandlerNotificationVibrateEnabled(String pkg) {
        ensureService();
        try {
            return mService.isPushMessageHandlerNotificationVibrateEnabled(pkg);
        } catch (Exception e) {
            handleException(e);
            return true;
        }
    }

    public void setPushMessageHandlerNotificationVibrateEnabled(String pkg, boolean enabled) {
        ensureService();
        try {
            mService.setPushMessageHandlerNotificationVibrateEnabled(pkg, enabled);
        } catch (Exception e) {
            handleException(e);

        }
    }

    public boolean isPushMessageHandlerMessageNotificationByAppEnabled(String pkg) {
        ensureService();
        try {
            return mService.isPushMessageHandlerMessageNotificationByAppEnabled(pkg);
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public void setPushMessageHandlerMessageNotificationByAppEnabled(String pkg, boolean enabled) {
        ensureService();
        try {
            mService.setPushMessageHandlerMessageNotificationByAppEnabled(pkg, enabled);
        } catch (Exception e) {
            handleException(e);

        }
    }

    public boolean isHandlingPushMessageIntent(String packageName) {
        ensureService();
        try {
            return mService.isHandlingPushMessageIntent(packageName);
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public boolean showToast(String message) {
        ensureService();
        try {
            return mService.showToast(message);
        } catch (Exception e) {
            handleException(e);
            return true;
        }
    }

    public List<BlockRecord2> getStartRecordsForPackage(String pkg) {
        ensureService();
        try {
            return mService.getStartRecordsForPackage(pkg);
        } catch (Exception e) {
            handleException(e);
            return new ArrayList<>(0);
        }
    }

    public void clearStartRecordsForPackage(String pkg) {
        ensureService();
        try {
            mService.clearStartRecordsForPackage(pkg);
        } catch (Exception e) {
            handleException(e);

        }
    }

    public boolean isWakeupOnNotificationEnabled() {
        ensureService();
        try {
            return mService.isWakeupOnNotificationEnabled();
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public void setWakeupOnNotificationEnabled(boolean enable) {
        ensureService();
        try {
            mService.setWakeupOnNotificationEnabled(enable);
        } catch (Exception e) {
            handleException(e);

        }
    }

    public void mockPushMessageReceived(String pkg, String message) {
        ensureService();
        try {
            mService.mockPushMessageReceived(pkg, message);
        } catch (Exception e) {
            handleException(e);

        }
    }

    public void registerController(IServiceControl control) {
        ensureService();
        try {
            mService.registerController(control);
        } catch (Exception e) {
            handleException(e);

        }
    }

    public void unRegisterController(IServiceControl control) {
        ensureService();
        try {
            mService.unRegisterController(control);
        } catch (Exception e) {
            handleException(e);

        }
    }

    public void setAppServiceLazyControlSolution(int solutionFlags, boolean enable) {
        ensureService();
        try {
            mService.setAppServiceLazyControlSolution(solutionFlags, enable);
        } catch (Exception e) {
            handleException(e);

        }
    }

    public boolean isAppServiceLazyControlSolutionEnable(int solutionFlags) {
        ensureService();
        try {
            return mService.isAppServiceLazyControlSolutionEnable(solutionFlags);
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public void forceIdlePackages(String[] packages) {
        ensureService();
        try {
            mService.forceIdlePackages(packages);
        } catch (Exception e) {
            handleException(e);

        }
    }

    public void stopService(Intent serviceIntent) {
        ensureService();
        try {
            mService.stopService(serviceIntent);
        } catch (Exception e) {
            handleException(e);

        }
    }

    public boolean isSystemPropEnabled() {
        ensureService();
        try {
            return mService.isSystemPropEnabled();
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public void setSystemPropEnabled(boolean enabled) {
        ensureService();
        try {
            mService.setSystemPropEnabled(enabled);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void addOrRemoveSystemPropProfile(SystemPropProfile profile, boolean add) {
        ensureService();
        try {
            mService.addOrRemoveSystemPropProfile(profile, add);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public Map getSystemPropProfiles() {
        ensureService();
        try {
            return mService.getSystemPropProfiles();
        } catch (Exception e) {
            handleException(e);
            return new HashMap(0);
        }
    }

    public void setActiveSystemPropProfileId(String profileId) {
        ensureService();
        try {
            mService.setActiveSystemPropProfileId(profileId);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public String getActiveSystemPropProfileId() {
        ensureService();
        try {
            return mService.getActiveSystemPropProfileId();
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    public SystemPropProfile getActiveSystemPropProfile() {
        ensureService();
        try {
            return mService.getActiveSystemPropProfile();
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    public void addOrRemoveSystemPropProfileApplyApps(String[] pkgs, boolean add) {
        ensureService();
        try {
            mService.addOrRemoveSystemPropProfileApplyApps(pkgs, add);
        } catch (Exception e) {
            handleException(e);

        }
    }

    public String[] getSystemPropProfileApplyApps(boolean apply) {
        ensureService();
        try {
            return mService.getSystemPropProfileApplyApps(apply);
        } catch (Exception e) {
            handleException(e);
            return ArrayUtil.emptyStringArray();
        }
    }

    public boolean isSystemPropProfileApplyApp(String packageName) {
        ensureService();
        try {
            return mService.isSystemPropProfileApplyApp(packageName);
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public boolean isPackageInstallVerifyEnabled() {
        ensureService();
        try {
            return mService.isPackageInstallVerifyEnabled();
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public void setPackageInstallVerifyEnabled(boolean enabled) {
        ensureService();
        try {
            mService.setPackageInstallVerifyEnabled(enabled);
        } catch (Exception e) {
            handleException(e);

        }
    }

    public String[] getPackageInstallerVerifyRules() {
        ensureService();
        try {
            return mService.getPackageInstallerVerifyRules();
        } catch (Exception e) {
            handleException(e);
            return ArrayUtil.emptyStringArray();
        }
    }

    public boolean addOrRemovePackageInstallerVerifyRules(String rule, boolean add) {
        ensureService();
        try {
            return mService.addOrRemovePackageInstallerVerifyRules(rule, add);
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public void onSourceApkFileDetected(String path, String pkg) {
        ensureService();
        try {
            mService.onSourceApkFileDetected(path, pkg);
        } catch (Exception e) {
            handleException(e);

        }
    }

    public String getCurrentTopPackage() {
        ensureService();
        try {
            return mService.getCurrentTopPackage();
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    public void registerTaskRemoveListener(ITaskRemoveListener listener) {
        ensureService();
        try {
            mService.registerTaskRemoveListener(listener);
        } catch (Exception e) {
            handleException(e);

        }
    }

    public void unRegisterTaskRemoveListener(ITaskRemoveListener listener) {
        ensureService();
        try {
            mService.unRegisterTaskRemoveListener(listener);
        } catch (Exception e) {
            handleException(e);

        }
    }

    public void setAppInactive(String packageName, boolean inactive, int userId) {
        ensureService();
        try {
            mService.setAppInactive(packageName, inactive, userId);
        } catch (Exception e) {
            handleException(e);

        }
    }

    public boolean isAppInactive(String packageName, int userId) {
        ensureService();
        try {
            return mService.isAppInactive(packageName, userId);
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    public void forceStopPackage(String packageName) {
        ensureService();
        try {
            mService.forceStopPackage(packageName);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void killBackgroundProcesses(String packageName) {
        ensureService();
        try {
            mService.killBackgroundProcesses(packageName);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void setAppInactivePolicyForModule(String module, int policy) {
        ensureService();
        try {
            mService.setAppInactivePolicyForModule(module, policy);
        } catch (Exception e) {
            handleException(e);

        }
    }

    public int getAppInactivePolicyForModule(String module) {
        ensureService();
        try {
            return mService.getAppInactivePolicyForModule(module);
        } catch (Exception e) {
            handleException(e);
            return AppInactivePolicy.FORCE_STOP;
        }
    }

    public void executeInputCommand(String[] args) {
        ensureService();
        try {
            mService.executeInputCommand(args);
        } catch (Exception e) {
            handleException(e);

        }
    }

    public void takeLongScreenShot() {
        ensureService();
        try {
            mService.takeLongScreenShot();
        } catch (Exception e) {
            handleException(e);

        }
    }

    public IBackupAgent getBackupAgent() {
        ensureService();
        try {
            return mService.getBackupAgent();
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    public void showRebootNeededNotification(String why) {
        ensureService();
        try {
            mService.showRebootNeededNotification(why);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void evaluateJsString(String[] args, IJsEvaluateListener listener) {
        ensureService();
        try {
            mService.evaluateJsString(args, listener);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void evaluateJsString(String[] args) {
        evaluateJsString(args, null);
    }

    public JavaScript getSavedJs(String id) {
        ensureService();
        try {
            return mService.getSavedJs(id);
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    public List<JavaScript> getSavedJses() {
        ensureService();
        try {
            return mService.getSavedJses();
        } catch (Exception e) {
            handleException(e);
            return new ArrayList<>(0);
        }
    }

    public void saveJs(JavaScript js) {
        ensureService();
        try {
            mService.saveJs(js);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void deleteJs(JavaScript js) {
        ensureService();
        try {
            mService.deleteJs(js);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public PackageInfo getPackageInfoForPackage(String pkgName) {
        ensureService();
        try {
            return mService.getPackageInfoForPackage(pkgName);
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    public ApplicationInfo getApplicationInfoForPackage(String pkgName) {
        ensureService();
        try {
            return mService.getApplicationInfoForPackage(pkgName);
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    public String getPackageNameForUid(int uid) {
        ensureService();
        try {
            return mService.getPackageNameForUid(uid);
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    public void addAppOpsTemplate(AppOpsTemplate template) {
        ensureService();
        try {
            mService.addAppOpsTemplate(template);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void removeAppOpsTemplate(AppOpsTemplate template) {
        ensureService();
        try {
            mService.removeAppOpsTemplate(template);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public List<AppOpsTemplate> getAppOpsTemplates() {
        ensureService();
        try {
            return mService.getAppOpsTemplates();
        } catch (Exception e) {
            handleException(e);
            return new ArrayList<>(0);
        }
    }

    public AppOpsTemplate getAppOpsTemplateById(String id) {
        ensureService();
        try {
            return mService.getAppOpsTemplateById(id);
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    public ComponentName componentNameForTaskId(int taskId) {
        ensureService();
        try {
            return mService.componentNameForTaskId(taskId);
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    public void reportBlurBadPerformance(long timeTaken) {
        ensureService();
        try {
            mService.reportBlurBadPerformance(timeTaken);
        } catch (Exception e) {
            handleException(e);
        }
    }

    private static void handleException(Throwable e) {
        if (BuildConfig.DEBUG) {
            //throw new RuntimeException(e);
            Log.e(XposedLog.TAG, Log.getStackTraceString(e));
        } else {
            Log.e(XposedLog.TAG, Log.getStackTraceString(e));
        }
    }

}
