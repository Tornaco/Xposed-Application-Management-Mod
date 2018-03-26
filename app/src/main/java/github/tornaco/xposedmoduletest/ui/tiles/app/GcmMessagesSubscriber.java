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

public class GcmMessagesSubscriber extends QuickTile {

    public GcmMessagesSubscriber(final Context context) {
        super(context);
        this.titleRes = R.string.title_subscribe_gcm_messages;
        this.summaryRes = R.string.summary_subscribe_gcm_messages;
        this.iconRes = R.drawable.ic_new_releases_black_24dp;
        this.tileView = new SwitchTileView(context) {
            @Override
            protected void onBindActionView(RelativeLayout container) {
                super.onBindActionView(container);
                setChecked(AppSettings.isSubscribeGcmMessage(context));
            }

            @Override
            protected void onCheckChanged(boolean checked) {
                super.onCheckChanged(checked);
                AppSettings.setSuscribeGcmMessage(context, checked);
            }
        };
    }
}
