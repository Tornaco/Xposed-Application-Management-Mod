package github.tornaco.xposedmoduletest.ui.activity.blur;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;

import github.tornaco.permission.requester.RuntimePermissions;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.activity.ag.GuardSettingsActivity;
import github.tornaco.xposedmoduletest.ui.widget.BlurRadiusPreference;
import github.tornaco.xposedmoduletest.xposed.app.XAppGuardManager;

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
            if (XAppGuardManager.get().isServiceAvailable()) {
                final BlurRadiusPreference blurRadiusPreference = (BlurRadiusPreference) findPreference("blur_radius");
                blurRadiusPreference.setCurrentRadius(XAppGuardManager.get()
                        .getBlurRadius());
                blurRadiusPreference.setOnSeekCompleteListener(new BlurRadiusPreference.OnSeekCompleteListener() {
                    @Override
                    public void onSeekComplete(int progress) {
                        XAppGuardManager.get().setBlurRadius(progress);
                    }
                });
            } else {
                getPreferenceScreen().setEnabled(false);
            }
        }
    }

}
