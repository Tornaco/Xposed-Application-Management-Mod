package github.tornaco.xposedmoduletest.provider;

import github.tornaco.xposedmoduletest.BuildConfig;

/**
 * Created by guohao4 on 2017/10/19.
 * Email: Tornaco@163.com
 */

public interface AppKey {
    String FIRST_RUN = "first_run_app" + BuildConfig.VERSION_NAME;
    String BUILD_DATE = "build_date";
    String SHOW_INFO_PREFIX = "key_show_info_";
    String MAIN_DASH_COLUMN_COUNT = "main_col_count_";
    String DRAW_VIBRATE = "draw_vibrate_enabled";
    String GUIDE_READ = "guide_read";
    String HIDE_TILE = "hide_tile_";
    String DONATED = "donated";
    String SHOW_TILE_DIVIDER = "show_tile_divider";
    String SELINUX_MODE_ENFORCE = "selinux_mode_enforce";
    String APPLOCK_WORKAROUND = "app_lock_workaround";
    String APP_ICON_PACK = "app_icon_pack";
    String BOTTOM_NO_SHIFT = "bottom_nav_no_shift";
    String SENT_TOKEN_TO_SERVER = "sent_token_to_server";
    String SUBSCRIBE_GCM_MESSAGES = "receive_gcm_messages";
    String GCM_INDICATOR = "gcm_indicator";
    String P_STYLE_ICON = "p_style_icon";
    String FORCE_HAS_GMS = "force_has_gms";
    String FILTER_OPTIONS = "filter_options_";
    String RECENT_TILE = "recent_tile";
    String RECENT_TILE_COUNT = "recent_tile_count";
    String ALILAY_RED_PACKET_RECEIVED = "alipay_red_packet_received_";
}
