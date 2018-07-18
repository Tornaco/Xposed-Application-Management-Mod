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
@Deprecated // Show it force.
public class GcmIndicator extends QuickTile {

    public GcmIndicator(final Context context) {
        super(context);
        this.titleRes = R.string.title_gcm_indicator;
        this.summaryRes = R.string.summary_gcm_indicator;
        this.iconRes = R.drawable.ic_filter_tilt_shift_black_24dp;
        this.tileView = new SwitchTileView(context) {
            @Override
            protected void onBindActionView(RelativeLayout container) {
                super.onBindActionView(container);
                setChecked(AppSettings.isShowGcmIndicator(context));
            }

            @Override
            protected void onCheckChanged(boolean checked) {
                super.onCheckChanged(checked);
                AppSettings.setShowGcmIndicator(context, checked);
            }
        };
    }
}
