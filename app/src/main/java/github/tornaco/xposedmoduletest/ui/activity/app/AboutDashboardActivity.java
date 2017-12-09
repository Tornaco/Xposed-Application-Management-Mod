package github.tornaco.xposedmoduletest.ui.activity.app;

import android.os.Bundle;
import android.support.annotation.Nullable;

import java.util.List;

import dev.nick.tiles.tile.Category;
import dev.nick.tiles.tile.DashboardFragment;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.license.RemoteConfigs;
import github.tornaco.xposedmoduletest.ui.activity.WithWithCustomTabActivity;
import github.tornaco.xposedmoduletest.ui.tiles.app.AppDeveloper;
import github.tornaco.xposedmoduletest.ui.tiles.app.AppDonate;
import github.tornaco.xposedmoduletest.ui.tiles.app.AppVersion;
import github.tornaco.xposedmoduletest.ui.tiles.app.OpenSource;
import github.tornaco.xposedmoduletest.ui.tiles.app.PayListTile;

/**
 * Created by guohao4 on 2017/11/2.
 * Email: Tornaco@163.com
 */

public class AboutDashboardActivity extends WithWithCustomTabActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_with_appbar_template);
        setupToolbar();
        showHomeAsUp();
        replaceV4(R.id.container, new Dashboards(), null, false);
    }

    public static class Dashboards extends DashboardFragment {
        @Override
        protected void onCreateDashCategories(List<Category> categories) {
            super.onCreateDashCategories(categories);

            Category personal = new Category();
            personal.titleRes = R.string.title_about;

            personal.addTile(new AppDeveloper(getActivity()));

            Category hook = new Category();
            hook.addTile(new OpenSource(getActivity()));

            Category hook2 = new Category();
            hook2.addTile(new AppVersion(getActivity()));

            Category support = new Category();
            support.titleRes = R.string.title_support;
            if (!BuildConfig.DEBUG && RemoteConfigs.getSingleton().getConfig().isDonate()) {
                support.addTile(new AppDonate(getActivity()));
            }
            support.addTile(new PayListTile(getActivity()));

            categories.add(support);
            categories.add(personal);
            categories.add(hook2);
            categories.add(hook);
        }
    }


}
