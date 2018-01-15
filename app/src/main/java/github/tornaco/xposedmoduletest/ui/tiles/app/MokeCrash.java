package github.tornaco.xposedmoduletest.ui.tiles.app;

import android.content.Context;
import android.view.View;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class MokeCrash extends QuickTile {

    public MokeCrash(final Context context) {
        super(context);
        this.titleRes = R.string.title_crash_moke;
        this.summaryRes = R.string.summary_crash_moke;
        this.iconRes = R.drawable.ic_healing_black_24dp;
        this.tileView = new QuickTileView(context, this) {
            @Override
            public void onClick(View v) {
                super.onClick(v);
                throw new IllegalStateException("=== Crash test by user ===");
            }
        };
    }
}
