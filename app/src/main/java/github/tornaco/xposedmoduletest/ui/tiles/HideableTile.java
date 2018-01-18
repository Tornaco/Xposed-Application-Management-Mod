package github.tornaco.xposedmoduletest.ui.tiles;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.TileListener;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.provider.AppSettings;

/**
 * Created by guohao4 on 2018/1/17.
 * Email: Tornaco@163.com
 */

public class HideableTile extends QuickTile {

    @Override
    protected boolean isEnabled() {
        return !isHidden();
    }

    public boolean isHidden() {
        return AppSettings.isHideTileInDashboard(getContext(), getClass().getName());
    }

    public HideableTile(final Context context) {

        super(context, new TileListener() {
            @Override
            public void onTileClick(@NonNull QuickTile tile) {

            }

            @Override
            public void onTileLongClick(@NonNull final QuickTile tile) {
                new AlertDialog.Builder(context)
                        .setTitle(R.string.title_hide_tile)
                        .setMessage(R.string.message_hide_tile)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AppSettings.hideDashboardTile(context, tile.getClass().getName(), true);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            }
        });
    }
}
