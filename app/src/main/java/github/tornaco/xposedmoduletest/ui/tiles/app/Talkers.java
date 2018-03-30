package github.tornaco.xposedmoduletest.ui.tiles.app;

import android.content.Context;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.xposed.XApp;

/**
 * Created by guohao4 on 2017/8/2.
 * Email: Tornaco@163.com
 */

public class Talkers extends QuickTile {

    public Talkers(final Context context) {
        super(context);
        this.titleRes = R.string.title_talkers;
        this.summaryRes = XApp.isPlayVersion() ? R.string.summary_talkers_play : R.string.summary_talkers_others;
        this.iconRes = R.drawable.ic_feedback_black_24dp;
        this.tileView = new QuickTileView(context, this);
    }
}
