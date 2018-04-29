package github.tornaco.xposedmoduletest.ui.tiles.app;

import android.content.Context;
import android.view.View;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.xposed.XAppBuildVar;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class OpenMarket extends QuickTile {

    public OpenMarket(final Context context) {
        super(context);
        this.titleRes = R.string.title_open_market;
        this.iconRes = R.drawable.ic_shop_black_24dp;
        boolean isPlayVersion = XAppBuildVar.BUILD_VARS.contains(XAppBuildVar.PLAY);
        this.summary = !isPlayVersion ? "其他应用市场" : "Google play";
        this.tileView = new QuickTileView(context, this) {

            @Override
            protected int getImageViewBackgroundRes() {
                return R.drawable.tile_bg_green;
            }

            @Override
            public void onClick(View v) {
                super.onClick(v);

                // MinerDialog.show(context);
            }
        };
    }
}
