package github.tornaco.xposedmoduletest.ui.tiles;

import android.content.Context;
import android.view.View;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class AndroidId extends QuickTile {

    public AndroidId(final Context context) {
        super(context);
        this.titleRes = R.string.title_privacy_android_id;
        this.iconRes = R.drawable.ic_phone_android_black_24dp;
        this.tileView = new QuickTileView(context, this) {
            @Override
            public void onClick(View v) {
                super.onClick(v);
                if (XAPMManager.get().isServiceAvailable())
                    XAPMManager.get().setUserDefinedAndroidId(null);
            }
        };
    }
}
