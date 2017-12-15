package github.tornaco.xposedmoduletest.ui.tiles;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class Greening extends QuickTile {

    public Greening(final Context context) {
        super(context);
        this.titleRes = R.string.title_greening;
        this.iconRes = R.drawable.ic_palette_green_24dp;
        this.tileView = new QuickTileView(context, this) {
            @Override
            public void onClick(View v) {
                super.onClick(v);
                Toast.makeText(context, "~~~~~~", Toast.LENGTH_SHORT).show();
            }

            @Override
            protected boolean useStaticTintColor() {
                return false;
            }
        };
    }
}
