package github.tornaco.xposedmoduletest.ui.tiles.app;

import android.content.Context;
import android.widget.RelativeLayout;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.SwitchTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class AutoBlackNotification extends QuickTile {

    public AutoBlackNotification(final Context context) {
        super(context);

        this.titleRes = R.string.title_auto_black_noti;
        this.iconRes = R.drawable.ic_notifications_black_24dp;
        this.tileView = new SwitchTileView(context) {

            @Override
            protected int getImageViewBackgroundRes() {
                return R.drawable.tile_bg_blue;
            }

            @Override
            protected void onBindActionView(RelativeLayout container) {
                super.onBindActionView(container);
                setChecked(XAPMManager.get().isServiceAvailable() &&
                        XAPMManager.get().isAutoAddBlackNotificationEnabled());
            }

            @Override
            protected void onCheckChanged(boolean checked) {
                super.onCheckChanged(checked);
                if (XAPMManager.get().isServiceAvailable()) {
                    XAPMManager.get().setAutoAddBlackNotificationEnabled(checked);
                }
            }
        };
    }
}
