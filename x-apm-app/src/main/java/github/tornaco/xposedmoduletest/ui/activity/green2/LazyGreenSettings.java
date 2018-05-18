package github.tornaco.xposedmoduletest.ui.activity.green2;

import android.app.Fragment;
import android.os.Bundle;
import android.preference.Preference;
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
public class LazyGreenSettings extends GuardSettingsActivity {
    @Override
    protected Fragment onCreateSettingsFragment() {
        return new SecureSettingsFragment();
    }

    public static class SecureSettingsFragment extends SettingsFragment {

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.lazy_general);
            if (XAPMManager.get().isServiceAvailable()) {
                SwitchPreference doNotKillSBNPref = (SwitchPreference) findPreference("do_not_kill_sbn");
                doNotKillSBNPref.setChecked(XAPMManager.get().isDoNotKillSBNEnabled(XAppBuildVar.APP_GREEN));
                doNotKillSBNPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        boolean enabled = (boolean) newValue;
                        XAPMManager.get().setDoNotKillSBNEnabled(enabled, XAppBuildVar.APP_GREEN);
                        return true;
                    }
                });
            } else {
                getPreferenceScreen().setEnabled(false);
            }
        }
    }

}
