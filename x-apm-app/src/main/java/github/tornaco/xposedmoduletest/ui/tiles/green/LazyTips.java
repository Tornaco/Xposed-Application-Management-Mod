package github.tornaco.xposedmoduletest.ui.tiles.green;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.RelativeLayout;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.SwitchTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;

/**
 * Created by Tornaco on 2018/4/28 13:36.
 * God bless no bug!
 */
public class LazyTips extends QuickTile {

    public LazyTips(@NonNull Context context) {
        super(context, null);

        this.titleRes = R.string.title_lazy_toast;
        this.summaryRes = R.string.summary_lazy_toast;
        this.iconRes = R.drawable.ic_notifications_active_black_24dp;

        this.tileView = new SwitchTileView(context) {
            @Override
            protected void onBindActionView(RelativeLayout container) {
                super.onBindActionView(container);
                setChecked(XAPMManager.get().isOptFeatureEnabled(XAPMManager.OPT.LAZY_APP_TIPS.name()));
            }

            @Override
            protected void onCheckChanged(boolean checked) {
                super.onCheckChanged(checked);
                XAPMManager.get().setOptFeatureEnabled(XAPMManager.OPT.LAZY_APP_TIPS.name(), checked);
            }
        };

    }
}
