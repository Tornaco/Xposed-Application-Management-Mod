package github.tornaco.xposedmoduletest.ui.activity.ag;

import android.app.Fragment;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.xposed.app.XAppGuardManager;
import github.tornaco.xposedmoduletest.xposed.bean.BlurSettings;

/**
 * Created by guohao4 on 2017/11/2.
 * Email: Tornaco@163.com
 */

public class ExperimentGuardSettings extends GuardSettingsActivity {
    @Override
    protected Fragment onCreateSettingsFragment() {
        return new SecureSettingsFragment();
    }

    public static class SecureSettingsFragment extends SettingsFragment {

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.ag_exp);


            if (!XAppGuardManager.singleInstance().isServiceAvailable()) {
                getPreferenceScreen().setEnabled(false);
                return;
            }


            BlurSettings blurSettings = XAppGuardManager.singleInstance()
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
                    if (finalBlurSettings.getPolicy() != XAppGuardManager.BlurPolicy.BLUR_ALL) {
                        finalBlurSettings.setPolicy(XAppGuardManager.BlurPolicy.BLUR_WATCHED);
                    }
                    XAppGuardManager.singleInstance().setBlurSettings(finalBlurSettings);
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
                    XAppGuardManager.singleInstance().setBlurSettings(finalBlurSettings);
                    return true;
                }
            });


            SwitchPreference uninstallPref = (SwitchPreference) findPreference("key_app_uninstall_pro_enabled");
            uninstallPref.setChecked(XAppGuardManager.singleInstance().isUninstallInterruptEnabled());
            uninstallPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    boolean v = (boolean) newValue;
                    XAppGuardManager.singleInstance().setUninstallInterruptEnabled(v);
                    return true;
                }
            });


        }
    }
}
