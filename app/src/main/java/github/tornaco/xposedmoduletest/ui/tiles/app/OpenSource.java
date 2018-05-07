package github.tornaco.xposedmoduletest.ui.tiles.app;

import android.content.Context;
import android.view.View;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.activity.AboutSettingsActivity;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class OpenSource extends QuickTile {

    public OpenSource(final Context context) {
        super(context);
        this.titleRes = R.string.title_open_source;
        this.iconRes = R.drawable.ic_code_white_24dp;
        this.summary = "Github@Tornaco/X-APM";
        this.tileView = new QuickTileView(context, this) {
            @Override
            protected int getImageViewBackgroundRes() {
                return R.drawable.tile_bg_blue_grey;
            }

            @Override
            public void onClick(View v) {
                super.onClick(v);
                AboutSettingsActivity a = (AboutSettingsActivity) context;
                a.navigateToWebPage("https://github.com/Tornaco/X-APM");
            }
        };
    }
}
