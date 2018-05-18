package github.tornaco.xposedmoduletest.ui.tiles;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.cache.InstalledAppsLoadingCache;
import github.tornaco.xposedmoduletest.ui.activity.comp.PackageViewerActivity;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class ComponentManager extends QuickTile {

    public ComponentManager(final Context context) {
        super(context);
        this.titleRes = R.string.title_component_manager;
        InstalledAppsLoadingCache.InstalledAppsData data = InstalledAppsLoadingCache.getInstance().getIfPresent();
        if (data != null) {
            this.summary = context.getString(R.string.summary_component_manager, String.valueOf(data.getAppCount()));
        }
        this.iconRes = R.drawable.ic_dashboard_black_24dp;
        this.tileView = new QuickTileView(context, this) {
            @Override
            protected int getImageViewBackgroundRes() {
                return R.drawable.tile_bg_blue;
            }

            @Override
            public void onClick(View v) {
                super.onClick(v);
                context.startActivity(new Intent(context, PackageViewerActivity.class));
            }
        };
    }
}
