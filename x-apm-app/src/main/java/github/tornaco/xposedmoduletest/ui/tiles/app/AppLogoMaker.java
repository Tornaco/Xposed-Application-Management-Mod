package github.tornaco.xposedmoduletest.ui.tiles.app;

import android.content.Context;
import android.view.View;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class AppLogoMaker extends QuickTile {

    private int clickedTimes = 0;

    public AppLogoMaker(final Context context) {
        super(context);
        this.titleRes = R.string.title_logo_maker;
        this.summary = "LogoMaker&RemixIcon";
        this.iconRes = R.drawable.ic_brush_black_24dp;
        this.tileView = new QuickTileView(context, this) {
            @Override
            protected int getImageViewBackgroundRes() {
                return R.drawable.tile_bg_indigo;
            }

            @Override
            public void onClick(View v) {
                super.onClick(v);
                clickedTimes++;

                if (clickedTimes >= 8) {
                    clickedTimes = 0;
                }
            }
        };
    }
}
