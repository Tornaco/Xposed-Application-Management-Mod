package github.tornaco.xposedmoduletest.ui.tiles.smartsense;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.activity.smartsense.FocusedAppActionActivity;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class AppFocusAction extends QuickTile {

    public AppFocusAction(final Context context) {
        super(context);
        this.titleRes = R.string.title_app_focus_acions;
        this.iconRes = R.drawable.ic_android_black_24dp;
        this.tileView = new QuickTileView(context, this) {
            @Override
            public void onClick(View v) {
                super.onClick(v);
                context.startActivity(new Intent(context, FocusedAppActionActivity.class));
            }
        };
    }
}
