package github.tornaco.xposedmoduletest.ui.tiles.smartsense;

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

public class LongPressBackKey extends QuickTile {

    public LongPressBackKey(final Context context) {
        super(context);
        this.titleRes = R.string.title_long_press_back;
        this.iconRes = R.drawable.ic_touch_app_black_24dp;
        this.tileView = new SwitchTileView(context) {
            @Override
            protected void onBindActionView(RelativeLayout container) {
                super.onBindActionView(container);
                setChecked(XAshmanManager.get().isServiceAvailable()
                        && XAshmanManager.get().isLPBKEnabled());
            }

            @Override
            protected void onCheckChanged(boolean checked) {
                super.onCheckChanged(checked);
                if (XAshmanManager.get().isServiceAvailable()) {
                    XAshmanManager.get().setLPBKEnabled(checked);
                }
            }
        };
    }
}
