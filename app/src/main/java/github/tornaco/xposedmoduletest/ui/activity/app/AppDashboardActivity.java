package github.tornaco.xposedmoduletest.ui.activity.app;

import android.os.Bundle;
import android.support.annotation.Nullable;

import java.util.List;

import dev.nick.tiles.tile.Category;
import dev.nick.tiles.tile.DashboardFragment;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.activity.WithWithCustomTabActivity;
import github.tornaco.xposedmoduletest.ui.tiles.app.AutoBlack;
import github.tornaco.xposedmoduletest.ui.tiles.app.RestoreDefault;
import github.tornaco.xposedmoduletest.ui.tiles.app.ThemeChooser;
import github.tornaco.xposedmoduletest.ui.tiles.app.WhiteSystemApp;

/**
 * Created by guohao4 on 2017/11/2.
 * Email: Tornaco@163.com
 */

public class AppDashboardActivity extends WithWithCustomTabActivity {
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

            Category systemProtect = new Category();
            systemProtect.titleRes = R.string.title_app_settings;
            systemProtect.addTile(new WhiteSystemApp(getActivity()));
            systemProtect.addTile(new AutoBlack(getActivity()));

            Category data = new Category();
            data.titleRes = R.string.title_data;
            data.addTile(new RestoreDefault(getActivity()));

            Category theme = new Category();
            theme.titleRes = R.string.title_style;
            theme.addTile(new ThemeChooser(getActivity()));

            categories.add(systemProtect);
            categories.add(data);
            categories.add(theme);
        }
    }


}
