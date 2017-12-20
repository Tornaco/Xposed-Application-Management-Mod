package github.tornaco.xposedmoduletest.ui.tiles;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.activity.helper.RunningServicesActivity;
import github.tornaco.xposedmoduletest.util.RunningServiceLauncher;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class RunningServices extends QuickTile {

    public RunningServices(final Context context) {
        super(context);
        this.titleRes = R.string.title_running_services;
        this.iconRes = R.drawable.ic_room_service_black_24dp;
        this.tileView = new QuickTileView(context, this) {
            @Override
            public void onClick(View v) {
                super.onClick(v);

                context.startActivity(new Intent(context, RunningServicesActivity.class));

//
//                if (!RunningServiceLauncher.launch((Activity) context)) {
//                    Toast.makeText(context, R.string.message_running_services_fail, Toast.LENGTH_SHORT).show();
//                }
            }
        };
    }
}
