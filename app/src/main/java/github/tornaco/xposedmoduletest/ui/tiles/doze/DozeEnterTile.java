package github.tornaco.xposedmoduletest.ui.tiles.doze;

import android.content.Context;

import java.util.Date;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;
import si.virag.fuzzydateformatter.FuzzyDateTimeFormatter;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class DozeEnterTile extends QuickTile {

    public DozeEnterTile(final Context context) {
        super(context);
        this.titleRes = R.string.title_last_doze;
        long lastTimeMills = XAshmanManager.get()
                .isServiceAvailable() ?
                XAshmanManager.get().getLastDozeEnterTimeMills()
                : -1;
        String lastDozeTimeStr = lastTimeMills > 0
                ? FuzzyDateTimeFormatter.getTimeAgo(context, new Date(lastTimeMills))
                : context.getResources().getString(R.string.summary_last_doze_unknown); // FIXME Extract to res.
        this.summary = context.getResources().getString(R.string.summary_last_doze, lastDozeTimeStr);
        this.iconRes = R.drawable.ic_access_time_black_24dp;
        this.tileView = new QuickTileView(context, this);
    }
}
