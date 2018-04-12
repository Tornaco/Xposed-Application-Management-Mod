package github.tornaco.xposedmoduletest.ui.tiles.pmh;

import android.content.Context;
import android.widget.RelativeLayout;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class TGPMHShowContentSettings extends QuickTile {

    public TGPMHShowContentSettings(final Context context) {
        super(context);
        this.titleRes = R.string.title_push_message_handler_show_content;
        this.summaryRes = R.string.summary_tg_push_message_handler_show_content;
        this.tileView = new QuickTileView(context, this) {
            @Override
            protected void onBindActionView(RelativeLayout container) {
                super.onBindActionView(container);
                container.setEnabled(false);
            }
        };
    }
}
