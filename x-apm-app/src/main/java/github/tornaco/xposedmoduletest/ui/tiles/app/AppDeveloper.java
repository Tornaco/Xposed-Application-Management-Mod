package github.tornaco.xposedmoduletest.ui.tiles.app;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.provider.AppSettings;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class AppDeveloper extends QuickTile {

    private int clickedTimes = 0;

    public AppDeveloper(final Context context) {
        super(context);
        this.titleRes = R.string.title_developer;
        this.summary = "Tornaco/tornaco@163.com";
        this.iconRes = R.drawable.ic_account_circle_black_24dp;
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
                    if (!AppSettings.isShowInfoEnabled(context, "show_hidden_features2", false)) {
                        AppSettings.setShowInfo(context, "show_hidden_features2", true);
                        Toast.makeText(context, "斤斤计较急急急", Toast.LENGTH_SHORT).show();
                    }
                    clickedTimes = 0;
                }
            }
        };
    }
}
