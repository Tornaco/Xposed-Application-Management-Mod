package github.tornaco.xposedmoduletest.ui.tiles.app;

import android.content.Context;
import android.widget.RelativeLayout;

import org.newstand.logger.Logger;
import org.newstand.logger.Settings;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.SwitchTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.provider.XSettings;
import github.tornaco.xposedmoduletest.xposed.app.XAppGuardManager;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class ShowFocusedActivity extends QuickTile {

    public ShowFocusedActivity(final Context context) {
        super(context);
        this.titleRes = R.string.title_show_focused_activity;
        this.summaryRes = R.string.summary_show_focused_activity;
        this.iconRes = R.drawable.ic_add_alert_black_24dp;
        this.tileView = new SwitchTileView(context) {
            @Override
            protected void onBindActionView(RelativeLayout container) {
                super.onBindActionView(container);
                setChecked(XAppGuardManager.get().isServiceAvailable() && XAppGuardManager.get().isDebug());
                XSettings.setInDevMode(context, isChecked());
            }

            @Override
            protected void onCheckChanged(boolean checked) {
                super.onCheckChanged(checked);
                if (XAppGuardManager.get().isServiceAvailable()) {
                    XAppGuardManager.get().setDebug(checked);
                    XSettings.setInDevMode(context, checked);

                    Logger.config(Settings.builder().tag("X-APM-C")
                            .logLevel(isChecked()
                                    ? Logger.LogLevel.VERBOSE : Logger.LogLevel.WARN)
                            .build());
                }
            }
        };
    }
}
