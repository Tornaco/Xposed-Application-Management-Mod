package github.tornaco.xposedmoduletest.ui.tiles.doze;

import android.content.Context;
import android.view.View;

import org.newstand.logger.Logger;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.activity.doze.DozeWhiteListViewerActivity;
import github.tornaco.xposedmoduletest.util.ArrayUtil;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class DozeWhitelistTile extends QuickTile {

    public DozeWhitelistTile(final Context context) {
        super(context);
        this.titleRes = R.string.title_doze_white_list;

        int whitelistSize = 0;
        try {
            whitelistSize =
                    ArrayUtil.combine(XAPMManager.get().getUserPowerWhitelist(),
                            XAPMManager.get().getSystemPowerWhitelist()).length;
        } catch (Throwable e) {
            Logger.e("combine system and usr: " + e);
        }

        this.summary = context.getString(R.string.summary_doze_white_list, String.valueOf(whitelistSize));

        this.iconRes = R.drawable.ic_format_align_justify_black_24dp;

        this.tileView = new QuickTileView(context, this) {
            @Override
            public void onClick(View v) {
                super.onClick(v);
                DozeWhiteListViewerActivity.start(context);
            }
        };
    }
}
