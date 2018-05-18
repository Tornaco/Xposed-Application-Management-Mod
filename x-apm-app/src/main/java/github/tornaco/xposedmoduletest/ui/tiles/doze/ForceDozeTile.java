package github.tornaco.xposedmoduletest.ui.tiles.doze;

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

public class ForceDozeTile extends QuickTile {

    public ForceDozeTile(final Context context) {
        super(context);
        this.titleRes = R.string.title_force_doze;
        this.summaryRes = R.string.summary_doze_force;
        this.iconRes = R.drawable.ic_child_friendly_black_24dp;

        this.tileView = new SwitchTileView(context) {
            @Override
            protected void onBindActionView(RelativeLayout container) {
                super.onBindActionView(container);
                setChecked(XAPMManager.get().isForceDozeEnabled());
            }

            @Override
            protected void onCheckChanged(boolean checked) {
                super.onCheckChanged(checked);
                XAPMManager.get().setForceDozeEnabled(checked);
            }
        };
    }
}
