package github.tornaco.xposedmoduletest.ui.activity.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.util.List;

import dev.nick.tiles.tile.Category;
import github.tornaco.android.common.util.ColorUtil;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.loader.PaletteColorPicker;
import github.tornaco.xposedmoduletest.provider.XSettings;
import github.tornaco.xposedmoduletest.ui.AppCustomDashboardFragment;
import github.tornaco.xposedmoduletest.ui.activity.WithWithCustomTabActivity;
import github.tornaco.xposedmoduletest.ui.tiles.config.DensityOverlay;
import github.tornaco.xposedmoduletest.ui.tiles.config.ExcludeFromRecent;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;

/**
 * Created by guohao4 on 2017/11/2.
 * Email: Tornaco@163.com
 */

public class AppConfigManifestDashboardActivity extends WithWithCustomTabActivity {

    public static void start(Context context, String pkg) {
        Intent starter = new Intent(context, AppConfigManifestDashboardActivity.class);
        starter.putExtra("pkg_name", pkg);
        starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.container_with_appbar_fab_template);
        setupToolbar();
        showHomeAsUp();

        String pkgName = getIntent().getStringExtra("pkg_name");
        if (pkgName == null) return;

        setTitle(PkgUtil.loadNameByPkgName(getContext(), pkgName));
        setSubTitleChecked(pkgName);

        replaceV4(R.id.container, Dashboards.newInstance(getContext(), pkgName), null, false);

        // Apply theme color.
        int color = ContextCompat.getColor(this, XSettings.getThemes(this).getThemeColor());

        // Apply palette color.
        // Workaround.
        if (!mUserTheme.isReverseTheme()) {
            PaletteColorPicker.pickPrimaryColor(this, new PaletteColorPicker.PickReceiver() {
                @Override
                public void onColorReady(int color) {
                    applyColor(color);
                }
            }, pkgName, color);
        }

        findViewById(R.id.fab).setVisibility(View.GONE);
    }

    @SuppressWarnings("ConstantConditions")
    private void applyColor(int color) {
        int dark = ColorUtil.colorBurn(color);
        getWindow().setStatusBarColor(dark);
        getWindow().setNavigationBarColor(dark);
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setBackgroundColor(color);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class Dashboards extends AppCustomDashboardFragment {

        @Override
        protected boolean androidPStyleIcon() {
            return false;
        }

        private String mPkg;

        public static Dashboards newInstance(Context context, String pkg) {
            Dashboards d = new Dashboards();
            Bundle bd = new Bundle(1);
            bd.putString("pkg_name", pkg);
            d.setArguments(bd);
            return d;
        }

        @Override
        protected int getLayoutId() {
            return R.layout.dashboard_fab_workaround;
        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            mPkg = getArguments().getString("pkg_name", null);
        }

        @Override
        protected void onCreateDashCategories(List<Category> categories) {
            super.onCreateDashCategories(categories);

            if (mPkg == null) return;

            Category category = new Category();
            category.addTile(new ExcludeFromRecent(getActivity(), mPkg));

            if (BuildConfig.DEBUG) {
                category.addTile(new DensityOverlay(getActivity(), mPkg));
            }

            categories.add(category);

        }
    }


}
