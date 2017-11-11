package github.tornaco.xposedmoduletest.ui.tiles;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.activity.LockKillAppNavActivity;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class LockKill extends QuickTile {

    public LockKill(final Context context) {
        super(context);
        this.titleRes = R.string.title_app_lock_kill;
        this.iconRes = R.drawable.ic_restaurant_menu_black_24dp;
        this.tileView = new QuickTileView(context, this) {
            @Override
            public void onClick(View v) {
                super.onClick(v);
                context.startActivity(new Intent(context, LockKillAppNavActivity.class));
            }
        };
    }
}
