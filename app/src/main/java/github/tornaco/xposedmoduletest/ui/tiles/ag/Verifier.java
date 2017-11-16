package github.tornaco.xposedmoduletest.ui.tiles.ag;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.activity.ag.VerifierGuardSettings;

/**
 * Created by guohao4 on 2017/11/2.
 * Email: Tornaco@163.com
 */

public class Verifier extends QuickTile {

    public Verifier(@NonNull final Context context) {
        super(context);
        this.titleRes = R.string.title_verifier;
        this.iconRes = R.drawable.ic_phone_android_black_24dp;
        this.tileView = new QuickTileView(context, this) {
            @Override
            public void onClick(View v) {
                super.onClick(v);
                context.startActivity(new Intent(context, VerifierGuardSettings.class));
            }
        };
    }
}
