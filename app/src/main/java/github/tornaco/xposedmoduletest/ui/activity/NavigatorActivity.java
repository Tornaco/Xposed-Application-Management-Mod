package github.tornaco.xposedmoduletest.ui.activity;

import android.app.ActivityManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.collect.ImmutableList;
import com.jaredrummler.android.shell.Shell;

import org.newstand.logger.Logger;

import java.util.List;
import java.util.UUID;

import dev.nick.tiles.tile.Category;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.compat.pm.PackageManagerCompat;
import github.tornaco.xposedmoduletest.license.DeveloperMessage;
import github.tornaco.xposedmoduletest.license.DeveloperMessages;
import github.tornaco.xposedmoduletest.provider.AppSettings;
import github.tornaco.xposedmoduletest.ui.ActivityLifeCycleDashboardFragment;
import github.tornaco.xposedmoduletest.ui.FragmentController;
import github.tornaco.xposedmoduletest.ui.Themes;
import github.tornaco.xposedmoduletest.ui.activity.app.AboutDashboardActivity;
import github.tornaco.xposedmoduletest.ui.activity.app.AppDashboardActivity;
import github.tornaco.xposedmoduletest.ui.activity.app.GetPlayVersionActivity;
import github.tornaco.xposedmoduletest.ui.activity.app.ToolsDashboardActivity;
import github.tornaco.xposedmoduletest.ui.activity.helper.RunningServicesActivity;
import github.tornaco.xposedmoduletest.ui.activity.test.TestAIOActivity;
import github.tornaco.xposedmoduletest.ui.activity.whyyouhere.UserGuideActivityA;
import github.tornaco.xposedmoduletest.ui.tiles.AppBoot;
import github.tornaco.xposedmoduletest.ui.tiles.AppGuard;
import github.tornaco.xposedmoduletest.ui.tiles.AppStart;
import github.tornaco.xposedmoduletest.ui.tiles.Blur;
import github.tornaco.xposedmoduletest.ui.tiles.CompReplacement;
import github.tornaco.xposedmoduletest.ui.tiles.ComponentManager;
import github.tornaco.xposedmoduletest.ui.tiles.Doze;
import github.tornaco.xposedmoduletest.ui.tiles.Greening;
import github.tornaco.xposedmoduletest.ui.tiles.Lazy;
import github.tornaco.xposedmoduletest.ui.tiles.LockKill;
import github.tornaco.xposedmoduletest.ui.tiles.NFManager;
import github.tornaco.xposedmoduletest.ui.tiles.PermControl;
import github.tornaco.xposedmoduletest.ui.tiles.Privacy;
import github.tornaco.xposedmoduletest.ui.tiles.RFKill;
import github.tornaco.xposedmoduletest.ui.tiles.Resident;
import github.tornaco.xposedmoduletest.ui.tiles.SmartSense;
import github.tornaco.xposedmoduletest.ui.tiles.TRKill;
import github.tornaco.xposedmoduletest.ui.tiles.UnInstall;
import github.tornaco.xposedmoduletest.util.GsonUtil;
import github.tornaco.xposedmoduletest.util.OSUtil;
import github.tornaco.xposedmoduletest.util.XExecutor;
import github.tornaco.xposedmoduletest.xposed.XApp;
import github.tornaco.xposedmoduletest.xposed.XAppBuildVar;
import github.tornaco.xposedmoduletest.xposed.app.XAppGuardManager;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;
import github.tornaco.xposedmoduletest.xposed.util.FileUtil;
import lombok.Getter;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class NavigatorActivity extends WithWithCustomTabActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Getter
    private FragmentController<ActivityLifeCycleDashboardFragment> cardController;

    protected int getUserSetThemeResId(Themes themes) {
        return themes.getThemeStyleResNoActionBarDrawer();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_navigator);

        setupView();
        setupFragment();

        // This is a workaround that some apps is installed on SD.
        // We trigger a package scan now, to ensure wo got all packages.
        if (XAshmanManager.get().isServiceAvailable()) {
            XAshmanManager.get().forceReloadPackages();
        }

        if (OSUtil.isOOrAbove()) {
            initTVStateForOreo();
        } else {
            initFirstRun();
        }
    }

    private void initTVStateForOreo() {
        showTvDialog();
    }

    private void showTvDialog() {
        Logger.w("showTvDialog");

        if (AppSettings.isShowInfoEnabled(this, "TV_FEATURE_WARN", true)
                || BuildConfig.DEBUG) {

            boolean hasTv = OSUtil.hasTvFeature(this);
            Logger.w("initTVStateForOreo, hasTvFeature: " + hasTv);

            new AlertDialog.Builder(NavigatorActivity.this)
                    .setTitle(R.string.title_app_oreo_update)
                    .setMessage(getString(
                            hasTv ?
                                    R.string.message_oreo_update
                                    : R.string.message_oreo_update_not_tv))
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            initFirstRun();
                            AppSettings.setShowInfo(getContext(), "TV_FEATURE_WARN", false);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                            PackageManagerCompat.unInstallUserAppWithIntent(getContext(), getPackageName());
                        }
                    })
                    .show();
        } else {
            initFirstRun();
        }
    }

    private boolean mDrawerUserInfoSetup;

    private void setupView() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                if (mDrawerUserInfoSetup) return;

                runOnUiThreadChecked(new Runnable() {
                    @Override
                    public void run() {
                        boolean guideRead = AppSettings.isGuideRead(getContext());
                        TextView userLevel = drawer.findViewById(R.id.user_name);

                        if ((XApp.isPlayVersion() || AppSettings.isDonated(getContext()) || BuildConfig.DEBUG)
                                && XAshmanManager.get().isServiceAvailable()) {
                            String userName = XAshmanManager.get().getUserName();
                            if (userName != null) {
                                userLevel.setText(userName);
                            } else {
                                userLevel.setText(guideRead ? R.string.user_level_middle : R.string.user_level_low);
                            }
                            Bitmap userIcon = XAshmanManager.get().getUserIcon();
                            if (userIcon != null) {
                                ImageView userIconView = drawer.findViewById(R.id.user_avatar);
                                userIconView.setImageBitmap(userIcon);
                            }
                        } else {
                            userLevel.setText(guideRead ? R.string.user_level_middle : R.string.user_level_low);
                        }

                        mDrawerUserInfoSetup = true;
                    }
                });
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        MenuItem play = navigationView.getMenu().findItem(R.id.action_play_version);
        MenuItem donate = navigationView.getMenu().findItem(R.id.action_donate);
        boolean isPlayVersion = XAppBuildVar.BUILD_VARS.contains(XAppBuildVar.PLAY);
        play.setVisible(!isPlayVersion);
        donate.setVisible(!isPlayVersion);

        navigationView.setCheckedItem(R.id.action_home);
    }

    protected void setupFragment() {
        final List<ActivityLifeCycleDashboardFragment> cards =
                ImmutableList.of(
                        onCreateMainFragment(),
                        new ToolsDashboardActivity.Dashboards(),
                        onCreateEXTFragment());
        cardController = new FragmentController<>(getSupportFragmentManager(), cards, R.id.container);
        cardController.setDefaultIndex(0);
        cardController.setCurrent(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cardController.getCurrent().onActivityResume();


        if (BuildConfig.DEBUG) {
            DeveloperMessage developerMessage = new DeveloperMessage();
            developerMessage.setTitle("Test title");
            developerMessage.setMessage("Test message");
            developerMessage.setCancelable(true);
            developerMessage.setMessageId(UUID.randomUUID().toString());
            developerMessage.setTimeMills(System.currentTimeMillis());
            Logger.e(GsonUtil.getGson().toJson(developerMessage));
        }

        // Load dev message.
        DeveloperMessages.loadAsync(new DeveloperMessages.Callback() {
            @Override
            public void onError(Throwable e) {
                // Noop.
            }

            @Override
            public void onSuccess(final List<DeveloperMessage> messages) {
                if (!isDestroyed() && messages != null && messages.size() > 0) {
                    runOnUiThreadChecked(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                showDeveloperMessage(messages.get(0));
                            } catch (Throwable ignored) {
                            }
                        }
                    });
                }
            }
        });
    }

    private void showDeveloperMessage(DeveloperMessage message) {
        final String messageId = message.getMessageId();
        boolean show = AppSettings.isShowInfoEnabled(getContext(), messageId);
        boolean debug = message.isTest();
        if (debug && !BuildConfig.DEBUG) return;
        Logger.d("showDeveloperMessage: " + message + ", " + show);
        if (show) {
            new AlertDialog.Builder(getActivity())
                    .setTitle(message.getTitle())
                    .setMessage(message.getMessage())
                    .setPositiveButton(android.R.string.ok, null)
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            AppSettings.setShowInfo(getContext(), messageId, false);
                        }
                    })
                    .setCancelable(message.isCancelable())
                    .create()
                    .show();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void initFirstRun() {
        try {
            if (AppSettings.isFirstRun(getApplicationContext())) {

                if (!XApp.isPlayVersion()) {
                    UserGuideActivityA.start(getActivity());
                }

                new AlertDialog.Builder(NavigatorActivity.this)
                        .setTitle(R.string.title_app_update_log)
                        .setMessage(getString(R.string.message_first_run))
                        .setCancelable(false)
                        .setNeutralButton(R.string.no_remind, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AppSettings.setFirstRun(getApplicationContext());
                            }
                        })
                        .setPositiveButton(android.R.string.ok, null)
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .show();
            }
        } catch (Throwable e) {
            Toast.makeText(getActivity(), R.string.init_first_run_fail, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_help) {
            navigateToWebPage(getString(R.string.app_wiki_url));
            AppSettings.setGuideRead(getContext(), true);
        }
        if (item.getItemId() == R.id.action_update_log) {
            navigateToWebPage(getString(R.string.app_rel_url));
        }

        if (item.getItemId() == R.id.action_uninstall) {
            onRequestUninstalledAPM();
        }

        if (item.getItemId() == R.id.action_change_column_count) {
            boolean two = AppSettings.show2ColumnsIn(getActivity(), NavigatorActivity.class.getSimpleName());
            AppSettings.setShow2ColumnsIn(getContext(), NavigatorActivity.class.getSimpleName(), !two);
            Toast.makeText(getContext(), "Duang~~~~~~~", Toast.LENGTH_SHORT).show();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void onRequestUninstalledAPM() {
        new AlertDialog.Builder(NavigatorActivity.this)
                .setTitle(R.string.title_uninstall_apm)
                .setMessage(getString(R.string.message_uninstall_apm))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (XAshmanManager.get().isServiceAvailable()) {
                            XAshmanManager.get().restoreDefaultSettings();
                            Toast.makeText(getContext(), R.string.summary_restore_done, Toast.LENGTH_SHORT).show();
                        }
                        PackageManagerCompat.unInstallUserAppWithIntent(getContext(), getPackageName());
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    protected ActivityLifeCycleDashboardFragment onCreateMainFragment() {
        return new NavigatorFragment();
    }

    protected ActivityLifeCycleDashboardFragment onCreateEXTFragment() {
        return new EXTFragment();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, AppDashboardActivity.class));
        }

        if (item.getItemId() == R.id.action_play_version) {
            GetPlayVersionActivity.start(getActivity());
        }

        if (item.getItemId() == R.id.action_about) {
            startActivity(new Intent(this, AboutDashboardActivity.class));
        }
        if (item.getItemId() == R.id.action_tools) {
            getCardController().setCurrent(1);
            setTitle(R.string.action_tools);
        }

        if (item.getItemId() == R.id.action_home) {
            getCardController().setCurrent(0);
            setTitle(R.string.app_name);
        }

        if (item.getItemId() == R.id.action_ext) {
            getCardController().setCurrent(2);
            setTitle(R.string.app_ext);
        }

        if (item.getItemId() == R.id.action_donate) {
            startActivity(new Intent(getContext(), DonateActivity.class));
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public static class EXTFragment extends ActivityLifeCycleDashboardFragment {
        @Getter
        private View rootView;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            rootView = super.onCreateView(inflater, container, savedInstanceState);
            return rootView;
        }

        @Override
        protected int getLayoutId() {
            return R.layout.fragment_ext;
        }
    }

    public static class NavigatorFragment extends ActivityLifeCycleDashboardFragment {
        @Getter
        private View rootView;

        @Override
        protected int getLayoutId() {
            return R.layout.fragment_navigator;
        }

        @Override
        protected int getNumColumes() {
            boolean two = AppSettings.show2ColumnsIn(getActivity(), NavigatorActivity.class.getSimpleName());
            return two ? 2 : 1;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            rootView = super.onCreateView(inflater, container, savedInstanceState);
            return rootView;
        }

        @Override
        public void onActivityResume() {
            super.onActivityResume();
            setupView();
            buildUI(getActivity());
        }

        private void setupView() {

            findView(rootView, R.id.card)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (BuildConfig.DEBUG) {
                                // Only show power for debug mode.
                                showPowerPopMenu(v);
                            }
                        }
                    });

            // Setup title.
            TextView statusTitle = findView(rootView, android.R.id.title);

            boolean isNewBuild = AppSettings.isNewBuild(getActivity());

            if (isNewBuild) {
                statusTitle.setText(R.string.title_service_need_action);
            } else {
                boolean isDonatedOrPlay = XApp.isPlayVersion() || AppSettings.isDonated(getContext());
                if (isServiceAvailable() && isDonatedOrPlay) {
                    statusTitle.setText(R.string.title_device_status);
                } else {
                    statusTitle.setText(isServiceAvailable() ? R.string.title_service_connected : R.string.title_service_not_connected);
                }
            }

            TextView summaryView = findView(rootView, android.R.id.text1);


            // Setup Icon.
            ImageView imageView = findView(rootView, R.id.icon1);

            if (isNewBuild) {
                summaryView.setText(R.string.app_intro_need_restart);
                ViewGroup header = findView(rootView, R.id.header1);
                header.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.amber));
                imageView.setImageResource(R.drawable.ic_error_black_24dp);
            } else {
                TypedValue typedValue = new TypedValue();
                getActivity().getTheme().resolveAttribute(R.attr.torCardAccentBackgroundColor, typedValue, true);
                int resId = typedValue.resourceId;

                int cardAccentColor = ContextCompat.getColor(getActivity(), resId);

                ViewGroup header = findView(rootView, R.id.header1);
                header.setBackgroundColor(
                        XAppGuardManager.get().isServiceAvailable() ?
                                cardAccentColor
                                : ContextCompat.getColor(getActivity(), R.color.red));
                boolean isDonatedOrPlay = XApp.isPlayVersion() || AppSettings.isDonated(getContext());
                imageView.setImageResource(isServiceAvailable()
                        ? isDonatedOrPlay ? R.drawable.ic_multiline_chart_black_24dp : R.drawable.ic_check_circle_black_24dp
                        : R.drawable.ic_error_black_24dp);
            }

            setupDeviceStatus();
        }

        private void setupDeviceStatus() {

            boolean isNewBuild = AppSettings.isNewBuild(getActivity());
            boolean isDonatedOrPlay = XApp.isPlayVersion() || AppSettings.isDonated(getContext());
            boolean serviceAvailable = isServiceAvailable();

            // Do not setup for new build.
            if (isNewBuild) return;

            final TextView summaryView = findView(rootView, android.R.id.text1);

            if (!serviceAvailable) {
                summaryView.setText(R.string.app_intro);
            }

            if (serviceAvailable) {

                Button button = findView(rootView, R.id.button);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(getActivity(), RunningServicesActivity.class));
                    }
                });

                final TextView memInfoText = rootView.findViewById(R.id.mem_info);
                final ProgressBar memPercentView = rootView.findViewById(R.id.mem_percent);
                XExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ActivityManager.MemoryInfo m = XAshmanManager.get().getMemoryInfo();
                            Logger.e("setupDeviceStatus: " + m);
                            if (m != null) {
                                final String infoStr = FileUtil.formatSize(m.availMem) + "/" + FileUtil.formatSize(m.totalMem);
                                final int percent = (int) (100 * (((float) m.availMem / (float) m.totalMem)));
                                BaseActivity baseActivity = (BaseActivity) getActivity();
                                if (baseActivity != null) {
                                    baseActivity.runOnUiThreadChecked(new Runnable() {
                                        @Override
                                        public void run() {
                                            memInfoText.setText(infoStr);
                                            memPercentView.setProgress(percent);
                                            boolean hasModuleError = XAshmanManager.get().hasModuleError();
                                            summaryView.setText(
                                                    hasModuleError
                                                            ? R.string.title_device_status_module_err
                                                            : R.string.title_device_status_good);
                                        }
                                    });
                                }
                            }
                        } catch (Throwable ignored) {
                            Logger.e("setupDeviceStatus: " + ignored);
                        }
                    }
                });
            } else {
                findView(rootView, R.id.mem).setVisibility(View.GONE);
            }
        }

        private boolean isServiceAvailable() {
            return XAppGuardManager.get().isServiceAvailable();
        }

        @Override
        protected void onCreateDashCategories(List<Category> categories) {
            super.onCreateDashCategories(categories);
            Category category = new Category();
            category.titleRes = R.string.title_secure;

            if (XAppBuildVar.BUILD_VARS.contains(XAppBuildVar.APP_LOCK)) {
                category.addTile(new AppGuard(getActivity()));
            }
            if (XAppBuildVar.BUILD_VARS.contains(XAppBuildVar.APP_BLUR)) {
                category.addTile(new Blur(getActivity()));
            }

            if (XAppBuildVar.BUILD_VARS.contains(XAppBuildVar.APP_UNINSTALL)) {
                category.addTile(new UnInstall(getActivity()));
                category.addTile(new Resident(getActivity()));
            }

            if (XAppBuildVar.BUILD_VARS.contains(XAppBuildVar.APP_PRIVACY)) {
                category.addTile(new Privacy(getActivity()));
            }

            Category rest = new Category();
            rest.titleRes = R.string.title_restrict;

            if (XAppBuildVar.BUILD_VARS.contains(XAppBuildVar.APP_BOOT)) {
                rest.addTile(new AppBoot(getActivity()));
            }
            if (XAppBuildVar.BUILD_VARS.contains(XAppBuildVar.APP_START)) {
                rest.addTile(new AppStart(getActivity()));
            }
            if (XAppBuildVar.BUILD_VARS.contains(XAppBuildVar.APP_LK)) {
                rest.addTile(new LockKill(getActivity()));
            }

            if (XAppBuildVar.BUILD_VARS.contains(XAppBuildVar.APP_RFK)) {
                rest.addTile(new RFKill(getActivity()));
                rest.addTile(new TRKill(getActivity()));
            }

            Category ash = new Category();
            ash.titleRes = R.string.title_control;

            if (XAppBuildVar.BUILD_VARS.contains(XAppBuildVar.APP_COMP_EDIT)) {
                ash.addTile(new ComponentManager(getActivity()));
            }

            if (XAppBuildVar.BUILD_VARS.contains(XAppBuildVar.APP_COMP_REPLACE)) {
                ash.addTile(new CompReplacement(getActivity()));
            }

            if (XAppBuildVar.BUILD_VARS.contains(XAppBuildVar.APP_OPS)) {
                ash.addTile(new PermControl(getActivity()));
            }

            if (XAppBuildVar.BUILD_VARS.contains(XAppBuildVar.APP_SMART_SENSE)) {
                ash.addTile(new SmartSense(getActivity()));
            }

            if (XAppBuildVar.BUILD_VARS.contains(XAppBuildVar.APP_GREEN)) {
                ash.addTile(new Greening(getActivity()));
            }

            // Only add when firewall is enabled for this build.
            if (XAppBuildVar.BUILD_VARS.contains(XAppBuildVar.APP_FIREWALL)) {
                ash.addTile(new NFManager(getActivity()));
            }

            Category exp = new Category();
            exp.titleRes = R.string.title_exp;

            // L do not support doze.
            if (OSUtil.isMOrAbove() && XAppBuildVar.BUILD_VARS.contains(XAppBuildVar.APP_DOZE)) {
                exp.addTile(new Doze(getActivity()));
            }


            if ((XAppBuildVar.BUILD_VARS.contains(XAppBuildVar.APP_LAZY))) {
                exp.addTile(new Lazy(getActivity()));
            }

            if (category.getTilesCount() > 0) categories.add(category);
            if (rest.getTilesCount() > 0) categories.add(rest);
            if (ash.getTilesCount() > 0) categories.add(ash);
            if (exp.getTilesCount() > 0) categories.add(exp);
        }

        @SuppressWarnings("unchecked")
        protected <T extends View> T findView(@IdRes int idRes) {
            return (T) getRootView().findViewById(idRes);
        }

        @SuppressWarnings("unchecked")
        protected <T extends View> T findView(View root, @IdRes int idRes) {
            return (T) root.findViewById(idRes);
        }

        protected void showPowerPopMenu(View anchor) {
            PopupMenu popupMenu = new PopupMenu(getContext(), anchor);
            popupMenu.inflate(getPopupMenuRes());
            popupMenu.setOnMenuItemClickListener(onCreateOnMenuItemClickListener());
            popupMenu.show();
        }

        private PopupMenu.OnMenuItemClickListener onCreateOnMenuItemClickListener() {
            return new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (item.getItemId() == R.id.action_soft_restart) {
                        executeCommandAsync("stop;start");
                    }
                    if (item.getItemId() == R.id.action_restart_rec) {
                        executeCommandAsync("reboot recovery");
                    }
                    if (item.getItemId() == R.id.action_restart_bl) {
                        executeCommandAsync("reboot bootloader");
                    }
                    if (item.getItemId() == R.id.action_start_test) {
                        TestAIOActivity.start(getContext());
                    }
                    if (item.getItemId() == R.id.action_running_services) {
                        startActivity(new Intent(getActivity(), RunningServicesActivity.class));
                    }
                    return false;
                }
            };
        }

        void executeCommandAsync(final String cmd) {
            XExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    Shell.SU.run(cmd);
                }
            });
        }

        public int getPopupMenuRes() {
            return R.menu.card;
        }
    }
}
