package github.tornaco.xposedmoduletest.ui.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.flipboard.bottomsheet.BottomSheetLayout;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.jaredrummler.android.shell.Shell;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import org.newstand.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import dev.nick.eventbus.Event;
import dev.nick.eventbus.EventBus;
import dev.nick.eventbus.EventReceiver;
import dev.nick.tiles.tile.Category;
import github.tornaco.permission.requester.RuntimePermissions;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.bean.Suggestion;
import github.tornaco.xposedmoduletest.bean.Suggestions;
import github.tornaco.xposedmoduletest.compat.pm.PackageManagerCompat;
import github.tornaco.xposedmoduletest.provider.AppSettings;
import github.tornaco.xposedmoduletest.ui.ActivityLifeCycleDashboardFragment;
import github.tornaco.xposedmoduletest.ui.FragmentController;
import github.tornaco.xposedmoduletest.ui.Themes;
import github.tornaco.xposedmoduletest.ui.activity.helper.RunningServicesActivity;
import github.tornaco.xposedmoduletest.ui.activity.stub.ClearStubActivity;
import github.tornaco.xposedmoduletest.ui.activity.stub.LockScreenStubActivity;
import github.tornaco.xposedmoduletest.ui.activity.test.TestAIOActivity;
import github.tornaco.xposedmoduletest.ui.adapter.suggest.SuggestionsAdapter;
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
import github.tornaco.xposedmoduletest.ui.tiles.PushMessageHandler;
import github.tornaco.xposedmoduletest.ui.tiles.RFKill;
import github.tornaco.xposedmoduletest.ui.tiles.Resident;
import github.tornaco.xposedmoduletest.ui.tiles.RunningServices;
import github.tornaco.xposedmoduletest.ui.tiles.SmartSense;
import github.tornaco.xposedmoduletest.ui.tiles.TRKill;
import github.tornaco.xposedmoduletest.ui.tiles.UnInstall;
import github.tornaco.xposedmoduletest.ui.tiles.app.AppDevMode;
import github.tornaco.xposedmoduletest.ui.tiles.app.AppDeveloper;
import github.tornaco.xposedmoduletest.ui.tiles.app.AppDonate;
import github.tornaco.xposedmoduletest.ui.tiles.app.AppGetPlay;
import github.tornaco.xposedmoduletest.ui.tiles.app.AppVersion;
import github.tornaco.xposedmoduletest.ui.tiles.app.AutoBlack;
import github.tornaco.xposedmoduletest.ui.tiles.app.AutoBlackNotification;
import github.tornaco.xposedmoduletest.ui.tiles.app.BackupRestoreSettings;
import github.tornaco.xposedmoduletest.ui.tiles.app.CleanUpSystemErrorTrace;
import github.tornaco.xposedmoduletest.ui.tiles.app.CrashDump;
import github.tornaco.xposedmoduletest.ui.tiles.app.GcmMessagesSubscriber;
import github.tornaco.xposedmoduletest.ui.tiles.app.GitContributors;
import github.tornaco.xposedmoduletest.ui.tiles.app.InactiveInsteadOfKillApp;
import github.tornaco.xposedmoduletest.ui.tiles.app.MokeCrash;
import github.tornaco.xposedmoduletest.ui.tiles.app.MokeSystemDead;
import github.tornaco.xposedmoduletest.ui.tiles.app.OpenMarket;
import github.tornaco.xposedmoduletest.ui.tiles.app.OpenSource;
import github.tornaco.xposedmoduletest.ui.tiles.app.PowerSave;
import github.tornaco.xposedmoduletest.ui.tiles.app.PrivacyPolicy;
import github.tornaco.xposedmoduletest.ui.tiles.app.ShowFocusedActivity;
import github.tornaco.xposedmoduletest.ui.tiles.app.StyleSettings;
import github.tornaco.xposedmoduletest.ui.tiles.app.Talkers;
import github.tornaco.xposedmoduletest.ui.tiles.app.WhiteSystemApp;
import github.tornaco.xposedmoduletest.ui.widget.BottomNavigationViewHelper;
import github.tornaco.xposedmoduletest.ui.widget.ToastManager;
import github.tornaco.xposedmoduletest.util.EmojiUtil;
import github.tornaco.xposedmoduletest.util.OSUtil;
import github.tornaco.xposedmoduletest.util.XExecutor;
import github.tornaco.xposedmoduletest.xposed.XApp;
import github.tornaco.xposedmoduletest.xposed.XAppBuildVar;
import github.tornaco.xposedmoduletest.xposed.app.XAppGuardManager;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */
@RuntimePermissions
public class NavigatorActivityBottomNav
        extends WithWithCustomTabActivity {

    interface INDEXS {
        int BASE = 0;
        int STATUS = BASE;
        int MANAGE = BASE + 1;
        int TOOLS = BASE + 2;
        int SETTINGS = BASE + 3;
        int ABOUT = BASE + 4;
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, NavigatorActivityBottomNav.class);
        context.startActivity(starter);
    }

    @Getter
    private FragmentController<ActivityLifeCycleDashboardFragment> cardController;

    @Getter
    @Setter
    private int bottomNavIndex;

    protected int getUserSetThemeResId(Themes themes) {
        return themes.getThemeStyleResNoActionBar();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_bottom_nav);

        setupView();
        setupFragment();

        // This is a workaround that some apps is installed on SD.
        // We trigger a package scan now, to ensure wo got all packages.
        if (XAshmanManager.get().isServiceAvailable()) {
            XAshmanManager.get().forceReloadPackages();
        }

        miscIfNotFirst();

        if (OSUtil.isOOrAbove()) {
            initTVStateForOreo();
        } else {
            initFirstRun();
        }

        registerEventReceivers();
    }

    private EventReceiver mEventReceiver = new EventReceiver() {
        @Override
        public void onReceive(@NonNull final Event event) {
            if (isDestroyed()) return;

            runOnUiThreadChecked(new Runnable() {
                @Override
                public void run() {
                    for (ActivityLifeCycleDashboardFragment f : getCardController().getPages()) {
                        f.onEvent(event);
                    }
                }
            });
        }

        @Override
        public int[] events() {
            return new int[]{
                    XApp.EVENT_INSTALLED_APPS_CACHE_UPDATE,
                    XApp.EVENT_RUNNING_SERVICE_CACHE_UPDATE,
            };
        }
    };

    private void registerEventReceivers() {
        EventBus.from().subscribe(mEventReceiver);
    }

    private void miscIfNotFirst() {
        if (!AppSettings.isFirstRun(getApplicationContext())) {
            // Dynamic update AppLock whitelist.
            loadAppLockConfig();
        }
    }

    private void loadAppLockConfig() {
        if (XAshmanManager.get().isServiceAvailable()) {
            XExecutor.execute(() -> {
                String[] whitelist = getResources().getStringArray(R.array.app_lock_white_list_activity);
                XAshmanManager.get().addAppLockWhiteListActivity(whitelist);
            });
        }
    }

    private void initTVStateForOreo() {
        showTvDialog();
    }

    private void showTvDialog() {
        Logger.w("showTvDialog");

        if (AppSettings.isShowInfoEnabled(this, "TV_FEATURE_WARN", true)) {

            boolean hasTv = OSUtil.hasTvFeature(this);
            Logger.w("initTVStateForOreo, hasTvFeature: " + hasTv);

            new AlertDialog.Builder(NavigatorActivityBottomNav.this)
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

    private BottomNavigationView.OnNavigationItemSelectedListener
            mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    cardController.setCurrent(INDEXS.STATUS);
                    break;
                case R.id.navigation_manage:
                    cardController.setCurrent(INDEXS.MANAGE);
                    break;
                case R.id.navigation_tools:
                    cardController.setCurrent(INDEXS.TOOLS);
                    break;
                case R.id.navigation_settings:
                    cardController.setCurrent(INDEXS.SETTINGS);
                    break;
                case R.id.navigation_about:
                    cardController.setCurrent(INDEXS.ABOUT);
                    break;
            }
            ActivityLifeCycleDashboardFragment dashboardFragment = getCardController().getCurrent();
            @StringRes int titleRes = dashboardFragment.getPageTitle();
            setTitle(titleRes);
            setBottomNavIndex(getCardController().getCurrentIndex());
            // Update menus.
            invalidateOptionsMenu();
            return true;
        }

    };

    private void setupView() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        if (AppSettings.isBottomNavNoShiftEnabled(getContext())) {
            BottomNavigationViewHelper.removeShiftMode(navigation);
        }
    }

    protected void setupFragment() {
        final List<ActivityLifeCycleDashboardFragment> cards =
                ImmutableList.of(
                        new DeviceStatusFragment(),
                        new ManageNavFragment(),
                        new ToolsNavFragment(),
                        new SettingsNavFragment(),
                        new AboutNavFragment());
        cardController = new FragmentController<>(getSupportFragmentManager(), cards, R.id.container);
        cardController.setDefaultIndex(0);
        cardController.setCurrent(0);

        // Set activity title from the first one.
        setTitle(cards.get(INDEXS.STATUS).getPageTitle());
    }

    @Override
    protected void onResume() {
        super.onResume();
        cardController.getCurrent().onActivityResume();
        checkForRedemptionMode();
    }

    private void checkForRedemptionMode() {
        boolean redemption = XAshmanManager.get().isServiceAvailable()
                && XAshmanManager.get().isInRedemptionMode();
        if (redemption) {
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.title_redemption_mode)
                    .setMessage(R.string.message_redemption_mode)
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> finishAffinity())
                    .setNeutralButton(R.string.learn_redemption_mode,
                            (dialog, which) -> {
                                finish();
                                navigateToWebPage(getString(R.string.app_wiki_url));
                            })
                    .setNegativeButton(R.string.leave_redemption_mode,
                            (dialog, which) -> {
                                XAshmanManager.get().leaveRedemptionMode();
                                finishAffinity();
                                Toast.makeText(getContext(), R.string.redemption_need_restart, Toast.LENGTH_SHORT).show();
                            })
                    .create()
                    .show();
        }
    }


    private void initFirstRun() {
        try {
            if (AppSettings.isFirstRun(getApplicationContext())) {

                new AlertDialog.Builder(NavigatorActivityBottomNav.this)
                        .setTitle(R.string.title_app_dev_say)
                        .setMessage(getString(R.string.message_first_run))
                        .setCancelable(false)
                        .setNeutralButton(R.string.no_remind, (dialog, which) -> {
                            AppSettings.setFirstRun(getApplicationContext());
                            showUpdateLog();
                        })
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> showUpdateLog())
                        .setNegativeButton(android.R.string.cancel, (dialog, which) -> finish())
                        .show();
            }
        } catch (Throwable e) {
            Toast.makeText(getActivity(), R.string.init_first_run_fail, Toast.LENGTH_SHORT).show();
        }
    }

    private void showUpdateLog() {
        final BottomSheetLayout bottomSheet = findViewById(R.id.bottomsheet);
        bottomSheet.showWithSheetView(LayoutInflater.from(getActivity())
                .inflate(R.layout.update_log_sheet_layout, bottomSheet, false));
        bottomSheet.findViewById(R.id.update_log_close_button)
                .setOnClickListener(v -> bottomSheet.dismissSheet());
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem changeCol = menu.findItem(R.id.action_change_column_count);
        changeCol.setVisible(INDEXS.STATUS != getBottomNavIndex());
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
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

        if (item.getItemId() == R.id.action_change_column_count) {
            ActivityLifeCycleDashboardFragment current = getCardController().getCurrent();
            boolean two = AppSettings.show2ColumnsIn(getActivity(), current.getClass().getSimpleName());
            AppSettings.setShow2ColumnsIn(getContext(), current.getClass().getSimpleName(), !two);
            Toast.makeText(getContext(), R.string.title_theme_need_restart_app, Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }


    public static class ToolsNavFragment extends ActivityLifeCycleDashboardFragment {
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
            boolean two = AppSettings.show2ColumnsIn(getActivity(), ToolsNavFragment.class.getSimpleName());
            return two ? 2 : 1;
        }

        @Override
        protected void onCreateDashCategories(List<Category> categories) {
            super.onCreateDashCategories(categories);

            Category dev = new Category();
            dev.titleRes = R.string.title_dev_tools;
            dev.addTile(new AppDevMode(getActivity()));
            dev.addTile(new CrashDump(getActivity()));
            dev.addTile(new MokeCrash(getActivity()));
            dev.addTile(new MokeSystemDead(getActivity()));

            Category user = new Category();
            user.titleRes = R.string.title_user_tools;
            user.addTile(new ShowFocusedActivity(getActivity()));
            user.addTile(new CleanUpSystemErrorTrace(getActivity()));

            categories.add(dev);
            categories.add(user);
        }
    }

    public static class DeviceStatusFragment extends ActivityLifeCycleDashboardFragment {

        @Getter
        private View rootView;

        @Override
        protected int getLayoutId() {
            return R.layout.fragment_dev_status;
        }

        @Override
        public int getPageTitle() {
            return R.string.title_device_status;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            rootView = super.onCreateView(inflater, container, savedInstanceState);
            return rootView;
        }

        @Override
        protected void onCreateDashCategories(List<Category> categories) {
            super.onCreateDashCategories(categories);

            Category assist = new Category();
            assist.titleRes = R.string.title_assistant;
            assist.numColumns = 1; // Force se to 1.
            assist.addTile(new RunningServices(getActivity()));

            if (XAppBuildVar.BUILD_VARS.contains(XAppBuildVar.APP_COMP_EDIT)) {
                assist.addTile(new ComponentManager(getActivity()));
            }

            Category boost = new Category();
            boost.moreDrawableRes = R.drawable.ic_more_vert_black_24dp;
            boost.onMoreButtonClickListener = v -> {
                // Build and show pop menu.
                PopupMenu popupMenu = new PopupMenu(getActivity(), v);
                popupMenu.inflate(R.menu.card_boost);
                popupMenu.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.action_lock_now) {
                        XAshmanManager.get().injectPowerEvent();
                    } else if (item.getItemId() == R.id.action_add_lock_shortcut) {
                        LockScreenStubActivity.addShortcut(getActivity());
                    } else if (item.getItemId() == R.id.action_add_shortcut) {
                        ClearStubActivity.addShortcut(getActivity());
                    } else {
                        ToastManager.show(getActivity(), "No impl, waiting for developer's work...");
                    }
                    return true;
                });
                popupMenu.show();

            };
            boost.titleRes = R.string.title_boost;
            boost.numColumns = 1; // Force se to 1.
            if (XAppBuildVar.BUILD_VARS.contains(XAppBuildVar.APP_LK)) {
                boost.addTile(new LockKill(getActivity()));
            }

            categories.add(assist);
            categories.add(boost);
        }

        @Override
        public void onActivityResume() {
            super.onActivityResume();
            setupView();
            buildUI(getActivity());
        }

        @Override
        public void onEvent(Event event) {
            super.onEvent(event);
            if (event.getEventType() == XApp.EVENT_RUNNING_SERVICE_CACHE_UPDATE
                    || event.getEventType() == XApp.EVENT_INSTALLED_APPS_CACHE_UPDATE) {

                BaseActivity activity = (BaseActivity) getActivity();
                boolean visible = activity != null && activity.isVisible();

                if (visible) {
                    buildUI(getActivity());
                }
            }
        }

        private void setupView() {

            findView(rootView, R.id.card)
                    .setOnClickListener(v -> {
                        if (BuildConfig.DEBUG) {
                            // Only show power for debug mode.
                            showPowerPopMenu(v);
                        }
                    });

            // Hide it... Move it to settings/data.
            findView(rootView, R.id.button).setVisibility(View.GONE);
            findView(rootView, R.id.button)
                    .setOnClickListener(v -> onRequestUninstalledAPM());

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

            buildSuggestions();
        }

        private SuggestionsAdapter mSuggestionsAdapter;

        private void buildSuggestions() {
            List<Suggestion> suggestionList = new ArrayList<>();

            // Guide
            if (!AppSettings.isGuideRead(getActivity())) {
                Suggestion suggestion = new Suggestion(
                        getString(R.string.suggestion_user_guide),
                        getString(R.string.suggestion_summary_user_guide),
                        getString(R.string.suggestion_action_user_guide),
                        R.drawable.ic_book_black_24dp,
                        (group, flatPosition, childIndex) -> {
                            WithWithCustomTabActivity customTabActivity = (WithWithCustomTabActivity) getActivity();
                            if (customTabActivity != null) {
                                customTabActivity.navigateToWebPage(getString(R.string.app_wiki_url));
                                if (!BuildConfig.DEBUG) {
                                    AppSettings.setGuideRead(getContext(), true);  // Keep this for debug.
                                }
                            }
                            // Keep this for debug.
                            return !BuildConfig.DEBUG;
                        });
                suggestionList.add(suggestion);
            }

            // Active!
            if (!XAshmanManager.get().isServiceAvailable()) {
                Suggestion suggestion = new Suggestion(
                        getString(R.string.suggestion_active),
                        getString(R.string.suggestion_summary_active),
                        getString(R.string.suggestion_action_active),
                        R.drawable.ic_extension_black_24dp,
                        (group, flatPosition, childIndex) -> {
                            try {
                                Intent xposedIntent = new Intent();
                                xposedIntent.setClassName("de.robv.android.xposed.installer", "de.robv.android.xposed.installer.WelcomeActivity");
                                xposedIntent.setPackage("de.robv.android.xposed.installer");
                                xposedIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(xposedIntent);
                            } catch (Throwable e) {
                                Toast.makeText(getActivity(), R.string.fail_launch_xposed_installer, Toast.LENGTH_LONG).show();
                            }
                            return false;
                        });
                suggestionList.add(suggestion);
            }

            // Donate
            boolean isPlayVersion = XAppBuildVar.BUILD_VARS.contains(XAppBuildVar.PLAY);
            if (!isPlayVersion && XAshmanManager.get().isServiceAvailable() && !AppSettings.isDonated(getActivity())) {
                Suggestion suggestion = new Suggestion(
                        getString(R.string.suggestion_donate),
                        getString(R.string.suggestion_summary_donate,
                                EmojiUtil.contactEmojiByUnicode(
                                        EmojiUtil.DOG,
                                        EmojiUtil.DOG,
                                        EmojiUtil.DOG)),
                        getString(R.string.suggestion_action_donate),
                        R.drawable.ic_payment_black_24dp,
                        (group, flatPosition, childIndex) -> {
                            DonateActivity.start(getActivity());
                            return false;
                        });
                suggestionList.add(suggestion);
            }

            // Debug mode.
            if (XAppGuardManager.get().isServiceAvailable() && XAppGuardManager.get().isDebug()) {
                Suggestion suggestion = new Suggestion(
                        getString(R.string.suggestion_turn_off_debug_mode),
                        getString(R.string.suggestion_summary_turn_off_debug_mode),
                        getString(R.string.suggestion_action_turn_off_debug_mode),
                        R.drawable.ic_developer_mode_black_24dp,
                        (group, flatPosition, childIndex) -> {
                            XAppGuardManager.get().setDebug(false);
                            return true;
                        });
                suggestionList.add(suggestion);
            }

            // Power save.
            if (XAshmanManager.get().isServiceAvailable() && !XAshmanManager.get().isPowerSaveModeEnabled()) {
                Suggestion suggestion = new Suggestion(
                        getString(R.string.suggestion_turn_on_power_save),
                        getString(R.string.suggestion_summary_turn_on_power_save),
                        getString(R.string.suggestion_action_turn_on_power_save),
                        R.drawable.ic_power_black_24dp,
                        (group, flatPosition, childIndex) -> {
                            XAshmanManager.get().setPowerSaveModeEnabled(true);
                            return true;
                        });
                suggestionList.add(suggestion);
            }

            RecyclerView recyclerView = rootView.findViewById(R.id.suggestion_recycler_view);
            if (suggestionList.size() > 0) {
                recyclerView.setVisibility(View.VISIBLE);
                Suggestions suggestions = new Suggestions(getString(R.string.suggestions_default), suggestionList);
                mSuggestionsAdapter = new SuggestionsAdapter(getActivity(), Lists.newArrayList(suggestions));
                LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(mSuggestionsAdapter);
            } else {
                recyclerView.setVisibility(View.GONE);
            }
        }

        private void onRequestUninstalledAPM() {
            if (getActivity() == null) return;
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.title_uninstall_apm)
                    .setMessage(getString(R.string.message_uninstall_apm))
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        if (XAshmanManager.get().isServiceAvailable()) {
                            XAshmanManager.get().restoreDefaultSettings();
                            Toast.makeText(getContext(), R.string.summary_restore_done, Toast.LENGTH_SHORT).show();
                        }
                        PackageManagerCompat.unInstallUserAppWithIntent(getContext(), BuildConfig.APPLICATION_ID);
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        }

        private void setupDeviceStatus() {

            boolean isNewBuild = AppSettings.isNewBuild(getActivity());
            boolean serviceAvailable = isServiceAvailable();

            // Do not setup for new build.
            if (isNewBuild) return;

            final TextView summaryView = findView(rootView, android.R.id.text1);

            if (!serviceAvailable) {
                summaryView.setText(R.string.app_intro);
            }

            if (serviceAvailable) {
                XExecutor.execute(() -> {
                    try {
                        final boolean hasModuleError = XAshmanManager.get().hasModuleError();
                        final boolean hasSystemError = XAshmanManager.get().hasSystemError();
                        Logger.d("hasModuleError %s hasSystemError %s", hasModuleError, hasSystemError);
                        BaseActivity baseActivity = (BaseActivity) getActivity();
                        if (baseActivity != null) {
                            baseActivity.runOnUiThreadChecked(new Runnable() {
                                @Override
                                public void run() {
                                    if (hasModuleError || hasSystemError) {
                                        ViewGroup header = findView(rootView, R.id.header1);
                                        header.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.amber));
                                        // Setup Icon.
                                        ImageView imageView = findView(rootView, R.id.icon1);
                                        imageView.setImageResource(R.drawable.ic_error_black_24dp);
                                    }

                                    String summary = getString(R.string.title_device_status_summary,
                                            (hasModuleError ? getString(R.string.title_device_status_summary_compat_ng) : getString(R.string.title_device_status_summary_good)),
                                            hasSystemError ? getString(R.string.title_device_status_summary_system_ng) : getString(R.string.title_device_status_summary_good));
                                    summaryView.setText(summary);
                                }
                            });
                        }
                    } catch (Throwable ignored) {
                        Logger.e("setupDeviceStatus: " + ignored);
                    }
                });
            }
        }

        private boolean isServiceAvailable() {
            return XAppGuardManager.get().isServiceAvailable();
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
            popupMenu.inflate(getCardPopupMenuRes());
            popupMenu.setOnMenuItemClickListener(onCreateOnMenuItemClickListener());
            popupMenu.show();
        }

        private PopupMenu.OnMenuItemClickListener onCreateOnMenuItemClickListener() {
            return item -> {
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
                    TestAIOActivity.start(getActivity());
                }
                return false;
            };
        }

        void executeCommandAsync(final String cmd) {
            XExecutor.execute(() -> Shell.SU.run(cmd));
        }

        public int getCardPopupMenuRes() {
            return R.menu.card;
        }
    }

    public static class ManageNavFragment
            extends ActivityLifeCycleDashboardFragment {
        @Getter
        private View rootView;

        @Override
        public int getPageTitle() {
            return R.string.title_manage;
        }

        @Override
        protected int getLayoutId() {
            return R.layout.dashboard_with_margin;
        }

        @Override
        protected int getNumColumns() {
            boolean two = AppSettings.show2ColumnsIn(getActivity(), ManageNavFragment.class.getSimpleName());
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
            buildUI(getActivity());
        }

        @Override
        protected void onCreateDashCategories(List<Category> categories) {
            super.onCreateDashCategories(categories);
            Category sec = new Category();
            sec.titleRes = R.string.title_secure;

            if (XAppBuildVar.BUILD_VARS.contains(XAppBuildVar.APP_LOCK)) {
                sec.addTile(new AppGuard(getActivity()));
            }
            if (XAppBuildVar.BUILD_VARS.contains(XAppBuildVar.APP_BLUR)) {
                sec.addTile(new Blur(getActivity()));
            }

            if (XAppBuildVar.BUILD_VARS.contains(XAppBuildVar.APP_UNINSTALL)) {
                sec.addTile(new UnInstall(getActivity()));
            }

            if (XAppBuildVar.BUILD_VARS.contains(XAppBuildVar.APP_RESIDENT)) {
                if (AppSettings.isShowInfoEnabled(getContext(), "show_hidden_features", false)) {
                    sec.addTile(new Resident(getActivity()));
                }
            }

            if (XAppBuildVar.BUILD_VARS.contains(XAppBuildVar.APP_PRIVACY)) {
                sec.addTile(new Privacy(getActivity()));
            }

            Category rest = new Category();
            rest.titleRes = R.string.title_restrict;

            if (XAppBuildVar.BUILD_VARS.contains(XAppBuildVar.APP_BOOT)) {
                rest.addTile(new AppBoot(getActivity()));
            }
            if (XAppBuildVar.BUILD_VARS.contains(XAppBuildVar.APP_START)) {
                rest.addTile(new AppStart(getActivity()));
            }

            if (XAppBuildVar.BUILD_VARS.contains(XAppBuildVar.APP_RFK)) {
                rest.addTile(new RFKill(getActivity()));
                rest.addTile(new TRKill(getActivity()));
            }

            Category ash = new Category();
            ash.titleRes = R.string.title_control;
            ash.moreDrawableRes = R.drawable.ic_help_black_24dp;
            ash.onMoreButtonClickListener = v -> Toast.makeText(getActivity(), R.string.category_help_advance, Toast.LENGTH_SHORT).show();

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
            exp.moreDrawableRes = R.drawable.ic_help_black_24dp;
            exp.onMoreButtonClickListener = v -> Toast.makeText(getActivity(),
                    R.string.category_help_exp, Toast.LENGTH_SHORT).show();
            exp.titleRes = R.string.title_exp;

            // L do not support doze.
            if (OSUtil.isMOrAbove() && XAppBuildVar.BUILD_VARS.contains(XAppBuildVar.APP_DOZE)) {
                exp.addTile(new Doze(getActivity()));
            }

            if ((XAppBuildVar.BUILD_VARS.contains(XAppBuildVar.APP_LAZY))) {
                exp.addTile(new Lazy(getActivity()));
            }

            if (XApp.isGMSSupported()) {
                exp.addTile(new PushMessageHandler(getActivity()));
            }

            if (sec.getTilesCount() > 0) categories.add(sec);
            if (rest.getTilesCount() > 0) categories.add(rest);
            if (ash.getTilesCount() > 0) categories.add(ash);
            if (exp.getTilesCount() > 0) categories.add(exp);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        NavigatorActivityBottomNavPermissionRequester.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    public static class SettingsNavFragment extends ActivityLifeCycleDashboardFragment {

        @Override
        public int getPageTitle() {
            return R.string.title_settings;
        }

        @Override
        protected int getLayoutId() {
            return R.layout.dashboard_with_margin;
        }

        @Override
        protected int getNumColumns() {
            boolean two = AppSettings.show2ColumnsIn(getActivity(),
                    SettingsNavFragment.class.getSimpleName());
            return two ? 2 : 1;
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
            policy.addTile(new AutoBlack(getActivity()));
            policy.addTile(new AutoBlackNotification(getActivity()));
            policy.addTile(new InactiveInsteadOfKillApp(getActivity()));

            Category data = new Category();
            data.titleRes = R.string.title_data;
            data.addTile(new BackupRestoreSettings(getActivity()));

            Category theme = new Category();
            theme.titleRes = R.string.title_style;
            theme.addTile(new StyleSettings(getActivity()));

            Category others = new Category();
            others.titleRes = R.string.title_others;
            if (XApp.isGMSSupported()) {
                others.addTile(new GcmMessagesSubscriber(getActivity()));
            }

            categories.add(system);
            categories.add(policy);
            categories.add(data);
            categories.add(theme);
            if (others.getTilesCount() > 0) categories.add(others);
        }
    }

    public static class AboutNavFragment extends ActivityLifeCycleDashboardFragment {
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
            boolean two = AppSettings.show2ColumnsIn(getActivity(), AboutNavFragment.class.getSimpleName());
            return two ? 2 : 1;
        }

        @Override
        protected void onCreateDashCategories(List<Category> categories) {
            super.onCreateDashCategories(categories);

            Category personal = new Category();
            personal.titleRes = R.string.title_about;
            personal.addTile(new AppDeveloper(getActivity()));
            personal.addTile(new AppVersion(getActivity()));
            personal.addTile(new OpenMarket(getActivity()));

            Category open = new Category();
            open.titleRes = R.string.title_open_info;
            open.addTile(new PrivacyPolicy(getActivity()));
            open.addTile(new OpenSource(getActivity()));
            open.addTile(new GitContributors(getActivity()));
            open.addTile(new Talkers(getActivity()));

            boolean isPlayVersion = XAppBuildVar.BUILD_VARS.contains(XAppBuildVar.PLAY);

            categories.add(personal);
            categories.add(open);

            if (!isPlayVersion) {
                Category help = new Category();
                help.titleRes = R.string.title_help_dev;
                help.addTile(new AppDonate(getActivity()));
                help.addTile(new AppGetPlay(getActivity()));
                categories.add(help);
            }
        }
    }
}
