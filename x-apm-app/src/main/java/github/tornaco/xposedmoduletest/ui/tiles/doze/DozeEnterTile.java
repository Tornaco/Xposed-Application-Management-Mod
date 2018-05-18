package github.tornaco.xposedmoduletest.ui.tiles.doze;

import android.content.Context;
import android.view.View;

import org.newstand.logger.Logger;

import java.util.Date;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.activity.doze.DozeEventHistoryViewerActivity;
import github.tornaco.xposedmoduletest.util.TimeUtil;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.bean.DozeEvent;
import si.virag.fuzzydateformatter.FuzzyDateTimeFormatter;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class DozeEnterTile extends QuickTile {

    public DozeEnterTile(final Context context) {
        super(context);
        this.titleRes = R.string.title_last_doze;
        DozeEvent lastDoze = XAPMManager.get().isServiceAvailable() ?
                XAPMManager.get().getLastDozeEvent() : null;

        Logger.d("lastDoze: " + lastDoze);

        long enterTimeMills = (lastDoze != null && lastDoze.getResult() == DozeEvent.RESULT_SUCCESS) ?
                lastDoze.getEnterTimeMills()
                : -1;
        String enterTimeStr = enterTimeMills > 0
                ? FuzzyDateTimeFormatter.getTimeAgo(context, new Date(enterTimeMills))
                : context.getResources().getString(R.string.summary_last_doze_unknown);

        long endTimeMills = (lastDoze != null && lastDoze.getResult() == DozeEvent.RESULT_SUCCESS) ?
                lastDoze.getEndTimeMills()
                : -1;
        String dozeTime = (enterTimeMills > 0 && endTimeMills > 0 && endTimeMills > enterTimeMills)
                ? TimeUtil.formatDuration(endTimeMills - enterTimeMills)
                : context.getResources().getString(R.string.summary_last_doze_unknown);

        this.summary = context.getResources().getString(R.string.summary_last_doze, enterTimeStr, dozeTime);
        this.iconRes = R.drawable.ic_access_time_black_24dp;
        this.tileView = new QuickTileView(context, this) {
            @Override
            public void onClick(View v) {
                super.onClick(v);
                DozeEventHistoryViewerActivity.start(context);
            }
        };
    }
}
