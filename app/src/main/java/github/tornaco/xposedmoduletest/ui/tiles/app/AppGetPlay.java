package github.tornaco.xposedmoduletest.ui.tiles.app;

import android.content.Context;
import android.view.View;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.activity.app.GetPlayVersionActivity;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class AppGetPlay extends QuickTile {

    public AppGetPlay(final Context context) {
        super(context);
        this.titleRes = R.string.title_get_play;
        this.iconRes = R.drawable.ic_shop_black_24dp;
        this.tileView = new QuickTileView(context, this) {
            @Override
            public void onClick(View v) {
                super.onClick(v);
                GetPlayVersionActivity.start(context);
            }

            @Override
            protected int getImageViewBackgroundRes() {
                return R.drawable.tile_bg_green;
            }
        };
    }
}
