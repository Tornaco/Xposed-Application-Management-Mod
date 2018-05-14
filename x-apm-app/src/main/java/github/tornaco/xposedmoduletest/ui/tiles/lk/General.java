package github.tornaco.xposedmoduletest.ui.tiles.lk;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.activity.lk.GeneralLKSettings;

/**
 * Created by guohao4 on 2017/11/16.
 * Email: Tornaco@163.com
 */

public class General extends QuickTile {

    public General(final Context context) {
        super(context);
        this.titleRes = R.string.title_general_settings;
        this.iconRes = R.drawable.ic_build_white_24dp;
        this.tileView = new QuickTileView(context, this) {
            @Override
            public void onClick(View v) {
                super.onClick(v);
                context.startActivity(new Intent(context, GeneralLKSettings.class));
            }
        };
    }
}
