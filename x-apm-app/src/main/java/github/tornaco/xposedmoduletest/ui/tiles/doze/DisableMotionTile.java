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

public class DisableMotionTile extends QuickTile {

    public DisableMotionTile(final Context context) {
        super(context);
        this.titleRes = R.string.title_disable_motion;
        this.summaryRes = R.string.summary_disable_motion;
        this.iconRes = R.drawable.ic_directions_run_black_16_sc_dp;

        this.tileView = new SwitchTileView(context) {
            @Override
            protected void onBindActionView(RelativeLayout container) {
                super.onBindActionView(container);
                setChecked(XAPMManager.get().isDisableMotionEnabled());
            }

            @Override
            protected void onCheckChanged(boolean checked) {
                super.onCheckChanged(checked);
                XAPMManager.get().setDisableMotionEnabled(checked);
                XAPMManager.get().showRebootNeededNotification("DisableMotion");
            }
        };
    }
}
