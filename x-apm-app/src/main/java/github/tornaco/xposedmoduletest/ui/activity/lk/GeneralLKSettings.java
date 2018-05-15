package github.tornaco.xposedmoduletest.ui.activity.lk;

import android.app.Fragment;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;

import github.tornaco.permission.requester.RuntimePermissions;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.activity.ag.GuardSettingsActivity;
import github.tornaco.xposedmoduletest.xposed.XAppBuildVar;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;

/**
 * Created by guohao4 on 2017/11/2.
 * Email: Tornaco@163.com
 */

@RuntimePermissions
public class GeneralLKSettings extends GuardSettingsActivity {
    @Override
    protected Fragment onCreateSettingsFragment() {
        return new SecureSettingsFragment();
    }

    public static class SecureSettingsFragment extends SettingsFragment {

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.lk_general);
            if (XAPMManager.get().isServiceAvailable()) {
                final ListPreference delayPref = (ListPreference) findPreference("key_lk_delay");
                long delay = XAPMManager.get().getLockKillDelay();
                int sec = (int) (delay / 1000);
                delayPref.setValue(String.valueOf(sec));
                delayPref.setSummary(delayPref.getEntries()[delayPref.findIndexOfValue(String.valueOf(sec))]);
                delayPref.setOnPreferenceChangeListener((preference, newValue) -> {
                    String vs = (String) newValue;
                    int sec1 = Integer.parseInt(vs);
                    long mills = sec1 * 1000;
                    XAPMManager.get().setLockKillDelay(mills);
                    delayPref.setSummary(delayPref.getEntries()[delayPref.findIndexOfValue(String.valueOf(sec1))]);
                    return true;
                });

                SwitchPreference doNotKillAudioPref = (SwitchPreference) findPreference("do_not_kill_audio");
                doNotKillAudioPref.setChecked(XAPMManager.get().isLockKillDoNotKillAudioEnabled());
                doNotKillAudioPref.setOnPreferenceChangeListener((preference, newValue) -> {
                    boolean enabled = (boolean) newValue;
                    XAPMManager.get().setLockKillDoNotKillAudioEnabled(enabled);
                    return true;
                });

                SwitchPreference doNotKillSBNPref = (SwitchPreference) findPreference("do_not_kill_sbn");
                doNotKillSBNPref.setChecked(XAPMManager.get().isDoNotKillSBNEnabled(XAppBuildVar.APP_LK));
                doNotKillSBNPref.setOnPreferenceChangeListener((preference, newValue) -> {
                    boolean enabled = (boolean) newValue;
                    XAPMManager.get().setDoNotKillSBNEnabled(enabled, XAppBuildVar.APP_LK);
                    return true;
                });

                SwitchPreference showAppProcessUpdate = (SwitchPreference) findPreference("show_app_process_update");
                showAppProcessUpdate.setChecked(XAPMManager.get().isShowAppProcessUpdateNotificationEnabled());
                showAppProcessUpdate.setOnPreferenceChangeListener((preference, newValue) -> {
                    boolean enabled = (boolean) newValue;
                    XAPMManager.get().setShowAppProcessUpdateNotificationEnabled(enabled);
                    return true;
                });
            } else {
                getPreferenceScreen().setEnabled(false);
            }
        }
    }

}
