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

public class UnInstallAPM extends QuickTile {

    public UnInstallAPM(final Context context) {
        super(context);

        this.titleRes = R.string.title_uninstall_apm;
        this.iconRes = R.drawable.ic_delete_black_24dp;
        this.tileView = new QuickTileView(context, this) {
            @Override
            public void onClick(View v) {
                super.onClick(v);
                BackupRestoreSettingsActivity backupRestoreSettingsActivity = (BackupRestoreSettingsActivity) context;
                backupRestoreSettingsActivity.onRequestUninstalledAPM();
            }
        };
    }
}
