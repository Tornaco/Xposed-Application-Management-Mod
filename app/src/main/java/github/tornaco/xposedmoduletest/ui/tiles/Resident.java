package github.tornaco.xposedmoduletest.ui.tiles;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.provider.AppSettings;
import github.tornaco.xposedmoduletest.ui.activity.resdient.ResdientAppNavActivity;
import github.tornaco.xposedmoduletest.xposed.XApp;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class Resident extends QuickTile {

    public Resident(final Context context) {
        super(context);
        this.titleRes = R.string.title_app_resident;

        final boolean donateOrPlay = (AppSettings.isDonated(getContext())
                || XApp.isPlayVersion());
        if (!donateOrPlay) {
            this.summaryRes = R.string.donated_available;
        } else if (XAshmanManager.get().isServiceAvailable()) {
            this.summaryRes = XAshmanManager.get().isResidentEnabled() ?
                    R.string.summary_func_enabled : 0;
        }
        this.iconRes = R.drawable.ic_grade_black_24dp;
        this.tileView = new QuickTileView(context, this) {
            @Override
            public void onClick(View v) {
                super.onClick(v);
                if (donateOrPlay) {
                    context.startActivity(new Intent(context, ResdientAppNavActivity.class));
                }
            }
        };
    }
}
