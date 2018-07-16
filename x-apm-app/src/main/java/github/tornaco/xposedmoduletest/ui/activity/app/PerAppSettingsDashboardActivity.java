package github.tornaco.xposedmoduletest.ui.activity.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.List;
import java.util.Objects;

import dev.nick.tiles.tile.Category;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.loader.GlideApp;
import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.ui.AppCustomDashboardFragment;
import github.tornaco.xposedmoduletest.ui.activity.WithWithCustomTabActivity;
import github.tornaco.xposedmoduletest.ui.tiles.AppOpsTemplateSetting;
import github.tornaco.xposedmoduletest.ui.tiles.PermControlTemplate;
import github.tornaco.xposedmoduletest.ui.tiles.app.per.AppBlurSetting;
import github.tornaco.xposedmoduletest.ui.tiles.app.per.AppBootSetting;
import github.tornaco.xposedmoduletest.ui.tiles.app.per.AppLKSetting;
import github.tornaco.xposedmoduletest.ui.tiles.app.per.AppLazySetting;
import github.tornaco.xposedmoduletest.ui.tiles.app.per.AppLockSetting;
import github.tornaco.xposedmoduletest.ui.tiles.app.per.AppPrivacySetting;
import github.tornaco.xposedmoduletest.ui.tiles.app.per.AppRFKSetting;
import github.tornaco.xposedmoduletest.ui.tiles.app.per.AppStartSetting;
import github.tornaco.xposedmoduletest.ui.tiles.app.per.AppTRKSetting;
import github.tornaco.xposedmoduletest.ui.tiles.app.per.AppUPSetting;
import github.tornaco.xposedmoduletest.xposed.XAppBuildVar;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.bean.AppSettings;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;

/**
 * Created by guohao4 on 2017/11/2.
 * Email: Tornaco@163.com
 */

public class PerAppSettingsDashboardActivity extends WithWithCustomTabActivity {

    public static void start(Context context, String pkg) {
        Intent starter = new Intent(context, PerAppSettingsDashboardActivity.class);
        starter.putExtra("pkg_name", pkg);
        starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.per_app_settings);
        setupToolbar();
        showHomeAsUp();

        String pkgName = getIntent().getStringExtra("pkg_name");
        if (pkgName == null) return;

        setTitle(getTitle() + "\t" + PkgUtil.loadNameByPkgName(getContext(), pkgName));
        setSubTitleChecked(pkgName);

        replaceV4(R.id.container, Dashboards.newInstance(getContext(), pkgName), null, false);

        findViewById(R.id.fab).setOnClickListener(v -> onFabClick());

        setupAppBar(pkgName);
    }

    void setupAppBar(String pkgName) {
        ImageView appIconView = findViewById(R.id.app_icon);
        CommonPackageInfo c = new CommonPackageInfo();
        c.setPkgName(pkgName);
        GlideApp.with(getContext())
                .asBitmap()
                .load(c)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource,
                                                Transition<? super Bitmap> transition) {
                        if (!isDestroyed()) {
                            appIconView.setImageBitmap(resource);
                        }
                    }
                });
    }

    void onFabClick() {
        finish();
    }

    AppSettings onRetrieveAppSettings(String pkg) {
        return XAPMManager.get()
                .retrieveAppSettingsForPackage(pkg);
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

            AppSettings appSettings = ((PerAppSettingsDashboardActivity)
                    Objects.requireNonNull(getActivity())).onRetrieveAppSettings(mPkg);
            if (appSettings == null) {
                appSettings = AppSettings.builder()
                        .boot(false)
                        .start(false)
                        .trk(false)
                        .rfk(false)
                        .lk(false)
                        .build();
            }

            Category sec = new Category();
            sec.titleRes = R.string.title_secure;

            sec.addTile(new AppLockSetting(getActivity(), appSettings));
            sec.addTile(new AppBlurSetting(getActivity(), appSettings));
            sec.addTile(new AppUPSetting(getActivity(), appSettings));
            sec.addTile(new AppPrivacySetting(getActivity(), appSettings));

            Category rest = new Category();
            rest.titleRes = R.string.title_restrict;
            rest.addTile(new AppBootSetting(getActivity(), appSettings));
            rest.addTile(new AppStartSetting(getActivity(), appSettings));
            rest.addTile(new AppLKSetting(getActivity(), appSettings));
            rest.addTile(new AppRFKSetting(getActivity(), appSettings));
            rest.addTile(new AppTRKSetting(getActivity(), appSettings));
            rest.addTile(new AppLazySetting(getActivity(), appSettings));

            Category perm = new Category();
            perm.titleRes = R.string.title_perm_control;

            if (XAppBuildVar.BUILD_VARS.contains(XAppBuildVar.APP_OPS)) {
                boolean isDummy = mPkg.equals(XAPMManager.APPOPS_WORKAROUND_DUMMY_PACKAGE_NAME);
                if (isDummy) {
                    perm.addTile(new AppOpsTemplateSetting(getActivity(), appSettings));
                } else {
                    perm.addTile(new PermControlTemplate(getActivity(), mPkg));
                }
            }

            categories.add(sec);
            categories.add(rest);
            categories.add(perm);
        }
    }


}
