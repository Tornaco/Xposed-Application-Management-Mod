package github.tornaco.xposedmoduletest.ui.activity.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import java.util.List;
import java.util.Objects;

import dev.nick.eventbus.Event;
import dev.nick.tiles.tile.Category;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.provider.AppSettings;
import github.tornaco.xposedmoduletest.provider.XSettings;
import github.tornaco.xposedmoduletest.ui.ActivityLifeCycleDashboardFragment;
import github.tornaco.xposedmoduletest.ui.activity.BaseActivity;
import github.tornaco.xposedmoduletest.ui.activity.WithWithCustomTabActivity;
import github.tornaco.xposedmoduletest.ui.tiles.app.ADBWireless;
import github.tornaco.xposedmoduletest.ui.tiles.app.AppDevMode;
import github.tornaco.xposedmoduletest.ui.tiles.app.CleanUpSystemErrorTrace;
import github.tornaco.xposedmoduletest.ui.tiles.app.CrashDump;
import github.tornaco.xposedmoduletest.ui.tiles.app.MokeCrash;
import github.tornaco.xposedmoduletest.ui.tiles.app.MokeSystemDead;
import github.tornaco.xposedmoduletest.ui.tiles.app.RedemptionEnable;
import github.tornaco.xposedmoduletest.ui.tiles.app.ShowFocusedActivity;
import github.tornaco.xposedmoduletest.xposed.XAPMApplication;

/**
 * Created by guohao4 on 2017/11/2.
 * Email: Tornaco@163.com
 */

public class ToolsDashboardActivity extends WithWithCustomTabActivity {

    public static void start(Context context) {
        Intent starter = new Intent(context, ToolsDashboardActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_with_appbar_template);
        setupToolbar();
        showHomeAsUp();
        replaceV4(R.id.container, new ToolsNavFragment(), null, false);
    }

    public static class ToolsNavFragment extends ActivityLifeCycleDashboardFragment {

        @Override
        protected boolean androidPStyleIcon() {
            return false;
        }

        @Override
        public int getPageTitle() {
            return R.string.title_tools;
        }

        @Override
        protected int getLayoutId() {
            return R.layout.dashboard_with_margin;
        }

        @Override
        protected int getNumColumns() {
            boolean two = AppSettings.show2ColumnsIn(Objects.requireNonNull(getActivity()),
                    ToolsNavFragment.class.getSimpleName());
            return two ? 2 : 1;
        }

        @Override
        public void onEvent(Event event) {
            super.onEvent(event);
            if (event.getEventType() == XAPMApplication.EVENT_APP_DEBUG_MODE_CHANGED) {

                BaseActivity activity = (BaseActivity) getActivity();
                boolean visible = activity != null && activity.isVisible();

                if (visible) {
                    buildUIDelay(getActivity(), 1000);
                }
            }
        }

        @Override
        protected void onCreateDashCategories(List<Category> categories) {
            super.onCreateDashCategories(categories);

            Category dev = new Category();
            dev.titleRes = R.string.title_dev_tools;
            dev.addTile(new AppDevMode(getActivity()));
            if (XSettings.isDevMode(getActivity())) {
                dev.addTile(new ADBWireless(getActivity()));
            }
            dev.addTile(new CrashDump(getActivity()));
            dev.addTile(new MokeCrash(getActivity()));
            dev.addTile(new MokeSystemDead(getActivity()));

            Category user = new Category();
            user.titleRes = R.string.title_user_tools;
            user.addTile(new ShowFocusedActivity(getActivity()));
            user.addTile(new CleanUpSystemErrorTrace(getActivity()));
            user.addTile(new RedemptionEnable(getActivity()));

            categories.add(dev);
            categories.add(user);
        }
    }


}
