package github.tornaco.xposedmoduletest.xposed.service;

import android.app.ActivityManager;
import android.app.IApplicationThread;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.view.KeyEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.robv.android.xposed.SELinuxHelper;
import github.tornaco.xposedmoduletest.IAshmanWatcher;
import github.tornaco.xposedmoduletest.IBooleanCallback1;
import github.tornaco.xposedmoduletest.IPackageUninstallCallback;
import github.tornaco.xposedmoduletest.IProcessClearListener;
import github.tornaco.xposedmoduletest.ITopPackageChangeListener;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;
import github.tornaco.xposedmoduletest.xposed.bean.AppSettings;
import github.tornaco.xposedmoduletest.xposed.bean.BlockRecord2;
import github.tornaco.xposedmoduletest.xposed.bean.DozeEvent;
import github.tornaco.xposedmoduletest.xposed.bean.OpLog;
import github.tornaco.xposedmoduletest.xposed.bean.OpsSettings;
import github.tornaco.xposedmoduletest.xposed.bean.VerifySettings;
import github.tornaco.xposedmoduletest.xposed.repo.RepoProxy;
import github.tornaco.xposedmoduletest.xposed.service.am.AMSProxy;
import github.tornaco.xposedmoduletest.xposed.service.am.UsageStatsServiceProxy;
import github.tornaco.xposedmoduletest.xposed.service.doze.DeviceIdleControllerProxy;
import github.tornaco.xposedmoduletest.xposed.service.dpm.DevicePolicyManagerServiceProxy;
import github.tornaco.xposedmoduletest.xposed.service.notification.NotificationManagerServiceProxy;
import github.tornaco.xposedmoduletest.xposed.service.policy.PhoneWindowManagerProxy;
import github.tornaco.xposedmoduletest.xposed.submodules.SubModuleManager;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2018/2/3.
 * Email: Tornaco@163.com
 */

// Empty impl for redemption mode.
public class XAshmanServiceImplRedemption extends XAshmanServiceAbs {

    @Override
    public void publish() {
        XposedLog.boot("Publishing redemption ashman!!!");

        try {
            String serviceName = XAshmanManager.SERVICE_NAME;
            XposedLog.boot("publishing redemption ash to: " + serviceName);
            ServiceManager.addService(serviceName, asBinder());
        } catch (Throwable e) {
            XposedLog.debug("*** FATAL*** Fail publish our svc:" + e);
        }
    }

