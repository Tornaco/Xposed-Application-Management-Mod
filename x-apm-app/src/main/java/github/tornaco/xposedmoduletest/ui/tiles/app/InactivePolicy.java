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

public class InactivePolicy extends QuickTile {

    public InactivePolicy(final Context context, String module) {
        super(context);

        this.titleRes = R.string.title_inactive_instead_of_kill;
        this.summaryRes = R.string.summary_inactive_instead_of_kill;
        this.iconRes = R.drawable.ic_stop_black_24dp;
        this.tileView = new SwitchTileView(context) {

            @Override
            protected int getImageViewBackgroundRes() {
                return R.drawable.tile_bg_red;
            }

            @Override
            protected void onBindActionView(RelativeLayout container) {
                super.onBindActionView(container);
                boolean isIdlePolicy = XAPMManager.get().getAppInactivePolicyForModule(module)
                        == XAPMManager.AppInactivePolicy.IDLE;
                setChecked(isIdlePolicy);
            }

            @Override
            protected void onCheckChanged(boolean checked) {
                super.onCheckChanged(checked);
                int policy = checked ? XAPMManager.AppInactivePolicy.IDLE : XAPMManager.AppInactivePolicy.FORCE_STOP;
                XAPMManager.get().setAppInactivePolicyForModule(module, policy);
            }
        };
    }
}
