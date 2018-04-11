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

public class PMHShowContentSettings extends QuickTile {

    public PMHShowContentSettings(final Context context) {
        super(context);
        this.titleRes = R.string.title_push_message_handler_show_content;
        this.summaryRes = R.string.summary_push_message_handler_show_content;
        this.iconRes = R.drawable.ic_remove_red_eye_black_24dp;
        this.tileView = new SwitchTileView(context) {
            @Override
            protected void onBindActionView(RelativeLayout container) {
                super.onBindActionView(container);
                setChecked(XAshmanManager.get().isServiceAvailable()
                        && XAshmanManager.get().isPushMessageHandlerShowContentEnabled("any"));
            }

            @Override
            protected void onCheckChanged(boolean checked) {
                super.onCheckChanged(checked);
                if (XAshmanManager.get().isServiceAvailable()) {
                    XAshmanManager.get().setPushMessageHandlerShowContentEnabled("any", checked);
                }
            }
        };
    }
}
