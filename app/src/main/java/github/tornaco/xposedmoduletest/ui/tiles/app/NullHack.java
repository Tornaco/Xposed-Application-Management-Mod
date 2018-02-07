package github.tornaco.xposedmoduletest.ui.tiles.app;

import android.content.Context;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;

/**
 * Created by guohao4 on 2017/11/16.
 * Email: Tornaco@163.com
 */

public class NullHack extends QuickTile {

    public NullHack(final Context context) {
        super(context);

        this.title = "<-- Hello world -->";
        this.tileView = new QuickTileView(context, this);
    }
}