    @Override
    public void systemReady() {
        XposedLog.boot("System ready redemption ashman!!!");
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
    public boolean isInRedemptionMode() throws RemoteException {
        return true; // Yes!!!
    }


    @Override
    public boolean hasSystemError() throws RemoteException {
        return true;  // Yes!!!
    }

    @Override
    public void cleanUpSystemErrorTraces() throws RemoteException {
        // Noop.
    }

    @Override
    public void addAppLockWhiteListActivity(String[] activities) throws RemoteException {

    }

    @Override
    public boolean isAutoAddBlackNotificationEnabled() throws RemoteException {
        return false;
    }

    @Override
    public void setAutoAddBlackNotificationEnabled(boolean value) throws RemoteException {

    }

    @Override
    public boolean isOptFeatureEnabled(String tag) throws RemoteException {
        return false;
    }

    @Override
    public void setOptFeatureEnabled(String tag, boolean enable) throws RemoteException {

    }

    @Override
    public int getRecentTaskExcludeSetting(ComponentName c) {
        return XAshmanManager.ExcludeRecentSetting.NONE;
    }

    @Override
    public void onStartProcessLocked(ApplicationInfo applicationInfo) {

    }

    @Override
    public void onRemoveProcessLocked(ApplicationInfo applicationInfo, boolean callerWillRestart, boolean allowRestart, String reason) {

    }

    @Override
    public void setRecentTaskExcludeSetting(ComponentName c, int setting) throws RemoteException {

    }

    @Override
    public int getAppConfigOverlayIntSetting(String appPackageName, String tag) throws RemoteException {
        return XAshmanManager.ConfigOverlays.NONE;
    }

    @Override
    public void setAppConfigOverlayIntSetting(String appPackageName, String tag, int value) throws RemoteException {

    }

    @Override
    public void injectPowerEvent() throws RemoteException {

    }

    @Override
    public String getServiceStarter(ComponentName service) throws RemoteException {
        return null;
    }

    @Override
    public boolean isInactiveAppInsteadOfKillPreferred() throws RemoteException {
        return false;
    }

    @Override
    public void setInactiveAppInsteadOfKillPreferred(boolean prefer) throws RemoteException {

    }

    @Override
    public void mockSystemDead(long delay) throws RemoteException {

    }

    @Override
    public void clearModuleSettings(String moduleVar) throws RemoteException {

    }

    @Override
    public boolean isDisableMotionEnabled() throws RemoteException {
        return false;
    }

    @Override
    public void setDisableMotionEnabled(boolean enable) throws RemoteException {

    }

    @Override
    public boolean isGCMSupportPackage(String pkg) throws RemoteException {
        return false;
    }

    @Override
    public boolean isShowAppProcessUpdateNotificationEnabled() throws RemoteException {
        return false;
    }

    @Override
    public void setShowAppProcessUpdateNotificationEnabled(boolean enabled) throws RemoteException {

    }

    @Override
    public boolean isStartRuleEnabled() throws RemoteException {
        return false;
    }

    @Override
    public void setStartRuleEnabled(boolean enabled) throws RemoteException {
    }

    @Override
    public boolean isPushMessageHandlerEnabled(String handlerTag) throws RemoteException {
        return false;
    }

    @Override
    public void setPushMessageHandlerEnabled(String handlerTag, boolean enabled) throws RemoteException {

    }

    @Override
    public boolean isPushMessageHandlerShowContentEnabled(String handlerTag) throws RemoteException {
        return false;
    }

    @Override
    public void setPushMessageHandlerShowContentEnabled(String handlerTag, boolean enabled) throws RemoteException {

    }

    @Override
    public boolean isPushMessageHandlerNotificationSoundEnabled(String handlerTag) throws RemoteException {
        return false;
    }

    @Override
    public void setPushMessageHandlerNotificationSoundEnabled(String handlerTag, boolean enabled) throws RemoteException {

    }

    @Override
    public boolean isPushMessageHandlerNotificationVibrateEnabled(String handlerTag) throws RemoteException {
        return false;
    }

    @Override
    public void setPushMessageHandlerNotificationVibrateEnabled(String handlerTag, boolean enabled) throws RemoteException {

    }

    @Override
    public boolean isPushMessageHandlerMessageNotificationByAppEnabled(String pkg) throws RemoteException {
        return false;
    }

    @Override
    public void setPushMessageHandlerMessageNotificationByAppEnabled(String pkg, boolean enabled) throws RemoteException {

    }

    @Override
    public boolean isPushMessageHandleEnabled() throws RemoteException {
        return false;
    }

    @Override
    public void setPushMessageHandleEnabled(boolean enabled) throws RemoteException {

    }

    @Override
    public void leaveRedemptionMode() throws RemoteException {
        RepoProxy.deleteFileIndicator(SubModuleManager.REDEMPTION);
    }

    @Override
    public void enterRedemptionMode() throws RemoteException {
        RepoProxy.createFileIndicator(SubModuleManager.REDEMPTION);
    }

    @Override
    public boolean isSELinuxEnabled() throws RemoteException {
        return SELinuxHelper.isSELinuxEnabled();
    }

    @Override
    public boolean isSELinuxEnforced() throws RemoteException {
        return SELinuxHelper.isSELinuxEnforced();
    }

    @Override
    public void setSelinuxEnforce(boolean enforce) throws RemoteException {

    }

    @Override
    public boolean isPowerSaveModeEnabled() throws RemoteException {
        return false;
    }

    @Override
    public void setPowerSaveModeEnabled(boolean enable) throws RemoteException {

    }

    @Override
    public String[] getStartRules() throws RemoteException {
        return new String[0];
    }

    @Override
    public boolean addOrRemoveStartRules(String rule, boolean add) throws RemoteException {
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
    public void notifyTaskCreated(int taskId, ComponentName componentName) {
        throwNoImpl();
    }

    @Override
    public ComponentName componentNameForTaskId(int taskId) {
        return throwNoImpl();
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
    public void attachUsageStatsService(UsageStatsServiceProxy proxy) {

    }

    @Override
    public void attachAMS(AMSProxy proxy) {

    }

    @Override
    public boolean checkService(Intent service, String callingPackage, int callingPid,
                                int callingUid, boolean callingFromFg) throws RemoteException {
        return false;
    }

    @Override
    public boolean checkRestartService(String packageName, ComponentName componentName) throws RemoteException {
        return false;
    }

    @Override
    public boolean checkBroadcastDeliver(Intent intent, String callerPackage,
                                         int callingPid, int callingUid) throws RemoteException {
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
    public void clearProcess(IProcessClearListener listener) throws RemoteException {

    }

    @Override
    public void setLockKillDelay(long delay) throws RemoteException {

    }

    @Override
    public long getLockKillDelay() throws RemoteException {
        return 0;
    }

    @Override
    public void setWhiteSysAppEnabled(boolean enabled) throws RemoteException {

    }

    @Override
    public boolean isWhiteSysAppEnabled() throws RemoteException {
        return false;
    }

    @Override
    public void setBootBlockEnabled(boolean enabled) throws RemoteException {

    }

    @Override
    public boolean isBlockBlockEnabled() throws RemoteException {
        return false;
    }

    @Override
    public void setStartBlockEnabled(boolean enabled) throws RemoteException {

    }

    @Override
    public boolean isStartBlockEnabled() throws RemoteException {
        return false;
    }

    @Override
    public void setLockKillEnabled(boolean enabled) throws RemoteException {

    }

    @Override
    public boolean isLockKillEnabled() throws RemoteException {
        return false;
    }

    @Override
    public void setRFKillEnabled(boolean enabled) throws RemoteException {

    }

    @Override
    public boolean isRFKillEnabled() throws RemoteException {
        return false;
    }

    @Override
    public void setGreeningEnabled(boolean enabled) throws RemoteException {

    }

    @Override
    public boolean isGreeningEnabled() throws RemoteException {
        return false;
    }

    @Override
    public boolean checkService(Intent intent, ComponentName servicePkgName, int callerUid) throws RemoteException {
        return false;
    }

    @Override
    public boolean checkBroadcast(Intent action, int receiverUid, int callerUid) throws RemoteException {
        return false;
    }

    @Override
    public List<BlockRecord2> getBlockRecords() throws RemoteException {
        return null;
    }

    @Override
    public void clearBlockRecords() throws RemoteException {

    }

    @Override
    public void setComponentEnabledSetting(ComponentName componentName, int newState, int flags) throws RemoteException {

    }

    @Override
    public int getComponentEnabledSetting(ComponentName componentName) throws RemoteException {
        return 0;
    }

    @Override
    public int getApplicationEnabledSetting(String packageName) throws RemoteException {
        return 0;
    }

    @Override
    public void setApplicationEnabledSetting(String packageName, int newState, int flags, boolean tmp) throws RemoteException {

    }

    @Override
    public void watch(IAshmanWatcher w) throws RemoteException {

    }

    @Override
    public void unWatch(IAshmanWatcher w) throws RemoteException {

    }

    @Override
    public void setNetworkPolicyUidPolicy(int uid, int policy) throws RemoteException {

    }

    @Override
    public void restrictAppOnData(int uid, boolean restrict) throws RemoteException {

    }

    @Override
    public void restrictAppOnWifi(int uid, boolean restrict) throws RemoteException {

    }

    @Override
    public boolean isRestrictOnData(int uid) throws RemoteException {
        return false;
    }

    @Override
    public boolean isRestrictOnWifi(int uid) throws RemoteException {
        return false;
    }

    @Override
    public void restart() throws RemoteException {

    }

    @Override
    public void setCompSettingBlockEnabled(boolean enabled) throws RemoteException {

    }

    @Override
    public boolean isCompSettingBlockEnabledEnabled() throws RemoteException {
        return false;
    }

    @Override
    public String[] getWhiteListApps(int filterOptions) throws RemoteException {
        return new String[0];
    }

    @Override
    public String[] getInstalledApps(int filterOptions) throws RemoteException {
        return new String[0];
    }

    @Override
    public String[] getBootBlockApps(boolean block) throws RemoteException {
        return new String[0];
    }

    @Override
    public void addOrRemoveBootBlockApps(String[] packages, int op) throws RemoteException {

    }

    @Override
    public String[] getStartBlockApps(boolean block) throws RemoteException {
        return new String[0];
    }

    @Override
    public void addOrRemoveStartBlockApps(String[] packages, int op) throws RemoteException {

    }

    @Override
    public String[] getLKApps(boolean kill) throws RemoteException {
        return new String[0];
    }

    @Override
    public void addOrRemoveLKApps(String[] packages, int op) throws RemoteException {

    }

    @Override
    public String[] getRFKApps(boolean kill) throws RemoteException {
        return new String[0];
    }

    @Override
    public void addOrRemoveRFKApps(String[] packages, int op) throws RemoteException {

    }

    @Override
    public String[] getGreeningApps(boolean greening) throws RemoteException {
        return new String[0];
    }

    @Override
    public void addOrRemoveGreeningApps(String[] packages, int op) throws RemoteException {

    }

    @Override
    public boolean isPackageGreening(String packageName) throws RemoteException {
        return false;
    }

    @Override
    public boolean isUidGreening(int uid) throws RemoteException {
        return false;
    }

    @Override
    public void unInstallPackage(String pkg, IPackageUninstallCallback callback) throws RemoteException {

    }

    @Override
    public boolean isLockKillDoNotKillAudioEnabled() throws RemoteException {
        return false;
    }

    @Override
    public void setLockKillDoNotKillAudioEnabled(boolean enabled) throws RemoteException {

    }

    @Override
    public int getControlMode() throws RemoteException {
        return 0;
    }

    @Override
    public void setControlMode(int mode) throws RemoteException {

    }

    @Override
    public String getBuildSerial() throws RemoteException {
        return null;
    }

    @Override
    public boolean isAutoAddBlackEnabled() throws RemoteException {
        return false;
    }

    @Override
    public void setAutoAddBlackEnable(boolean enable) throws RemoteException {

    }

    @Override
    public void forceReloadPackages() throws RemoteException {

    }

    @Override
    public void setPermissionControlEnabled(boolean enabled) throws RemoteException {

    }

    @Override
    public boolean isPermissionControlEnabled() throws RemoteException {
        return false;
    }

    @Override
    public int getPermissionControlBlockModeForPkg(int code, String pkg, boolean log, String[] payload) throws RemoteException {
        return 0;
    }

    @Override
    public int getPermissionControlBlockModeForUid(int code, int uid, boolean log, String[] payload) throws RemoteException {
        return 0;
    }

    @Override
    public void setPermissionControlBlockModeForPkg(int code, String pkg, int mode) throws RemoteException {

    }

    @Override
    public void setUserDefinedAndroidId(String id) throws RemoteException {

    }

    @Override
    public void setUserDefinedDeviceId(String id) throws RemoteException {

    }

    @Override
    public void setUserDefinedLine1Number(String id) throws RemoteException {

    }

    @Override
    public String getAndroidId() throws RemoteException {
        return null;
    }

    @Override
    public String getDeviceId() throws RemoteException {
        return null;
    }

    @Override
    public String getLine1Number() throws RemoteException {
        return null;
    }

    @Override
    public String getUserDefinedLine1Number() throws RemoteException {
        return null;
    }

    @Override
    public String getUserDefinedDeviceId() throws RemoteException {
        return null;
    }

    @Override
    public String getUserDefinedAndroidId() throws RemoteException {
        return null;
    }

    @Override
    public boolean isPackageInPrivacyList(String pkg) throws RemoteException {
        return false;
    }

    @Override
    public boolean isUidInPrivacyList(int uid) throws RemoteException {
        return false;
    }

    @Override
    public int getPrivacyAppsCount() throws RemoteException {
        return 0;
    }

    @Override
    public String[] getPrivacyList(boolean priv) throws RemoteException {
        return new String[0];
    }

    @Override
    public void addOrRemoveFromPrivacyList(String pkg, int op) throws RemoteException {

    }

    @Override
    public boolean showFocusedActivityInfoEnabled() throws RemoteException {
        return false;
    }

    @Override
    public void setShowFocusedActivityInfoEnabled(boolean enabled) throws RemoteException {

    }

    @Override
    public void restoreDefaultSettings() throws RemoteException {

    }

    @Override
    public List<ActivityManager.RunningServiceInfo> getRunningServices(int max) throws RemoteException {
        return null;
    }

    @Override
    public List<ActivityManager.RunningAppProcessInfo> getRunningAppProcesses() throws RemoteException {
        return null;
    }

    @Override
    public void writeSystemSettings(String key, String value) throws RemoteException {

    }

    @Override
    public String getSystemSettings(String key) throws RemoteException {
        return null;
    }

    @Override
    public long[] getProcessPss(int[] pids) throws RemoteException {
        return new long[0];
    }

    @Override
    public boolean onApplicationUncaughtException(String packageName, String thread, String exception, String trace) throws RemoteException {
        return false;
    }

    @Override
    public boolean isAppCrashDumpEnabled() throws RemoteException {
        return false;
    }

    @Override
    public void setAppCrashDumpEnabled(boolean enabled) throws RemoteException {

    }

    @Override
    public void registerOnTopPackageChangeListener(ITopPackageChangeListener listener) throws RemoteException {

    }

    @Override
    public void unRegisterOnTopPackageChangeListener(ITopPackageChangeListener listener) throws RemoteException {

    }

    @Override
    public boolean isLazyModeEnabled() throws RemoteException {
        return false;
    }

    @Override
    public boolean isLazyModeEnabledForPackage(String pkg) throws RemoteException {
        return false;
    }

    @Override
    public void setLazyModeEnabled(boolean enabled) throws RemoteException {

    }

    @Override
    public String[] getLazyApps(boolean lazy) throws RemoteException {
        return new String[0];
    }

    @Override
    public void addOrRemoveLazyApps(String[] packages, int op) throws RemoteException {

    }

    @Override
    public void setLPBKEnabled(boolean enabled) throws RemoteException {

    }

    @Override
    public boolean isLPBKEnabled() throws RemoteException {
        return false;
    }

    @Override
    public void onTaskRemoving(int callingUid, int taskId) throws RemoteException {

    }

    @Override
    public void addOrRemoveAppFocusAction(String pkg, String[] actions, boolean add) throws RemoteException {

    }

    @Override
    public String[] getAppFocusActionPackages() throws RemoteException {
        return new String[0];
    }

    @Override
    public String[] getAppFocusActions(String pkg) throws RemoteException {
        return new String[0];
    }

    @Override
    public void addOrRemoveAppUnFocusAction(String pkg, String[] actions, boolean add) throws RemoteException {

    }

    @Override
    public String[] getAppUnFocusActionPackages() throws RemoteException {
        return new String[0];
    }

    @Override
    public String[] getAppUnFocusActions(String pkg) throws RemoteException {
        return new String[0];
    }

    @Override
    public void setDozeEnabled(boolean enable) throws RemoteException {

    }

    @Override
    public boolean isDozeEnabled() throws RemoteException {
        return false;
    }

    @Override
    public void setForceDozeEnabled(boolean enable) throws RemoteException {

    }

    @Override
    public boolean isForceDozeEnabled() throws RemoteException {
        return false;
    }

    @Override
    public long getLastDozeEnterTimeMills() throws RemoteException {
        return 0;
    }

    @Override
    public DozeEvent getLastDozeEvent() throws RemoteException {
        return null;
    }

    @Override
    public long getDozeDelayMills() throws RemoteException {
        return 0;
    }

    @Override
    public void setDozeDelayMills(long delayMills) throws RemoteException {

    }

    @Override
    public void setDoNotKillSBNEnabled(boolean enable, String module) throws RemoteException {

    }

    @Override
    public boolean isDoNotKillSBNEnabled(String module) throws RemoteException {
        return false;
    }

    @Override
    public void setTaskRemoveKillEnabled(boolean enable) throws RemoteException {

    }

    @Override
    public boolean isTaskRemoveKillEnabled() throws RemoteException {
        return false;
    }

    @Override
    public String[] getTRKApps(boolean kill) throws RemoteException {
        return new String[0];
    }

    @Override
    public void addOrRemoveTRKApps(String[] packages, int op) throws RemoteException {

    }

    @Override
    public List<DozeEvent> getDozeEventHistory() throws RemoteException {
        return null;
    }

    @Override
    public void setPrivacyEnabled(boolean enable) throws RemoteException {

    }

    @Override
    public boolean isPrivacyEnabled() throws RemoteException {
        return false;
    }

    @Override
    public String[] getPluginApps() throws RemoteException {
        return new String[0];
    }

    @Override
    public boolean isAppInPluginList(String pkg) throws RemoteException {
        return false;
    }

    @Override
    public void addOrRemovePluginApp(String appPackageName, boolean add) throws RemoteException {

    }

    @Override
    public boolean hasNotificationForPackage(String pkg) throws RemoteException {
        return false;
    }

    @Override
    public int getAppLevel(String pkg) throws RemoteException {
        return 0;
    }

    @Override
    public String packageForTaskId(int taskId) throws RemoteException {
        return null;
    }

    @Override
    public boolean isAppLockEnabled() throws RemoteException {
        return false;
    }

    @Override
    public void setAppLockEnabled(boolean enabled) throws RemoteException {

    }

    @Override
    public boolean isBlurEnabled() throws RemoteException {
        return false;
    }

    @Override
    public boolean isBlurEnabledForPackage(String packageName) throws RemoteException {
        return false;
    }

    @Override
    public void setBlurEnabled(boolean enabled) throws RemoteException {

    }

    @Override
    public int getBlurRadius() throws RemoteException {
        return 0;
    }

    @Override
    public void setBlurRadius(int r) throws RemoteException {

    }

    @Override
    public boolean isUninstallInterruptEnabled() throws RemoteException {
        return false;
    }

    @Override
    public void setUninstallInterruptEnabled(boolean enabled) throws RemoteException {

    }

    @Override
    public void setVerifySettings(VerifySettings settings) throws RemoteException {

    }

    @Override
    public VerifySettings getVerifySettings() throws RemoteException {
        return null;
    }

    @Override
    public void setResult(int transactionID, int res) throws RemoteException {

    }

    @Override
    public boolean isTransactionValid(int transactionID) throws RemoteException {
        return false;
    }

    @Override
    public void mockCrash() throws RemoteException {

    }

    @Override
    public void setVerifierPackage(String pkg) throws RemoteException {

    }

    @Override
    public void injectHomeEvent() throws RemoteException {

    }

    @Override
    public void setDebug(boolean debug) throws RemoteException {

    }

    @Override
    public boolean isDebug() throws RemoteException {
        return false;
    }

    @Override
    public void onActivityPackageResume(String pkg) throws RemoteException {

    }

    @Override
    public boolean isInterruptFPEventVBEnabled(int event) throws RemoteException {
        return false;
    }

    @Override
    public void setInterruptFPEventVBEnabled(int event, boolean enabled) throws RemoteException {

    }

    @Override
    public void addOrRemoveComponentReplacement(ComponentName from, ComponentName to, boolean add) throws RemoteException {

    }

    @Override
    public Map getComponentReplacements() throws RemoteException {
        return null;
    }

    @Override
    public String[] getLockApps(boolean lock) throws RemoteException {
        return new String[0];
    }

    @Override
    public void addOrRemoveLockApps(String[] packages, boolean add) throws RemoteException {

    }

    @Override
    public String[] getBlurApps(boolean lock) throws RemoteException {
        return new String[0];
    }

    @Override
    public void addOrRemoveBlurApps(String[] packages, boolean blur) throws RemoteException {

    }

    @Override
    public String[] getUPApps(boolean lock) throws RemoteException {
        return new String[0];
    }

    @Override
    public void addOrRemoveUPApps(String[] packages, boolean add) throws RemoteException {

    }

    @Override
    public AppSettings retrieveAppSettingsForPackage(String pkg) throws RemoteException {
        return null;
    }

    @Override
    public void applyAppSettingsForPackage(String pkg, AppSettings settings) throws RemoteException {

    }

    @Override
    public void backupTo(String dir) throws RemoteException {

    }

    @Override
    public void restoreFrom(String dir) throws RemoteException {

    }

    @Override
    public String[] getRawPermSettings(int page, int countInPage) throws RemoteException {
        return new String[0];
    }

    @Override
    public void setAppInstalledAutoApplyTemplate(AppSettings settings) throws RemoteException {

    }

    @Override
    public AppSettings getAppInstalledAutoApplyTemplate() throws RemoteException {
        return null;
    }

    @Override
    public List<OpLog> getOpLogForPackage(String packageName) throws RemoteException {
        return null;
    }

    @Override
    public List<OpLog> getOpLogForOp(int code) throws RemoteException {
        return null;
    }

    @Override
    public void clearOpLogForPackage(String packageName) throws RemoteException {

    }

    @Override
    public void clearOpLogForOp(int cod) throws RemoteException {

    }

    @Override
    public String getUserName() throws RemoteException {
        return null;
    }

    @Override
    public Bitmap getUserIcon() throws RemoteException {
        return null;
    }

    @Override
    public void addPendingDisableApps(String pkg) throws RemoteException {

    }

    @Override
    public void addPowerSaveWhitelistApp(String pkg) throws RemoteException {

    }

    @Override
    public void removePowerSaveWhitelistApp(String pkg) throws RemoteException {

    }

    @Override
    public String[] getFullPowerWhitelist() throws RemoteException {
        return new String[0];
    }

    @Override
    public String[] getSystemPowerWhitelist() throws RemoteException {
        return new String[0];
    }

    @Override
    public String[] getUserPowerWhitelist() throws RemoteException {
        return new String[0];
    }

    @Override
    public ActivityManager.MemoryInfo getMemoryInfo() throws RemoteException {
        return null;
    }

    @Override
    public void enableKeyguard(boolean enabled) throws RemoteException {

    }

    @Override
    public void exitKeyguardSecurely(IBooleanCallback1 result) throws RemoteException {

    }

    @Override
    public void dismissKeyguardLw() throws RemoteException {

    }

    @Override
    public boolean isKeyguardLocked() throws RemoteException {
        return false;
    }

    @Override
    public int getRunningProcessCount() throws RemoteException {
        return 0;
    }

    @Override
    public void setAppOpsTemplate(OpsSettings opsSettings) throws RemoteException {

    }

    @Override
    public OpsSettings getAppOpsTemplate(OpsSettings opsSettings) throws RemoteException {
        return null;
    }

    @Override
    public void addPendingDisableAppsTR(String pkg) throws RemoteException {

    }

    @Override
    public void setResidentEnabled(boolean enable) throws RemoteException {

    }

    @Override
    public boolean isResidentEnabled() throws RemoteException {
        return false;
    }

    @Override
    public boolean isResidentEnabledForPackage(String who) throws RemoteException {
        return false;
    }

    @Override
    public void addOrRemoveResidentApps(String app, boolean add) throws RemoteException {

    }

    @Override
    public String[] getResidentApps(boolean resident) throws RemoteException {
        return new String[0];
    }

    @Override
    public boolean isPanicHomeEnabled() throws RemoteException {
        return false;
    }

    @Override
    public void setPanicHomeEnabled(boolean enable) throws RemoteException {

    }

    @Override
    public boolean isPanicLockEnabled() throws RemoteException {
        return false;
    }

    @Override
    public void setPanicLockEnabled(boolean enable) throws RemoteException {

    }

    @Override
    public void lockNow() throws RemoteException {

    }
}
