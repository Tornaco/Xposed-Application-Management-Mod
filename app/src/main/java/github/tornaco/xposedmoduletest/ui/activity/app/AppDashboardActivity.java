package github.tornaco.xposedmoduletest.ui.activity.app;

import android.os.Bundle;
import android.support.annotation.Nullable;

import java.util.List;

import dev.nick.tiles.tile.Category;
import dev.nick.tiles.tile.DashboardFragment;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.activity.BaseActivity;
import github.tornaco.xposedmoduletest.ui.tiles.ag.AppDeveloper;
import github.tornaco.xposedmoduletest.ui.tiles.ag.AppDonate;
import github.tornaco.xposedmoduletest.ui.tiles.app.AppVersion;

/**
 * Created by guohao4 on 2017/11/2.
 * Email: Tornaco@163.com
 */

public class AppDashboardActivity extends BaseActivity {
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
            personal.addTile(new AppVersion(getActivity()));

            Category support = new Category();
            support.titleRes = R.string.title_support;
            support.addTile(new AppDonate(getActivity()));

            categories.add(support);
            categories.add(personal);
        }
    }


}
