package github.tornaco.xposedmoduletest.ui.tiles;

import android.content.Context;
import android.view.View;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.bean.RecentTile;
import github.tornaco.xposedmoduletest.provider.AppSettings;
import github.tornaco.xposedmoduletest.ui.activity.pmh.PMHDashboardActivity;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class PushMessageHandler extends QuickTile {

    public PushMessageHandler(final Context context) {
        super(context);
        this.titleRes = R.string.title_push_message_handler;
        if (XAPMManager.get().isServiceAvailable()) {
            this.summaryRes = XAPMManager.get().isPushMessageHandleEnabled() ?
                    R.string.summary_func_enabled : 0;
        }
        this.iconRes = R.drawable.ic_notification_badge_fill;
        this.tileView = new QuickTileView(context, this) {
            @Override
            protected int getImageViewBackgroundRes() {
                return R.drawable.tile_bg_green;
            }

            @Override
            public void onClick(View v) {
                super.onClick(v);
                PMHDashboardActivity.start(context);
                // Save to recent.
                AppSettings.addRecentTile(context, RecentTile.from(TileManager.getTileKey(PushMessageHandler.class)));
            }
        };
    }
}
