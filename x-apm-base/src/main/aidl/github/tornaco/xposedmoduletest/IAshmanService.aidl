package github.tornaco.xposedmoduletest;

import github.tornaco.xposedmoduletest.IBooleanCallback1;
import github.tornaco.xposedmoduletest.IProcessClearListener;
import github.tornaco.xposedmoduletest.IPackageUninstallCallback;
import github.tornaco.xposedmoduletest.IAshmanWatcher;
import github.tornaco.xposedmoduletest.ITopPackageChangeListener;
import github.tornaco.xposedmoduletest.ITaskRemoveListener;
import github.tornaco.xposedmoduletest.IServiceControl;
import github.tornaco.xposedmoduletest.IBackupAgent;
import github.tornaco.xposedmoduletest.IJsEvaluateListener;
import github.tornaco.xposedmoduletest.xposed.bean.BlockRecord2;
import github.tornaco.xposedmoduletest.xposed.bean.DozeEvent;

import github.tornaco.xposedmoduletest.xposed.bean.OpLog;
import github.tornaco.xposedmoduletest.xposed.bean.AppSettings;
import github.tornaco.xposedmoduletest.xposed.bean.BlurSettings;
import github.tornaco.xposedmoduletest.xposed.bean.PackageSettings;
import github.tornaco.xposedmoduletest.xposed.bean.VerifySettings;
import github.tornaco.xposedmoduletest.xposed.bean.SystemPropProfile;
import github.tornaco.xposedmoduletest.xposed.bean.SystemProp;
import github.tornaco.xposedmoduletest.xposed.bean.JavaScript;
import github.tornaco.xposedmoduletest.xposed.bean.AppOpsTemplate;

import android.content.ComponentName;
import java.util.Map;

interface IAshmanService {

    void clearProcess(in IProcessClearListener listener, boolean doNotClearWhenIntervative, boolean onlyForThoseInList);

    void setLockKillDelay(long delay);
    long getLockKillDelay();

    void setWhiteSysAppEnabled(boolean enabled);
    boolean isWhiteSysAppEnabled();

    void setBootBlockEnabled(boolean enabled);
    boolean isBlockBlockEnabled();

    void setStartBlockEnabled(boolean enabled);
    boolean isStartBlockEnabled();

    void setLockKillEnabled(boolean enabled);
    boolean isLockKillEnabled();

    void setRFKillEnabled(boolean enabled);
    boolean isRFKillEnabled();

    void setGreeningEnabled(boolean enabled);
    boolean isGreeningEnabled();

    // API For Firewall.
    boolean checkService(in Intent intent, in ComponentName servicePkgName, int callerUid);
    boolean checkBroadcast(in Intent action, int receiverUid, int callerUid);

    List<BlockRecord2> getBlockRecords();

    void clearBlockRecords();

    void setComponentEnabledSetting(in ComponentName componentName, int newState, int flags);

    int getComponentEnabledSetting(in ComponentName componentName);

    int getApplicationEnabledSetting(String packageName);

    void setApplicationEnabledSetting(String packageName, int newState, int flags, boolean tmp);

    void watch(in IAshmanWatcher w);
    void unWatch(in IAshmanWatcher w);

    // Network policy API.
    void setNetworkPolicyUidPolicy(int uid, int policy);

    void restrictAppOnData(int uid, boolean restrict);
    void restrictAppOnWifi(int uid, boolean restrict);

    boolean isRestrictOnData(int uid);
    boolean isRestrictOnWifi(int uid);

    // Power API.
    void restart();

    // Extra API.
    void setCompSettingBlockEnabled(boolean enabled);
    boolean isCompSettingBlockEnabledEnabled();

    // Package loader API.
    String[] getWhiteListApps(int filterOptions);
    String[] getInstalledApps(int filterOptions);

    String[] getBootBlockApps(boolean block);
    void addOrRemoveBootBlockApps(in String[] packages, int op);

