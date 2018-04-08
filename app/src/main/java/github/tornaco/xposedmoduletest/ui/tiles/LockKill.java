package github.tornaco.xposedmoduletest.ui.tiles;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.view.View;
import android.widget.ProgressBar;

import org.newstand.logger.Logger;

import dev.nick.tiles.tile.ActionTextTileView;
import dev.nick.tiles.tile.QuickTile;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.activity.lk.LockKillAppNavActivity;
import github.tornaco.xposedmoduletest.ui.widget.ToastManager;
import github.tornaco.xposedmoduletest.util.XExecutor;
import github.tornaco.xposedmoduletest.xposed.app.IProcessClearListenerAdapter;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class LockKill extends QuickTile {

    private ProgressBar mProgressBar;

    public LockKill(final Context context) {
        super(context);
        this.titleRes = R.string.title_app_mem_boost;
        if (XAshmanManager.get().isServiceAvailable()) {
            this.summaryRes = XAshmanManager.get().isLockKillEnabled() ?
                    R.string.summary_func_lk_enabled : R.string.summary_func_lk_disabled;
        }
        this.iconRes = R.drawable.ic_donut_small_black_24px;

        this.tileView = new ActionTextTileView(context) {

            @Override
            protected void onViewInflated(View view) {
                super.onViewInflated(view);
                mProgressBar = view.findViewById(R.id.progress_bar);
                mProgressBar.setProgress(getMemoryUsagePercent());
            }

            @Override
            public String getActionText(Context context) {
                return context.getString(R.string.clear_process_now);
            }

            @Override
            protected void onAction() {
                super.onAction();
                // Toast.makeText(context, "XXX", Toast.LENGTH_SHORT).show();
                final int[] clearNum = {0};
                XAshmanManager.get().clearProcess(new IProcessClearListenerAdapter() {
                    @Override
                    public boolean doNotClearWhenIntervative() throws RemoteException {
                        return false;
                    }

                    @Override
                    public void onPrepareClearing() throws RemoteException {
                        super.onPrepareClearing();
                        clearNum[0] = 0;
                    }

                    @Override
                    public void onClearedPkg(final String pkg) throws RemoteException {
                        super.onClearedPkg(pkg);
                        XExecutor.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastManager.show(context, context.getString(R.string.clearing_process_app, PkgUtil.loadNameByPkgName(context, pkg)));
                            }
                        });
                        clearNum[0]++;
                    }

                    @Override
                    public void onAllCleared(final String[] pkg) throws RemoteException {
                        super.onAllCleared(pkg);
                        XExecutor.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastManager.show(context, context.getString(R.string.clear_process_complete_with_num, String.valueOf(clearNum[0])));

                                // Update mem state.
                                mProgressBar.setProgress(getMemoryUsagePercent());
                            }
                        });
                    }
                });
            }

            @Override
            public void onClick(View v) {
                super.onClick(v);
                context.startActivity(new Intent(context, LockKillAppNavActivity.class));
            }

            @Override
            protected int getLayoutId() {
                return R.layout.dashboard_tile_with_progress;
            }
        };
    }

    private int getMemoryUsagePercent() {
        ActivityManager.MemoryInfo m = XAshmanManager.get().getMemoryInfo();
        if (m != null) {
            final String infoStr = (m.totalMem - m.availMem) + "/" + m.totalMem;
            Logger.e("getMemoryUsagePercent: " + infoStr);
            return (int) (100 * (((float) (m.totalMem - m.availMem) / (float) m.totalMem)));
        }
        return 24;
    }
}
