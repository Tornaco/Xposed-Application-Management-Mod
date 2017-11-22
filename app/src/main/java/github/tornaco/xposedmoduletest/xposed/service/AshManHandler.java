package github.tornaco.xposedmoduletest.xposed.service;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.RemoteException;

import github.tornaco.apigen.CreateMessageIdWithMethods;
import github.tornaco.xposedmoduletest.IAshmanWatcher;
import github.tornaco.xposedmoduletest.IProcessClearListener;
import github.tornaco.xposedmoduletest.xposed.util.XLog;
import lombok.Getter;

/**
 * Created by guohao4 on 2017/11/1.
 * Email: Tornaco@163.com
 */
@CreateMessageIdWithMethods(fallbackMessageDecode = "UNKNOWN")
interface AshManHandler {

    void setBootBlockEnabled(boolean enabled);

    void setStartBlockEnabled(boolean enabled);

    void setLockKillEnabled(boolean enabled);

    void clearProcess(IProcessClearListener listener);

    void clearBlockRecords();

    void setLockKillDelay(long delay);

    void onScreenOff();

    void onScreenOn();

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

    void watch(WatcherClient w);

    void unWatch(WatcherClient w);

    void notifyStartBlock(String pkg);

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
            XLog.logF("WatcherClient, binder die...");
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
