package github.tornaco.xposedmoduletest.ui.tiles.app;

import android.content.Context;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class AppDeveloper extends QuickTile {

    public AppDeveloper(final Context context) {
        super(context);
        this.titleRes = R.string.title_developer;
        this.summary = "Tornaco/tornaco@163.com";
        this.iconRes = R.drawable.ic_account_circle_black_24dp;
        this.tileView = new QuickTileView(context, this) {
            @Override
            protected int getImageViewBackgroundRes() {
                return R.drawable.tile_bg_indigo;
            }
        };
    }
}
