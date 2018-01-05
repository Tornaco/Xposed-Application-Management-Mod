package github.tornaco.xposedmoduletest.ui.tiles;

import android.content.Context;
import android.view.View;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class PrivacyIccSerial extends QuickTile {

    public PrivacyIccSerial(final Context context) {
        super(context);
        this.titleRes = R.string.title_privacy_line_number;

        this.summary = context.getString(R.string.summary_tile_privacy_common,
                getLine1Number(), getUserSetLine1Number());

        this.iconRes = R.drawable.ic_sim_card_black_24dp;
        this.tileView = new QuickTileView(context, this) {
            @Override
            public void onClick(View v) {
                super.onClick(v);
            }
        };
    }

    private String getLine1Number() {
        return XAshmanManager.get().getLine1Number();
    }

    private String getUserSetLine1Number() {
        return XAshmanManager.get().getUserDefinedLine1Number();
    }
}
