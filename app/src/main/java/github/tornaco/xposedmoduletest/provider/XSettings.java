package github.tornaco.xposedmoduletest.provider;

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

    private static XSettings sMe = new XSettings();

    private XSettings() {
    }

    public static XSettings get() {
        return sMe;
    }

    public static boolean takenPhotoEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(XKey.TAKE_PHOTO_ENABLED, true);
    }

    public static boolean fpEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(XKey.FP_ENABLED, false);
    }

    public static boolean cropEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(XKey.CROP_CIRCLE_ENABLED, false);
    }

    public static boolean showAppIconEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(XKey.SHOW_APP_ICON_ENABLED, false);
    }

    public static File getPhotosDir(Context context) {
        return new File(context.getFilesDir()
                + File.separator + "photos");
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
                .getBoolean(XKey.DEV_MODE, true);
    }

    public static boolean isStartBlockNotify(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(XKey.START_BLOCK_NOTIFY_ENABLED, true);
    }

    public static void setStartBlockNotify(Context context, boolean enabled) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(XKey.START_BLOCK_NOTIFY_ENABLED, enabled)
                .apply();
    }

    public static void setInDevMode(Context context, boolean in) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(XKey.DEV_MODE, in)
                .apply();
    }

    public static Themes getThemes(Context c) {
        return Themes.valueOfChecked(PreferenceManager.getDefaultSharedPreferences(c)
                .getString(XKey.THEME, Themes.DEFAULT.name()));
    }
}
