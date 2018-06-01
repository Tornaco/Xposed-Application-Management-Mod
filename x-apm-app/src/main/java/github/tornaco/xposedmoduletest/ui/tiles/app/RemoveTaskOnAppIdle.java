package github.tornaco.xposedmoduletest.ui.tiles.app;

import android.content.Context;
import android.widget.RelativeLayout;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.SwitchTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class RemoveTaskOnAppIdle extends QuickTile {

    public RemoveTaskOnAppIdle(final Context context) {
        super(context);

        this.titleRes = R.string.title_remove_task_on_app_idle;
        this.iconRes = R.drawable.ic_remove_circle_black_24dp;
        this.tileView = new SwitchTileView(context) {

            @Override
            protected int getImageViewBackgroundRes() {
                return R.drawable.tile_bg_red;
            }

            @Override
            protected void onBindActionView(RelativeLayout container) {
                super.onBindActionView(container);
                setChecked(XAPMManager.get().isServiceAvailable() &&
                        XAPMManager.get().isOptFeatureEnabled(XAPMManager.OPT.REMOVE_TASK_ON_APP_IDLE.name()));
            }

            @Override
            protected void onCheckChanged(boolean checked) {
                super.onCheckChanged(checked);
                if (XAPMManager.get().isServiceAvailable()) {
                    XAPMManager.get().setOptFeatureEnabled(XAPMManager.OPT.REMOVE_TASK_ON_APP_IDLE.name(), checked);
                }
            }
        };
    }
}
