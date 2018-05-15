package github.tornaco.xposedmoduletest.ui.tiles.pmh;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.util.OSUtil;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.service.opt.gcm.WeChatPushNotificationHandler;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class WeChatPMHNotificationSettingsOreo extends QuickTile {

    public WeChatPMHNotificationSettingsOreo(final Context context) {
        super(context);
        this.titleRes = R.string.title_push_message_handler_notification_settings_oreo;
        this.summaryRes = R.string.error_notification_channel_activity_not_found;
        this.tileView = new QuickTileView(context, this) {
            @Override
            public void onClick(View v) {
                super.onClick(v);
                if (OSUtil.isOOrAbove()) {
                    if (!WeChatPushNotificationHandler.launchNotificationChannelSettingsForOreo(context,
                            !XAPMManager.get()
                                    .isPushMessageHandlerMessageNotificationByAppEnabled(WeChatPushNotificationHandler.WECHAT_PKG_NAME))) {
                        Toast.makeText(context, R.string.error_notification_channel_activity_not_found, Toast.LENGTH_LONG).show();
                    }
                }
            }
        };
    }
}
