package github.tornaco.xposedmoduletest.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Toast;

import github.tornaco.permission.requester.RequiresPermission;
import github.tornaco.permission.requester.RuntimePermissions;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.compat.fingerprint.FingerprintManagerCompat;
import github.tornaco.xposedmoduletest.x.XAppGithubCommitSha;
import github.tornaco.xposedmoduletest.x.XAppGuardManager;
import github.tornaco.xposedmoduletest.x.XEnc;
import github.tornaco.xposedmoduletest.x.XKey;
import github.tornaco.xposedmoduletest.x.XSettings;
import github.tornaco.xposedmoduletest.x.XStatus;

/**
 * Created by guohao4 on 2017/9/7.
 * Email: Tornaco@163.com
 */
@RuntimePermissions
public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_with_container_with_appbar_template);
        showHomeAsUp();
        getFragmentManager().beginTransaction().replace(R.id.container,
                new SettingsFragment()).commitAllowingStateLoss();
    }

    protected void showHomeAsUp() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @RequiresPermission({Manifest.permission.CAMERA})
    void requestCameraPermission() {

    }

    @TargetApi(Build.VERSION_CODES.M)
    @RequiresPermission({Manifest.permission.USE_FINGERPRINT})
    void requestFPPermission() {

    }

    public static class SettingsFragment extends PreferenceFragment {

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);

            findPreference(XKey.VERIFY_ON_HOME)
                    .setOnPreferenceChangeListener(new Preference
                            .OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            XSettings.get().setChangedL();
                            XSettings.get().notifyObservers();
                            return true;
                        }
                    });

            // Below is very ugly:() =.=
            final Preference passcodePref = findPreference(getString(R.string.title_setup_passcode));
            passcodePref.setEnabled(XAppGuardManager.from().isServiceConnected());
            if (XEnc.isPassCodeValid(XSettings.getPassCodeEncrypt(getActivity()))) {
                passcodePref.setSummary(R.string.summary_setup_passcode_set);
            } else {
                passcodePref.setSummary(R.string.summary_setup_passcode_none_set);
            }
            passcodePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    PassCodeSetup passCodeSetup = new PassCodeSetup(getActivity());
                    passCodeSetup.setSetupListener(new PassCodeSetup.SetupListener() {
                        @Override
                        public void onSetSuccess() {
                            Toast.makeText(getActivity(), R.string.summary_setup_passcode_set, Toast.LENGTH_LONG).show();

                            if (XEnc.isPassCodeValid(XSettings.getPassCodeEncrypt(getActivity()))) {
                                passcodePref.setSummary(R.string.summary_setup_passcode_set);
                            } else {
                                passcodePref.setSummary(R.string.summary_setup_passcode_none_set);
                            }
                        }

                        @Override
                        public void onSetFail(String reason) {
                            Toast.makeText(getActivity(), reason, Toast.LENGTH_LONG).show();

                            if (XEnc.isPassCodeValid(XSettings.getPassCodeEncrypt(getActivity()))) {
                                passcodePref.setSummary(R.string.summary_setup_passcode_set);
                            } else {
                                passcodePref.setSummary(R.string.summary_setup_passcode_none_set);
                            }
                        }
                    });
                    passCodeSetup.setup(false);
                    return true;
                }
            });

            SwitchPreference fpPre = (SwitchPreference) findPreference(XKey.FP_ENABLED);
            fpPre.setEnabled(FingerprintManagerCompat.from(getActivity()).isHardwareDetected());

            SwitchPreference photoPref = (SwitchPreference) findPreference(XKey.TAKE_PHOTO_ENABLED);
            photoPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    SettingsActivityPermissionRequester.requestCameraPermissionChecked((SettingsActivity) getActivity());
                    return true;
                }
            });

            findPreference(getString(R.string.title_view_photos))
                    .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            startActivity(new Intent(getActivity(), PhotoViewerActivity.class));
                            return true;
                        }
                    });

            SwitchPreference blurPref = (SwitchPreference) findPreference(XKey.BLUR);
            blurPref.setChecked(XAppGuardManager.from().isBlur());
            blurPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    boolean enabled = (boolean) newValue;
                    XAppGuardManager.from().setBlur(enabled);
                    return true;
                }
            });

            SwitchPreference blurAllPref = (SwitchPreference) findPreference(XKey.BLUR_ALL);
            blurAllPref.setChecked(XAppGuardManager.from().getBlurPolicy() == XAppGuardManager.BlurPolicy.BLUR_ALL);
            blurAllPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    boolean enabled = (boolean) newValue;
                    XAppGuardManager.from().setBlurPolicy(enabled ? XAppGuardManager.BlurPolicy.BLUR_ALL : XAppGuardManager.BlurPolicy.BLUR_WATCHED);
                    return true;
                }
            });

            SwitchPreference verHome = (SwitchPreference) findPreference(XKey.VERIFY_ON_HOME);
            verHome.setChecked(XAppGuardManager.from().isVerifyOnHome());
            verHome.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    boolean enabled = (boolean) newValue;
                    XAppGuardManager.from().setVerifyOnHome(enabled);
                    return true;
                }
            });

            SwitchPreference verScr = (SwitchPreference) findPreference(XKey.VERIFY_ON_SCREEN_OFF);
            verScr.setChecked(XAppGuardManager.from().isVerifyOnScreenOff());
            verScr.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    boolean enabled = (boolean) newValue;
                    XAppGuardManager.from().setVerifyOnScreenOff(enabled);
                    return true;
                }
            });

            findPreference(getString(R.string.test_noter))
                    .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            if (XAppGuardManager.from().isServiceConnected())
                                XAppGuardManager.from().testUI();
                            else
                                VerifyDisplayerActivity.startAsTest(getActivity());
                            return true;
                        }
                    });

            findPreference(getString(R.string.dump_module))
                    .setSummary(XStatus.valueOf(XAppGuardManager.from().getStatus()).name());

            SwitchPreference allow3rdVerifierPref = (SwitchPreference) findPreference(XKey.ALLOW_3RD_VERIFIER);
            allow3rdVerifierPref.setChecked(XAppGuardManager.from().isAllow3rdVerifier());
            allow3rdVerifierPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    boolean enabled = (boolean) newValue;
                    XAppGuardManager.from().setAllow3rdVerifier(enabled);
                    return true;
                }
            });

            Preference buildInfo = findPreference(getString(R.string.title_app_ver));
            buildInfo.setSummary(BuildConfig.VERSION_NAME
                    + "-" + BuildConfig.BUILD_TYPE
                    + "\nCommit@" + XAppGithubCommitSha.LATEST_SHA
                    + "\nDate@" + XAppGithubCommitSha.LATEST_SHA_DATE);

            Preference actCode = findPreference(getString(R.string.title_my_act_code));
            String act = XSettings.getActivateCode(getActivity());
            actCode.setSummary(TextUtils.isEmpty(act) ? "NONE" : act);
        }
    }


}
