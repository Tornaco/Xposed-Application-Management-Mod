package github.tornaco.xposedmoduletest.ui.tiles;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.activity.doze.DozeNavActivity;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class Doze extends QuickTile {

    public Doze(final Context context) {
        super(context);
        this.titleRes = R.string.title_doze;
        this.iconRes = R.drawable.ic_airline_seat_individual_suite_black_24dp;
        this.tileView = new QuickTileView(context, this) {
            @Override
            public void onClick(View v) {
                super.onClick(v);
                context.startActivity(new Intent(context, DozeNavActivity.class));
            }

            @Override
            protected boolean useStaticTintColor() {
                return true;
            }
        };
    }
}
