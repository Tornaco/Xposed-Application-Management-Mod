package github.tornaco.xposedmoduletest.ui.tiles;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.cache.RunningServicesLoadingCache;
import github.tornaco.xposedmoduletest.ui.activity.helper.RunningServicesActivity;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class RunningServices extends QuickTile {

    public RunningServices(final Context context) {
        super(context);
        this.titleRes = R.string.title_running_services;
        RunningServicesLoadingCache.RunningServicesData data =
                RunningServicesLoadingCache.getInstance().getIfPresent();
        if (data != null) {
            this.summary = context.getString(R.string.summary_running_services,
                    String.valueOf(data.getAppCount()), String.valueOf(data.getServiceCount()));
        }
        this.iconRes = R.drawable.ic_list_settings_fill;
        this.tileView = new QuickTileView(context, this) {
            @Override
            protected int getImageViewBackgroundRes() {
                return R.drawable.tile_bg_amber;
            }

            @Override
            public void onClick(View v) {
                super.onClick(v);
                context.startActivity(new Intent(context, RunningServicesActivity.class));
                //startRunningServicesActivity(context);

            }
        };
    }

    private void startRunningServicesActivity(Context context) {
        Intent intent = new Intent();
        intent.setClassName("com.android.settings",
                "com.android.settings.Settings$DevRunningServicesActivity");
        context.startActivity(intent);
    }
}
