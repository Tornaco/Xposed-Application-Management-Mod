package github.tornaco.xposedmoduletest.xposed.service;

import android.content.ComponentName;
import android.content.pm.PackageManager;

import github.tornaco.apigen.CreateMessageIdWithMethods;
import github.tornaco.xposedmoduletest.IProcessClearListener;

/**
 * Created by guohao4 on 2017/11/1.
 * Email: Tornaco@163.com
 */
@CreateMessageIdWithMethods(fallbackMessageDecode = "UNKNOWN")
interface IntentFirewallHandler {

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
}
