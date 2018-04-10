package github.tornaco.xposedmoduletest.ui.activity.smartsense;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;

import java.util.List;

import dev.nick.tiles.tile.Category;
import dev.nick.tiles.tile.DashboardFragment;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.AppCustomDashboardFragment;
import github.tornaco.xposedmoduletest.ui.activity.BaseActivity;
import github.tornaco.xposedmoduletest.ui.tiles.app.DetailedToast;
import github.tornaco.xposedmoduletest.ui.tiles.app.ForegroundNotificationOptActivity;
import github.tornaco.xposedmoduletest.ui.tiles.app.IconToast;
import github.tornaco.xposedmoduletest.ui.tiles.smartsense.LongPressBackKey;
import github.tornaco.xposedmoduletest.ui.tiles.smartsense.PanicLock;

/**
 * Created by guohao4 on 2017/11/2.
 * Email: Tornaco@163.com
 */

public class SmartSenseDashboardActivity extends BaseActivity {

    public static void start(Context context) {
        Intent starter = new Intent(context, SmartSenseDashboardActivity.class);
        context.startActivity(starter);
    }

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

            Category keys = new Category();
            keys.titleRes = R.string.title_keys;
            keys.addTile(new LongPressBackKey(getActivity()));

            Category panic = new Category();
            panic.titleRes = R.string.title_panic;
            panic.addTile(new PanicLock(getActivity()));

            Category app = new Category();
            app.titleRes = R.string.title_app;
            // app.addTile(new AppFocusAction(getActivity()));
            app.addTile(new DetailedToast(getActivity()));
            app.addTile(new IconToast(getActivity()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                app.addTile(new ForegroundNotificationOptActivity(getActivity()));
            }

            categories.add(keys);
            categories.add(panic);
            categories.add(app);
        }
    }

}
