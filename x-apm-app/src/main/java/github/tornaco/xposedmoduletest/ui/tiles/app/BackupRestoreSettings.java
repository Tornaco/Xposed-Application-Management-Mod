package github.tornaco.xposedmoduletest.ui.tiles.app;

import android.content.Context;
import android.view.View;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.activity.BackupRestoreSettingsActivity;

/**
 * Created by guohao4 on 2017/11/16.
 * Email: Tornaco@163.com
 */

public class BackupRestoreSettings extends QuickTile {

    public BackupRestoreSettings(final Context context) {
        super(context);

        this.titleRes = R.string.title_backup_restore;
        this.summaryRes = R.string.summary_backup_restore;
        this.iconRes = R.drawable.ic_settings_backup_restore_black_24dp;

        this.tileView = new QuickTileView(context, this) {

            @Override
            protected int getImageViewBackgroundRes() {
                return R.drawable.tile_bg_blue_grey;
            }

            @Override
            public void onClick(View v) {
                super.onClick(v);
                BackupRestoreSettingsActivity.start(context);
            }
        };
    }
}
