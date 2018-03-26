package github.tornaco.xposedmoduletest.xposed.app;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.support.annotation.Nullable;

import com.google.common.base.Optional;

import org.newstand.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.IAshmanService;
import github.tornaco.xposedmoduletest.IAshmanWatcher;
import github.tornaco.xposedmoduletest.IBooleanCallback1;
import github.tornaco.xposedmoduletest.IPackageUninstallCallback;
import github.tornaco.xposedmoduletest.ITopPackageChangeListener;
import github.tornaco.xposedmoduletest.compat.os.AppOpsManagerCompat;
import github.tornaco.xposedmoduletest.util.ArrayUtil;
import github.tornaco.xposedmoduletest.util.OSUtil;
import github.tornaco.xposedmoduletest.util.Singleton;
import github.tornaco.xposedmoduletest.xposed.bean.AppSettings;
import github.tornaco.xposedmoduletest.xposed.bean.BlockRecord2;
import github.tornaco.xposedmoduletest.xposed.bean.DozeEvent;
import github.tornaco.xposedmoduletest.xposed.bean.OpLog;
import github.tornaco.xposedmoduletest.xposed.bean.OpsSettings;

/**
 * Created by guohao4 on 2017/11/9.
 * Email: Tornaco@163.com
 */

@SuppressWarnings("EmptyCatchBlock")
public class XAshmanManager {

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

    public interface ConfigOverlays {
        int NONE = Integer.MIN_VALUE;
    }

    public static final String SERVICE_NAME =
            OSUtil.isOOrAbove() ? Context.TV_INPUT_SERVICE : "user.tor_ash";

    public static final String VERIFIER_CLASS_NAME = "github.tornaco.xposedmoduletest.ui.activity.ag.VerifyDisplayerActivity";

    private IAshmanService mService;

    private static final Singleton<XAshmanManager> sManager
            = new Singleton<XAshmanManager>() {
        @Override
        protected XAshmanManager create() {
            return new XAshmanManager();
        }
    };

    private XAshmanManager() {
        retrieveService();
    }

    public static XAshmanManager get() {
        return sManager.get();
    }

    public boolean isServiceAvailable() {
        return mService != null;
    }

    public void retrieveService() {
        mService = IAshmanService.Stub.asInterface(ServiceManager.getService(SERVICE_NAME));
    }

    public Optional<XAshmanManager> optional() {
        return Optional.fromNullable(get());
    }

    private void ensureService() {
        if (mService == null) {
            Logger.e("Service is not available");
        }
    }

    public int getAppLevel(String pkg) {
        ensureService();
        try {
            return mService.getAppLevel(pkg);
        } catch (Exception e) {
            return AppLevel.THIRD_PARTY;
        }
    }

    public String packageForTaskId(int taskId) {
        ensureService();
        try {
            return mService.packageForTaskId(taskId);
        } catch (Exception e) {
            return null;
        }
    }

    public void clearProcess(IProcessClearListenerAdapter adapter) {
        ensureService();
        try {
            mService.clearProcess(adapter);
        } catch (Exception ignored) {
        }
    }

    public void setBootBlockEnabled(boolean enabled) {
        ensureService();
        try {
            mService.setBootBlockEnabled(enabled);
        } catch (Exception e) {
        }
    }

