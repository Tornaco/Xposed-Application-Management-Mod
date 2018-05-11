package github.tornaco.xposedmoduletest.ui.tiles.app;

import android.content.Context;
import android.view.View;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.activity.PolicySettingsActivity;

/**
 * Created by guohao4 on 2017/11/16.
 * Email: Tornaco@163.com
 */

public class PolicySettings extends QuickTile {

    public PolicySettings(final Context context) {
        super(context);

        this.titleRes = R.string.title_policy_settings;
        this.summaryRes = R.string.summary_policy_settings;
        this.iconRes = R.drawable.ic_directions_boat_black_24dp;

        this.tileView = new QuickTileView(context, this) {

            @Override
            protected int getImageViewBackgroundRes() {
                return R.drawable.tile_bg_blue;
            }

            @Override
            public void onClick(View v) {
                super.onClick(v);
                PolicySettingsActivity.start(context);
            }
        };
    }
}
