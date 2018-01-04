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

public class DoNotKillSBNApp extends QuickTile {

    public DoNotKillSBNApp(final Context context) {
        super(context);

        this.titleRes = R.string.title_do_not_kill_sbn_app;
        this.summaryRes = R.string.summary_do_not_kill_sbn_app;
        this.iconRes = R.drawable.ic_notifications_black_24dp;
        this.tileView = new SwitchTileView(context) {
            @Override
            protected void onBindActionView(RelativeLayout container) {
                super.onBindActionView(container);
                setChecked(XAshmanManager.get().isServiceAvailable() &&
                        XAshmanManager.get().isDoNotKillSBNEnabled());
            }

            @Override
            protected void onCheckChanged(boolean checked) {
                super.onCheckChanged(checked);
                if (XAshmanManager.get().isServiceAvailable()) {
                    XAshmanManager.get().setDoNotKillSBNEnabled(checked);
                }
            }
        };
    }
}
