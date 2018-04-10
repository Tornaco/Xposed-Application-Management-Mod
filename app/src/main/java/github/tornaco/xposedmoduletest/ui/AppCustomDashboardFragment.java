package github.tornaco.xposedmoduletest.ui;

import dev.nick.tiles.tile.DashboardFragment;
import github.tornaco.xposedmoduletest.provider.AppSettings;

/**
 * Created by Tornaco on 2018/4/10 10:03.
 * God bless no bug!
 */
public class AppCustomDashboardFragment extends DashboardFragment {

    @Override
    protected boolean androidPStyleIcon() {
        return AppSettings.isPStyleIcon(this.getActivity());
    }
}
