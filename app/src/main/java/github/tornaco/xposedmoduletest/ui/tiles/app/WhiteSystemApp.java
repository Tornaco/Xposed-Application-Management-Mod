package github.tornaco.xposedmoduletest.ui.tiles.app;

import android.content.Context;
import android.widget.RelativeLayout;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.SwitchTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class WhiteSystemApp extends QuickTile {

    public WhiteSystemApp(final Context context) {
        super(context);

        this.titleRes = R.string.title_white_system_app;
        this.summaryRes = R.string.summary_white_system_app;
        this.iconRes = R.drawable.ic_android_black_24dp;
        this.tileView = new SwitchTileView(context) {
            @Override
            protected void onBindActionView(RelativeLayout container) {
                super.onBindActionView(container);
                setChecked(XAshmanManager.get().isServiceAvailable() &&
                        XAshmanManager.get().isWhiteSysAppEnabled());
            }

            @Override
            protected void onCheckChanged(boolean checked) {
                super.onCheckChanged(checked);
                if (XAshmanManager.get().isServiceAvailable()) {
                    XAshmanManager.get().setWhiteSysAppEnabled(checked);
                }
            }
        };
    }
}
