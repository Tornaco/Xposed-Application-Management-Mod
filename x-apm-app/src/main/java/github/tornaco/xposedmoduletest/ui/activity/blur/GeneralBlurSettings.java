package github.tornaco.xposedmoduletest.ui.activity.blur;

import android.app.Fragment;
import android.os.Bundle;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;

import github.tornaco.permission.requester.RuntimePermissions;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.activity.ag.GuardSettingsActivity;
import github.tornaco.xposedmoduletest.ui.widget.BlurRadiusPreference;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.app.XAppLockManager;

/**
 * Created by guohao4 on 2017/11/2.
 * Email: Tornaco@163.com
 */

@RuntimePermissions
public class GeneralBlurSettings extends GuardSettingsActivity {
    @Override
    protected Fragment onCreateSettingsFragment() {
        return new SecureSettingsFragment();
    }

    public static class SecureSettingsFragment extends SettingsFragment {

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.blur);
            if (XAppLockManager.get().isServiceAvailable()) {
                final BlurRadiusPreference blurRadiusPreference = (BlurRadiusPreference) findPreference("blur_radius");
                blurRadiusPreference.setCurrentRadius(XAppLockManager.get()
                        .getBlurRadius());
                blurRadiusPreference.setOnSeekCompleteListener(progress -> XAppLockManager.get().setBlurRadius(progress));

                SwitchPreference blurOptPreference = (SwitchPreference) findPreference("opt_blur_cache");
                blurOptPreference.setChecked(XAPMManager.get().isOptFeatureEnabled(XAPMManager.OPT.OPT_BLUR_CACHE.name()));
                blurOptPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                    boolean enable = (boolean) newValue;
                    XAPMManager.get().setOptFeatureEnabled(XAPMManager.OPT.OPT_BLUR_CACHE.name(), enable);
                    return true;
                });
            } else {
                getPreferenceScreen().setEnabled(false);
            }
        }
    }

}
