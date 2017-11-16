package github.tornaco.xposedmoduletest.ui.activity;

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
            addPreferencesFromResource(R.xml.advanced);

            if (!XAppGuardManager.singleInstance().isServiceAvailable()) {
                getPreferenceScreen().setEnabled(false);
                return;
            }


            findPreference("key_test_noter")
                    .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            VerifyDisplayerActivity.startAsTest(getActivity());
                            return true;
                        }
                    });

//            SwitchPreference hideAppIcon = (SwitchPreference) findPreference("key_hide_app_icon");
//            hideAppIcon.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//                @Override
//                public boolean onPreferenceChange(Preference preference, Object newValue) {
//                    boolean enabled = (boolean) newValue;
//                    XApp.getApp().hideAppIcon(enabled);
//                    ProgressDialog p = new ProgressDialog(getActivity());
//                    p.setMessage("&*^$%$(-)$##@%%%%^-^");
//                    p.setIndeterminate(true);
//                    p.setCancelable(false);
//                    p.show();
//                    BaseActivity b = (BaseActivity) getActivity();
//                    b.getUIThreadHandler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            getActivity().finishAffinity();
//                        }
//                    }, 8 * 1000);
//                    return true;
//                }
//            });

            final StringBuilder moduleStatus = new StringBuilder();
            Collections.consumeRemaining(XAppGuardManager.singleInstance().getSubModules(),
                    new Consumer<String>() {
                        @Override
                        public void accept(String s) {
                            moduleStatus.append(s)
                                    .append(": ")
                                    .append(SubModule.SubModuleStatus.valueOf(XAppGuardManager.singleInstance().getSubModuleStatus(s)))
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

//            SwitchPreference debugPref = (SwitchPreference) findPreference("dev_mode_enabled");
//            debugPref.setChecked(XAppGuardManager.singleInstance().isDebug());
//            debugPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//                @Override
//                public boolean onPreferenceChange(Preference preference, Object newValue) {
//                    boolean v = (boolean) newValue;
//                    XAppGuardManager.singleInstance().setDebug(v);
//                    return true;
//                }
//            });
        }
    }
}
