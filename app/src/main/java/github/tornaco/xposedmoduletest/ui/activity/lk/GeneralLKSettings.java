package github.tornaco.xposedmoduletest.ui.activity.lk;

import android.app.Fragment;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;

import github.tornaco.permission.requester.RuntimePermissions;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.activity.ag.GuardSettingsActivity;
import github.tornaco.xposedmoduletest.xposed.XAppBuildVar;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;

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
            if (XAshmanManager.get().isServiceAvailable()) {
                final ListPreference delayPref = (ListPreference) findPreference("key_lk_delay");
                long delay = XAshmanManager.get().getLockKillDelay();
                int sec = (int) (delay / 1000);
                delayPref.setValue(String.valueOf(sec));
                delayPref.setSummary(delayPref.getEntries()[delayPref.findIndexOfValue(String.valueOf(sec))]);
                delayPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        String vs = (String) newValue;
                        int sec = Integer.parseInt(vs);
                        long mills = sec * 1000;
                        XAshmanManager.get().setLockKillDelay(mills);
                        delayPref.setSummary(delayPref.getEntries()[delayPref.findIndexOfValue(String.valueOf(sec))]);
                        return true;
                    }
                });

                SwitchPreference doNotKillAudioPref = (SwitchPreference) findPreference("do_not_kill_audio");
                doNotKillAudioPref.setChecked(XAshmanManager.get().isLockKillDoNotKillAudioEnabled());
                doNotKillAudioPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        boolean enabled = (boolean) newValue;
                        XAshmanManager.get().setLockKillDoNotKillAudioEnabled(enabled);
                        return true;
                    }
                });

                SwitchPreference doNotKillSBNPref = (SwitchPreference) findPreference("do_not_kill_sbn");
                doNotKillSBNPref.setChecked(XAshmanManager.get().isDoNotKillSBNEnabled(XAppBuildVar.APP_LK));
                doNotKillSBNPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        boolean enabled = (boolean) newValue;
                        XAshmanManager.get().setDoNotKillSBNEnabled(enabled, XAppBuildVar.APP_LK);
                        return true;
                    }
                });
            } else {
                getPreferenceScreen().setEnabled(false);
            }
        }
    }

}
