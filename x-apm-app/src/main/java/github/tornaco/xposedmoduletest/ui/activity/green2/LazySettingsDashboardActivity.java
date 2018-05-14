package github.tornaco.xposedmoduletest.ui.activity.green2;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.util.List;

import dev.nick.tiles.tile.Category;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.AppCustomDashboardFragment;
import github.tornaco.xposedmoduletest.ui.activity.BaseActivity;
import github.tornaco.xposedmoduletest.ui.tiles.green.General;
import github.tornaco.xposedmoduletest.ui.tiles.green.LazySolutionApp;
import github.tornaco.xposedmoduletest.ui.tiles.green.LazySolutionFw;
import github.tornaco.xposedmoduletest.ui.tiles.green.LazyTips;

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
        protected boolean androidPStyleIcon() {
            return false;
        }

        @Override
        protected void onCreateDashCategories(List<Category> categories) {
            super.onCreateDashCategories(categories);
            Category personal = new Category();
            personal.titleRes = R.string.title_personal;
            personal.addTile(new General(getActivity()));

            Category solutions = new Category();
            solutions.moreDrawableRes = R.drawable.ic_help_black_24dp;
            solutions.onMoreButtonClickListener = v -> {
                Toast.makeText(getActivity(), R.string.summary_lazy_solution_suggestion, Toast.LENGTH_LONG).show();
            };
            solutions.titleRes = R.string.title_lazy_solutions;
            solutions.addTile(new LazySolutionApp(getActivity()));
            solutions.addTile(new LazySolutionFw(getActivity()));
            solutions.addTile(new LazyTips(getActivity()));
            // solutions.addTile(new LazySolutionSuggestion(getActivity()));

            categories.add(personal);
            categories.add(solutions);
        }
    }


}
