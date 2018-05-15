package github.tornaco.xposedmoduletest.ui.tiles.pmh;

import android.content.Context;
import android.widget.RelativeLayout;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.SwitchTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.service.opt.gcm.TGPushNotificationHandler;

/**
 * Created by Tornaco on 2018/4/11 13:21.
 * God bless no bug!
 */
public class TGPMH extends QuickTile {

    public TGPMH(final Context context) {
        super(context);
        this.titleRes = R.string.title_push_message_handler_tg;
        this.iconRes = R.drawable.ic_send_black_24dp;
        this.tileView = new SwitchTileView(context) {

            @Override
            protected int getImageViewBackgroundRes() {
                return R.drawable.tile_bg_blue;
            }

            @Override
            protected void onBindActionView(RelativeLayout container) {
                super.onBindActionView(container);
                setChecked(XAPMManager.get().isServiceAvailable()
                        && XAPMManager.get().isPushMessageHandlerEnabled(TGPushNotificationHandler.TG_PKG_NAME));
            }

            @Override
            protected void onCheckChanged(boolean checked) {
                super.onCheckChanged(checked);
                if (XAPMManager.get().isServiceAvailable()) {
                    XAPMManager.get().setPushMessageHandlerEnabled(TGPushNotificationHandler.TG_PKG_NAME, checked);
                }
            }
        };
    }
}