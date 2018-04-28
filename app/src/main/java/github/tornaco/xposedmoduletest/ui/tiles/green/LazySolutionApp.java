package github.tornaco.xposedmoduletest.ui.tiles.green;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.RelativeLayout;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.SwitchTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;

/**
 * Created by Tornaco on 2018/4/28 13:36.
 * God bless no bug!
 */
public class LazySolutionApp extends QuickTile {

    public LazySolutionApp(@NonNull Context context) {
        super(context, null);

        this.titleRes = R.string.title_lazy_solution_app;
        this.summaryRes = R.string.summary_lazy_solution_app;
        this.iconRes = R.drawable.ic_looks_one_black_24dp;

        this.tileView = new SwitchTileView(context) {
            @Override
            protected void onBindActionView(RelativeLayout container) {
                super.onBindActionView(container);
                setChecked(XAshmanManager.get().isAppServiceLazyControlSolutionEnable(XAshmanManager.AppServiceControlSolutions.FLAG_APP));
            }

            @Override
            protected void onCheckChanged(boolean checked) {
                super.onCheckChanged(checked);
                XAshmanManager.get().setAppServiceLazyControlSolution(XAshmanManager.AppServiceControlSolutions.FLAG_APP, checked);
            }
        };

    }
}
