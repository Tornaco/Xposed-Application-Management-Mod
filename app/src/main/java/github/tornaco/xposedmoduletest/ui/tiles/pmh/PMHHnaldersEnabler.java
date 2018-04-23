package github.tornaco.xposedmoduletest.ui.tiles.pmh;

import android.content.Context;
import android.view.View;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.activity.pmh.PMHAvailableHandlersActivity;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class PMHHnaldersEnabler extends QuickTile {

    public PMHHnaldersEnabler(final Context context) {
        super(context);
        this.titleRes = R.string.title_push_message_available_handlers;
        this.iconRes = R.drawable.ic_receipt_black_24dp;
        this.tileView = new QuickTileView(context, this) {
            @Override
            public void onClick(View v) {
                super.onClick(v);
                PMHAvailableHandlersActivity.start(context);
            }
        };
    }
}
