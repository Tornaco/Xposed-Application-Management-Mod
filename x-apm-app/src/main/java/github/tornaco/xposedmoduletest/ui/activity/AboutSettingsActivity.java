package github.tornaco.xposedmoduletest.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import java.util.List;
import java.util.Objects;

import dev.nick.tiles.tile.Category;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.provider.AppSettings;
import github.tornaco.xposedmoduletest.ui.ActivityLifeCycleDashboardFragment;
import github.tornaco.xposedmoduletest.ui.tiles.app.AppDeveloper;
import github.tornaco.xposedmoduletest.ui.tiles.app.AppDonate;
import github.tornaco.xposedmoduletest.ui.tiles.app.AppGetPlay;
import github.tornaco.xposedmoduletest.ui.tiles.app.AppLogoMaker;
import github.tornaco.xposedmoduletest.ui.tiles.app.AppUpdateLog;
import github.tornaco.xposedmoduletest.ui.tiles.app.AppVersion;
import github.tornaco.xposedmoduletest.ui.tiles.app.GitContributors;
import github.tornaco.xposedmoduletest.ui.tiles.app.OpenMarket;
import github.tornaco.xposedmoduletest.ui.tiles.app.OpenSource;
import github.tornaco.xposedmoduletest.ui.tiles.app.OpenSourceLicenses;
import github.tornaco.xposedmoduletest.ui.tiles.app.PrivacyPolicy;
import github.tornaco.xposedmoduletest.xposed.XAppBuildVar;

/**
 * Created by guohao4 on 2017/9/7.
 * Email: Tornaco@163.com
 */
public class AboutSettingsActivity extends WithWithCustomTabActivity {

    public static void start(Context context) {
        Intent starter = new Intent(context, AboutSettingsActivity.class);
        starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_with_appbar_template);
        setupToolbar();
        showHomeAsUp();
        replaceV4(R.id.container, onCreateSettingsFragment(), null, false);
    }

    protected Fragment onCreateSettingsFragment() {
        return new AboutNavFragment();
    }

    public static class AboutNavFragment extends ActivityLifeCycleDashboardFragment {
        @Override
        protected boolean androidPStyleIcon() {
            return false;
        }

        @Override
        public int getPageTitle() {
            return R.string.title_about;
        }

        @Override
        protected int getLayoutId() {
            return R.layout.dashboard_with_margin;
        }

        @Override
        protected int getNumColumns() {
            boolean two = AppSettings.show2ColumnsIn(Objects.requireNonNull(getActivity()),
                    AboutNavFragment.class.getSimpleName());
            return two ? 2 : 1;
        }

        @Override
        protected void onCreateDashCategories(List<Category> categories) {
            super.onCreateDashCategories(categories);

            Category personal = new Category();
            personal.titleRes = R.string.title_about;
            personal.addTile(new AppDeveloper(getActivity()));
            personal.addTile(new AppVersion(getActivity()));
            personal.addTile(new AppUpdateLog(getActivity()));
            personal.addTile(new OpenMarket(getActivity()));

            Category open = new Category();
            open.titleRes = R.string.title_open_info;
            open.addTile(new PrivacyPolicy(getActivity()));
            open.addTile(new OpenSource(getActivity()));
            open.addTile(new OpenSourceLicenses(getActivity()));
            open.addTile(new GitContributors(getActivity()));
            open.addTile(new AppLogoMaker(getActivity()));

            boolean isPlayVersion = XAppBuildVar.BUILD_VARS.contains(XAppBuildVar.PLAY);

            categories.add(personal);
            categories.add(open);

            if (!isPlayVersion) {
                Category help = new Category();
                help.titleRes = R.string.title_help_dev;
                help.addTile(new AppDonate(getActivity()));
                categories.add(help);
            }
        }
    }
}
