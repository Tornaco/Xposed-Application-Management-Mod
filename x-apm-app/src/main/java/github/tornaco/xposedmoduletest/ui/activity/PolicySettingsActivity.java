package github.tornaco.xposedmoduletest.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import java.util.List;

import dev.nick.tiles.tile.Category;
import github.tornaco.permission.requester.RuntimePermissions;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.AppCustomDashboardFragment;
import github.tornaco.xposedmoduletest.ui.tiles.app.AutoApplyAppSettingsTemplate;
import github.tornaco.xposedmoduletest.ui.tiles.app.AutoBlackNotification;
import github.tornaco.xposedmoduletest.ui.tiles.app.PowerSave;
import github.tornaco.xposedmoduletest.ui.tiles.app.RemoveTaskOnAppIdle;
import github.tornaco.xposedmoduletest.ui.tiles.app.WhiteSystemApp;

/**
 * Created by guohao4 on 2017/9/7.
 * Email: Tornaco@163.com
 */
@RuntimePermissions
public class PolicySettingsActivity extends BaseActivity {

    public static void start(Context context) {
        Intent starter = new Intent(context, PolicySettingsActivity.class);
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
        return new SettingsNavFragment();
    }

    public static class SettingsNavFragment
            extends AppCustomDashboardFragment {

        @Override
        protected boolean androidPStyleIcon() {
            return false;
        }

        @Override
        protected void onCreateDashCategories(List<Category> categories) {
            super.onCreateDashCategories(categories);

            Category system = new Category();
            system.titleRes = R.string.title_opt;
            system.addTile(new PowerSave(getActivity()));

            Category policy = new Category();
            policy.titleRes = R.string.title_policy;
            policy.addTile(new WhiteSystemApp(getActivity()));
            policy.addTile(new AutoApplyAppSettingsTemplate(getActivity()));
            policy.addTile(new AutoBlackNotification(getActivity()));

            Category others = new Category();
            others.titleRes = R.string.title_others;
            others.addTile(new RemoveTaskOnAppIdle(getActivity()));

            categories.add(system);
            categories.add(policy);
            categories.add(others);
        }
    }
}
