package github.tornaco.xposedmoduletest.ui.activity.app;

import android.os.Bundle;
import android.support.annotation.Nullable;

import java.util.List;

import dev.nick.tiles.tile.Category;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.AppCustomDashboardFragment;
import github.tornaco.xposedmoduletest.ui.activity.WithWithCustomTabActivity;
import github.tornaco.xposedmoduletest.ui.tiles.app.AppDeveloper;
import github.tornaco.xposedmoduletest.ui.tiles.app.AppVersion;
import github.tornaco.xposedmoduletest.ui.tiles.app.OpenMarket;
import github.tornaco.xposedmoduletest.ui.tiles.app.OpenSource;
import github.tornaco.xposedmoduletest.ui.tiles.app.PrivacyPolicy;

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
        replaceV4(R.id.container, new AboutNavFragment(), null, false);
    }

    public static class AboutNavFragment extends AppCustomDashboardFragment {

        @Override
        protected boolean androidPStyleIcon() {
            return false;
        }

        @Override
        protected void onCreateDashCategories(List<Category> categories) {
            super.onCreateDashCategories(categories);

            Category personal = new Category();
            personal.titleRes = R.string.title_about;

            personal.addTile(new AppDeveloper(getActivity()));

            Category hook = new Category();
            hook.addTile(new OpenMarket(getActivity()));
            hook.addTile(new PrivacyPolicy(getActivity()));
            hook.addTile(new OpenSource(getActivity()));

            Category hook2 = new Category();
            hook2.addTile(new AppVersion(getActivity()));

            categories.add(personal);
            categories.add(hook2);
            categories.add(hook);
        }
    }


}