    String[] getStartBlockApps(boolean block);
    void addOrRemoveStartBlockApps(in String[] packages, int op);

    String[] getLKApps(boolean kill);
    void addOrRemoveLKApps(in String[] packages, int op);

    String[] getRFKApps(boolean kill);
    void addOrRemoveRFKApps(in String[] packages, int op);

    String[] getGreeningApps(boolean greening);
    void addOrRemoveGreeningApps(in String[] packages, int op);

    boolean isPackageGreening(String packageName);
    boolean isUidGreening(int uid);

    // PM API.
    void unInstallPackage(String pkg, in IPackageUninstallCallback callback);

    boolean isLockKillDoNotKillAudioEnabled();
    void setLockKillDoNotKillAudioEnabled(boolean enabled);

    int getControlMode();
    void setControlMode(int mode);

    // Server info.
    String getBuildSerial();
    String getBuildVersionName();
    int getBuildVersionCode();

    boolean isAutoAddBlackEnabled();
    void setAutoAddBlackEnable(boolean enable);

    void forceReloadPackages();

    void setPermissionControlEnabled(boolean enabled);
    boolean isPermissionControlEnabled();

    int getPermissionControlBlockModeForPkg(int code, String pkg, boolean log, in String[] payload);
    int getPermissionControlBlockModeForUid(int code, int uid, boolean log, in String[] payload);
    void setPermissionControlBlockModeForPkg(int code, String pkg, int mode);

    void setUserDefinedAndroidId(String id);
    void setUserDefinedDeviceId(String id);
    void setUserDefinedLine1Number(String id);

    String getAndroidId();
    String getDeviceId();
    String getLine1Number();

    String getUserDefinedLine1Number();
    String getUserDefinedDeviceId();
    String getUserDefinedAndroidId();

    boolean isPackageInPrivacyList(String pkg);
    boolean isUidInPrivacyList(int uid);
    int getPrivacyAppsCount();

    String[] getPrivacyList(boolean priv);
    void addOrRemoveFromPrivacyList(String pkg, int op);

    boolean showFocusedActivityInfoEnabled();
    void setShowFocusedActivityInfoEnabled(boolean enabled);

    void restoreDefaultSettings();

    List<RunningServiceInfo> getRunningServices(int max);
    List<RunningAppProcessInfo> getRunningAppProcesses();

    void writeSystemSettings(String key, String value);
    String getSystemSettings(String key);

    long[] getProcessPss(in int[] pids);

    boolean onApplicationUncaughtException(String packageName, String thread, String exception, String trace);
    boolean isAppCrashDumpEnabled();
    void setAppCrashDumpEnabled(boolean enabled);

    void registerOnTopPackageChangeListener(in ITopPackageChangeListener listener);
    void unRegisterOnTopPackageChangeListener(in ITopPackageChangeListener listener);

    boolean isLazyModeEnabled();
    boolean isLazyModeEnabledForPackage(String pkg);
    void setLazyModeEnabled(boolean enabled);

    String[] getLazyApps(boolean lazy);
    void addOrRemoveLazyApps(in String[] packages, int op);

    // Long press back kill.
    void setLPBKEnabled(boolean enabled);
    boolean isLPBKEnabled();

    // Usually called by systemui.
    void onTaskRemoving(int callingUid, int taskId);

    void addOrRemoveActivityFocusAction(in ComponentName comp, in String[] actions, boolean add);
    ComponentName[] getActivityFocusActionComponents();
    String[] getActivityFocusActions(in ComponentName comp);

    void addOrRemoveActivityUnFocusAction(in ComponentName comp, in String[] actions, boolean add);
    ComponentName[] getActivityUnFocusActionComponents();
    String[] getActivityUnFocusActions(in ComponentName comp);

    void setDozeEnabled(boolean enable);
    boolean isDozeEnabled();

    void setForceDozeEnabled(boolean enable);
    boolean isForceDozeEnabled();

