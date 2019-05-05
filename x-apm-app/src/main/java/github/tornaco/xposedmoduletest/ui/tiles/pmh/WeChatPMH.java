package github.tornaco.xposedmoduletest.ui.tiles.pmh;

import android.content.Context;
import android.widget.RelativeLayout;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.SwitchTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.service.opt.gcm.WeChatPushNotificationHandler;

/**
 * Created by Tornaco on 2018/4/11 13:21.
 * God bless no bug!
 */
public class WeChatPMH extends QuickTile {

    public WeChatPMH(final Context context) {
        super(context);
        this.titleRes = R.string.title_push_message_handler_wechat;
        this.iconRes = R.drawable.ic_wechat_2_fill;
        this.tileView = new SwitchTileView(context) {

            @Override
            protected int getImageViewBackgroundRes() {
                return R.drawable.tile_bg_green;
            }

            @Override
            protected void onBindActionView(RelativeLayout container) {
                super.onBindActionView(container);
                setChecked(XAPMManager.get().isServiceAvailable()
                        && XAPMManager.get().isPushMessageHandlerEnabled(WeChatPushNotificationHandler.WECHAT_PKG_NAME));
            }

            @Override
            protected void onCheckChanged(boolean checked) {
                super.onCheckChanged(checked);
                if (XAPMManager.get().isServiceAvailable()) {
                    XAPMManager.get().setPushMessageHandlerEnabled(WeChatPushNotificationHandler.WECHAT_PKG_NAME, checked);
                }
            }
        };
    }
}