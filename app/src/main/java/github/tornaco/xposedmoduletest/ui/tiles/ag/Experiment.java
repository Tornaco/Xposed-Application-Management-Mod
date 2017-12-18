package github.tornaco.xposedmoduletest.ui.tiles.ag;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.activity.ag.ExperimentGuardSettings;

/**
 * Created by guohao4 on 2017/11/8.
 * Email: Tornaco@163.com
 */
@Deprecated
public class Experiment extends QuickTile {
    public Experiment(final Context context) {
        super(context);
        this.titleRes = R.string.title_exp;
        this.iconRes = R.drawable.ic_adb_black_24dp;
        this.tileView = new QuickTileView(context, this) {
            @Override
            public void onClick(View v) {
                super.onClick(v);
                context.startActivity(new Intent(context, ExperimentGuardSettings.class));
            }
        };
    }
}
