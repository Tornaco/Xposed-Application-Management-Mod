package github.tornaco.xposedmoduletest.xposed.service;

import android.app.ActivityManager;
import android.app.IApplicationThread;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.service.notification.StatusBarNotification;
import android.util.Pair;
import android.view.Display;
import android.view.KeyEvent;
import android.view.WindowManagerPolicy;

import com.android.server.notification.NotificationRecord;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.robv.android.xposed.SELinuxHelper;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.IAshmanWatcher;
import github.tornaco.xposedmoduletest.IBackupAgent;
import github.tornaco.xposedmoduletest.IBooleanCallback1;
import github.tornaco.xposedmoduletest.IJsEvaluateListener;
import github.tornaco.xposedmoduletest.IPackageUninstallCallback;
import github.tornaco.xposedmoduletest.IProcessClearListener;
import github.tornaco.xposedmoduletest.IServiceControl;
import github.tornaco.xposedmoduletest.ITaskRemoveListener;
import github.tornaco.xposedmoduletest.ITopPackageChangeListener;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.bean.AppOpsTemplate;
import github.tornaco.xposedmoduletest.xposed.bean.AppSettings;
import github.tornaco.xposedmoduletest.xposed.bean.BlockRecord2;
import github.tornaco.xposedmoduletest.xposed.bean.DozeEvent;
import github.tornaco.xposedmoduletest.xposed.bean.JavaScript;
import github.tornaco.xposedmoduletest.xposed.bean.OpLog;
import github.tornaco.xposedmoduletest.xposed.bean.SystemPropProfile;
import github.tornaco.xposedmoduletest.xposed.bean.VerifySettings;
import github.tornaco.xposedmoduletest.xposed.repo.RepoProxy;
import github.tornaco.xposedmoduletest.xposed.service.am.AMSProxy;
import github.tornaco.xposedmoduletest.xposed.service.am.ActiveServicesProxy;
import github.tornaco.xposedmoduletest.xposed.service.am.UsageStatsServiceProxy;
import github.tornaco.xposedmoduletest.xposed.service.doze.DeviceIdleControllerProxy;
import github.tornaco.xposedmoduletest.xposed.service.dpm.DevicePolicyManagerServiceProxy;
import github.tornaco.xposedmoduletest.xposed.service.notification.NotificationManagerServiceProxy;
import github.tornaco.xposedmoduletest.xposed.service.policy.PhoneWindowManagerProxy;
import github.tornaco.xposedmoduletest.xposed.service.power.PowerManagerServiceProxy;
import github.tornaco.xposedmoduletest.xposed.submodules.SubModuleManager;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2018/2/3.
 * Email: Tornaco@163.com
 */

// Empty impl for redemption mode.
public class XAshmanServiceImplRedemption extends XAshmanServiceAbs {

    private boolean mSystemReady;

    @Override
    public void publish() {
        XposedLog.boot("Publishing redemption ashman!!!");

        try {
            String serviceName = XAPMManager.SERVICE_NAME;
            XposedLog.boot("publishing redemption ash to: " + serviceName);
            ServiceManager.addService(serviceName, asBinder());
        } catch (Throwable e) {
            XposedLog.debug("*** FATAL*** Fail publish our svc:" + e);
        }
    }

    @Override
    public void systemReady() {
        XposedLog.boot("System ready redemption ashman!!!");
        mSystemReady = true;
    }

    @Override
    public void retrieveSettings() {
        XposedLog.boot("retrieveSettings redemption ashman!!!");
    }

    @Override
    public void shutdown() {
        XposedLog.boot("shutdown redemption ashman!!!");
    }

    @Override
    public boolean isInRedemptionMode() {
        return true; // Yes!!!
    }


    @Override
    public boolean hasSystemError() {
        return true;  // Yes!!!
    }

    @Override
    public void cleanUpSystemErrorTraces() {
        // Noop.
    }

    @Override
    public void addAppLockWhiteListActivity(String[] activities) {

    }

    @Override
    public boolean isAutoAddBlackNotificationEnabled() {
        return false;
    }

    @Override
    public void setAutoAddBlackNotificationEnabled(boolean value) {

    }

    @Override
    public boolean isOptFeatureEnabled(String tag) {
        return false;
    }

    @Override
    public void setOptFeatureEnabled(String tag, boolean enable) {

    }

    @Override
    public int getRecentTaskExcludeSetting(ComponentName c) {
        return XAPMManager.ExcludeRecentSetting.NONE;
    }

