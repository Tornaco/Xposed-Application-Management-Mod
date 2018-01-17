package github.tornaco.xposedmoduletest.ui.tiles.app;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Toast;

import com.jaredrummler.android.shell.Shell;

import org.newstand.logger.Logger;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.util.XExecutor;
import github.tornaco.xposedmoduletest.xposed.app.XAppGuardManager;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;

/**
 * Created by guohao4 on 2017/11/16.
 * Email: Tornaco@163.com
 */

public class RestoreDefault extends QuickTile {
    public RestoreDefault(final Context context) {
        super(context);

        this.titleRes = R.string.title_restore_def;
        this.summaryRes = R.string.summary_restore_def;
        this.iconRes = R.drawable.ic_settings_backup_restore_black_24dp;
        this.tileView = new QuickTileView(context, this) {
            @Override
            public void onClick(View v) {
                super.onClick(v);

                new AlertDialog.Builder(context)
                        .setTitle(R.string.title_restore_def)
                        .setMessage(R.string.summary_restore_def)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (XAshmanManager.get().isServiceAvailable()) {
                                    XAshmanManager.get().restoreDefaultSettings();
                                    XAppGuardManager.get().restoreDefaultSettings();
                                    Toast.makeText(context, R.string.summary_restore_done, Toast.LENGTH_SHORT).show();
                                } else {
                                    XExecutor.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            Logger.e("Mount: " + Shell.SU.run("mount -o remount,rw /data").isSuccessful());
                                            final boolean res = Shell.SU.run("rm -rf data/system/tor")
                                                    .isSuccessful();
                                            Shell.SU.run("rm -rf data/system/tor_apm");
                                            XExecutor.runOnUIThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (res) {
                                                        Toast.makeText(context, R.string.summary_restore_done, Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Toast.makeText(context, R.string.summary_restore_fail, Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }
                                    });
                                }

                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();


            }
        };
    }
}
