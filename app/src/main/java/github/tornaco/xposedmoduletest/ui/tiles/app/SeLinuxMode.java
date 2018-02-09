package github.tornaco.xposedmoduletest.ui.tiles.app;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.RelativeLayout;
import android.widget.Toast;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.SwitchTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.provider.AppSettings;
import github.tornaco.xposedmoduletest.util.SeLinuxModeUtil;
import github.tornaco.xposedmoduletest.util.XExecutor;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class SeLinuxMode extends QuickTile {

    public SeLinuxMode(final Context context) {
        super(context);

        this.titleRes = R.string.title_selinux_mode;
        final boolean mode = XAshmanManager.get().isServiceAvailable()
                && XAshmanManager.get().isSELinuxEnabled()
                && XAshmanManager.get().isSELinuxEnforced();
        this.summaryRes = mode ? R.string.summary_selinux_enforcing
                : R.string.summary_selinux_not_enforcing;
        this.iconRes = R.drawable.ic_security_black_24dp;
        this.tileView = new SwitchTileView(context) {
            @Override
            protected void onBindActionView(RelativeLayout container) {
                super.onBindActionView(container);
                setChecked(mode);
            }

            @Override
            protected void onCheckChanged(final boolean checked) {
                super.onCheckChanged(checked);

                new AlertDialog.Builder(context)
                        .setTitle(R.string.title_dangerous_selinux_mode_change)
                        .setMessage(R.string.message_dangerous_selinux_mode_change)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AppSettings.setSelinuxModeEnforceEnabled(context, checked);
                                if (!XAshmanManager.get().isServiceAvailable()) return;
                                boolean selinuxEnforcing = XAshmanManager.get().isSELinuxEnabled()
                                        && XAshmanManager.get().isSELinuxEnforced();
                                if (checked == selinuxEnforcing) return;
                                boolean isSelinuxEnabled = XAshmanManager.get().isSELinuxEnabled();
                                if (!isSelinuxEnabled) {
                                    Toast.makeText(context, R.string.message_selinux_not_enabled, Toast.LENGTH_SHORT).show();
                                } else {
                                    SeLinuxModeUtil.applyMode(checked);
                                    XExecutor.runOnUIThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                getSummaryTextView().setText(checked ? R.string.summary_selinux_enforcing
                                                        : R.string.summary_selinux_not_enforcing);
                                            } catch (Throwable ignored) {
                                            }
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
