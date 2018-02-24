package github.tornaco.xposedmoduletest.ui.tiles.app;

import android.content.Context;
import android.widget.RelativeLayout;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.SwitchTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class DetailedToastActivity extends QuickTile {

    public DetailedToastActivity(final Context context) {
        super(context);
        this.titleRes = R.string.title_opt_toast_caller;
        this.iconRes = R.drawable.ic_add_alert_black_24dp;
        this.tileView = new SwitchTileView(context) {
            @Override
            protected void onBindActionView(RelativeLayout container) {
                super.onBindActionView(container);
                setChecked(XAshmanManager.get().isServiceAvailable() && XAshmanManager.get().isOptFeatureEnabled(XAshmanManager.OPT.TOAST.name()));
            }

            @Override
            protected void onCheckChanged(boolean checked) {
                super.onCheckChanged(checked);
                if (XAshmanManager.get().isServiceAvailable()) {
                    XAshmanManager.get().setOptFeatureEnabled(XAshmanManager.OPT.TOAST.name(), checked);
                }
            }
        };
    }
}
