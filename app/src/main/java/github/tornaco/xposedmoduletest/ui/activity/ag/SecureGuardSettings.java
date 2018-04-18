package github.tornaco.xposedmoduletest.ui.activity.ag;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;

import github.tornaco.permission.requester.RequiresPermission;
import github.tornaco.permission.requester.RuntimePermissions;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.provider.AppKey;
import github.tornaco.xposedmoduletest.provider.AppSettings;
import github.tornaco.xposedmoduletest.provider.LockStorage;
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

        private void onRequestSetupSecurePassport() {
            final int[] selection = {-1};
            String[] choice = new String[]{"数字密码", "图案密码"};
            new AlertDialog.Builder(getActivity())
                    .setSingleChoiceItems(choice, selection[0], new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            selection[0] = which;
                            if (selection[0] == 0) {
                                PinSetupActivity.start(getActivity());
                            } else {
                                PatternSetupActivity.start(getActivity());
                            }
                            dialog.dismiss();
                            getActivity().finish();
                        }
                    })
                    .show();
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.ag_secure);

            Preference lockSettingsPref = findPreference("verify_method");
            LockStorage.LockMethod lockMethod = LockStorage.getLockMethod(getActivity());

            switch (lockMethod) {
                case Pin:
                    lockSettingsPref.setSummary("数字密码");
                    break;
                case Pattern:
                    lockSettingsPref.setSummary("图案密码");
                    break;
            }

            lockSettingsPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    onRequestSetupSecurePassport();
                    return true;
                }
            });

            SwitchPreference workaround = (SwitchPreference) findPreference(AppKey.APPLOCK_WORKAROUND);
            workaround.setChecked(AppSettings.isAppLockWorkaroundEnabled(getActivity()));

            if (XAppGuardManager.get().isServiceAvailable()) {

                verifySettings = XAppGuardManager.get().getVerifySettings();
                if (verifySettings == null) verifySettings = new VerifySettings();

                final boolean _sp = LockStorage.checkSP(getActivity());
                SwitchPreference spPref = (SwitchPreference) findPreference("sp_enabled");
                spPref.setChecked(_sp);


                if (lockMethod != LockStorage.LockMethod.Pattern)
                    ((PreferenceCategory) findPreference("ags")).removePreference(spPref);
                spPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        boolean v = (boolean) newValue;
                        LockStorage.setSP(getActivity(), v);
                        return true;
                    }
                });

                final boolean verifyOnHome = verifySettings.isVerifyOnAppSwitch();
                SwitchPreference homePref = (SwitchPreference) findPreference("ver_on_home");
                homePref.setChecked(verifyOnHome);
                homePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        boolean v = (boolean) newValue;
                        verifySettings.setVerifyOnAppSwitch(v);
                        XAppGuardManager.get().setVerifySettings(verifySettings);
                        return true;
                    }
                });

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

                boolean verifyOnTaskRemoved = verifySettings != null && verifySettings.isVerifyOnTaskRemoved();
                SwitchPreference taskPref = (SwitchPreference) findPreference("ver_on_task_removed");
                taskPref.setChecked(verifyOnTaskRemoved);
                taskPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        boolean v = (boolean) newValue;
                        verifySettings.setVerifyOnTaskRemoved(v);
                        XAppGuardManager.get().setVerifySettings(verifySettings);
                        return true;
                    }
                });


                SwitchPreference photoPref = (SwitchPreference) findPreference(XKey.TAKE_PHOTO_ENABLED);
                if (photoPref != null) {
                    photoPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            SecureGuardSettingsPermissionRequester
                                    .requestCameraPermissionChecked((SecureGuardSettings) getActivity());
                            return true;
                        }
                    });
                }

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
