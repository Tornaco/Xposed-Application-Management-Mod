package github.tornaco.xposedmoduletest.ui.tiles.app;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.widget.UpdateLogDialog;

/**
 * Created by guohao4 on 2017/11/16.
 * Email: Tornaco@163.com
 */

public class AppUpdateLog extends QuickTile {

    public AppUpdateLog(final Context context) {
        super(context);

        this.titleRes = R.string.title_app_update_log;
        this.iconRes = R.drawable.ic_featured_play_list_black_24dp;
        this.tileView = new QuickTileView(context, this) {

            @Override
            protected int getImageViewBackgroundRes() {
                return R.drawable.tile_bg_grey;
            }

            @Override
            public void onClick(View v) {
                super.onClick(v);
                UpdateLogDialog.show((Activity) context);
            }
        };
    }
}
