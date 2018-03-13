package github.tornaco.xposedmoduletest.ui.tiles.app;

import android.content.Context;
import android.widget.RelativeLayout;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.SwitchTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.provider.AppSettings;

/**
 * Created by guohao4 on 2017/8/2.
 * Email: Tornaco@163.com
 */

public class ShowTileDivider extends QuickTile {

    public ShowTileDivider(final Context context) {
        super(context);
        this.titleRes = R.string.title_show_tile_divider;
        this.iconRes = R.drawable.ic_line_style_black_24dp;
        this.tileView = new SwitchTileView(context) {
            @Override
            protected void onBindActionView(RelativeLayout container) {
                super.onBindActionView(container);
                setChecked(AppSettings.isShowTileDivider(context));
            }

            @Override
            protected void onCheckChanged(boolean checked) {
                super.onCheckChanged(checked);
                AppSettings.setShowDivider(context, checked);
            }
        };
    }
}
