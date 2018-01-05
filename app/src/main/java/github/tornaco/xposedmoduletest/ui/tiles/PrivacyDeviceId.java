package github.tornaco.xposedmoduletest.ui.tiles;

import android.content.Context;
import android.view.View;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class PrivacyDeviceId extends QuickTile {

    public PrivacyDeviceId(final Context context) {
        super(context);
        this.titleRes = R.string.title_privacy_device_id;
        this.summary = context.getString(R.string.summary_tile_privacy_common,
                getDeviceId(), getUserSetDeviceId());

        this.iconRes = R.drawable.ic_phone_android_black_24dp;
        this.tileView = new QuickTileView(context, this) {
            @Override
            public void onClick(View v) {
                super.onClick(v);
            }
        };
    }

    private String getDeviceId() {
        return XAshmanManager.get().getDeviceId();
    }

    private String getUserSetDeviceId() {
        return XAshmanManager.get().getUserDefinedDeviceId();
    }
}
