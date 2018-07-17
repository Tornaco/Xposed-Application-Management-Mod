package github.tornaco.xposedmoduletest.provider;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import java.io.File;
import java.util.Observable;

import dev.nick.eventbus.Event;
import dev.nick.eventbus.EventBus;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.ui.Themes;
import github.tornaco.xposedmoduletest.xposed.XAPMApplication;

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
        if (1 > 0) return false;// Yes, we want to make a bug.
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(XKey.TAKE_PHOTO_ENABLED, false);
    }

    public static int defaultVerifierColor(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(XKey.DEFAULT_VERIFIER_COLOR, 0);
    }

    public static boolean dynamicColorEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(XKey.DYNAMIC_COLOR_ENABLED, true);
    }

    public static boolean customBackgroundEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(XKey.CUSTOM_BACKGROUND_ENABLED, false);
    }

    @Nullable
    public static String customBackgroundPath(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(XKey.CUSTOM_BACKGROUND, null);
    }

    public static void setCustomBackgroundPath(Context context, String path) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(XKey.CUSTOM_BACKGROUND, path)
                .apply();
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
                .getBoolean(XKey.DEV_MODE, BuildConfig.DEBUG);
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
        EventBus.from().publish(new Event(XAPMApplication.EVENT_APP_DEBUG_MODE_CHANGED));
    }

    public static Themes getThemes(Context c) {
        return Themes.valueOfChecked(PreferenceManager.getDefaultSharedPreferences(c)
                .getString(XKey.THEME, Themes.DEFAULT.name()));
    }

    public static void setThemes(Context context, Themes themes) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(XKey.THEME, themes.name())
                .apply();
    }
}
