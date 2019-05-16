package github.tornaco.xposedmoduletest.ui.tiles.app;

import android.content.Context;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.R;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class OpenMarket extends QuickTile {

    public OpenMarket(final Context context) {
        super(context);
        this.titleRes = R.string.title_open_market;
        this.iconRes = R.drawable.ic_shop_black_24dp;
        this.summary = BuildConfig.FLAVOR;
        this.tileView = new QuickTileView(context, this) {

            @Override
            protected int getImageViewBackgroundRes() {
                return R.drawable.tile_bg_green;
            }
        };
    }
}