    // Deperacated, please use getLastDozeEvent instead.
    long getLastDozeEnterTimeMills();

    DozeEvent getLastDozeEvent();

    long getDozeDelayMills();
    void setDozeDelayMills(long delayMills);

    void setDoNotKillSBNEnabled(boolean enable, String module);// Flag distint which module
    boolean isDoNotKillSBNEnabled(String module);

    void setTaskRemoveKillEnabled(boolean enable);
    boolean isTaskRemoveKillEnabled();

    // Get or Add task remove kill apps.
    String[] getTRKApps(boolean kill);
    void addOrRemoveTRKApps(in String[] packages, int op);

    List<DozeEvent> getDozeEventHistory();

    void setPrivacyEnabled(boolean enable);
    boolean isPrivacyEnabled();

    String[] getPluginApps();
    boolean isAppInPluginList(String pkg);
    void addOrRemovePluginApp(String appPackageName, boolean add);

    boolean hasNotificationForPackage(String pkg);

    int getAppLevel(String pkg);

    String packageForTaskId(int taskId);

    // APP GUARD SERVICE API.

    boolean isAppLockEnabled();
    void setAppLockEnabled(boolean enabled);

    boolean isBlurEnabled();
    boolean isBlurEnabledForPackage(String packageName);
    void setBlurEnabled(boolean enabled);

    int getBlurRadius();
    void setBlurRadius(int r);

    boolean isUninstallInterruptEnabled();
    void setUninstallInterruptEnabled(boolean enabled);

    void setVerifySettings(in VerifySettings settings);

    VerifySettings getVerifySettings();

    void setResult(int transactionID, int res);

    boolean isTransactionValid(int transactionID);

    // For test only.
    void mockCrash();

    void setVerifierPackage(String pkg);

    void injectHomeEvent();

    void setDebug(boolean debug);

    boolean isDebug();

    void onActivityPackageResume(String pkg);

    boolean isInterruptFPEventVBEnabled(int event);
    void setInterruptFPEventVBEnabled(int event, boolean enabled);

    void addOrRemoveComponentReplacement(in ComponentName from, in ComponentName to, boolean add);
    Map getComponentReplacements();

    // void forceReloadPackages();

    String[] getLockApps(boolean lock);
    void addOrRemoveLockApps(in String[] packages, boolean add);

    String[] getBlurApps(boolean lock);
    void addOrRemoveBlurApps(in String[] packages, boolean blur);

    String[] getUPApps(boolean lock);
    void addOrRemoveUPApps(in String[] packages, boolean add);

    // void restoreDefaultSettings();

    // void onTaskRemoving(String pkg);

    // APPGUARD API END.

    AppSettings retrieveAppSettingsForPackage(String pkg);
    void applyAppSettingsForPackage(String pkg, in AppSettings settings);

    // Deprecate.
    void backupTo(String dir);
    // Deprecate.
    void restoreFrom(String dir);

    String[] getRawPermSettings(int page, int countInPage);

    void setAppInstalledAutoApplyTemplate(in AppSettings settings);
    AppSettings getAppInstalledAutoApplyTemplate();

    List<OpLog> getOpLogForPackage(String packageName);
    List<OpLog> getOpLogForOp(int code);
    void clearOpLogForPackage(String packageName);
    void clearOpLogForOp(int cod);

    String getUserName();
    Bitmap getUserIcon();

    void addPendingDisableApps(String pkg);

    // Doze list api.
    void addPowerSaveWhitelistApp(String pkg);
    void removePowerSaveWhitelistApp(String pkg);
    String[] getFullPowerWhitelist();
    String[] getSystemPowerWhitelist();
    String[] getUserPowerWhitelist();

    // Memory api.
    MemoryInfo getMemoryInfo();

    // Keyguard api.
    void enableKeyguard(boolean enabled);

    void exitKeyguardSecurely(in IBooleanCallback1 result);

    void dismissKeyguardLw();

    boolean isKeyguardLocked();

