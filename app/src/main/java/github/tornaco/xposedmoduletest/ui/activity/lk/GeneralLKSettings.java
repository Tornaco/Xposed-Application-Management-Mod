package github.tornaco.xposedmoduletest.ui.activity.lk;

import android.app.Fragment;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.support.annotation.Nullable;

import github.tornaco.permission.requester.RuntimePermissions;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.activity.ag.GuardSettingsActivity;
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
            if (XAshmanManager.singleInstance().isServiceAvailable()) {
                final ListPreference delayPref = (ListPreference) findPreference("key_lk_delay");
                long delay = XAshmanManager.singleInstance().getLockKillDelay();
                int sec = (int) (delay / 1000);
                delayPref.setValue(String.valueOf(sec));
                delayPref.setSummary(delayPref.getEntries()[delayPref.findIndexOfValue(String.valueOf(sec))]);
                delayPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        String vs = (String) newValue;
                        int sec = Integer.parseInt(vs);
                        long mills = sec * 1000;
                        XAshmanManager.singleInstance().setLockKillDelay(mills);
                        delayPref.setSummary(delayPref.getEntries()[delayPref.findIndexOfValue(String.valueOf(sec))]);
                        return true;
                    }
                });
            } else {
                getPreferenceScreen().setEnabled(false);
            }
        }
    }

}
