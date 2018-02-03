package github.tornaco.xposedmoduletest.ui;

import dev.nick.tiles.tile.DashboardFragment;
import github.tornaco.xposedmoduletest.provider.AppSettings;

/**
 * Created by guohao4 on 2018/1/24.
 * Email: Tornaco@163.com
 */

public class ActivityLifeCycleDashboardFragment extends DashboardFragment {

    public void onActivityResume() {
    }

    @Override
    protected boolean showDivider() {
        if (getActivity() == null) return false;
        return AppSettings.isShowTileDivider(getActivity());
    }
}
