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
    String AUTO_SELINUX_MODE = "auto_selinux_mode";
    String APPLOCK_WORKAROUND = "app_lock_workaround";
    String APP_ICON_PACK = "app_icon_pack";
}
