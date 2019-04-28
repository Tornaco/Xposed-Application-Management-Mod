package github.tornaco.xposedmoduletest.xposed.service;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.RemoteException;

import github.tornaco.apigen.CreateMessageIdWithMethods;
import github.tornaco.xposedmoduletest.IAshmanWatcher;
import github.tornaco.xposedmoduletest.IProcessClearListener;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;
import lombok.Getter;

/**
 * Created by guohao4 on 2017/11/1.
 * Email: Tornaco@163.com
 */
@CreateMessageIdWithMethods(fallbackMessageDecode = "UNKNOWN")
interface AshManHandler {
    void setPrivacyEnabled(boolean enable);

    void setWhiteSysAppEnabled(boolean enabled);

    void setDoNotKillSBNEnabled(boolean enable, String module);

    void setTaskRemoveKillEnabled(boolean enable);

    void setBootBlockEnabled(boolean enabled);

    void setStartBlockEnabled(boolean enabled);

    void setStartRuleEnabled(boolean enabled);

    void setLockKillEnabled(boolean enabled);

    void setShowAppProcessUpdateNotificationEnabled(boolean enabled);

    void setLockKillDoNotKillAudioEnabled(boolean enabled);

    void setLazyModeEnabled(boolean enabled);

    void setLazyRuleEnabled(boolean enabled);

    void setAppServiceLazyControlSolution(int solutionFlags, boolean enabled);

    void setRFKillEnabled(boolean enabled);

    void setLPBKEnabled(boolean enabled);

    void setCompSettingBlockEnabled(boolean enabled);

    void clearProcess(IProcessClearListener listener, boolean doNotClearWhenIntervative, boolean onlyForThoseInList);

    void clearBlockRecords();

    void setLockKillDelay(long delay);

    void onScreenOff();

    void onScreenOn();

    void restrictAppOnData(int uid, boolean restrict, boolean force);

    void restrictAppOnWifi(int uid, boolean restrict, boolean force);

    /**
     * Set the enabled setting for a package component (activity, receiver, service, provider).
     * This setting will override any enabled state which may have been set by the component in its
     * manifest.
     *
     * @param componentName The component to enable
     * @param newState      The new enabled state for the component.
     * @param flags         Optional behavior flags.
     */
    void setComponentEnabledSetting(ComponentName componentName,
                                    @PackageManager.EnabledState int newState,
                                    @PackageManager.EnabledFlags int flags);

    /**
     * Return the enabled setting for a package component (activity,
     * receiver, service, provider).  This returns the last value set by
     * {@link #setComponentEnabledSetting(ComponentName, int, int)}; in most
     * cases this value will be {@link PackageManager#COMPONENT_ENABLED_STATE_DEFAULT} since
     * the value originally specified in the manifest has not been modified.
     *
     * @param componentName The component to retrieve.
     * @return Returns the current enabled state for the component.
     */
    @PackageManager.EnabledState
    int getComponentEnabledSetting(
            ComponentName componentName);

    /**
     * Set the enabled setting for an application
     * This setting will override any enabled state which may have been set by the application in
     * its manifest.  It also overrides the enabled state set in the manifest for any of the
     * application's components.  It does not override any enabled state set by
     * {@link #setComponentEnabledSetting} for any of the application's components.
     *
     * @param packageName The package name of the application to enable
     * @param newState    The new enabled state for the application.
     * @param flags       Optional behavior flags.
     */
    void setApplicationEnabledSetting(String packageName,
                                      int newState, int flags, boolean tmp);

    /**
     * Return the enabled setting for an application. This returns
     * the last value set by
     * {@link PackageManager#setApplicationEnabledSetting(String, int, int)}; in most
     * cases this value will be {@link PackageManager#COMPONENT_ENABLED_STATE_DEFAULT} since
     * the value originally specified in the manifest has not been modified.
     *
     * @param packageName The package name of the application to retrieve.
     * @return Returns the current enabled state for the application.
     * @throws IllegalArgumentException if the named package does not exist.
     */
    int getApplicationEnabledSetting(String packageName);

    void watch(WatcherClient w);

    void unWatch(WatcherClient w);

    void notifyStartBlock(String pkg);

    /**
     * Set policy flags for specific UID.
     */
    void setNetworkPolicyUidPolicy(int uid, int policy);

    void onAudioFocusedPackageChanged(String who);

    void onAudioFocusedPackageAbandoned(String who);

    void setControlMode(int mode);

    void setResidentEnabled(boolean enabled);

    void setPowerSaveModeEnabled(boolean enabled);

    void setPanicHomeEnabled(boolean enabled);

    void setPanicLockEnabled(boolean enabled);

    void setAutoAddBlackEnable(boolean enable);

    void setAutoAddBlackNotificationEnabled(boolean value);

    void forceReloadPackages();

    void setPermissionControlEnabled(boolean enabled);

    void setUserDefinedAndroidId(String id);

    void setUserDefinedDeviceId(String id);

    void setUserDefinedLine1Number(String id);

    void setShowFocusedActivityInfoEnabled(boolean enabled);

    void setGreeningEnabled(boolean enabled);

    void restoreDefaultSettings();

    void onApplicationUncaughtException(String packageName, String thread, String exception, String trace);

    void setAppCrashDumpEnabled(boolean enabled);

    void setWakeupOnNotificationEnabled(boolean enable);

    @Getter
    class WatcherClient implements IBinder.DeathRecipient {

        private IAshmanWatcher watcher;

        private boolean alive = true;

        public WatcherClient(IAshmanWatcher watcher) {
            this.watcher = watcher;
            try {
                this.watcher.asBinder().linkToDeath(this, 0);
            } catch (RemoteException ignored) {

            }
        }

        private void unLinkToDeath() {
            if (alive) {
                this.watcher.asBinder().unlinkToDeath(this, 0);
            }
        }

        @Override
        public void binderDied() {
            alive = false;
            unLinkToDeath();
            XposedLog.wtf("WatcherClient, binder die...");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            WatcherClient that = (WatcherClient) o;

            return watcher.equals(that.watcher);
        }

        @Override
        public int hashCode() {
            return watcher.hashCode();
        }
    }
}
