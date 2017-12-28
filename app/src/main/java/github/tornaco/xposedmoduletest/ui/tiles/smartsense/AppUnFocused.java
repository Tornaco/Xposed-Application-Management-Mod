package github.tornaco.xposedmoduletest.ui.tiles.smartsense;

import android.content.Context;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class AppUnFocused extends QuickTile {

    public AppUnFocused(final Context context) {
        super(context);
        this.titleRes = R.string.title_app_unfocused;
        this.iconRes = R.drawable.ic_android_black_24dp;
        this.tileView = new QuickTileView(context, this);
    }
}
