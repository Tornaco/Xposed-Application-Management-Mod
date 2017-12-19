package github.tornaco.xposedmoduletest.ui.activity.ag;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;

import github.tornaco.permission.requester.RequiresPermission;
import github.tornaco.permission.requester.RuntimePermissions;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.provider.XKey;
import github.tornaco.xposedmoduletest.ui.activity.PhotoViewerActivity;
import github.tornaco.xposedmoduletest.xposed.app.XAppGuardManager;
import github.tornaco.xposedmoduletest.xposed.bean.VerifySettings;

/**
 * Created by guohao4 on 2017/11/2.
 * Email: Tornaco@163.com
 */

@RuntimePermissions
public class SecureGuardSettings extends GuardSettingsActivity {
    @Override
    protected Fragment onCreateSettingsFragment() {
        return new SecureSettingsFragment();
    }

    public static class SecureSettingsFragment extends GuardSettingsActivity.SettingsFragment {

        private VerifySettings verifySettings = null;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.ag_secure);

            Preference lockSettingsPref = findPreference("verify_method");
            lockSettingsPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(getActivity(), PatternSetupActivity.class));
                    return true;
                }
            });


            if (XAppGuardManager.get().isServiceAvailable()) {

                verifySettings = XAppGuardManager.get().getVerifySettings();
                if (verifySettings == null) verifySettings = new VerifySettings();

                final boolean verifyOnHome = verifySettings.isVerifyOnAppSwitch();
                SwitchPreference homePref = (SwitchPreference) findPreference("ver_on_home");
                homePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        boolean v = (boolean) newValue;
                        verifySettings.setVerifyOnAppSwitch(v);
                        XAppGuardManager.get().setVerifySettings(verifySettings);
                        return true;
                    }
                });
                homePref.setChecked(verifyOnHome);
                boolean verifyOnScreen = verifySettings != null && verifySettings.isVerifyOnScreenOff();
                SwitchPreference screenPref = (SwitchPreference) findPreference("ver_on_screenoff");
                screenPref.setChecked(verifyOnScreen);
                screenPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        boolean v = (boolean) newValue;
                        verifySettings.setVerifyOnScreenOff(v);
                        XAppGuardManager.get().setVerifySettings(verifySettings);
                        return true;
                    }
                });


                SwitchPreference photoPref = (SwitchPreference) findPreference(XKey.TAKE_PHOTO_ENABLED);

                if (photoPref != null)
                    photoPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            SecureGuardSettingsPermissionRequester
                                    .requestCameraPermissionChecked((SecureGuardSettings) getActivity());
                            return true;
                        }
                    });

                findPreference("key_view_photos")
                        .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                            @Override
                            public boolean onPreferenceClick(Preference preference) {
                                startActivity(new Intent(getActivity(), PhotoViewerActivity.class));
                                return true;
                            }
                        });

            } else {
                getPreferenceScreen().setEnabled(false);
            }
        }
    }

    @RequiresPermission({Manifest.permission.CAMERA})
    void requestCameraPermission() {

    }

    @TargetApi(Build.VERSION_CODES.M)
    @RequiresPermission({Manifest.permission.USE_FINGERPRINT})
    void requestFPPermission() {

    }
}
