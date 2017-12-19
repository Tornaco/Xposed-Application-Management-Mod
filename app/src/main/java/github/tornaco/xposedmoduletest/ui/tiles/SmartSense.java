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

public class SmartSense extends QuickTile {

    public SmartSense(final Context context) {
        super(context);
        this.titleRes = R.string.title_smart_sense;
        this.iconRes = R.drawable.ic_airport_shuttle_black_24dp;
        this.tileView = new QuickTileView(context, this) {
            @Override
            public void onClick(View v) {
                super.onClick(v);
                Toast.makeText(context, "埋头调试中@-@", Toast.LENGTH_SHORT).show();
            }
        };
    }
}
