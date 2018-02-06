package github.tornaco.xposedmoduletest.ui.tiles.app;

import android.content.Context;
import android.widget.RelativeLayout;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.SwitchTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.provider.AppSettings;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class AutoSeLinuxMode extends QuickTile {

    public AutoSeLinuxMode(final Context context) {
        super(context);

        this.titleRes = R.string.title_selinux_mode_auto;
        this.summaryRes = R.string.summary_selinux_mode_auto;
        this.iconRes = R.drawable.ic_brightness_auto_black_24dp;
        this.tileView = new SwitchTileView(context) {
            @Override
            protected void onBindActionView(RelativeLayout container) {
                super.onBindActionView(container);
                setChecked(AppSettings.isSelinuxModeAutoSetEnabled(context));
            }

            @Override
            protected void onCheckChanged(final boolean checked) {
                super.onCheckChanged(checked);
                AppSettings.setSelinuxModeAutoSetEnabled(context, checked);
            }
        };
    }
}
