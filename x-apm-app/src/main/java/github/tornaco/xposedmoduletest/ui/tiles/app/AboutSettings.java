package github.tornaco.xposedmoduletest.ui.tiles.app;

import android.content.Context;
import android.view.View;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.activity.AboutSettingsActivity;

/**
 * Created by guohao4 on 2017/11/16.
 * Email: Tornaco@163.com
 */

public class AboutSettings extends QuickTile {

    public AboutSettings(final Context context) {
        super(context);

        this.titleRes = R.string.title_about;
        this.summaryRes = R.string.summary_about_settings;
        this.iconRes = R.drawable.ic_info_black_24dp;

        this.tileView = new QuickTileView(context, this) {

            @Override
            protected int getImageViewBackgroundRes() {
                return R.drawable.tile_bg_indigo;
            }

            @Override
            public void onClick(View v) {
                super.onClick(v);
                AboutSettingsActivity.start(context);
            }
        };
    }
}
