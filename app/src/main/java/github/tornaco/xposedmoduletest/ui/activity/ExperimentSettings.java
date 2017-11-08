package github.tornaco.xposedmoduletest.ui.activity;

import android.app.Fragment;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.SettingsActivity;
import github.tornaco.xposedmoduletest.x.app.XAppGuardManager;
import github.tornaco.xposedmoduletest.x.bean.BlurSettings;

/**
 * Created by guohao4 on 2017/11/2.
 * Email: Tornaco@163.com
 */

public class ExperimentSettings extends SettingsActivity {
    @Override
    protected Fragment onCreateSettingsFragment() {
        return new SecureSettingsFragment();
    }

    public static class SecureSettingsFragment extends SettingsFragment {

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.exp);

            if (XAppGuardManager.defaultInstance().isServiceAvailable()) {
                BlurSettings blurSettings = XAppGuardManager.defaultInstance()
                        .getBlurSettings();
                if (blurSettings == null) blurSettings = new BlurSettings();

                final BlurSettings finalBlurSettings = blurSettings;

                SwitchPreference blurPref = (SwitchPreference) findPreference("blur_enabled");
                blurPref.setChecked(blurSettings.isEnabled());
                blurPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        boolean v = (boolean) newValue;
                        finalBlurSettings.setEnabled(v);
                        XAppGuardManager.defaultInstance().setBlurSettings(finalBlurSettings);
                        return true;
                    }
                });

                SwitchPreference blurAllPref = (SwitchPreference) findPreference("blur_all_enabled");
                blurAllPref.setChecked(blurSettings.getPolicy() == XAppGuardManager.BlurPolicy.BLUR_ALL);
                blurAllPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        boolean v = (boolean) newValue;
                        finalBlurSettings.setPolicy(v ? XAppGuardManager.BlurPolicy.BLUR_ALL : XAppGuardManager.BlurPolicy.BLUR_WATCHED);
                        XAppGuardManager.defaultInstance().setBlurSettings(finalBlurSettings);
                        return true;
                    }
                });


                SwitchPreference uninstallPref = (SwitchPreference) findPreference("key_app_uninstall_pro_enabled");
                uninstallPref.setChecked(XAppGuardManager.defaultInstance().isUninstallInterruptEnabled());
                uninstallPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        boolean v = (boolean) newValue;
                        XAppGuardManager.defaultInstance().setUninstallInterruptEnabled(v);
                        return true;
                    }
                });

            }


        }
    }
}
