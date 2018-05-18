package github.tornaco.xposedmoduletest.ui.tiles.pmh;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.service.opt.gcm.WeChatPushNotificationHandler;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class WeChatPMHMockMessage extends QuickTile {

    private int messageCnt = 0;

    public WeChatPMHMockMessage(final Context context) {
        super(context);
        this.titleRes = R.string.title_push_message_handler_notification_mock_message;
        this.tileView = new QuickTileView(context, this) {
            @Override
            public void onClick(View v) {
                super.onClick(v);
                String userName = XAPMManager.get().getUserName();
                if (TextUtils.isEmpty(userName)) {
                    userName = Build.DEVICE;
                }
                XAPMManager.get().mockPushMessageReceived(
                        WeChatPushNotificationHandler.WECHAT_PKG_NAME,
                        String.format("Hello %s, this is the %s-st message!", userName, ++messageCnt));
            }
        };
    }
}
