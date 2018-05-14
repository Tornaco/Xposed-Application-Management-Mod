package github.tornaco.xposedmoduletest.ui.tiles.ag;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.activity.ag.AdvancedGuardSettings;

/**
 * Created by guohao4 on 2017/11/2.
 * Email: Tornaco@163.com
 */
@Deprecated
public class Advanced extends QuickTile {

    public Advanced(@NonNull final Context context) {
        super(context);
        this.titleRes = R.string.title_advance;
        this.iconRes = R.drawable.ic_build_white_24dp;
        this.tileView = new QuickTileView(context, this) {
            @Override
            public void onClick(View v) {
                super.onClick(v);
                context.startActivity(new Intent(context, AdvancedGuardSettings.class));
            }
        };
    }
}
