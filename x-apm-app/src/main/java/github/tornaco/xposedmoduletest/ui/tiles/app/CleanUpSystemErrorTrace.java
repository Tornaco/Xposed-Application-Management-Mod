package github.tornaco.xposedmoduletest.ui.tiles.app;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class CleanUpSystemErrorTrace extends QuickTile {

    public CleanUpSystemErrorTrace(final Context context) {
        super(context);
        this.titleRes = R.string.title_cleanup_system_error_trace;
        this.summaryRes = R.string.summary_cleanup_system_error_trace;
        this.iconRes = R.drawable.ic_clear_all_black_24dp;
        this.tileView = new QuickTileView(context, this) {
            @Override
            public void onClick(View v) {
                super.onClick(v);
                if (XAPMManager.get().isServiceAvailable()) {
                    XAPMManager.get().cleanUpSystemErrorTraces();
                    Toast.makeText(context, R.string.res_cleanup_system_error_trace_success, Toast.LENGTH_SHORT).show();
                }
            }
        };
    }
}
