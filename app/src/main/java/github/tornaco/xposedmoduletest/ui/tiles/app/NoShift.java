package github.tornaco.xposedmoduletest.ui.tiles.app;

import android.content.Context;
import android.widget.RelativeLayout;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.SwitchTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.provider.AppSettings;

/**
 * Created by guohao4 on 2017/8/2.
 * Email: Tornaco@163.com
 */

public class NoShift extends QuickTile {

    public NoShift(final Context context) {
        super(context);
        this.titleRes = R.string.title_bottom_nav_no_shift;
        this.summaryRes = R.string.summary_bottom_nav_no_shift;
        this.iconRes = R.drawable.ic_border_bottom_black_24dp;
        this.tileView = new SwitchTileView(context) {
            @Override
            protected void onBindActionView(RelativeLayout container) {
                super.onBindActionView(container);
                setChecked(AppSettings.isBottomNavNoShiftEnabled(context));
            }

            @Override
            protected void onCheckChanged(boolean checked) {
                super.onCheckChanged(checked);
                AppSettings.setBottomNavNoShiftEnabled(context, checked);
            }
        };
    }
}
