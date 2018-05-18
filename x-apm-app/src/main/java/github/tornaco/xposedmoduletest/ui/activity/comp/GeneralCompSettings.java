package github.tornaco.xposedmoduletest.ui.activity.comp;

import android.app.Fragment;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;

import github.tornaco.permission.requester.RuntimePermissions;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.activity.ag.GuardSettingsActivity;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;

/**
 * Created by guohao4 on 2017/11/2.
 * Email: Tornaco@163.com
 */

@RuntimePermissions
public class GeneralCompSettings extends GuardSettingsActivity {
    @Override
    protected Fragment onCreateSettingsFragment() {
        return new SecureSettingsFragment();
    }

    public static class SecureSettingsFragment extends SettingsFragment {

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.comp_general);
            if (XAPMManager.get().isServiceAvailable()) {
                SwitchPreference switchPreference = (SwitchPreference) findPreference("comp_setting_block");
                switchPreference.setChecked(XAPMManager.get().isCompSettingBlockEnabledEnabled());
                switchPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        boolean block = (boolean) newValue;
                        XAPMManager.get().setCompSettingBlockEnabled(block);
                        return true;
                    }
                });
            } else {
                getPreferenceScreen().setEnabled(false);
            }
        }
    }

}