    int getRunningProcessCount();

    boolean hasModuleError();

    // New API since 4.4.6
    void addPendingDisableAppsTR(String pkg);

    // New API since 4.4.8
    void setResidentEnabled(boolean enable);
    boolean isResidentEnabled();
    boolean isResidentEnabledForPackage(String who);
    void addOrRemoveResidentApps(String app, boolean add);
    String[] getResidentApps(boolean resident);

    boolean isPanicHomeEnabled();
    void setPanicHomeEnabled(boolean enable);
    boolean isPanicLockEnabled();
    void setPanicLockEnabled(boolean enable);

    // DPM API
    void lockNow();

    boolean isInRedemptionMode();
    void leaveRedemptionMode();
    void enterRedemptionMode();

    boolean isSELinuxEnabled();
    boolean isSELinuxEnforced();
    void setSelinuxEnforce(boolean enforce);

    boolean isPowerSaveModeEnabled();
    void setPowerSaveModeEnabled(boolean enable);

    String[] getStartRules();
    boolean addOrRemoveStartRules(String rule, boolean add);

    boolean hasSystemError();
    void cleanUpSystemErrorTraces();

    void addAppLockWhiteListActivity(in String[] activities);


    boolean isAutoAddBlackNotificationEnabled();
    void setAutoAddBlackNotificationEnabled(boolean value);

    boolean isOptFeatureEnabled(String tag);
    void setOptFeatureEnabled(String tag, boolean enable);

    // Config/Manigest
    int getRecentTaskExcludeSetting(in ComponentName c);
    void setRecentTaskExcludeSetting(in ComponentName c, int setting);

    int getAppConfigOverlayIntSetting(String appPackageName, String tag);
    void setAppConfigOverlayIntSetting(String appPackageName, String tag, int value);

    void injectPowerEvent();

    String getServiceStarter(in ComponentName service);

    boolean isInactiveAppInsteadOfKillPreferred();
    void setInactiveAppInsteadOfKillPreferred(boolean prefer);

    void mockSystemDead(long delay);

    void clearModuleSettings(String moduleVar);

    // Doze motion.
    boolean isDisableMotionEnabled();
    void setDisableMotionEnabled(boolean enable);

    boolean isGCMSupportPackage(String pkg);

    boolean isShowAppProcessUpdateNotificationEnabled();
    void setShowAppProcessUpdateNotificationEnabled(boolean enabled);

    boolean isStartRuleEnabled();
    void setStartRuleEnabled(boolean enabled);

    // Push message handlers.
    boolean isPushMessageHandlerEnabled(String pkg);
    void setPushMessageHandlerEnabled(String pkg, boolean enabled);

    boolean isPushMessageHandlerShowContentEnabled(String pkg);
    void setPushMessageHandlerShowContentEnabled(String pkg, boolean enabled);

    boolean isPushMessageHandlerNotificationSoundEnabled(String pkg);
    void setPushMessageHandlerNotificationSoundEnabled(String pkg, boolean enabled);

    boolean isPushMessageHandlerNotificationVibrateEnabled(String pkg);
    void setPushMessageHandlerNotificationVibrateEnabled(String pkg, boolean enabled);

    // Whether send notification from App or FW.
    boolean isPushMessageHandlerMessageNotificationByAppEnabled(String pkg);
    void setPushMessageHandlerMessageNotificationByAppEnabled(String pkg, boolean enabled);

    // AIO toggle.
    boolean isPushMessageHandleEnabled();
    void setPushMessageHandleEnabled(boolean enabled);

    boolean isHandlingPushMessageIntent(String packageName);

    // Provide an API to show toast anywhere!!!
    boolean showToast(String message);

    List<BlockRecord2> getStartRecordsForPackage(String pkg);
    void clearStartRecordsForPackage(String pkg);

    boolean isWakeupOnNotificationEnabled();
    void setWakeupOnNotificationEnabled(boolean enable);

