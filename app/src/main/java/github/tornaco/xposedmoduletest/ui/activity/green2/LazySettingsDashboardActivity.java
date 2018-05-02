package github.tornaco.xposedmoduletest.ui.activity.green2;

import android.os.Bundle;
import android.support.annotation.Nullable;

import java.util.List;

import dev.nick.tiles.tile.Category;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.AppCustomDashboardFragment;
import github.tornaco.xposedmoduletest.ui.activity.BaseActivity;
import github.tornaco.xposedmoduletest.ui.tiles.green.General;
import github.tornaco.xposedmoduletest.ui.tiles.green.LazySolutionApp;
import github.tornaco.xposedmoduletest.ui.tiles.green.LazySolutionFw;

/**
 * Created by guohao4 on 2017/11/2.
 * Email: Tornaco@163.com
 */

public class LazySettingsDashboardActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_with_appbar_template);
        setupToolbar();
        showHomeAsUp();
        replaceV4(R.id.container, new Dashboards(), null, false);
    }

    public static class Dashboards extends AppCustomDashboardFragment {
        @Override
        protected void onCreateDashCategories(List<Category> categories) {
            super.onCreateDashCategories(categories);
            Category personal = new Category();
            personal.titleRes = R.string.title_personal;
            personal.addTile(new General(getActivity()));

            Category solutions = new Category();
            solutions.titleRes = R.string.title_lazy_solutions;
            solutions.addTile(new LazySolutionApp(getActivity()));
            solutions.addTile(new LazySolutionFw(getActivity()));

            categories.add(personal);
            categories.add(solutions);
        }
    }


}
