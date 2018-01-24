package github.tornaco.xposedmoduletest.ui.tiles;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.activity.trk.TRKillAppNavActivity;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class TRKill extends QuickTile {

    public TRKill(final Context context) {
        super(context);
        this.titleRes = R.string.title_app_tr_kill;
        if (XAshmanManager.get().isServiceAvailable()) {
            this.summaryRes = XAshmanManager.get().isTaskRemoveKillEnabled() ?
                    R.string.summary_func_enabled : 0;
        }
        this.iconRes = R.drawable.ic_clear_all_black_24dp;
        this.tileView = new QuickTileView(context, this) {
            @Override
            public void onClick(View v) {
                super.onClick(v);
                context.startActivity(new Intent(context, TRKillAppNavActivity.class));
            }
        };
    }
}
