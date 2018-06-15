package github.tornaco.xposedmoduletest.ui.tiles.workflow;

import android.content.Context;
import android.view.View;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.bean.RecentTile;
import github.tornaco.xposedmoduletest.provider.AppSettings;
import github.tornaco.xposedmoduletest.ui.activity.prop.PropNavActivity;
import github.tornaco.xposedmoduletest.ui.activity.workflow.WorkflowNavActivity;
import github.tornaco.xposedmoduletest.ui.tiles.TileManager;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class Workflow extends QuickTile {

    public Workflow(final Context context) {
        super(context);
        this.titleRes = R.string.title_workflow;
        this.iconRes = R.drawable.ic_work_black_24dp;
        this.tileView = new QuickTileView(context, this) {
            @Override
            protected int getImageViewBackgroundRes() {
                return R.drawable.tile_bg_amber;
            }

            @Override
            public void onClick(View v) {
                super.onClick(v);
                WorkflowNavActivity.start(context);
                // Save to recent.
                AppSettings.addRecentTile(context, RecentTile.from(TileManager.getTileKey(Workflow.class)));
            }
        };
    }
}
