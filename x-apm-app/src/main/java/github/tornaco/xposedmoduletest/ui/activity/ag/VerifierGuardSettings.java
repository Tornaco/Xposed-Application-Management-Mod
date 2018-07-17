package github.tornaco.xposedmoduletest.ui.activity.ag;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.common.io.Files;
import com.mvc.imagepicker.ImagePicker;

import org.newstand.logger.Logger;

import java.io.File;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.provider.XSettings;
import github.tornaco.xposedmoduletest.xposed.app.XAppLockManager;
import github.tornaco.xposedmoduletest.xposed.util.DateUtils;

/**
 * Created by guohao4 on 2017/11/2.
 * Email: Tornaco@163.com
 */

public class VerifierGuardSettings extends GuardSettingsActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // TODO Do it async.
        String imagePath = ImagePicker.getImagePathFromResult(getContext(), requestCode, resultCode, data);
        if (!TextUtils.isEmpty(imagePath)) {
            String cachePath = getCacheDir() + File.separator + DateUtils.formatForFileName(System.currentTimeMillis());
            try {
                Files.createParentDirs(new File(cachePath));
                Files.copy(new File(imagePath), new File(cachePath));
                XSettings.setCustomBackgroundPath(getContext(), cachePath);
                Toast.makeText(getActivity(), R.string.toast_custom_image_saved, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(getActivity(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                Logger.e("Fail save custom image: " + Logger.getStackTraceString(e));
            }
        }
    }

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
                switchPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                    boolean interrupt = !(boolean) newValue;
                    XAppLockManager.get().setInterruptFPEventVBEnabled(XAppLockManager.FPEvent.SUCCESS, interrupt);
                    return true;
                });

                switchPreference = (SwitchPreference) findPreference("error_vibrate_enabled");
                switchPreference.setChecked(!XAppLockManager.get().isInterruptFPEventVBEnabled(XAppLockManager.FPEvent.ERROR));
                switchPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                    boolean interrupt = !(boolean) newValue;
                    XAppLockManager.get().setInterruptFPEventVBEnabled(XAppLockManager.FPEvent.ERROR, interrupt);
                    return true;
                });

//                boolean customBgEnabled = XSettings.customBackgroundEnabled(getActivity());
//                boolean dynamicColorEnabled = XSettings.dynamicColorEnabled(getActivity());
//
//                SwitchPreference dynamicColorPref = (SwitchPreference) findPreference("key_dynamic_color_enabled");
//                Preference defColorPickerPref = findPreference("key_def_verifier_color");
//
//                dynamicColorPref.setEnabled(!customBgEnabled);
//                defColorPickerPref.setEnabled(!customBgEnabled && !dynamicColorEnabled);
//
//                SwitchPreference customBgEnabledPref = (SwitchPreference) findPreference("key_custom_background_enabled");
//
//                Preference customBgPickerPref = findPreference("key_custom_background_picker");
//                customBgPickerPref.setOnPreferenceClickListener(preference -> {
//                    ImagePicker.pickImage(getActivity());
//                    return true;
//                });
            }

        }

    }
}
