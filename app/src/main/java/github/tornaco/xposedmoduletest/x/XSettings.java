package github.tornaco.xposedmoduletest.x;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import org.newstand.logger.Logger;

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

    public boolean cropEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(XKey.CROP_CIRCLE_ENABLED, false);
    }

    public boolean showAppIconEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(XKey.SHOW_APP_ICON_ENABLED, false);
    }

    public boolean patternLockEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(XKey.LOCK_PATTERN_ENABLED, true);
    }


    public static File getPhotosDir(Context context) {
        return new File(context.getFilesDir()
                + File.separator + "photos");
    }

    @Nullable
    public static String getPassCodeEncrypt(Context context) {
        return null;
    }

    public void setPassCodeEncrypt(Context context, String code) {

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

    public static boolean setFirstRun(Context context) {
        boolean first = isFirstRun(context);
        if (first) {
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit()
                    .putBoolean(XKey.FIRST_RUN, false)
                    .apply();
        }
        return first;
    }

    public static Themes getThemes(Context c) {
        return Themes.valueOfChecked(PreferenceManager.getDefaultSharedPreferences(c)
                .getString(XKey.THEME, Themes.DEFAULT.name()));
    }

    public static int getPinLockBtnSize(Context context, int def) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(XKey.PIN_PAD_BTN_SIZE, def);
    }

    public static int getPinLockTextSize(Context context, int def) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(XKey.PIN_PAD_TEXT_SIZE, def);
    }

    public static int getPinLockW(Context context, int def) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(XKey.PIN_PAD_W, def);
    }

    public static int getPinLockH(Context context, int def) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(XKey.PIN_PAD_H, def);
    }

    public static void setPinLockBtnSize(Context context, int size) {
        Logger.d("setPinLockBtnSize:" + size);
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(XKey.PIN_PAD_BTN_SIZE, size)
                .apply();
    }

    public static void setPinLockTextSize(Context context, int size) {
        Logger.d("setPinLockTextSize:" + size);
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(XKey.PIN_PAD_TEXT_SIZE, size)
                .apply();
    }

    public static void setPinLockW(Context context, int size) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(XKey.PIN_PAD_W, size)
                .apply();
    }

    public static void setPinLockH(Context context, int size) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(XKey.PIN_PAD_H, size)
                .apply();
    }
}
