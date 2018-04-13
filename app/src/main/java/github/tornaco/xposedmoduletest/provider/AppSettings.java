package github.tornaco.xposedmoduletest.provider;

import android.content.Context;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import java.util.Observable;

import github.tornaco.xposedmoduletest.util.OSUtil;
import github.tornaco.xposedmoduletest.util.WorkaroundFixer;
import github.tornaco.xposedmoduletest.xposed.XApp;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;
import github.tornaco.xposedmoduletest.xposed.service.BuildFingerprintBuildHostInfo;

/**
 * Created by guohao4 on 2017/10/19.
 * Email: Tornaco@163.com
 */

public class AppSettings extends Observable {

    private static final String PREF_NAME = "app_settings";

    private static AppSettings sMe = new AppSettings();

    private AppSettings() {
    }

    public static AppSettings get() {
        return sMe;
    }

    public static boolean isHideTileInDashboard(Context context, String which) {
        return XApp.isPlayVersion()
                && PreferenceManager.getDefaultSharedPreferences(context).getBoolean(AppKey.HIDE_TILE + which, false);
    }

    public static void hideDashboardTile(Context context, String which, boolean hide) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(AppKey.HIDE_TILE + which, hide)
                .apply();
    }

    public static boolean isDrawVibrateEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(AppKey.DRAW_VIBRATE, false);
    }

    // Always return true for ZUK or Lenovo device.
    public static boolean isDonated(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(AppKey.DONATED, OSUtil.isLenovoDevice()
                        || OSUtil.isNTDDevice());
    }

    public static void setDonated(Context context, boolean donated) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit().putBoolean(AppKey.DONATED, donated).apply();
    }

    public static boolean isFirstRun(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(AppKey.FIRST_RUN, true);
    }

    public static boolean setFirstRun(Context context) {
        boolean first = isFirstRun(context);
        if (first) {
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit()
                    .putBoolean(AppKey.FIRST_RUN, false)
                    .apply();
        }
        return first;
    }

    public static boolean isGuideRead(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(AppKey.GUIDE_READ, false);
    }

    public static void setGuideRead(Context context, boolean read) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(AppKey.GUIDE_READ, read)
                .apply();

    }

    public static boolean isAppLockWorkaroundEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(AppKey.APPLOCK_WORKAROUND,
                        WorkaroundFixer.isThisDeviceVerifyDisplayerNeedDelayRes());
    }

    public static void setAppLockWorkaroundEnabled(Context context, boolean b) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(AppKey.APPLOCK_WORKAROUND, b)
                .apply();

    }

    public static boolean isBottomNavNoShiftEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(AppKey.BOTTOM_NO_SHIFT,
                        true);
    }

    public static void setBottomNavNoShiftEnabled(Context context, boolean b) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(AppKey.BOTTOM_NO_SHIFT, b)
                .apply();

    }

    public static String getAppIconPack(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(AppKey.APP_ICON_PACK, null);
    }

    public static void setAppIconPack(Context context, String p) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(AppKey.APP_ICON_PACK, p)
                .apply();

    }

    public static boolean isSelinuxModeEnforceEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(AppKey.SELINUX_MODE_ENFORCE, true);
    }

    public static void setSelinuxModeEnforceEnabled(Context context, boolean read) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(AppKey.SELINUX_MODE_ENFORCE, read)
                .apply();

    }

    public static boolean isShowTileDivider(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(AppKey.SHOW_TILE_DIVIDER, false);
    }

    public static void setShowDivider(Context context, boolean show) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(AppKey.SHOW_TILE_DIVIDER, show)
                .apply();

    }

    public static void setFirstSee(Context context, String tag) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(tag, false)
                .apply();
    }

    public static boolean isFirstSee(Context context, String tag) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(tag, true);
    }

    public static void setSentTokenToServer(Context context, boolean sent) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(AppKey.SENT_TOKEN_TO_SERVER, sent)
                .apply();
    }

    public static boolean hasSentTokenToServer(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(AppKey.SENT_TOKEN_TO_SERVER, false);
    }

    public static void setSuscribeGcmMessage(Context context, boolean rec) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(AppKey.SUBSCRIBE_GCM_MESSAGES, rec)
                .apply();
    }

    public static boolean isSubscribeGcmMessage(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(AppKey.SUBSCRIBE_GCM_MESSAGES, true);
    }

    public static void setShowGcmIndicator(Context context, boolean rec) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(AppKey.GCM_INDICATOR, rec)
                .apply();
    }

    public static boolean isShowGcmIndicator(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(AppKey.GCM_INDICATOR, true);
    }

    public static void setPStyleIcon(Context context, boolean p) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(AppKey.P_STYLE_ICON, p)
                .apply();
    }

    public static boolean isForceHasGMS(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(AppKey.FORCE_HAS_GMS, false);
    }

    public static void setForceHasGMS(Context context, boolean p) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(AppKey.FORCE_HAS_GMS, p)
                .apply();
    }

    public static boolean isPStyleIcon(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(AppKey.P_STYLE_ICON, true);
    }

    public static boolean isNewBuild(Context context) {
        String serverSerial = XAshmanManager.get().isServiceAvailable() ? XAshmanManager.get().getBuildSerial() : null;
        if (serverSerial == null) return false;
        String appBuildSerial = BuildFingerprintBuildHostInfo.BUILD_FINGER_PRINT;
        return !TextUtils.isEmpty(appBuildSerial) && !appBuildSerial.equals(serverSerial);
    }

    public static void setShowInfo(Context context, String who, boolean show) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit().putBoolean(AppKey.SHOW_INFO_PREFIX + who, show)
                .apply();
    }

    public static boolean isShowInfoEnabled(Context context, String who) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getBoolean(AppKey.SHOW_INFO_PREFIX + who, true);
    }

    public static boolean isShowInfoEnabled(Context context, String who, boolean def) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getBoolean(AppKey.SHOW_INFO_PREFIX + who, def);
    }

    public static boolean show2ColumnsIn(Context context, String where) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getBoolean(AppKey.MAIN_DASH_COLUMN_COUNT + where, false);
    }

    public static void setShow2ColumnsIn(Context context, String who, boolean show) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit().putBoolean(AppKey.MAIN_DASH_COLUMN_COUNT + who, show)
                .apply();
    }

}
