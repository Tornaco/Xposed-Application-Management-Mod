package github.tornaco.xposedmoduletest.ui.tiles.pmh;

import android.content.Context;
import android.widget.RelativeLayout;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.SwitchTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class WeChatPMHSoundSettings extends QuickTile {

    public WeChatPMHSoundSettings(final Context context) {
        super(context);
        this.titleRes = R.string.title_push_message_handler_sound;
        this.summaryRes = R.string.summarywechat__push_message_handler_sound;
        this.tileView = new SwitchTileView(context) {
            @Override
            protected void onBindActionView(RelativeLayout container) {
                super.onBindActionView(container);
                setChecked(XAshmanManager.get().isServiceAvailable()
                        && XAshmanManager.get().isPushMessageHandlerNotificationSoundEnabled("wechat"));
            }

            @Override
            protected void onCheckChanged(boolean checked) {
                super.onCheckChanged(checked);
                if (XAshmanManager.get().isServiceAvailable()) {
                    XAshmanManager.get().setPushMessageHandlerNotificationSoundEnabled("wechat", checked);
                }
            }
        };
    }
}
