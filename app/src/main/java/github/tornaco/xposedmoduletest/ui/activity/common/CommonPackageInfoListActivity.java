package github.tornaco.xposedmoduletest.ui.activity.common;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.google.common.collect.Lists;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import org.newstand.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import github.tornaco.android.common.Collections;
import github.tornaco.android.common.Consumer;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.provider.AppSettings;
import github.tornaco.xposedmoduletest.ui.activity.NeedLockActivity;
import github.tornaco.xposedmoduletest.ui.adapter.common.CommonPackageInfoAdapter;
import github.tornaco.xposedmoduletest.ui.widget.SwitchBar;
import github.tornaco.xposedmoduletest.util.XExecutor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by guohao4 on 2017/12/18.
 * Email: Tornaco@163.com
 */

public abstract class CommonPackageInfoListActivity extends NeedLockActivity<CommonPackageInfo>
        implements CommonPackageInfoAdapter.ChoiceModeListener,
        CommonPackageInfoAdapter.ItemCheckListener {

    protected FloatingActionButton fab;

    private SwipeRefreshLayout swipeRefreshLayout;

    @Getter
    protected CommonPackageInfoAdapter commonPackageInfoAdapter;

    @Getter
    protected RecyclerView recyclerView;

    @Getter
    @Setter
    private int sortBy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutRes());
        setupToolbar();
        showHomeAsUp();
        initView();
    }

    @Override
    protected boolean isLockNeeded() {
        return false;
    }

    protected int getLayoutRes() {
        return R.layout.app_list;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (hasRecyclerView()) {
            startLoading();
        }
    }

    @SuppressWarnings("unchecked")
    void onRequestSearch() {
        mSearchView.display();
        openKeyboard();
    }

    private int indexOf(final CommonPackageInfo pkg) {
        return getCommonPackageInfoAdapter().getCommonPackageInfos().indexOf(pkg);
    }

    protected boolean hasRecyclerView() {
        return true;
    }

    protected void initView() {
        fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFabClick();
            }
        });

        if (hasRecyclerView()) {
            recyclerView = findViewById(R.id.recycler_view);
            swipeRefreshLayout = findViewById(R.id.swipe);
            swipeRefreshLayout.setColorSchemeColors(getResources().getIntArray(R.array.polluted_waves));


            commonPackageInfoAdapter = onCreateAdapter();
            commonPackageInfoAdapter.setChoiceModeListener(this);
            commonPackageInfoAdapter.setItemCheckListener(this);

            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(this,
                    LinearLayoutManager.VERTICAL, false));
            recyclerView.setAdapter(commonPackageInfoAdapter);
            if (recyclerView instanceof FastScrollRecyclerView) {
                ((FastScrollRecyclerView) recyclerView).setAutoHideEnabled(true);
            }


            swipeRefreshLayout.setOnRefreshListener(
                    new SwipeRefreshLayout.OnRefreshListener() {
                        @Override
                        public void onRefresh() {
                            startLoading();
                        }
                    });

            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        if (recyclerView instanceof FastScrollRecyclerView) {
                        }
                    }
                }
            });
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SwitchBar switchBar = findViewById(R.id.switchbar);
                if (switchBar != null) {
                    onInitSwitchBar(switchBar);

                    if (switchBar.isShowing()) {
                        showSwitchBarIntro(switchBar);
                    }
                }

                ViewGroup filterContainer = findViewById(R.id.apps_filter_spinner_container);
                onInitFilterSpinner(filterContainer);
            }
        });

        setupSummaryView();
    }

    protected void showSwitchBarIntro(SwitchBar switchBar) {
        if (isLocking()) return;
//        new MaterialIntroView.Builder(getActivity())
//                .enableDotAnimation(true)
//                .enableIcon(false)
//                .setFocusGravity(FocusGravity.RIGHT)
//                .setFocusType(Focus.MINIMUM)
//                .setDelayMillis(500)
//                .enableFadeAnimation(true)
//                .performClick(false)
//                .setInfoText(getString(R.string.app_intro_switchbar))
//                .setTarget(switchBar)
//                .setUsageId("switchBar")
//                .show();
    }

    protected void showFilterSpinnerIntro(Spinner spinner) {

    }

    protected void hideSwitchBar() {
        SwitchBar switchBar = findViewById(R.id.switchbar);
        if (switchBar == null) return;
        switchBar.hide();
    }

    protected void showSwitchBar() {
        SwitchBar switchBar = findViewById(R.id.switchbar);
        if (switchBar == null) return;
        switchBar.show();
    }

    protected void onInitFilterSpinner(ViewGroup filterContainer) {
        if (filterContainer == null) return;
        Spinner spinner = filterContainer.findViewById(R.id.filter_spinner);
        SpinnerAdapter adapter = onCreateSpinnerAdapter(spinner);
        if (adapter == null) {
            filterContainer.setVisibility(View.GONE);
        } else {
            filterContainer.setVisibility(View.VISIBLE);
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(onCreateSpinnerItemSelectListener());
            if (getDefaultFilterSpinnerSelection() > 0) {
                spinner.setSelection(getDefaultFilterSpinnerSelection());
            }
            showFilterSpinnerIntro(spinner);

            // Setup sort.
            View sortView = filterContainer.findViewById(R.id.filter_se);
            if (enableSortSelection()) {
                sortView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectSortOption();
                    }
                });
            } else {
                sortView.setVisibility(View.GONE);
            }

        }
    }

    protected int getDefaultFilterSpinnerSelection() {
        return -1;
    }

    protected AdapterView.OnItemSelectedListener onCreateSpinnerItemSelectListener() {
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Nothing.
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Nothing.
            }
        };
    }

    protected SpinnerAdapter onCreateSpinnerAdapter(Spinner spinner) {
        return null;
    }

    protected void onFabClick() {
        Logger.e("onFabClick");
        if (commonPackageInfoAdapter.isChoiceMode()) {
            onRequestClearItems();
        } else {
            onRequestPick();
        }
    }

    @Override
    public void onItemCheckChanged(int total, int checked) {
        swipeRefreshLayout.setEnabled(checked == 0);
        if (checked == 0) {
            commonPackageInfoAdapter.onBackPressed();
        }
    }

    private void onRequestClearItems() {
        final ProgressDialog p = new ProgressDialog(getActivity());
        p.setCancelable(false);
        p.setIndeterminate(true);
        p.setMessage(getString(R.string.message_saving_changes));
        p.show();

        XExecutor.execute(() -> {
            try {
                if (isDestroyed()) return;
                onRequestClearItemsInBackground();
            } catch (final Throwable e) {
                Logger.e("onRequestClearItems: " + Logger.getStackTraceString(e));
                if (isDestroyed()) return;
                runOnUiThread(() -> showSimpleDialog(getString(R.string.title_error_occur), Logger.getStackTraceString(e)));
            } finally {
                runOnUiThread(() -> {
                    p.dismiss();
                    getCommonPackageInfoAdapter().onBackPressed();// Leave choice mode ugly.
                });
            }

        });
    }

    protected void onRequestClearItemsInBackground() {
    }

    protected void onInitSwitchBar(SwitchBar switchBar) {
    }

    protected void onRequestPick() {
    }

    protected void setupSummaryView() {
        String who = getClass().getSimpleName();
        boolean showInfo = AppSettings.isShowInfoEnabled(this, who);
        TextView textView = findViewById(R.id.summary);
        if (!showInfo) {
            textView.setVisibility(View.GONE);
        } else {
            @StringRes
            int strId = getSummaryRes();
            if (strId > 0) {
                textView.setVisibility(View.VISIBLE);
                textView.setText(strId);
            } else {
                textView.setVisibility(View.GONE);
            }
        }
    }

    protected @StringRes
    abstract int getSummaryRes();

    protected abstract CommonPackageInfoAdapter onCreateAdapter();

    protected void startLoading() {
        if (!hasRecyclerView()) return;
        swipeRefreshLayout.setRefreshing(true);
        XExecutor.execute(() -> {
            final List<? extends CommonPackageInfo> res = performLoading();
            runOnUiThread(() -> {
//                        if (Collections.isNullOrEmpty(res)) {
//                            Toast.makeText(getContext(), R.string.loading_res_empty, Toast.LENGTH_SHORT).show();
//                        }

                swipeRefreshLayout.setRefreshing(false);
                commonPackageInfoAdapter.update(res);
            });
        });
    }

    protected abstract List<? extends CommonPackageInfo> performLoading();

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        if (item.getItemId() == R.id.action_info) {
            String who = getClass().getSimpleName();
            AppSettings.setShowInfo(this, who, !AppSettings.isShowInfoEnabled(this, who));
            setupSummaryView();
        }
        if (item.getItemId() == R.id.action_search) {
            onRequestSearch();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (hasRecyclerView()
                && getCommonPackageInfoAdapter().onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onEnterChoiceMode() {
        fab.hide(new FloatingActionButton.OnVisibilityChangedListener() {
            @Override
            public void onHidden(FloatingActionButton fab) {
                super.onHidden(fab);
                fab.setImageResource(R.drawable.ic_clear_all_black_24dp);
                fab.show();
            }
        });

        swipeRefreshLayout.setEnabled(false);
    }

    @Override
    public void onLeaveChoiceMode() {
        fab.hide(new FloatingActionButton.OnVisibilityChangedListener() {
            @Override
            public void onHidden(FloatingActionButton fab) {
                super.onHidden(fab);
                fab.setImageResource(R.drawable.ic_add_black_24dp);
                fab.show();

                startLoading();
            }
        });

        swipeRefreshLayout.setEnabled(true);
    }

    @Override
    public void onItemClicked(CommonPackageInfo item) {
        int index = indexOf(item);
        getRecyclerView().scrollToPosition(index);
        getCommonPackageInfoAdapter().setSelection(index);
    }

    @NonNull
    @Override
    public ArrayList<CommonPackageInfo> findItem(final String query, int page) {
        final ArrayList<CommonPackageInfo> res = new ArrayList<>();
        Collections.consumeRemaining(getCommonPackageInfoAdapter().getCommonPackageInfos(),
                new Consumer<CommonPackageInfo>() {
                    @Override
                    public void accept(CommonPackageInfo info) {
                        if (info.getAppName().toLowerCase().contains(query.toLowerCase())) {
                            res.add(info);
                        }
                    }
                });
        return res;
    }

    protected boolean enableSortSelection() {
        return false;
    }

    protected List<SortOption> onCreateSortOptions() {
        return Lists.newArrayList(
                new SortOption(R.string.sort_name, SortOption.SORT_BY_NAME),
                new SortOption(R.string.sort_installed_time, SortOption.SORT_BY_INSTALL_TIME),
                new SortOption(R.string.sort_usage_times, SortOption.SORT_BY_USAGE_TIMES)
        );
    }

    protected void selectSortOption() {
        final List<SortOption> all = onCreateSortOptions();
        String[] names = new String[all.size()];
        int currentIndex = 0;
        int sortBy = getSortBy();
        for (int i = 0; i < names.length; i++) {
            names[i] = getString(all.get(i).getTitleRes());
            if (all.get(i).getOption() == sortBy) {
                currentIndex = i;
            }
        }
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.select_sort)
                .setSingleChoiceItems(names, currentIndex,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SortOption op = all.get(which);
                                setSortBy(op.getOption());
                            }
                        })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onSortByChanged();
                    }
                })
                .create().show();
    }

    protected void onSortByChanged() {
        startLoading();
    }

    @Getter
    @AllArgsConstructor
    @ToString
    public static class SortOption {

        public static final int SORT_BY_NAME = 0x1;
        public static final int SORT_BY_INSTALL_TIME = 0x2;
        public static final int SORT_BY_USAGE_TIMES = 0x3;

        private int titleRes;
        private int option;
    }

    @Getter
    @AllArgsConstructor
    @ToString
    public static class FilterOption {
        public static final int OPTION_ALL_APPS = 0x1;
        public static final int OPTION_ENABLED_APPS = 0x2;
        public static final int OPTION_DISABLED_APPS = 0x3;

        public static final int OPTION_SYSTEM_APPS = 0x4;
        public static final int OPTION_SYSTEM_CORE_APPS = 0x5;
        public static final int OPTION_3RD_APPS = 0x6;

        public static final int OPTION_EXT_OP = 0x7;
        public static final int OPTION_DEFAULT_OP = 0x8;
        public static final int OPTION_ALL_OP = 0x9;


        private int titleRes;
        private int option;
    }

    @Getter
    public static class FilterSpinnerAdapter extends ArrayAdapter<CharSequence> {

        private List<FilterOption> filterOptions;
        private Context context;


        public FilterSpinnerAdapter(@NonNull Context context,
                                    @NonNull List<FilterOption> filterOptions) {
            super(context, R.layout.filter_spinner_item);
            this.context = context;
            this.filterOptions = filterOptions;
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }

        @Override
        public int getCount() {
            return filterOptions.size();
        }

        @Override
        public CharSequence getItem(int position) {
            return getFilterString(filterOptions.get(position));
        }

        private CharSequence getFilterString(FilterOption filter) {
            return getContext().getString(filter.getTitleRes());
        }
    }
}
