package github.tornaco.xposedmoduletest.provider;

import android.content.Context;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import org.newstand.logger.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;

import dev.nick.eventbus.Event;
import dev.nick.eventbus.EventBus;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.bean.DaoManager;
import github.tornaco.xposedmoduletest.bean.DaoSession;
import github.tornaco.xposedmoduletest.bean.RecentTile;
import github.tornaco.xposedmoduletest.util.OSUtil;
import github.tornaco.xposedmoduletest.util.WorkaroundFixer;
import github.tornaco.xposedmoduletest.util.XExecutor;
import github.tornaco.xposedmoduletest.xposed.XAPMApplication;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
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
        return XAPMApplication.isPlayVersion()
                && PreferenceManager.getDefaultSharedPreferences(context).getBoolean(AppKey.HIDE_TILE + which, false);
    }

    public static void hideDashboardTile(Context context, String which, boolean hide) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(AppKey.HIDE_TILE + which, hide)
                .apply();
    }

    public static int getFilterOptions(Context context, String which, int defOptions) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(AppKey.FILTER_OPTIONS + which, defOptions);
    }

    public static void setFilterOptions(Context context, String which, int options) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(AppKey.FILTER_OPTIONS + which, options)
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

    public static boolean isAliPayRedPacketReceivedToady(Context context) {
        // Base on version name, show it at every new version.
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(AppKey.ALILAY_RED_PACKET_RECEIVED + BuildConfig.VERSION_NAME, false);
    }

    public static void setAliPayRedPacketReceivedToady(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit().putBoolean(AppKey.ALILAY_RED_PACKET_RECEIVED + BuildConfig.VERSION_NAME, true)
                .apply();
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
        try {
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit()
                    .putBoolean(AppKey.P_STYLE_ICON, p)
                    .apply();
        } catch (Throwable ignored) {
            // Fuck it.
        }
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
        try {
            return PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean(AppKey.P_STYLE_ICON, true);
        } catch (Exception e) {
            try {
                PreferenceManager.getDefaultSharedPreferences(context)
                        .edit().remove(AppKey.P_STYLE_ICON)
                        .apply();
            } catch (Throwable ignored) {
            }
            return true;
        }
    }

    public static boolean isNewBuild(Context context) {
        String serverSerial = XAPMManager.get().isServiceAvailable() ? XAPMManager.get().getBuildSerial() : null;
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

    // Async method.
    public static void clearRecentTile(Context context) {
        XExecutor.execute(() -> {
            try {
                DaoSession daoSession = DaoManager.getInstance().getSession(context);
                if (daoSession != null) {
                    daoSession.getRecentTileDao().deleteAll();
                    cacheRecentTiles(context);
                }
            } catch (Throwable e) {
                Logger.e("Fail addRecentTile: " + Logger.getStackTraceString(e));
            }
        });
    }

    // Async method.
    public static void addRecentTile(Context context, RecentTile tile) {
        XExecutor.execute(() -> {
            try {
                DaoSession daoSession = DaoManager.getInstance().getSession(context);
                if (daoSession != null) {
                    daoSession.getRecentTileDao().insertOrReplace(tile);
                    cacheRecentTiles(context);

                    // Clean up.
                    daoSession.getRecentTileDao().deleteAll();
                    List<RecentTile> recentTiles = getCachedTiles();
                    daoSession.getRecentTileDao().insertOrReplaceInTx(recentTiles);
                }
            } catch (Throwable e) {
                Logger.e("Fail addRecentTile: " + Logger.getStackTraceString(e));
            }
        });
    }

    private static final List<RecentTile> sCachedTiles = new ArrayList<>();
    private static final int MAX_CACHE_SAVED_TILES = 8;
    private static int sCacheDefaultMaxTileCount = 0;
    private static int sCacheUserMaxTileCount = 0;

    // Async.
    public static void cacheRecentTilesAsync(Context context) {
        XExecutor.execute(() -> {
            cacheRecentTiles(context);
        });
    }

    public static void cacheRecentTiles(Context context) {
        synchronized (sCachedTiles) {
            sCachedTiles.clear();
            List<RecentTile> recentTiles = getRecentTiles(context);
            if (recentTiles != null) {
                Logger.d("cacheRecentTiles, got: " + recentTiles.size());
                for (RecentTile r : recentTiles) {
                    if (!sCachedTiles.contains(r)) {
                        sCachedTiles.add(r);
                    }
                    if (sCachedTiles.size() >= MAX_CACHE_SAVED_TILES) break;
                }
            }
        }
        EventBus.from().publish(new Event(XAPMApplication.EVENT_RECENT_TILE_CHANGED));
    }

    public static List<RecentTile> getCachedTiles() {
        synchronized (sCachedTiles) {
            return new ArrayList<>(sCachedTiles);
        }
    }

    public static List<RecentTile> getRecentTiles(Context context) {
        try {
            DaoSession daoSession = DaoManager.getInstance().getSession(context);
            if (daoSession != null) {
                List<RecentTile> res = daoSession.getRecentTileDao().loadAll();
                Collections.reverse(res);
                return res;
            }
        } catch (Throwable e) {
            Logger.e("Fail getRecentTiles: " + Logger.getStackTraceString(e));
        }
        return new ArrayList<>(0);
    }

    public static int getDefaultMaxRecentTileCount(Context context) {
        if (sCacheDefaultMaxTileCount > 0) {
            return sCacheDefaultMaxTileCount;
        }
        sCacheDefaultMaxTileCount = context.getResources().getInteger(R.integer.dashboard_max_recent_tile_count);
        return sCacheDefaultMaxTileCount;
    }

    public static int getUserMaxRecentTileCount(Context context) {
        if (sCacheUserMaxTileCount > 0) {
            return sCacheUserMaxTileCount;
        }
        sCacheUserMaxTileCount = PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(AppKey.RECENT_TILE_COUNT, getDefaultMaxRecentTileCount(context));
        if (sCacheUserMaxTileCount < 0) {
            sCacheUserMaxTileCount = getDefaultMaxRecentTileCount(context);
        }
        return sCacheUserMaxTileCount;
    }

    public static void increaseOrDecreaseUserMaxRecentTileCount(Context context, boolean increase) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(AppKey.RECENT_TILE_COUNT,
                        Math.abs(increase ? getUserMaxRecentTileCount(context) + 1 : getUserMaxRecentTileCount(context) - 1))
                .apply();
        // Update cache.
        sCacheUserMaxTileCount = PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(AppKey.RECENT_TILE_COUNT, getDefaultMaxRecentTileCount(context));
    }
}
