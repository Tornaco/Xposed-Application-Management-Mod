package github.tornaco.xposedmoduletest.ui.tiles.app;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.activity.extra.PayListBrowserActivity;

/**
 * Created by Tornaco on 2017/7/28.
 * Licensed with Apache.
 */

public class PayListTile extends QuickTile {

    public PayListTile(final Context context) {
        super(context);

        this.iconRes = R.drawable.ic_payment_black_24dp;
        this.titleRes = R.string.title_pay_list;

        this.tileView = new QuickTileView(context, this) {
            @Override
            public void onClick(View v) {
                super.onClick(v);
                context.startActivity(new Intent(context, PayListBrowserActivity.class));
            }
        };


    }
}
