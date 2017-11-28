package github.tornaco.xposedmoduletest.ui.activity.ag;

import android.app.Fragment;
import android.os.Bundle;
import android.preference.Preference;
import android.support.annotation.Nullable;

import github.tornaco.android.common.Collections;
import github.tornaco.android.common.Consumer;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.xposed.app.XAppGuardManager;
import github.tornaco.xposedmoduletest.xposed.submodules.SubModule;

/**
 * Created by guohao4 on 2017/11/2.
 * Email: Tornaco@163.com
 */

public class AdvancedGuardSettings extends GuardSettingsActivity {
    @Override
    protected Fragment onCreateSettingsFragment() {
        return new SecureSettingsFragment();
    }

    public static class SecureSettingsFragment extends SettingsFragment {

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.ag_advanced);

            if (!XAppGuardManager.singleInstance().isServiceAvailable()) {
                getPreferenceScreen().setEnabled(false);
                return;
            }

            final StringBuilder moduleStatus = new StringBuilder();
            Collections.consumeRemaining(XAppGuardManager.singleInstance().getSubModules(),
                    new Consumer<String>() {
                        @Override
                        public void accept(String s) {
                            moduleStatus.append(s)
                                    .append(": ")
                                    .append(SubModule.SubModuleStatus.valueOf(XAppGuardManager.singleInstance()
                                            .getSubModuleStatus(s)))
                                    .append("\n");
                        }
                    });
            findPreference("key_dump_module")
                    .setSummary(moduleStatus.toString());

            findPreference("key_crash_module")
                    .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            XAppGuardManager.singleInstance().mockCrash();
                            return true;
                        }
                    });
        }
    }
}