    @Override
    public boolean checkStartProcess(ApplicationInfo applicationInfo, String hostType, String hostName) {
        return true; // ALLOW
    }

    @Override
    public void onStartProcessLocked(ApplicationInfo applicationInfo) {

    }

    @Override
    public void onRemoveProcessLocked(ApplicationInfo applicationInfo, boolean callerWillRestart, boolean allowRestart, String reason) {

    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {

    }

    @Override
    public void onNotificationPosted(NotificationRecord sbn) {

    }

    @Override
    public void onNotificationRemoved(NotificationRecord sbn) {

    }

    @Override
    public void onInputEvent(Object arg) {

    }

    @Override
    public void onSourceApkFileDetected(String path, String apkPackageName) {

    }

    @Override
    public String getCurrentTopPackage() {
        return null;
    }

    @Override
    public void registerTaskRemoveListener(ITaskRemoveListener listener) {

    }

    @Override
    public void unRegisterTaskRemoveListener(ITaskRemoveListener listener) {

    }

    @Override
    public void setAppInactive(String packageName, boolean inactive, int userId) {

    }

    @Override
    public boolean isAppInactive(String packageName, int userId) {
        return false;
    }

    @Override
    public void forceStopPackage(String packageName) {

    }

    @Override
    public void setAppInactivePolicyForModule(String module, int policy) {

    }

    @Override
    public int getAppInactivePolicyForModule(String module) {
        return 0;
    }

    @Override
    public void executeInputCommand(String[] args) {

    }

    @Override
    public void takeLongScreenShot() {

    }

    @Override
    public IBackupAgent getBackupAgent() {
        return RepoProxy.getProxy().getBackupAgent();
    }

    @Override
    public void showRebootNeededNotification(String why) {

    }

    @Override
    public void evaluateJsString(String[] args, IJsEvaluateListener listener) {

    }

    @Override
    public JavaScript getSavedJs(String id) {
        return null;
    }

    @Override
    public List<JavaScript> getSavedJses() {
        return null;
    }

    @Override
    public void saveJs(JavaScript js) {

    }

    @Override
    public void deleteJs(JavaScript js) {

    }

    @Override
    public boolean checkInstallApk(Object installArgsObject) {
        return true; // Yes.
    }

    @Override
    public void setRecentTaskExcludeSetting(ComponentName c, int setting) {

    }

    @Override
    public int getAppConfigOverlayIntSetting(String appPackageName, String tag) {
        return XAPMManager.ConfigOverlays.NONE;
    }

    @Override
    public void setAppConfigOverlayIntSetting(String appPackageName, String tag, int value) {

    }

    @Override
    public void injectPowerEvent() {

    }

    @Override
    public String getServiceStarter(ComponentName service) {
        return null;
    }

    @Override
    public boolean isInactiveAppInsteadOfKillPreferred() {
        return false;
    }

    @Override
    public void setInactiveAppInsteadOfKillPreferred(boolean prefer) {

    }

    @Override
    public void mockSystemDead(long delay) {

    }

    @Override
    public void clearModuleSettings(String moduleVar) {

    }

    @Override
    public boolean isDisableMotionEnabled() {
        return false;
    }

    @Override
    public void setDisableMotionEnabled(boolean enable) {

    }

    @Override
    public boolean isGCMSupportPackage(String pkg) {
        return false;
    }

    @Override
    public boolean isShowAppProcessUpdateNotificationEnabled() {
        return false;
    }

    @Override
    public void setShowAppProcessUpdateNotificationEnabled(boolean enabled) {

    }

    @Override
    public boolean isStartRuleEnabled() {
        return false;
    }

    @Override
    public void setStartRuleEnabled(boolean enabled) {
    }

    @Override
    public boolean isPushMessageHandlerEnabled(String handlerTag) {
        return false;
    }

    @Override
    public void setPushMessageHandlerEnabled(String handlerTag, boolean enabled) {

    }

    @Override
    public boolean isPushMessageHandlerShowContentEnabled(String handlerTag) {
        return false;
    }

    @Override
    public void setPushMessageHandlerShowContentEnabled(String handlerTag, boolean enabled) {

    }

    @Override
    public boolean isPushMessageHandlerNotificationSoundEnabled(String handlerTag) {
        return false;
    }

    @Override
    public void setPushMessageHandlerNotificationSoundEnabled(String handlerTag, boolean enabled) {

    }

    @Override
    public boolean isPushMessageHandlerNotificationVibrateEnabled(String handlerTag) {
        return false;
    }

    @Override
    public void setPushMessageHandlerNotificationVibrateEnabled(String handlerTag, boolean enabled) {

    }

    @Override
    public boolean isPushMessageHandlerMessageNotificationByAppEnabled(String pkg) {
        return false;
    }

    @Override
    public void setPushMessageHandlerMessageNotificationByAppEnabled(String pkg, boolean enabled) {

    }

    @Override
    public boolean isPushMessageHandleEnabled() {
        return false;
    }

    @Override
    public void setPushMessageHandleEnabled(boolean enabled) {

    }

    @Override
    public boolean isHandlingPushMessageIntent(String packageName) {
        return false;
    }

    @Override
    public boolean showToast(String message) {
        return false;
    }

    @Override
    public List<BlockRecord2> getStartRecordsForPackage(String pkg) {
        return null;
    }

    @Override
    public void clearStartRecordsForPackage(String pkg) {

    }

    @Override
    public boolean isWakeupOnNotificationEnabled() {
        return false;
    }

    @Override
    public void setWakeupOnNotificationEnabled(boolean enable) {

    }

    @Override
    public boolean addOrRemoveLazyRules(String rule, boolean add) {
        return false;
    }

    @Override
    public String[] getLazyRules() {
        return new String[0];
    }

    @Override
    public boolean isLazyRuleEnabled() {
        return false;
    }

    @Override
    public void setLazyRuleEnabled(boolean enable) {

    }

    @Override
    public void createMultipleProfile() {

    }

    @Override
    public boolean installAppToMultipleAppsUser(String pkgName) {
        return false;
    }

    @Override
    public void startActivityAsUser(Intent intent, int userId) {

    }

    @Override
    public void launchMultipleAppsForPackage(String packageName) {

    }

    @Override
    public void mockPushMessageReceived(String pkg, String message) {

    }

    @Override
    public void registerController(IServiceControl control) {

    }

    @Override
    public void unRegisterController(IServiceControl control) {

    }

    @Override
    public void stopService(Intent serviceIntent) {

    }

    @Override
    public void setAppServiceLazyControlSolution(int solutionFlags, boolean enabled) {

    }

    @Override
    public boolean isAppServiceLazyControlSolutionEnable(int solutionFlags) {
        return false;
    }

    @Override
    public void forceIdlePackages(String[] packages) {

    }

    @Override
    public boolean isSystemPropEnabled() {
        return false;
    }

    @Override
    public void setSystemPropEnabled(boolean enabled) {

    }

    @Override
    public void addOrRemoveSystemPropProfile(SystemPropProfile profile, boolean add) {

    }

    @Override
    public Map getSystemPropProfiles() {
        return null;
    }

    @Override
    public void setActiveSystemPropProfileId(String profileId) {

    }

    @Override
    public String getActiveSystemPropProfileId() {
        return null;
    }

    @Override
    public SystemPropProfile getActiveSystemPropProfile() {
        return null;
    }

    @Override
    public void addOrRemoveSystemPropProfileApplyApps(String[] pkgs, boolean add) {

    }

    @Override
    public String[] getSystemPropProfileApplyApps(boolean apply) {
        return new String[0];
    }

    @Override
    public boolean isSystemPropProfileApplyApp(String packageName) {
        return false;
    }

    @Override
    public boolean isPackageInstallVerifyEnabled() {
        return false;
    }

    @Override
    public void setPackageInstallVerifyEnabled(boolean enabled) {

    }

    @Override
    public String[] getPackageInstallerVerifyRules() {
        return new String[0];
    }

    @Override
    public boolean addOrRemovePackageInstallerVerifyRules(String rule, boolean add) {
        return false;
    }

    @Override
    public void leaveRedemptionMode() {
        RepoProxy.deleteFileIndicator(SubModuleManager.REDEMPTION);
    }

    @Override
    public void enterRedemptionMode() {
        RepoProxy.createFileIndicator(SubModuleManager.REDEMPTION);
    }

    @Override
    public boolean isSELinuxEnabled() {
        return SELinuxHelper.isSELinuxEnabled();
    }

    @Override
    public boolean isSELinuxEnforced() {
        return SELinuxHelper.isSELinuxEnforced();
    }

    @Override
    public void setSelinuxEnforce(boolean enforce) {

    }

    @Override
    public boolean isPowerSaveModeEnabled() {
        return false;
    }

    @Override
    public void setPowerSaveModeEnabled(boolean enable) {

    }

    @Override
    public String[] getStartRules() {
        return new String[0];
    }

    @Override
    public boolean addOrRemoveStartRules(String rule, boolean add) {
        return false;
    }

    // Below API will be ignored.

    @Override
    public void onPackageMoveToFront(Intent who) {
        throwNoImpl();
    }

    @Override
    public String serial() {
        return UUID.randomUUID().toString();
    }

    @Override
    public boolean onKeyEvent(KeyEvent keyEvent, String source) {
        return throwNoImpl();
    }

    @Override
    public boolean checkBroadcastIntentSending(IApplicationThread caller, Intent intent) {
        return throwNoImpl();
    }

    @Override
    public int onHookBroadcastPerformResult(Intent intent, int resultCode) {
        return 0;
    }

    @Override
    public boolean beforeHookBroadcastPerformResult() {
        return false;
    }

    @Override
    public void notifyTaskCreated(int taskId, ComponentName componentName) {
        throwNoImpl();
    }

    @Override
    public ComponentName componentNameForTaskId(int taskId) {
        return throwNoImpl();
    }

    @Override
    public void reportBlurBadPerformance(long timeTaken) throws RemoteException {

    }

    @Override
    public boolean isRedemptionModeEnabled() throws RemoteException {
        return false;
    }

    @Override
    public void setRedemptionModeEnabled(boolean enabled) throws RemoteException {

    }

    @Override
    public boolean isSystemReady() {
        return mSystemReady;
    }

    @Override
    public boolean interruptPackageRemoval(String pkg) {
        return throwNoImpl();
    }

    @Override
    public boolean interruptPackageDataClear(String pkg) {
        return throwNoImpl();
    }

    @Override
    public void notifyPackageRemovalInterrupt(String pkg) {
        throwNoImpl();
    }

    @Override
    public void notifyPackageDataClearInterrupt(String pkg) {
        throwNoImpl();
    }

    @Override
    public boolean onEarlyVerifyConfirm(String pkg, String reason) {
        return throwNoImpl();
    }

    @Override
    public void verify(Bundle options, String pkg, ComponentName componentName, int uid, int pid, VerifyListener listener) {

    }

    @Override
    public void reportActivityLaunching(Intent intent, String reason) {

    }

    @Override
    public Intent checkIntent(Intent from) {
        return throwNoImpl();
    }

    @Override
    public long wrapCallingUidForIntent(long from, Intent intent) {
        return throwNoImpl();
    }

    @Override
    public boolean isBlurForPkg(String pkg) {
        return throwNoImpl();
    }

    @Override
    public float getBlurScale() {
        return throwNoImpl();
    }

    @Override
    public Pair<Integer, Integer> getScreenSize() {
        return new Pair<>(0, 0);
    }

    @Override
    public Bitmap getAppIconBitmap(String pkgName) {
        return throwNoImpl();
    }

    @Override
    public boolean interruptFPSuccessVibrate() {
        return throwNoImpl();
    }

    @Override
    public boolean interruptFPErrorVibrate() {
        return throwNoImpl();
    }

    @Override
    public boolean isActivityStartShouldBeInterrupted(ComponentName componentName) {
        return throwNoImpl();
    }


    // FIXME Throw for below api.
    @Override
    public void attachDeviceIdleController(DeviceIdleControllerProxy proxy) {

    }

    @Override
    public void attachNotificationService(NotificationManagerServiceProxy proxy) {

    }

    @Override
    public void attachDevicePolicyManagerService(DevicePolicyManagerServiceProxy proxy) {

    }

    @Override
    public void attachPhoneWindowManager(PhoneWindowManagerProxy proxy) {

    }

    @Override
    public void initPhoneWindowManager(Context context, WindowManagerPolicy.WindowManagerFuncs funcs) {

    }

    @Override
    public void onPhoneWindowManagerSetInitialDisplaySize(Display display) {

    }

    @Override
    public void attachUsageStatsService(UsageStatsServiceProxy proxy) {

    }

    @Override
    public void attachAMS(AMSProxy proxy) {

    }

    @Override
    public void attachActiveServices(ActiveServicesProxy proxy) {

    }

    @Override
    public void attachPowerManagerServices(PowerManagerServiceProxy proxy) {

    }

    @Override
    public boolean checkAcquireWakeLockInternal(int flags, String tag, String packageName) {
        return true;
    }

    @Override
    public boolean checkService(Intent service, String callingPackage, int callingPid,
                                int callingUid, boolean callingFromFg) {
        return false;
    }

    @Override
    public boolean checkRestartService(String packageName, ComponentName componentName) {
        return false;
    }

    @Override
    public boolean checkBroadcastDeliver(Intent intent, String callerPackage,
                                         int callingPid, int callingUid) {
        return false;
    }

    @Override
    public void onActivityDestroy(Intent intent, String reason) {

    }

    @Override
    public boolean checkComponentSetting(ComponentName componentName, int newState, int flags, int callingUid) {
        return false;
    }

    @Override
    public void onNetWorkManagementServiceReady(NativeDaemonConnector connector) {

    }

    @Override
    public void onRequestAudioFocus(int type, int res, int callingUid, String callingPkg) {

    }

    @Override
    public void onAbandonAudioFocus(int res, int callingUid, String callingPkg) {

    }

    @Override
    public int checkPermission(String perm, int pid, int uid) {
        return 0;
    }

    @Override
    public int checkOperation(int code, int uid, String packageName, String reason) {
        return 0;
    }

    @Override
    public boolean resident(String pkgName) {
        return false;
    }

    @Override
    public boolean residentEnableInternal() {
        return false;
    }

    @Override
    public void clearProcess(IProcessClearListener listener, boolean doNotClearWhenIntervative, boolean onlyForThoseInList) {

    }

    @Override
    public void setLockKillDelay(long delay) {

    }

    @Override
    public long getLockKillDelay() {
        return 0;
    }

    @Override
    public void setWhiteSysAppEnabled(boolean enabled) {

    }

    @Override
    public boolean isWhiteSysAppEnabled() {
        return false;
    }

    @Override
    public void setBootBlockEnabled(boolean enabled) {

    }

    @Override
    public boolean isBlockBlockEnabled() {
        return false;
    }

    @Override
    public void setStartBlockEnabled(boolean enabled) {

    }

    @Override
    public boolean isStartBlockEnabled() {
        return false;
    }

    @Override
    public void setLockKillEnabled(boolean enabled) {

    }

    @Override
    public boolean isLockKillEnabled() {
        return false;
    }

    @Override
    public void setRFKillEnabled(boolean enabled) {

    }

    @Override
    public boolean isRFKillEnabled() {
        return false;
    }

    @Override
    public void setGreeningEnabled(boolean enabled) {

    }

    @Override
    public boolean isGreeningEnabled() {
        return false;
    }

    @Override
    public boolean checkService(Intent intent, ComponentName servicePkgName, int callerUid) {
        return false;
    }

    @Override
    public boolean checkBroadcast(Intent action, int receiverUid, int callerUid) {
        return false;
    }

    @Override
    public List<BlockRecord2> getBlockRecords() {
        return null;
    }

    @Override
    public void clearBlockRecords() {

    }

    @Override
    public void setComponentEnabledSetting(ComponentName componentName, int newState, int flags) {

    }

    @Override
    public int getComponentEnabledSetting(ComponentName componentName) {
        return 0;
    }

    @Override
    public int getApplicationEnabledSetting(String packageName) {
        return 0;
    }

    @Override
    public void setApplicationEnabledSetting(String packageName, int newState, int flags, boolean tmp) {

    }

    @Override
    public void watch(IAshmanWatcher w) {

    }

    @Override
    public void unWatch(IAshmanWatcher w) {

    }

    @Override
    public void setNetworkPolicyUidPolicy(int uid, int policy) {

    }

    @Override
    public void restrictAppOnData(int uid, boolean restrict) {

    }

    @Override
    public void restrictAppOnWifi(int uid, boolean restrict) {

    }

    @Override
    public boolean isRestrictOnData(int uid) {
        return false;
    }

    @Override
    public boolean isRestrictOnWifi(int uid) {
        return false;
    }

    @Override
    public void restart() {

    }

    @Override
    public void setCompSettingBlockEnabled(boolean enabled) {

    }

    @Override
    public boolean isCompSettingBlockEnabledEnabled() {
        return false;
    }

    @Override
    public String[] getWhiteListApps(int filterOptions) {
        return new String[0];
    }

    @Override
    public String[] getInstalledApps(int filterOptions) {
        return new String[0];
    }

    @Override
    public String[] getBootBlockApps(boolean block) {
        return new String[0];
    }

    @Override
    public void addOrRemoveBootBlockApps(String[] packages, int op) {

    }

    @Override
    public String[] getStartBlockApps(boolean block) {
        return new String[0];
    }

    @Override
    public void addOrRemoveStartBlockApps(String[] packages, int op) {

    }

    @Override
    public String[] getLKApps(boolean kill) {
        return new String[0];
    }

    @Override
    public void addOrRemoveLKApps(String[] packages, int op) {

    }

    @Override
    public String[] getRFKApps(boolean kill) {
        return new String[0];
    }

    @Override
    public void addOrRemoveRFKApps(String[] packages, int op) {

    }

    @Override
    public String[] getGreeningApps(boolean greening) {
        return new String[0];
    }

    @Override
    public void addOrRemoveGreeningApps(String[] packages, int op) {

    }

    @Override
    public boolean isPackageGreening(String packageName) {
        return false;
    }

    @Override
    public boolean isUidGreening(int uid) {
        return false;
    }

    @Override
    public void unInstallPackage(String pkg, IPackageUninstallCallback callback) {

    }

    @Override
    public boolean isLockKillDoNotKillAudioEnabled() {
        return false;
    }

    @Override
    public void setLockKillDoNotKillAudioEnabled(boolean enabled) {

    }

    @Override
    public int getControlMode() {
        return 0;
    }

    @Override
    public void setControlMode(int mode) {

    }

    @Override
    public String getBuildSerial() {
        return null;
    }

    @Override
    public String getBuildVersionName() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public int getBuildVersionCode() {
        return BuildConfig.VERSION_CODE;
    }

    @Override
    public boolean isAutoAddBlackEnabled() {
        return false;
    }

    @Override
    public void setAutoAddBlackEnable(boolean enable) {

    }

    @Override
    public void forceReloadPackages() {

    }

    @Override
    public void setPermissionControlEnabled(boolean enabled) {

    }

    @Override
    public boolean isPermissionControlEnabled() {
        return false;
    }

    @Override
    public int getPermissionControlBlockModeForPkg(int code, String pkg, boolean log, String[] payload) {
        return 0;
    }

    @Override
    public int getPermissionControlBlockModeForUid(int code, int uid, boolean log, String[] payload) {
        return 0;
    }

    @Override
    public void setPermissionControlBlockModeForPkg(int code, String pkg, int mode) {

    }

    @Override
    public void setUserDefinedAndroidId(String id) {

    }

    @Override
    public void setUserDefinedDeviceId(String id) {

    }

    @Override
    public void setUserDefinedLine1Number(String id) {

    }

    @Override
    public String getAndroidId() {
        return null;
    }

    @Override
    public String getDeviceId() {
        return null;
    }

    @Override
    public String getLine1Number() {
        return null;
    }

    @Override
    public String getUserDefinedLine1Number() {
        return null;
    }

    @Override
    public String getUserDefinedDeviceId() {
        return null;
    }

    @Override
    public String getUserDefinedAndroidId() {
        return null;
    }

    @Override
    public boolean isPackageInPrivacyList(String pkg) {
        return false;
    }

    @Override
    public boolean isUidInPrivacyList(int uid) {
        return false;
    }

    @Override
    public int getPrivacyAppsCount() {
        return 0;
    }

    @Override
    public String[] getPrivacyList(boolean priv) {
        return new String[0];
    }

    @Override
    public void addOrRemoveFromPrivacyList(String pkg, int op) {

    }

    @Override
    public boolean showFocusedActivityInfoEnabled() {
        return false;
    }

    @Override
    public void setShowFocusedActivityInfoEnabled(boolean enabled) {

    }

    @Override
    public void restoreDefaultSettings() {

    }

    @Override
    public List<ActivityManager.RunningServiceInfo> getRunningServices(int max) {
        return null;
    }

    @Override
    public List<ActivityManager.RunningAppProcessInfo> getRunningAppProcesses() {
        return null;
    }

    @Override
    public void writeSystemSettings(String key, String value) {

    }

    @Override
    public String getSystemSettings(String key) {
        return null;
    }

    @Override
    public long[] getProcessPss(int[] pids) {
        return new long[0];
    }

    @Override
    public boolean onApplicationUncaughtException(String packageName, String thread, String exception, String trace) {
        return false;
    }

    @Override
    public boolean isAppCrashDumpEnabled() {
        return false;
    }

    @Override
    public void setAppCrashDumpEnabled(boolean enabled) {

    }

    @Override
    public void registerOnTopPackageChangeListener(ITopPackageChangeListener listener) {

    }

    @Override
    public void unRegisterOnTopPackageChangeListener(ITopPackageChangeListener listener) {

    }

    @Override
    public boolean isLazyModeEnabled() {
        return false;
    }

    @Override
    public boolean isLazyModeEnabledForPackage(String pkg) {
        return false;
    }

    @Override
    public void setLazyModeEnabled(boolean enabled) {

    }

    @Override
    public String[] getLazyApps(boolean lazy) {
        return new String[0];
    }

    @Override
    public void addOrRemoveLazyApps(String[] packages, int op) {

    }

    @Override
    public void setLPBKEnabled(boolean enabled) {

    }

    @Override
    public boolean isLPBKEnabled() {
        return false;
    }

    @Override
    public void onTaskRemoving(int callingUid, int taskId) {

    }

    @Override
    public void addOrRemoveActivityFocusAction(ComponentName comp, String[] actions, boolean add) {

    }

    @Override
    public ComponentName[] getActivityFocusActionComponents() {
        return new ComponentName[0];
    }

    @Override
    public String[] getActivityFocusActions(ComponentName comp) {
        return new String[0];
    }

    @Override
    public void addOrRemoveActivityUnFocusAction(ComponentName comp, String[] actions, boolean add) {

    }

    @Override
    public ComponentName[] getActivityUnFocusActionComponents() {
        return new ComponentName[0];
    }

    @Override
    public String[] getActivityUnFocusActions(ComponentName comp) {
        return new String[0];
    }


    @Override
    public void setDozeEnabled(boolean enable) {

    }

    @Override
    public boolean isDozeEnabled() {
        return false;
    }

    @Override
    public void setForceDozeEnabled(boolean enable) {

    }

    @Override
    public boolean isForceDozeEnabled() {
        return false;
    }

    @Override
    public long getLastDozeEnterTimeMills() {
        return 0;
    }

    @Override
    public DozeEvent getLastDozeEvent() {
        return null;
    }

    @Override
    public long getDozeDelayMills() {
        return 0;
    }

    @Override
    public void setDozeDelayMills(long delayMills) {

    }

    @Override
    public void setDoNotKillSBNEnabled(boolean enable, String module) {

    }

    @Override
    public boolean isDoNotKillSBNEnabled(String module) {
        return false;
    }

    @Override
    public void setTaskRemoveKillEnabled(boolean enable) {

    }

    @Override
    public boolean isTaskRemoveKillEnabled() {
        return false;
    }

    @Override
    public String[] getTRKApps(boolean kill) {
        return new String[0];
    }

    @Override
    public void addOrRemoveTRKApps(String[] packages, int op) {

    }

    @Override
    public List<DozeEvent> getDozeEventHistory() {
        return null;
    }

    @Override
    public void setPrivacyEnabled(boolean enable) {

    }

    @Override
    public boolean isPrivacyEnabled() {
        return false;
    }

    @Override
    public String[] getPluginApps() {
        return new String[0];
    }

    @Override
    public boolean isAppInPluginList(String pkg) {
        return false;
    }

    @Override
    public void addOrRemovePluginApp(String appPackageName, boolean add) {

    }

    @Override
    public boolean hasNotificationForPackage(String pkg) {
        return false;
    }

    @Override
    public int getAppLevel(String pkg) {
        return 0;
    }

    @Override
    public String packageForTaskId(int taskId) {
        return null;
    }

    @Override
    public boolean isAppLockEnabled() {
        return false;
    }

    @Override
    public void setAppLockEnabled(boolean enabled) {

    }

    @Override
    public boolean isBlurEnabled() {
        return false;
    }

    @Override
    public boolean isBlurEnabledForPackage(String packageName) {
        return false;
    }

    @Override
    public void setBlurEnabled(boolean enabled) {

    }

    @Override
    public int getBlurRadius() {
        return 0;
    }

    @Override
    public void setBlurRadius(int r) {

    }

    @Override
    public boolean isUninstallInterruptEnabled() {
        return false;
    }

    @Override
    public void setUninstallInterruptEnabled(boolean enabled) {

    }

    @Override
    public void setVerifySettings(VerifySettings settings) {

    }

    @Override
    public VerifySettings getVerifySettings() {
        return null;
    }

    @Override
    public void setResult(int transactionID, int res) {

    }

    @Override
    public boolean isTransactionValid(int transactionID) {
        return false;
    }

    @Override
    public void mockCrash() {

    }

    @Override
    public void setVerifierPackage(String pkg) {

    }

    @Override
    public void injectHomeEvent() {

    }

    @Override
    public void setDebug(boolean debug) {

    }

    @Override
    public boolean isDebug() {
        return false;
    }

    @Override
    public void onActivityPackageResume(String pkg) {

    }

    @Override
    public boolean isInterruptFPEventVBEnabled(int event) {
        return false;
    }

    @Override
    public void setInterruptFPEventVBEnabled(int event, boolean enabled) {

    }

    @Override
    public void addOrRemoveComponentReplacement(ComponentName from, ComponentName to, boolean add) {

    }

    @Override
    public Map getComponentReplacements() {
        return null;
    }

    @Override
    public String[] getLockApps(boolean lock) {
        return new String[0];
    }

    @Override
    public void addOrRemoveLockApps(String[] packages, boolean add) {

    }

    @Override
    public String[] getBlurApps(boolean lock) {
        return new String[0];
    }

    @Override
    public void addOrRemoveBlurApps(String[] packages, boolean blur) {

    }

    @Override
    public String[] getUPApps(boolean lock) {
        return new String[0];
    }

    @Override
    public void addOrRemoveUPApps(String[] packages, boolean add) {

    }

    @Override
    public AppSettings retrieveAppSettingsForPackage(String pkg) {
        return null;
    }

    @Override
    public void applyAppSettingsForPackage(String pkg, AppSettings settings) {

    }

    @Override
    public void backupTo(String dir) {

    }

    @Override
    public void restoreFrom(String dir) {

    }

    @Override
    public String[] getRawPermSettings(int page, int countInPage) {
        return new String[0];
    }

    @Override
    public void setAppInstalledAutoApplyTemplate(AppSettings settings) {

    }

    @Override
    public AppSettings getAppInstalledAutoApplyTemplate() {
        return null;
    }

    @Override
    public List<OpLog> getOpLogForPackage(String packageName) {
        return null;
    }

    @Override
    public List<OpLog> getOpLogForOp(int code) {
        return null;
    }

    @Override
    public void clearOpLogForPackage(String packageName) {

    }

    @Override
    public void clearOpLogForOp(int cod) {

    }

    @Override
    public String getUserName() {
        return null;
    }

    @Override
    public Bitmap getUserIcon() {
        return null;
    }

    @Override
    public void addPendingDisableApps(String pkg) {

    }

    @Override
    public void addPowerSaveWhitelistApp(String pkg) {

    }

    @Override
    public void removePowerSaveWhitelistApp(String pkg) {

    }

    @Override
    public String[] getFullPowerWhitelist() {
        return new String[0];
    }

    @Override
    public String[] getSystemPowerWhitelist() {
        return new String[0];
    }

    @Override
    public String[] getUserPowerWhitelist() {
        return new String[0];
    }

    @Override
    public ActivityManager.MemoryInfo getMemoryInfo() {
        return null;
    }

    @Override
    public void enableKeyguard(boolean enabled) {

    }

    @Override
    public void exitKeyguardSecurely(IBooleanCallback1 result) {

    }

    @Override
    public void dismissKeyguardLw() {

    }

    @Override
    public boolean isKeyguardLocked() {
        return false;
    }

    @Override
    public int getRunningProcessCount() {
        return 0;
    }

    @Override
    public void addPendingDisableAppsTR(String pkg) {

    }

    @Override
    public void setResidentEnabled(boolean enable) {

    }

    @Override
    public boolean isResidentEnabled() {
        return false;
    }

    @Override
    public boolean isResidentEnabledForPackage(String who) {
        return false;
    }

    @Override
    public void addOrRemoveResidentApps(String app, boolean add) {

    }

    @Override
    public String[] getResidentApps(boolean resident) {
        return new String[0];
    }

    @Override
    public boolean isPanicHomeEnabled() {
        return false;
    }

    @Override
    public void setPanicHomeEnabled(boolean enable) {

    }

    @Override
    public boolean isPanicLockEnabled() {
        return false;
    }

    @Override
    public void setPanicLockEnabled(boolean enable) {

    }

    @Override
    public void lockNow() {

    }

    @Override
    public PackageInfo getPackageInfoForPackage(String pkgName) {
        return null;
    }

    @Override
    public ApplicationInfo getApplicationInfoForPackage(String pkgName) {
        return null;
    }

    @Override
    public String getPackageNameForUid(int uid) {
        return null;
    }

    @Override
    public void killBackgroundProcesses(String packageName) {

    }

    @Override
    public AppOpsTemplate getAppOpsTemplateById(String id) throws RemoteException {
        return null;
    }

    @Override
    public void addAppOpsTemplate(AppOpsTemplate template) {

    }

    @Override
    public void removeAppOpsTemplate(AppOpsTemplate template) {

    }

    @Override
    public List<AppOpsTemplate> getAppOpsTemplates() {
        return null;
    }

    @Override
    public boolean isMiPushSupportPackage(String pkg) {
        return false;
    }


}
