package github.tornaco.xposedmoduletest.ui.tiles.pmh;

import android.content.Context;
import android.widget.RelativeLayout;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.SwitchTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.service.opt.gcm.WeChatPushNotificationHandler;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class WeChatPMHNotificationPostByApp extends QuickTile {

    public WeChatPMHNotificationPostByApp(final Context context) {
        super(context);
        this.titleRes = R.string.title_push_message_handler_notification_by_app;
        this.summaryRes = R.string.summarywechat__push_message_handler_notification_by_app;
        this.tileView = new SwitchTileView(context) {
            @Override
            protected void onBindActionView(RelativeLayout container) {
                super.onBindActionView(container);
                setChecked(XAPMManager.get().isPushMessageHandlerMessageNotificationByAppEnabled(WeChatPushNotificationHandler.WECHAT_PKG_NAME));
            }

            @Override
            protected void onCheckChanged(boolean checked) {
                super.onCheckChanged(checked);
                XAPMManager.get().setPushMessageHandlerMessageNotificationByAppEnabled(WeChatPushNotificationHandler.WECHAT_PKG_NAME, checked);
            }
        };
    }
}
