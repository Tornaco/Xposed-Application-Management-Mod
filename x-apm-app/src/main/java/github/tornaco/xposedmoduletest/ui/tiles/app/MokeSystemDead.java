package github.tornaco.xposedmoduletest.ui.tiles.app;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Toast;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class MokeSystemDead extends QuickTile {

    public MokeSystemDead(final Context context) {
        super(context);
        this.titleRes = R.string.title_system_dead_moke;
        this.summaryRes = R.string.summary_system_dead_moke;
        this.iconRes = R.drawable.ic_warning_black_24dp;
        this.tileView = new QuickTileView(context, this) {
            @Override
            public void onClick(View v) {
                super.onClick(v);
                if (XAPMManager.get().isServiceAvailable()) {
                    new AlertDialog.Builder(context)
                            .setTitle(R.string.title_system_dead_moke)
                            .setMessage(R.string.summary_system_dead_moke)
                            .setCancelable(true)
                            .setPositiveButton(context.getString(R.string.title_click_to_continue), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    XAPMManager.get().mockSystemDead(5 * 1000);
                                    Toast.makeText(context, R.string.summary_system_dead_moke, Toast.LENGTH_LONG).show();
                                }
                            })
                            .show();
                }
            }
        };
    }
}
