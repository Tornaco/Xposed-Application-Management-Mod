package github.tornaco.xposedmoduletest.ui.tiles.app;

import android.content.Context;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class OpenSource extends QuickTile {

    public OpenSource(final Context context) {
        super(context);
        this.titleRes = R.string.title_open_source;
        this.iconRes = R.drawable.ic_code_white_24dp;
        this.summary = "Github@Tornaco/X-APM";
        this.tileView = new QuickTileView(context, this) {
            @Override
            protected int getImageViewBackgroundRes() {
                return R.drawable.tile_bg_blue_grey;
            }
        };
    }
}
