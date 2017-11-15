package github.tornaco.xposedmoduletest.ui.tiles;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.activity.ClearProcessActivity;
import github.tornaco.xposedmoduletest.util.EmojiUtil;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class ClearProgress extends QuickTile {

    public ClearProgress(final Context context) {
        super(context);
        this.title = context.getResources().getString(R.string.clear_process,
                EmojiUtil.getEmojiByUnicode(EmojiUtil.BOOST));
        this.iconRes = R.drawable.ic_memory_black_24dp;
        this.tileView = new QuickTileView(context, this) {
            @Override
            public void onClick(View v) {
                super.onClick(v);
                context.startActivity(new Intent(context, ClearProcessActivity.class));
            }
        };
    }
}
