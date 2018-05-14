package github.tornaco.xposedmoduletest.ui.tiles.app;

import android.content.Context;
import android.widget.RelativeLayout;
import android.widget.Toast;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.SwitchTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.provider.AppSettings;

/**
 * Created by guohao4 on 2017/8/2.
 * Email: Tornaco@163.com
 */

public class PStyleIcon extends QuickTile {

    public PStyleIcon(final Context context) {
        super(context);
        this.titleRes = R.string.title_p_style_icon;
        this.iconRes = R.drawable.ic_beach_access_black_24dp;
        this.tileView = new SwitchTileView(context) {
            @Override
            protected void onBindActionView(RelativeLayout container) {
                super.onBindActionView(container);
                setChecked(AppSettings.isPStyleIcon(context));
            }

            @Override
            protected void onCheckChanged(boolean checked) {
                super.onCheckChanged(checked);
                AppSettings.setPStyleIcon(context, checked);
                Toast.makeText(context, R.string.title_theme_need_restart_app, Toast.LENGTH_SHORT).show();
            }
        };
    }
}
