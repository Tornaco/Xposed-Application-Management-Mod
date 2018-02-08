package github.tornaco.xposedmoduletest.ui.activity.app;

import android.os.Bundle;
import android.support.annotation.Nullable;

import java.util.List;

import dev.nick.tiles.tile.Category;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.ActivityLifeCycleDashboardFragment;
import github.tornaco.xposedmoduletest.ui.activity.WithWithCustomTabActivity;
import github.tornaco.xposedmoduletest.ui.tiles.app.AppDevMode;
import github.tornaco.xposedmoduletest.ui.tiles.app.AutoSeLinuxMode;
import github.tornaco.xposedmoduletest.ui.tiles.app.CleanUpSystemErrorTrace;
import github.tornaco.xposedmoduletest.ui.tiles.app.CrashDump;
import github.tornaco.xposedmoduletest.ui.tiles.app.MokeCrash;
import github.tornaco.xposedmoduletest.ui.tiles.app.NullHack;
import github.tornaco.xposedmoduletest.ui.tiles.app.SeLinuxMode;
import github.tornaco.xposedmoduletest.ui.tiles.app.ShowFocusedActivity;

/**
 * Created by guohao4 on 2017/11/2.
 * Email: Tornaco@163.com
 */

public class ToolsDashboardActivity extends WithWithCustomTabActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_with_appbar_template);
        setupToolbar();
        showHomeAsUp();
        replaceV4(R.id.container, new Dashboards(), null, false);
    }

    public static class Dashboards extends ActivityLifeCycleDashboardFragment {
        @Override
        protected void onCreateDashCategories(List<Category> categories) {
            super.onCreateDashCategories(categories);

            Category settings = new Category();
            settings.titleRes = R.string.title_dev_tools;
            settings.addTile(new AppDevMode(getActivity()));
            settings.addTile(new ShowFocusedActivity(getActivity()));
            settings.addTile(new CrashDump(getActivity()));
            settings.addTile(new MokeCrash(getActivity()));
            settings.addTile(new CleanUpSystemErrorTrace(getActivity()));

            Category selinux = new Category();
            selinux.titleRes = R.string.title_selinux;
            selinux.addTile(new SeLinuxMode(getActivity()));
            selinux.addTile(new AutoSeLinuxMode(getActivity()));

            categories.add(settings);
            categories.add(selinux);
        }
    }


}
