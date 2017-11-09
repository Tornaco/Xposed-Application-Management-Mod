package github.tornaco.xposedmoduletest.ui.activity;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.SettingsActivity;
import github.tornaco.xposedmoduletest.x.app.XAppGuardManager;

/**
 * Created by guohao4 on 2017/11/2.
 * Email: Tornaco@163.com
 */

public class VerifierSettings extends SettingsActivity {
    @Override
    protected Fragment onCreateSettingsFragment() {
        return new SecureSettingsFragment();
    }

    public static class SecureSettingsFragment extends SettingsFragment {

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.verifier);


            if (!XAppGuardManager.defaultInstance().isServiceAvailable()) {
                getPreferenceScreen().setEnabled(false);
                return;
            }

        }

    }
}
