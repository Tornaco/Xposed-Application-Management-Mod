package github.tornaco.xposedmoduletest.ui.tiles.app;

import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.SwitchTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.activity.app.InstalledAppTemplateSettingsDashboardActivity;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class AutoApplyAppSettingsTemplate extends QuickTile {

    public AutoApplyAppSettingsTemplate(final Context context) {
        super(context);

        this.titleRes = R.string.title_auto_black;
        this.summaryRes = R.string.summary_auto_black;
        this.iconRes = R.drawable.ic_brightness_auto_black_24dp;
        this.tileView = new SwitchTileView(context) {

            @Override
            protected int getImageViewBackgroundRes() {
                return R.drawable.tile_bg_blue;
            }

            @Override
            public void onClick(View v) {
                InstalledAppTemplateSettingsDashboardActivity.start(context, XAPMManager.APPOPS_WORKAROUND_DUMMY_PACKAGE_NAME);
            }

            @Override
            protected void onBindActionView(RelativeLayout container) {
                super.onBindActionView(container);
                setChecked(XAPMManager.get().isServiceAvailable() &&
                        XAPMManager.get().isAutoAddBlackEnabled());
            }

            @Override
            protected void onCheckChanged(boolean checked) {
                super.onCheckChanged(checked);
                if (XAPMManager.get().isServiceAvailable()) {
                    XAPMManager.get().setAutoAddBlackEnable(checked);
                }
            }
        };
    }
}
