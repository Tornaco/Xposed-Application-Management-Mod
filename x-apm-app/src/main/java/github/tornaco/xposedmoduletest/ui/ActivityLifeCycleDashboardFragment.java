package github.tornaco.xposedmoduletest.ui;

import android.support.annotation.StringRes;

import dev.nick.eventbus.Event;
import github.tornaco.xposedmoduletest.provider.AppSettings;
import github.tornaco.xposedmoduletest.ui.activity.BaseActivity;

/**
 * Created by guohao4 on 2018/1/24.
 * Email: Tornaco@163.com
 */

public abstract class ActivityLifeCycleDashboardFragment
        extends AppCustomDashboardFragment {

    public void onActivityResume() {
    }

    @Override
    protected boolean showDivider() {
        return getActivity() != null && AppSettings.isShowTileDivider(getActivity());
    }

    @StringRes
    public int getPageTitle() {
        return 0;
    }

    public void onSetActivityTitle(BaseActivity activity) {
        if (activity != null && !activity.isDestroyed()) {
            activity.setTitle(getPageTitle());
            activity.setSubTitleChecked(null);
        }
    }

    public void onEvent(Event event) {
    }
}
