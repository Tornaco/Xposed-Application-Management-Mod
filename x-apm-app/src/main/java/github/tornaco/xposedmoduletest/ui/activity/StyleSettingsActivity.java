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
import github.tornaco.xposedmoduletest.ui.tiles.app.GcmIndicator;
import github.tornaco.xposedmoduletest.ui.tiles.app.IconPack;
import github.tornaco.xposedmoduletest.ui.tiles.app.NoShift;
import github.tornaco.xposedmoduletest.ui.tiles.app.PStyleIcon;
import github.tornaco.xposedmoduletest.ui.tiles.app.ShowTileDivider;
import github.tornaco.xposedmoduletest.ui.tiles.app.ThemeChooser;

/**
 * Created by guohao4 on 2017/9/7.
 * Email: Tornaco@163.com
 */
@RuntimePermissions
public class StyleSettingsActivity extends BaseActivity {

    public static void start(Context context) {
        Intent starter = new Intent(context, StyleSettingsActivity.class);
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

            Category theme = new Category();
            theme.titleRes = R.string.title_style;
            theme.addTile(new ThemeChooser(getActivity()));
            theme.addTile(new ShowTileDivider(getActivity()));
            theme.addTile(new IconPack(getActivity()));
            theme.addTile(new NoShift(getActivity()));
            theme.addTile(new PStyleIcon(getActivity()));

            categories.add(theme);
        }
    }
}
