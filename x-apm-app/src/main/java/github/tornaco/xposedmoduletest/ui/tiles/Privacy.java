package github.tornaco.xposedmoduletest.ui.tiles;

import android.content.Context;
import android.view.View;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.bean.RecentTile;
import github.tornaco.xposedmoduletest.provider.AppSettings;
import github.tornaco.xposedmoduletest.ui.activity.app.PrivacyNavActivity;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class Privacy extends QuickTile {

    public Privacy(final Context context) {
        super(context);
        this.titleRes = R.string.title_privacy;
        if (XAPMManager.get().isServiceAvailable()) {
            this.summaryRes = XAPMManager.get().isPrivacyEnabled() ?
                    R.string.summary_func_enabled : 0;
        }
        this.iconRes = R.drawable.ic_track_changes_black_24dp;
        this.tileView = new QuickTileView(context, this) {
            @Override
            protected int getImageViewBackgroundRes() {
                return R.drawable.tile_bg_grey;
            }

            @Override
            public void onClick(View v) {
                super.onClick(v);
                PrivacyNavActivity.start(context);
                // Save to recent.
                AppSettings.addRecentTile(context, RecentTile.from(TileManager.getTileKey(Privacy.class)));
            }
        };
    }
}