    public boolean isBlockBlockEnabled() {
        ensureService();
        try {
            return mService.isBlockBlockEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    public void setStartBlockEnabled(boolean enabled) {
        ensureService();
        try {
            mService.setStartBlockEnabled(enabled);
        } catch (Exception e) {
        }
    }

    public boolean isStartBlockEnabled() {
        ensureService();
        try {
            return mService.isStartBlockEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    public void setLockKillEnabled(boolean enabled) {
        ensureService();
        try {
            mService.setLockKillEnabled(enabled);
        } catch (Exception e) {
        }
    }

    public List<BlockRecord2> getBlockRecords() {
        ensureService();
        try {
            return mService.getBlockRecords();
        } catch (Exception e) {
            return new ArrayList<>(0);
        }
    }

    public void clearBlockRecords() {
        ensureService();
        try {
            mService.clearBlockRecords();
        } catch (Exception e) {
        }
    }

    public boolean isLockKillEnabled() {
        ensureService();
        try {
            return mService.isLockKillEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    public void setLockKillDelay(long delay) {
        ensureService();
        try {
            mService.setLockKillDelay(delay);
        } catch (Exception e) {
        }
    }

    public long getLockKillDelay() {
        ensureService();
        try {
            return mService.getLockKillDelay();
        } catch (Exception e) {
            return 0L;
        }
    }

    public void setRFKillEnabled(boolean enabled) {
        try {
            mService.setRFKillEnabled(enabled);
        } catch (Exception e) {
        }
    }

    public boolean isRFKillEnabled() {
        try {
            return mService.isRFKillEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean checkService(Intent intent, ComponentName servicePkgName, int callerUid) {
        ensureService();
        try {
            return mService.checkService(intent, servicePkgName, callerUid);
        } catch (Exception ignored) {
            return true;
        }
    }

    public boolean checkBroadcast(Intent action, int receiverUid, int callerUid) {
        ensureService();
        try {
            return mService.checkBroadcast(action, receiverUid, callerUid);
        } catch (Exception ignored) {
            return true;
        }
    }

    public void setComponentEnabledSetting(ComponentName componentName, int newState, int flags) {
        ensureService();
        try {
            mService.setComponentEnabledSetting(componentName, newState, flags);
        } catch (Exception e) {
        }
    }

    public int getComponentEnabledSetting(ComponentName componentName) {
        ensureService();
        try {
            return mService.getComponentEnabledSetting(componentName);
        } catch (Exception e) {
            return Integer.MIN_VALUE + PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;
        }
    }

    public int getApplicationEnabledSetting(String packageName) {
        ensureService();
        try {
            return mService.getApplicationEnabledSetting(packageName);
        } catch (Exception e) {
            return Integer.MIN_VALUE + PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;
        }
    }

    public void setApplicationEnabledSetting(String packageName, int newState, int flags, boolean tmp) {
        ensureService();
        try {
            mService.setApplicationEnabledSetting(packageName, newState, flags, tmp);
        } catch (Exception e) {
        }
    }

    public void addPendingDisableApps(String pkg) {
        ensureService();
        try {
            mService.addPendingDisableApps(pkg);
        } catch (Exception e) {

        }
    }

    public void addPendingDisableAppsTR(String pkg) {
        ensureService();
        try {
            mService.addPendingDisableAppsTR(pkg);
        } catch (Exception e) {

        }
    }

    public void watch(IAshmanWatcher w) {
        ensureService();
        try {
            mService.watch(w);
        } catch (Exception e) {
        }
    }

    public void unWatch(IAshmanWatcher w) {
        ensureService();
        try {
            mService.unWatch(w);
        } catch (Exception e) {
        }
    }

    public void setNetworkPolicyUidPolicy(int uid, int policy) {
        ensureService();
        try {
            mService.setNetworkPolicyUidPolicy(uid, policy);
        } catch (Exception e) {
        }
    }

    public void restart() {
        ensureService();
        try {
            mService.restart();
        } catch (Exception e) {
        }
    }

    public void setCompSettingBlockEnabled(boolean enabled) {
        ensureService();
        try {
            mService.setCompSettingBlockEnabled(enabled);
        } catch (Exception e) {
        }
    }

    public boolean isCompSettingBlockEnabledEnabled() {
        ensureService();
        try {
            return mService.isCompSettingBlockEnabledEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    public String[] getWhiteListApps(int filterOptions) {
        ensureService();
        try {
            return mService.getWhiteListApps(filterOptions);
        } catch (Exception e) {
            return new String[0];
        }
    }

    public String[] getBootBlockApps(boolean block) {
        ensureService();
        try {
            return mService.getBootBlockApps(block);
        } catch (Exception e) {
            return new String[0];
        }
    }

    public void addOrRemoveBootBlockApps(String[] packages, int op) {
        ensureService();
        try {
            mService.addOrRemoveBootBlockApps(packages, op);
        } catch (Exception e) {
        }
    }

    public String[] getStartBlockApps(boolean block) {
        ensureService();
        try {
            return mService.getStartBlockApps(block);
        } catch (Exception e) {
            return new String[0];
        }
    }

    public void addOrRemoveStartBlockApps(String[] packages, int op) {
        ensureService();
        try {
            mService.addOrRemoveStartBlockApps(packages, op);
        } catch (Exception e) {
        }
    }

    public String[] getLKApps(boolean kill) {
        ensureService();
        try {
            return mService.getLKApps(kill);
        } catch (Exception e) {
            return new String[0];
        }
    }

    public void addOrRemoveLKApps(String[] packages, int op) {
        ensureService();
        try {
            mService.addOrRemoveLKApps(packages, op);
        } catch (Exception e) {
        }
    }

    public String[] getRFKApps(boolean kill) {
        ensureService();
        try {
            return mService.getRFKApps(kill);
        } catch (Exception e) {
            return new String[0];
        }
    }

    public void addOrRemoveRFKApps(String[] packages, int op) {
        ensureService();
        try {
            mService.addOrRemoveRFKApps(packages, op);
        } catch (Exception e) {
        }
    }

    public void setWhiteSysAppEnabled(boolean enabled) {
        ensureService();
        try {
            mService.setWhiteSysAppEnabled(enabled);
        } catch (Exception e) {
        }
    }

    public boolean isWhiteSysAppEnabled() {
        ensureService();
        try {
            return mService.isWhiteSysAppEnabled();
        } catch (Exception e) {
            return true;
        }
    }

    public void unInstallPackage(String pkg, IPackageUninstallCallback callback) {
        ensureService();
        try {
            mService.unInstallPackage(pkg, callback);
        } catch (Exception e) {
        }
    }

    public void restrictAppOnData(int uid, boolean restrict) {
        ensureService();
        try {
            mService.restrictAppOnData(uid, restrict);
        } catch (Exception e) {
        }
    }

    public void restrictAppOnWifi(int uid, boolean restrict) {
        ensureService();
        try {
            mService.restrictAppOnWifi(uid, restrict);
        } catch (Exception e) {
        }
    }

    public boolean isRestrictOnData(int uid) {
        ensureService();
        try {
            return mService.isRestrictOnData(uid);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isRestrictOnWifi(int uid) {
        ensureService();
        try {
            return mService.isRestrictOnWifi(uid);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isLockKillDoNotKillAudioEnabled() {
        ensureService();
        try {
            return mService.isLockKillDoNotKillAudioEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    public void setLockKillDoNotKillAudioEnabled(boolean enabled) {
        ensureService();
        try {
            mService.setLockKillDoNotKillAudioEnabled(enabled);
        } catch (Exception e) {
        }
    }

    public int getControlMode() {
        try {
            return mService.getControlMode();
        } catch (Exception e) {
            return ControlMode.UNKNOWN;
        }
    }

    public void setControlMode(int mode) {
        ensureService();
        try {
            mService.setControlMode(mode);
        } catch (Exception e) {
        }
    }

    public String getBuildSerial() {
        ensureService();
        try {
            return mService.getBuildSerial();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isAutoAddBlackEnabled() {
        ensureService();
        try {
            return mService.isAutoAddBlackEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    public void setAutoAddBlackEnable(boolean enable) {
        ensureService();
        try {
            mService.setAutoAddBlackEnable(enable);
        } catch (Exception ignored) {
        }
    }

    public void forceReloadPackages() {
        ensureService();
        try {
            mService.forceReloadPackages();
        } catch (Exception e) {
        }
    }

    public void setPermissionControlEnabled(boolean enabled) {
        ensureService();
        try {
            mService.setPermissionControlEnabled(enabled);
        } catch (Exception e) {
        }
    }

    public boolean isPermissionControlEnabled() {
        ensureService();
        try {
            return mService.isPermissionControlEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    public int getPermissionControlBlockModeForPkg(int code, String pkg, boolean log, String[] payload) {
        ensureService();
        try {
            return mService.getPermissionControlBlockModeForPkg(code, pkg, log, payload);
        } catch (Exception e) {
            return AppOpsManagerCompat.MODE_ALLOWED;
        }
    }

    public int getPermissionControlBlockModeForUid(int code, int uid, boolean log, String[] payload) {
        ensureService();
        try {
            return mService.getPermissionControlBlockModeForUid(code, uid, log, payload);
        } catch (Exception e) {
            return AppOpsManagerCompat.MODE_ALLOWED;
        }
    }

    public int getPermissionControlBlockModeForPkg(int code, String pkg, boolean log) {
        ensureService();
        try {
            return mService.getPermissionControlBlockModeForPkg(code, pkg, log, null);
        } catch (Exception e) {
            return AppOpsManagerCompat.MODE_ALLOWED;
        }
    }

    public int getPermissionControlBlockModeForUid(int code, int uid, boolean log) {
        ensureService();
        try {
            return mService.getPermissionControlBlockModeForUid(code, uid, log, null);
        } catch (Exception e) {
            return AppOpsManagerCompat.MODE_ALLOWED;
        }
    }

    public void setPermissionControlBlockModeForPkg(int code, String pkg, int mode) {
        ensureService();
        try {
            mService.setPermissionControlBlockModeForPkg(code, pkg, mode);
        } catch (Exception ignored) {
        }
    }

    public void setUserDefinedAndroidId(String id) {
        ensureService();
        try {
            mService.setUserDefinedAndroidId(id);
        } catch (Exception ignored) {
        }
    }

    public void setUserDefinedDeviceId(String id) {
        ensureService();
        try {
            mService.setUserDefinedDeviceId(id);
        } catch (Exception ignored) {
        }
    }

    public void setUserDefinedLine1Number(String id) {
        ensureService();
        try {
            mService.setUserDefinedLine1Number(id);
        } catch (Exception ignored) {

        }
    }

    public String getAndroidId() {
        ensureService();
        try {
            return mService.getAndroidId();
        } catch (Exception e) {
            return null;
        }
    }

    public String getDeviceId() {
        ensureService();
        try {
            return mService.getDeviceId();
        } catch (Exception ignored) {
            return null;
        }
    }

    public String getLine1Number() {
        ensureService();
        try {
            return mService.getLine1Number();
        } catch (Exception ignored) {
            return null;
        }
    }

    public String getUserDefinedLine1Number() {
        ensureService();
        try {
            return mService.getUserDefinedLine1Number();
        } catch (Exception ignored) {
            return null;
        }
    }

    public String getUserDefinedDeviceId() {
        ensureService();
        try {
            return mService.getUserDefinedDeviceId();
        } catch (Exception e) {
            return null;
        }
    }

    public String getUserDefinedAndroidId() {
        ensureService();
        try {
            return mService.getUserDefinedAndroidId();
        } catch (Exception ignored) {
            return null;
        }
    }

    public boolean showFocusedActivityInfoEnabled() {
        ensureService();
        try {
            return mService.showFocusedActivityInfoEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    public void setShowFocusedActivityInfoEnabled(boolean enabled) {
        ensureService();
        try {
            mService.setShowFocusedActivityInfoEnabled(enabled);
        } catch (Exception ignored) {

        }
    }

    public void setPrivacyEnabled(boolean enable) {
        ensureService();
        try {
            mService.setPrivacyEnabled(enable);
        } catch (Exception e) {

        }
    }

    public boolean isPrivacyEnabled() {
        ensureService();
        try {
            return mService.isPrivacyEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isPackageInPrivacyList(String pkg) {
        ensureService();
        if (BuildConfig.DEBUG) try {
            return mService.isPackageInPrivacyList(pkg);
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public boolean isUidInPrivacyList(int uid) {
        ensureService();
        if (BuildConfig.DEBUG) try {
            return mService.isUidInPrivacyList(uid);
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public String[] getPrivacyList(boolean priv) {
        ensureService();
        try {
            return mService.getPrivacyList(priv);
        } catch (Exception ignored) {
            return new String[0];
        }
    }

    public int getPrivacyAppsCount() {
        try {
            return mService.getPrivacyAppsCount();
        } catch (Exception e) {
            return 0;
        }
    }

    public void addOrRemoveFromPrivacyList(String pkg, int op) {
        ensureService();
        try {
            mService.addOrRemoveFromPrivacyList(pkg, op);
        } catch (Exception ignored) {
        }
    }

    public String[] getGreeningApps(boolean greening) {
        ensureService();
        try {
            return mService.getGreeningApps(greening);
        } catch (Exception e) {
            return new String[0];
        }
    }

    public void addOrRemoveGreeningApps(String[] packages, int op) {
        ensureService();
        try {
            mService.addOrRemoveGreeningApps(packages, op);
        } catch (Exception ignored) {
        }
    }

    public void setGreeningEnabled(boolean enabled) {
        ensureService();
        try {
            mService.setGreeningEnabled(enabled);
        } catch (Exception ignored) {
        }
    }

    public boolean isGreeningEnabled() {
        ensureService();
        try {
            return mService.isGreeningEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isPackageGreening(String packageName) {
        ensureService();
        if (BuildConfig.DEBUG) try {
            return mService.isPackageGreening(packageName);
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public boolean isUidGreening(int uid) {
        ensureService();
        try {
            return mService.isUidGreening(uid);
        } catch (Exception e) {
            return false;
        }
    }

    public String[] getInstalledApps(int filterOptions) {
        ensureService();
        try {
            return mService.getInstalledApps(filterOptions);
        } catch (Exception e) {
            return new String[0];
        }
    }

    public void restoreDefaultSettings() {
        ensureService();
        try {
            mService.restoreDefaultSettings();
        } catch (Exception ignored) {
        }
    }

    public List<ActivityManager.RunningServiceInfo> getRunningServices(int max) {
        ensureService();
        try {
            return mService.getRunningServices(max);
        } catch (Exception e) {
            return new ArrayList<>(0);
        }
    }

    public List<ActivityManager.RunningAppProcessInfo> getRunningAppProcesses() {
        try {
            return mService.getRunningAppProcesses();
        } catch (Exception e) {
            return new ArrayList<>(0);
        }
    }

    public void writeSystemSettings(String key, String value) {
        ensureService();
        try {
            mService.writeSystemSettings(key, value);
        } catch (Exception ignored) {
        }
    }

    public String getSystemSettings(String key) {
        ensureService();
        try {
            return mService.getSystemSettings(key);
        } catch (Exception e) {
            return null;
        }
    }

    public long[] getProcessPss(int[] pids) {
        ensureService();
        try {
            return mService.getProcessPss(pids);
        } catch (Exception e) {
            return new long[0];
        }
    }

    public boolean onApplicationUncaughtException(String packageName, String thread, String exception, String trace) {
        ensureService();
        try {
            return mService.onApplicationUncaughtException(packageName, thread, exception, trace);
        } catch (Exception ignored) {
            return false;
        }
    }

    public boolean isAppCrashDumpEnabled() {
        ensureService();
        try {
            return mService.isAppCrashDumpEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    public void setAppCrashDumpEnabled(boolean enabled) {
        ensureService();
        try {
            mService.setAppCrashDumpEnabled(enabled);
        } catch (Exception ignored) {

        }
    }

    public void registerOnTopPackageChangeListener(ITopPackageChangeListener listener) {
        ensureService();
        try {
            mService.registerOnTopPackageChangeListener(listener);
        } catch (Exception ignored) {
        }
    }

    public void unRegisterOnTopPackageChangeListener(ITopPackageChangeListener listener) {
        ensureService();
        try {
            mService.unRegisterOnTopPackageChangeListener(listener);
        } catch (Exception ignored) {

        }
    }

    public boolean isLazyModeEnabled() {
        ensureService();
        try {
            return mService.isLazyModeEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isLazyModeEnabledForPackage(String pkg) {
        ensureService();
        try {
            return mService.isLazyModeEnabledForPackage(pkg);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean hasNotificationForPackage(String pkg) {
        ensureService();
        try {
            return mService.hasNotificationForPackage(pkg);
        } catch (Exception e) {
            return false;
        }
    }

    public void setLazyModeEnabled(boolean enabled) {
        ensureService();
        try {
            mService.setLazyModeEnabled(enabled);
        } catch (Exception ignored) {
        }
    }

    public String[] getLazyApps(boolean lazy) {
        ensureService();
        try {
            return mService.getLazyApps(lazy);
        } catch (Exception e) {
            return new String[0];
        }
    }

    public void addOrRemoveLazyApps(String[] packages, int op) {
        ensureService();
        try {
            mService.addOrRemoveLazyApps(packages, op);
        } catch (Exception ignored) {
        }
    }

    public void setLPBKEnabled(boolean enabled) {
        ensureService();
        try {
            mService.setLPBKEnabled(enabled);
        } catch (Exception ignored) {

        }
    }

    public boolean isLPBKEnabled() {
        ensureService();
        try {
            return mService.isLPBKEnabled();
        } catch (Exception ignored) {
            return false;
        }
    }

    public void onTaskRemoving(int callingUid, int taskId) {
        ensureService();
        try {
            mService.onTaskRemoving(callingUid, taskId);
        } catch (Exception ignored) {

        }
    }

    public void addOrRemoveAppFocusAction(String pkg, String[] actions, boolean add) {
        ensureService();
        try {
            mService.addOrRemoveAppFocusAction(pkg, actions, add);
        } catch (Exception e) {

        }
    }

    public void addOrRemoveAppUnFocusAction(String pkg, String[] actions, boolean add) {
        ensureService();
        try {
            mService.addOrRemoveAppUnFocusAction(pkg, actions, add);
        } catch (Exception e) {

        }
    }

    public String[] getAppFocusActionPackages() {
        ensureService();
        try {
            return mService.getAppFocusActionPackages();
        } catch (Exception e) {
            return new String[0];
        }
    }

    public String[] getAppFocusActions(String pkg) {
        ensureService();
        try {
            return mService.getAppFocusActions(pkg);
        } catch (Exception e) {
            return new String[0];
        }
    }

    public String[] getAppUnFocusActionPackages() {
        ensureService();
        try {
            return mService.getAppUnFocusActionPackages();
        } catch (Exception e) {
            return new String[0];
        }
    }

    public String[] getAppUnFocusActions(String pkg) {
        ensureService();
        try {
            return mService.getAppUnFocusActions(pkg);
        } catch (Exception e) {
            return new String[0];
        }
    }

    public long getLastDozeEnterTimeMills() {
        ensureService();
        try {
            return mService.getLastDozeEnterTimeMills();
        } catch (Exception e) {
            return -1;
        }
    }

    @Nullable
    public DozeEvent getLastDozeEvent() {
        ensureService();
        try {
            return mService.getLastDozeEvent();
        } catch (Exception e) {
            return null;
        }
    }

    public void setDozeEnabled(boolean enable) {
        ensureService();
        try {
            mService.setDozeEnabled(enable);
        } catch (Exception e) {

        }
    }

    public boolean isDozeEnabled() {
        ensureService();
        try {
            return mService.isDozeEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    public long getDozeDelayMills() {
        ensureService();
        try {
            return mService.getDozeDelayMills();
        } catch (Exception e) {
            return -1; // FIXME Make a meaningful res.
        }
    }

    public void setDozeDelayMills(long delayMills) {
        ensureService();
        try {
            mService.setDozeDelayMills(delayMills);
        } catch (Exception e) {

        }
    }

    public void setDoNotKillSBNEnabled(boolean enable, String module) {
        ensureService();
        try {
            mService.setDoNotKillSBNEnabled(enable, module);
        } catch (Exception e) {

        }
    }

    public boolean isDoNotKillSBNEnabled(String module) {
        ensureService();
        try {
            return mService.isDoNotKillSBNEnabled(module);
        } catch (Exception e) {
            return false;
        }
    }

    public void setTaskRemoveKillEnabled(boolean enable) {
        ensureService();
        try {
            mService.setTaskRemoveKillEnabled(enable);
        } catch (Exception e) {

        }
    }

    public boolean isTaskRemoveKillEnabled() {
        ensureService();
        try {
            return mService.isTaskRemoveKillEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    public String[] getTRKApps(boolean kill) {
        ensureService();
        try {
            return mService.getTRKApps(kill);
        } catch (Exception e) {
            return new String[0];
        }
    }

    public void addOrRemoveTRKApps(String[] packages, int op) {
        ensureService();
        try {
            mService.addOrRemoveTRKApps(packages, op);
        } catch (Exception e) {

        }
    }

    public List<DozeEvent> getDozeEventHistory() {
        ensureService();
        try {
            return mService.getDozeEventHistory();
        } catch (Exception e) {
            return new ArrayList<>(0);
        }
    }

    public void setForceDozeEnabled(boolean enable) {
        ensureService();
        try {
            mService.setForceDozeEnabled(enable);
        } catch (Exception e) {

        }
    }

    public boolean isForceDozeEnabled() {
        ensureService();
        try {
            return mService.isForceDozeEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    public AppSettings retrieveAppSettingsForPackage(String pkg) {
        ensureService();
        try {
            return mService.retrieveAppSettingsForPackage(pkg);
        } catch (Exception e) {
            return AppSettings.builder().pkgName(pkg).appName("NULL").build();
        }
    }

    public void applyAppSettingsForPackage(String pkg, AppSettings settings) {
        ensureService();
        try {
            mService.applyAppSettingsForPackage(pkg, settings);
        } catch (Exception e) {

        }
    }

    public void backupTo(String dir) {
        ensureService();
        try {
            mService.backupTo(dir);
        } catch (Exception e) {

        }
    }

    public void restoreFrom(String dir) {
        ensureService();
        try {
            mService.restoreFrom(dir);
        } catch (Exception e) {

        }
    }

    public String[] getRawPermSettings(int page, int countInPage) {
        ensureService();
        try {
            return mService.getRawPermSettings(page, countInPage);
        } catch (Exception e) {
            return new String[0];
        }
    }

    public void setAppInstalledAutoApplyTemplate(AppSettings settings) {
        ensureService();
        try {
            mService.setAppInstalledAutoApplyTemplate(settings);
        } catch (Exception e) {

        }
    }

    public AppSettings getAppInstalledAutoApplyTemplate() {
        ensureService();
        try {
            return mService.getAppInstalledAutoApplyTemplate();
        } catch (Exception e) {
            return AppSettings.fromJson("XXX");
        }
    }

    public List<OpLog> getOpLogForPackage(String packageName) {
        ensureService();
        try {
            return mService.getOpLogForPackage(packageName);
        } catch (Exception e) {
            return new ArrayList<>(0);
        }
    }

    public List<OpLog> getOpLogForOp(int code) {
        ensureService();
        try {
            return mService.getOpLogForOp(code);
        } catch (Exception e) {
            return new ArrayList<>(0);
        }
    }

    public void clearOpLogForPackage(String packageName) {
        ensureService();
        try {
            mService.clearOpLogForPackage(packageName);
        } catch (RemoteException e) {

        }
    }

    public void clearOpLogForOp(int cod) {
        ensureService();
        try {
            mService.clearOpLogForOp(cod);
        } catch (RemoteException e) {

        }
    }

    public String getUserName() {
        ensureService();
        try {
            return mService.getUserName();
        } catch (Exception e) {
            return null;
        }
    }

    public Bitmap getUserIcon() {
        ensureService();
        try {
            return mService.getUserIcon();
        } catch (Exception e) {
            return null;
        }
    }

    public void addPowerSaveWhitelistApp(String pkg) {
        ensureService();
        try {
            mService.addPowerSaveWhitelistApp(pkg);
        } catch (Exception e) {

        }
    }

    public void removePowerSaveWhitelistApp(String pkg) {
        ensureService();
        try {
            mService.removePowerSaveWhitelistApp(pkg);
        } catch (Exception e) {

        }
    }

    public String[] getFullPowerWhitelist() {
        ensureService();
        try {
            return mService.getFullPowerWhitelist();
        } catch (Exception e) {
            return ArrayUtil.newEmptyStringArray();
        }
    }

    public String[] getSystemPowerWhitelist() {
        ensureService();
        try {
            return mService.getSystemPowerWhitelist();
        } catch (Exception e) {
            return ArrayUtil.newEmptyStringArray();
        }
    }

    public String[] getUserPowerWhitelist() {
        ensureService();
        try {
            return mService.getUserPowerWhitelist();
        } catch (Exception e) {
            return ArrayUtil.newEmptyStringArray();
        }
    }

    public ActivityManager.MemoryInfo getMemoryInfo() {
        ensureService();
        try {
            return mService.getMemoryInfo();
        } catch (Exception e) {
            return new ActivityManager.MemoryInfo();
        }
    }

    public void enableKeyguard(boolean enabled) {
        ensureService();
        try {
            mService.enableKeyguard(enabled);
        } catch (Exception e) {

        }
    }

    public void exitKeyguardSecurely(IBooleanCallback1 result) {
        ensureService();
        try {
            mService.exitKeyguardSecurely(result);
        } catch (Exception e) {

        }
    }

    public void dismissKeyguardLw() {
        ensureService();
        try {
            mService.dismissKeyguardLw();
        } catch (Exception e) {

        }
    }

    public boolean isKeyguardLocked() {
        ensureService();
        try {
            return mService.isKeyguardLocked();
        } catch (Exception e) {
            return false;
        }
    }

    public int getRunningProcessCount() {
        ensureService();
        try {
            return mService.getRunningProcessCount();
        } catch (Exception e) {
            return 0;
        }
    }

    public boolean hasModuleError() {
        ensureService();
        try {
            return mService.hasModuleError();
        } catch (Exception e) {
            return false;
        }
    }

    public void setAppOpsTemplate(OpsSettings opsSettings) {
        ensureService();
        try {
            mService.setAppOpsTemplate(opsSettings);
        } catch (Exception e) {
        }
    }

    public OpsSettings getAppOpsTemplate(OpsSettings opsSettings) {
        ensureService();
        try {
            return mService.getAppOpsTemplate(opsSettings);
        } catch (Exception e) {
            return new OpsSettings(AppOpsManagerCompat.getDefaultModes());
        }
    }

    public void setResidentEnabled(boolean enable) {
        ensureService();
        try {
            mService.setResidentEnabled(enable);
        } catch (Exception e) {

        }
    }

    public boolean isResidentEnabled() {
        ensureService();
        try {
            return mService.isResidentEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isResidentEnabledForPackage(String who) {
        try {
            return mService.isResidentEnabledForPackage(who);
        } catch (Exception e) {
            return false;
        }
    }

    public void addOrRemoveResidentApps(String app, boolean add) {
        ensureService();
        try {
            mService.addOrRemoveResidentApps(app, add);
        } catch (Exception e) {

        }
    }

    public String[] getResidentApps(boolean resident) {
        ensureService();
        try {
            return mService.getResidentApps(resident);
        } catch (Exception e) {
            return ArrayUtil.emptyStringArray();
        }
    }

    public boolean isPanicHomeEnabled() {
        ensureService();
        try {
            return mService.isPanicHomeEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    public void setPanicHomeEnabled(boolean enable) {
        ensureService();
        try {
            mService.setPanicHomeEnabled(enable);
        } catch (Exception e) {

        }
    }

    public boolean isPanicLockEnabled() {
        ensureService();
        try {
            return mService.isPanicLockEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    public void setPanicLockEnabled(boolean enable) {
        ensureService();
        try {
            mService.setPanicLockEnabled(enable);
        } catch (Exception e) {
        }
    }

    public void lockNow() {
        ensureService();
        try {
            mService.lockNow();
        } catch (Exception e) {

        }
    }

    public boolean isInRedemptionMode() {
        ensureService();
        try {
            return mService.isInRedemptionMode();
        } catch (RemoteException e) {
            return false;
        }
    }

    public void leaveRedemptionMode() {
        ensureService();
        try {
            mService.leaveRedemptionMode();
        } catch (RemoteException e) {

        }
    }

    public void enterRedemptionMode() {
        ensureService();
        try {
            mService.enterRedemptionMode();
        } catch (RemoteException e) {

        }
    }

    public boolean isSELinuxEnabled() {
        ensureService();
        try {
            return mService.isSELinuxEnabled();
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean isSELinuxEnforced() {
        ensureService();
        try {
            return mService.isSELinuxEnforced();
        } catch (RemoteException e) {
            return false;
        }
    }

    public void setSelinuxEnforce(boolean enforce) {
        ensureService();
        try {
            mService.setSelinuxEnforce(enforce);
        } catch (RemoteException e) {

        }
    }

    public boolean isPowerSaveModeEnabled() {
        ensureService();
        try {
            return mService.isPowerSaveModeEnabled();
        } catch (RemoteException e) {
            return false;
        }
    }

    public void setPowerSaveModeEnabled(boolean enable) {
        ensureService();
        try {
            mService.setPowerSaveModeEnabled(enable);
        } catch (RemoteException e) {

        }
    }

    public String[] getStartRules() {
        ensureService();
        try {
            return mService.getStartRules();
        } catch (RemoteException e) {
            return ArrayUtil.newEmptyStringArray();
        }
    }

    public boolean addOrRemoveStartRules(String rule, boolean add) {
        ensureService();
        try {
            return mService.addOrRemoveStartRules(rule, add);
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean hasSystemError() {
        ensureService();
        try {
            return mService.hasSystemError();
        } catch (RemoteException e) {
            return false;
        }
    }

    public void cleanUpSystemErrorTraces() {
        ensureService();
        try {
            mService.cleanUpSystemErrorTraces();
        } catch (RemoteException e) {

        }
    }

    public void addAppLockWhiteListActivity(String[] activities) {
        ensureService();
        try {
            mService.addAppLockWhiteListActivity(activities);
        } catch (RemoteException e) {

        }
    }

    public boolean isAutoAddBlackNotificationEnabled() {
        ensureService();
        try {
            return mService.isAutoAddBlackNotificationEnabled();
        } catch (RemoteException e) {
            return false;
        }
    }

    public void setAutoAddBlackNotificationEnabled(boolean value) {
        ensureService();
        try {
            mService.setAutoAddBlackNotificationEnabled(value);
        } catch (RemoteException e) {

        }
    }

    public boolean isOptFeatureEnabled(String tag) {
        ensureService();
        try {
            return mService.isOptFeatureEnabled(tag);
        } catch (RemoteException e) {
            return false;
        }
    }

    public void setOptFeatureEnabled(String tag, boolean enable) {
        ensureService();
        try {
            mService.setOptFeatureEnabled(tag, enable);
        } catch (RemoteException e) {

        }
    }

    public int getRecentTaskExcludeSetting(ComponentName c) {
        ensureService();
        try {
            return mService.getRecentTaskExcludeSetting(c);
        } catch (RemoteException e) {
            return ExcludeRecentSetting.NONE;
        }
    }

    public void setRecentTaskExcludeSetting(ComponentName c, int setting) {
        ensureService();
        try {
            mService.setRecentTaskExcludeSetting(c, setting);
        } catch (RemoteException e) {

        }
    }

    public int getAppConfigOverlayIntSetting(String appPackageName, String tag) {
        ensureService();
        try {
            return mService.getAppConfigOverlayIntSetting(appPackageName, tag);
        } catch (RemoteException e) {
            return ConfigOverlays.NONE;
        }
    }

    public void setAppConfigOverlayIntSetting(String appPackageName, String tag, int value) {
        ensureService();
        try {
            mService.setAppConfigOverlayIntSetting(appPackageName, tag, value);
        } catch (RemoteException e) {

        }
    }

    public void injectPowerEvent() {
        ensureService();
        try {
            mService.injectPowerEvent();
        } catch (RemoteException e) {

        }
    }

    public String getServiceStarter(ComponentName service) {
        ensureService();
        try {
            return mService.getServiceStarter(service);
        } catch (RemoteException e) {
            return null;
        }
    }

    public boolean isInactiveAppInsteadOfKillPreferred() {
        ensureService();
        try {
            return mService.isInactiveAppInsteadOfKillPreferred();
        } catch (RemoteException e) {
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
        } catch (RemoteException e) {

        }
    }

    public void mockSystemDead(long delay) {
        ensureService();
        try {
            mService.mockSystemDead(delay);
        } catch (RemoteException e) {

        }
    }

    public void clearModuleSettings(String moduleVar) {
        ensureService();
        try {
            mService.clearModuleSettings(moduleVar);
        } catch (RemoteException e) {

        }
    }

    public boolean isDisableMotionEnabled() {
        try {
            return mService.isDisableMotionEnabled();
        } catch (RemoteException e) {
            return false;
        }
    }

    public void setDisableMotionEnabled(boolean enable) {
        try {
            mService.setDisableMotionEnabled(enable);
        } catch (RemoteException e) {

        }
    }
}
