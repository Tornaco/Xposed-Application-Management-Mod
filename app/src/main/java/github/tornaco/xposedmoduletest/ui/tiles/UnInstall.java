package github.tornaco.xposedmoduletest.ui.tiles;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.activity.uninstall.UnstallProAppNavActivity;
import github.tornaco.xposedmoduletest.xposed.app.XAppGuardManager;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class UnInstall extends QuickTile {

    public UnInstall(final Context context) {
        super(context);
        this.titleRes = R.string.title_app_uninstall_pro;
        if (XAppGuardManager.get().isServiceAvailable()) {
            this.summaryRes = XAppGuardManager.get().isUninstallInterruptEnabled() ?
                    R.string.summary_func_enabled : 0;
        }
        this.iconRes = R.drawable.ic_do_not_disturb_on_black_24dp;
        this.tileView = new QuickTileView(context, this) {
            @Override
            protected int getImageViewBackgroundRes() {
                return R.drawable.tile_bg_pink;
            }
            @Override
            public void onClick(View v) {
                super.onClick(v);
                context.startActivity(new Intent(context, UnstallProAppNavActivity.class));
            }
        };
    }
}