    // Lazy rule.
    boolean addOrRemoveLazyRules(String rule, boolean add);
    String[] getLazyRules();

    boolean isLazyRuleEnabled();
    void setLazyRuleEnabled(boolean enable);

    // Multiple Apps.
    // Debug only!!!
    void createMultipleProfile();
    boolean installAppToMultipleAppsUser(String pkgName);
    void startActivityAsUser(in Intent intent, int userId);
    void launchMultipleAppsForPackage(String packageName);

    // Mock GCM Message.
    void mockPushMessageReceived(String pkg, String message);

    // App service control for LAZY mode.
    void registerController(in IServiceControl control);
    void unRegisterController(in IServiceControl control);
    void stopService(in Intent serviceIntent);
    void setAppServiceLazyControlSolution(int solutionFlag, boolean enable);
    boolean isAppServiceLazyControlSolutionEnable(int solutionFlag);

    // Force stop package or force idle?
    void forceIdlePackages(in String[] packages);

    // System prop profiles.
    boolean isSystemPropEnabled();
    void setSystemPropEnabled(boolean enabled);

    void addOrRemoveSystemPropProfile(in SystemPropProfile profile, boolean add);
    Map getSystemPropProfiles();

    void setActiveSystemPropProfileId(String profileId);
    String getActiveSystemPropProfileId();
    SystemPropProfile getActiveSystemPropProfile();
    void addOrRemoveSystemPropProfileApplyApps(in String[] pkgs, boolean add);
    String[] getSystemPropProfileApplyApps(boolean apply);
    boolean isSystemPropProfileApplyApp(String packageName);

    // PM.
    boolean isPackageInstallVerifyEnabled();
    void setPackageInstallVerifyEnabled(boolean enabled);
    String[] getPackageInstallerVerifyRules();
    boolean addOrRemovePackageInstallerVerifyRules(String rule, boolean add);
    void onSourceApkFileDetected(String path, String pkg);

    // Retrieve current top activity package name.
    String getCurrentTopPackage();

    // Tasks.
    void registerTaskRemoveListener(in ITaskRemoveListener listener);
    void unRegisterTaskRemoveListener(in ITaskRemoveListener listener);

    // Usages.
    void setAppInactive(String packageName, boolean inactive, int userId);
    boolean isAppInactive(String packageName, int userId);

    // Package manage.
    void forceStopPackage(String packageName);

    // Inactive policy.
    void setAppInactivePolicyForModule(String module, int policy);
    int getAppInactivePolicyForModule(String module);

    // Input.
    void executeInputCommand(in String[] args);

    // Screenshots.
    void takeLongScreenShot();

    // Backup And Restore.
    IBackupAgent getBackupAgent();

    // Reboot action.
    void showRebootNeededNotification(in String why);

    // JS.
    void evaluateJsString(in String[] args, in IJsEvaluateListener listener);
    // JS store.
    JavaScript getSavedJs(String id);
    List<JavaScript> getSavedJses();
    void saveJs(in JavaScript js);
    void deleteJs(in JavaScript js);

    // PM
    PackageInfo getPackageInfoForPackage(String pkgName);
    ApplicationInfo getApplicationInfoForPackage(String pkgName);
    String getPackageNameForUid(int uid);

    // Package manage #2.
    void killBackgroundProcesses(String packageName);

    // Templates for ops.
    AppOpsTemplate getAppOpsTemplateById(String id);
    void addAppOpsTemplate(in AppOpsTemplate template);
    void removeAppOpsTemplate(in AppOpsTemplate template);
    List<AppOpsTemplate> getAppOpsTemplates();

    // MiPush.
    boolean isMiPushSupportPackage(String pkg);

    // Query name from task id.
    ComponentName componentNameForTaskId(int taskId);

    void reportBlurBadPerformance(long timeTaken);

    boolean isRedemptionModeEnabled();
    void setRedemptionModeEnabled(boolean enabled);
}
