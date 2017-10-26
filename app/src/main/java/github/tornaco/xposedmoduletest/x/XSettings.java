package github.tornaco.xposedmoduletest.x;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import java.io.File;
import java.util.Observable;

import github.tornaco.xposedmoduletest.ui.Themes;

/**
 * Created by guohao4 on 2017/10/19.
 * Email: Tornaco@163.com
 */

public class XSettings extends Observable {

    static final int PASSCODE_LEVEL = 4;

    private static XSettings sMe = new XSettings();

    private XSettings() {
    }

    public static XSettings get() {
        return sMe;
    }

    public void setChangedL() {
        setChanged();
    }

    public boolean takenPhotoEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(XKey.TAKE_PHOTO_ENABLED, true);
    }

    public boolean fpEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(XKey.FP_ENABLED, false);
    }

    public boolean alwaysNote(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(XKey.ALWAYS_NOTE, false);
    }

    public boolean fullScreenNoter(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(XKey.TAKE_FULL_SCREEN_NOTER, false);
    }

    public static File getPhotosDir(Context context) {
        return new File(context.getFilesDir()
                + File.separator + "photos");
    }

    @Nullable
    public static String getPassCodeEncrypt(Context context) {
        return XAppGuardManager.from().getPasscode();
    }

    public void setPassCodeEncrypt(Context context, String code) {
        XAppGuardManager.from().setPasscode(code);
    }

    public static void setActivateCode(Context context, String code) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit().putString(XKey.ACTIVATE_CODE, code)
                .apply();
    }

    @Nullable
    public static String getActivateCode(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(XKey.ACTIVATE_CODE, null);
    }

    public static boolean isDevMode(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(XKey.DEV_MODE, false);
    }

    public static void setInDevMode(Context context, boolean in) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(XKey.DEV_MODE, in)
                .apply();
    }

    public static boolean isFirstRun(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(XKey.FIRST_RUN, true);
    }

    public static boolean setFirstRun(Context context, boolean firstRun) {
        boolean first = isFirstRun(context);
        if (first != firstRun) {
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit()
                    .putBoolean(XKey.FIRST_RUN, firstRun)
                    .apply();
        }
        return first;
    }

    public static Themes getThemes(Context c) {
        return Themes.valueOfChecked(PreferenceManager.getDefaultSharedPreferences(c)
                .getString(XKey.THEME, Themes.DEFAULT.name()));
    }
}
