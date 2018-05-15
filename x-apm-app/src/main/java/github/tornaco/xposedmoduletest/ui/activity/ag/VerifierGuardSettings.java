package github.tornaco.xposedmoduletest.ui.activity.ag;

import android.app.Fragment;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.xposed.app.XAppLockManager;

/**
 * Created by guohao4 on 2017/11/2.
 * Email: Tornaco@163.com
 */

public class VerifierGuardSettings extends GuardSettingsActivity {
    @Override
    protected Fragment onCreateSettingsFragment() {
        return new SecureSettingsFragment();
    }

    public static class SecureSettingsFragment extends SettingsFragment {

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.ag_verifier);

            if (!XAppLockManager.get().isServiceAvailable()) {
                getPreferenceScreen().setEnabled(false);
            } else {
                SwitchPreference switchPreference = (SwitchPreference) findPreference("unlock_vibrate_enabled");
                switchPreference.setChecked(!XAppLockManager.get().isInterruptFPEventVBEnabled(XAppLockManager.FPEvent.SUCCESS));
                switchPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        boolean interrupt = !(boolean) newValue;
                        XAppLockManager.get().setInterruptFPEventVBEnabled(XAppLockManager.FPEvent.SUCCESS, interrupt);
                        return true;
                    }
                });

                switchPreference = (SwitchPreference) findPreference("error_vibrate_enabled");
                switchPreference.setChecked(!XAppLockManager.get().isInterruptFPEventVBEnabled(XAppLockManager.FPEvent.ERROR));
                switchPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        boolean interrupt = !(boolean) newValue;
                        XAppLockManager.get().setInterruptFPEventVBEnabled(XAppLockManager.FPEvent.ERROR, interrupt);
                        return true;
                    }
                });
            }

        }

    }
}
