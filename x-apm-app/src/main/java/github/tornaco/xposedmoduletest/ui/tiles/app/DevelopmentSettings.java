package github.tornaco.xposedmoduletest.ui.tiles.app;

import android.content.Context;
import android.view.View;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.activity.app.ToolsDashboardActivity;

/**
 * Created by guohao4 on 2017/11/16.
 * Email: Tornaco@163.com
 */

public class DevelopmentSettings extends QuickTile {

    public DevelopmentSettings(final Context context) {
        super(context);

        this.titleRes = R.string.title_dev_tools;
        this.summaryRes = R.string.summary_dev_settings;
        this.iconRes = R.drawable.ic_android_fill;

        this.tileView = new QuickTileView(context, this) {

            @Override
            protected int getImageViewBackgroundRes() {
                return R.drawable.tile_bg_green;
            }

            @Override
            public void onClick(View v) {
                super.onClick(v);
                ToolsDashboardActivity.start(context);
            }
        };
    }
}
