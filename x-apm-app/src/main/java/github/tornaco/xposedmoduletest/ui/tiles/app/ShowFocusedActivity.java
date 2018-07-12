package github.tornaco.xposedmoduletest.ui.tiles.app;

import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.widget.RelativeLayout;

import org.newstand.logger.Logger;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.SwitchTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.service.ActivityToastQuickTileService;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;

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
                setChecked(XAPMManager.get().isServiceAvailable() && XAPMManager.get().showFocusedActivityInfoEnabled());
            }

            @Override
            protected void onCheckChanged(boolean checked) {
                super.onCheckChanged(checked);
                if (XAPMManager.get().isServiceAvailable()) {
                    XAPMManager.get().setShowFocusedActivityInfoEnabled(checked);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        try {
                            ComponentName componentName = new ComponentName(context, ActivityToastQuickTileService.class);
                            ActivityToastQuickTileService.requestListeningState(context, componentName);
                        } catch (Throwable e) {
                            Logger.e("Fail requestListeningState to tile: " + e);
                        }
                    }
                }
            }
        };
    }
}
