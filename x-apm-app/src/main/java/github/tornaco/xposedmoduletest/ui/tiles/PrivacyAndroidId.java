package github.tornaco.xposedmoduletest.ui.tiles;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.activity.app.PrivacyNavActivity;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class PrivacyAndroidId extends PrivacyItemTile {

    public PrivacyAndroidId(final Context context) {
        super(context);
        this.titleRes = R.string.title_privacy_android_id;
        this.summary = context.getString(R.string.summary_tile_privacy_common,
                getAndroidId(), getUserSetAndroidId());

        this.iconRes = R.drawable.ic_android_black_24dp;
        this.tileView = new QuickTileView(context, this) {
            @Override
            public void onClick(View v) {
                super.onClick(v);

                showEditTextDialog((Activity) context, new EditTextAction() {
                    @Override
                    public void onAction(String text) {
                        XAPMManager.get().setUserDefinedAndroidId(text);

                        PrivacyNavActivity pa = (PrivacyNavActivity) context;
                        pa.reload();
                    }
                });


            }
        };
    }

    private String getAndroidId() {
        return XAPMManager.get().getAndroidId();
    }

    private String getUserSetAndroidId() {
        return XAPMManager.get().getUserDefinedAndroidId();
    }
}
