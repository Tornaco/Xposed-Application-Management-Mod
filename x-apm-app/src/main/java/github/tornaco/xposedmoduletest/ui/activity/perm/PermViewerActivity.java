package github.tornaco.xposedmoduletest.ui.activity.perm;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.collect.Lists;
import com.shahroz.svlibrary.interfaces.onSimpleSearchActionsListener;
import com.shahroz.svlibrary.widgets.SearchViewResults;

import org.newstand.logger.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.compat.os.XAppOpsManagerRes;
import github.tornaco.xposedmoduletest.compat.pm.PackageManagerCompat;
import github.tornaco.xposedmoduletest.loader.ComponentLoader;
import github.tornaco.xposedmoduletest.loader.PermissionLoader;
import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.provider.AppSettings;
import github.tornaco.xposedmoduletest.ui.activity.WithSearchActivity;
import github.tornaco.xposedmoduletest.ui.activity.common.CommonPackageInfoListActivity;
import github.tornaco.xposedmoduletest.ui.adapter.common.CommonPackageInfoAdapter;
import github.tornaco.xposedmoduletest.ui.adapter.common.CommonPackageInfoViewerAdapter;
import github.tornaco.xposedmoduletest.ui.widget.SwitchBar;
import github.tornaco.xposedmoduletest.util.SpannableUtil;
import github.tornaco.xposedmoduletest.util.XExecutor;
import github.tornaco.xposedmoduletest.xposed.XAPMApplication;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import lombok.Getter;

public class PermViewerActivity extends WithSearchActivity<CommonPackageInfo> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.permission_viewer);

        setupToolbar();
        showHomeAsUp();

        initOpsSettingsDialog();

        setupViews();

        initPages();
    }

    private void initOpsSettingsDialog() {
        if (AppSettings.isShowInfoEnabled(getContext(), "ops_settings", true)) {
            new AlertDialog.Builder(PermViewerActivity.this)
                    .setTitle(R.string.title_app_ops_tip)
                    .setMessage(getString(R.string.message_app_ops_tip))
                    .setCancelable(false)
                    .setNeutralButton(R.string.no_remind, (dialog, which) -> AppSettings.setShowInfo(getApplicationContext(), "ops_settings", false))
                    .setPositiveButton(android.R.string.ok, null)
                    .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                        finishAffinity();
                        PackageManagerCompat.unInstallUserAppWithIntent(getContext(), getPackageName());
                    })
                    .show();
        }
    }


    private void setupViews() {
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        ViewPager mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                mCurrentFragment = mFragments.get(position);
            }

            @Override
            public void onPageSelected(int position) {
                mCurrentFragment = mFragments.get(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        setTitle(R.string.title_perm_control);

        setSummaryView();

        runOnUiThread(() -> {
            SwitchBar switchBar = findViewById(R.id.switchbar);
            switchBar.show();
            switchBar.setChecked(XAPMManager.get().isPermissionControlEnabled());
            switchBar.addOnSwitchChangeListener((switchView, isChecked) -> XAPMManager.get().setPermissionControlEnabled(isChecked));
        });

        boolean donateOrPlay = XAPMApplication.isPlayVersion() || AppSettings.isDonated(getContext());
        if (!donateOrPlay) {
            Objects.requireNonNull(tabLayout.getTabAt(INDEX_OPS)).setText(R.string.donated_available);
        }

    }

    protected void setSummaryView() {
        String who = getClass().getSimpleName();
        boolean showInfo = AppSettings.isShowInfoEnabled(this, who);
        TextView textView = findViewById(R.id.summary);
        if (!showInfo) {
            textView.setVisibility(View.GONE);
        } else {
            int normalColor = ContextCompat.getColor(getActivity(), R.color.white);
            int highlightColor = ContextCompat.getColor(getActivity(), R.color.amber);
            int strId = R.string.summary_perm_control;
            textView.setText(SpannableUtil.buildHighLightString(getActivity(), normalColor, highlightColor, strId));
            textView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_perm_viewer, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_info) {
            String who = getClass().getSimpleName();
            AppSettings.setShowInfo(this, who, !AppSettings.isShowInfoEnabled(this, who));
            setSummaryView();
        }
        if (item.getItemId() == R.id.action_search) {
            mSearchView.display();
            openKeyboard();
        }
        if (item.getItemId() == R.id.action_ops_template) {
            if (AppSettings.isDonated(getContext())
                    || XAPMApplication.isPlayVersion()) {
                AppOpsTemplateListActivity.start(getActivity());
            } else {
                Toast.makeText(getContext(), R.string.donated_available, Toast.LENGTH_SHORT).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private static final int INDEX_APPS = 0;
    private static final int INDEX_OPS = 1;
    private static final int TAB_COUNT = 2;

    private static final int FRAGMENT_COUNT = TAB_COUNT;
    private final List<OpsViewerFragment> mFragments = new ArrayList<>(FRAGMENT_COUNT);

    private void initPages() {
        mFragments.clear();
        mFragments.add(OpsViewerFragment.newInstance(INDEX_APPS));
        mFragments.add(OpsViewerFragment.newInstance(INDEX_OPS));
    }

    @NonNull
    @Override
    public ArrayList<CommonPackageInfo> findItem(String query, int page) {
        return mCurrentFragment.findItem(query, page);
    }

    @Override
    public void onItemClicked(CommonPackageInfo item) {
        super.onItemClicked(item);
        mCurrentFragment.onItemClicked(item);
    }

    private OpsViewerFragment mCurrentFragment;

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class OpsViewerFragment extends Fragment
            implements SearchViewResults.SearchPerformer<CommonPackageInfo>,
            onSimpleSearchActionsListener<CommonPackageInfo>,
            AdapterView.OnItemSelectedListener {

        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";


        private SwipeRefreshLayout swipeRefreshLayout;
        private CommonPackageInfoViewerAdapter commonPackageInfoViewerAdapter;

        private RecyclerView recyclerView;

        private int index;

        protected void startLoading() {
            swipeRefreshLayout.setRefreshing(true);
            XExecutor.execute(() -> {
                final List res = performLoading();
                if (getActivity() == null || isDetached()) return;
                getActivity().runOnUiThread(() -> {
                    swipeRefreshLayout.setRefreshing(false);
                    //noinspection unchecked
                    commonPackageInfoViewerAdapter.update(res);
                });
            });
        }

        public OpsViewerFragment() {
        }

        protected List performLoading() {
            switch (index) {
                case INDEX_APPS:
                    return ComponentLoader.Impl.create(getActivity()).loadInstalledApps(true, ComponentLoader.Sort.byName(), mFilterOption);
                case INDEX_OPS:
                    boolean donateOrPlay = XAPMApplication.isPlayVersion() || AppSettings.isDonated(getContext());
                    if (!donateOrPlay) return new ArrayList(0);
                    return PermissionLoader.Impl.create(getActivity())
                            .loadOps(mFilterOption);
            }
            return new ArrayList(0);
        }

        protected CommonPackageInfoViewerAdapter onCreateAdapter() {
            CommonPackageInfoViewerAdapter adapter =
                    new CommonPackageInfoViewerAdapter(getActivity()) {

                        @Override
                        protected boolean enableLongPressTriggerAllSelection() {
                            return false;
                        }

                        @Override
                        protected boolean onBuildPushSupportIndicator(CommonPackageInfo info, TextView textView) {
                            return false;
                        }

                        @Override
                        protected CommonViewHolder onCreateViewHolder(View root) {
                            return new OpsItemViewHolder(root);
                        }

                        @Override
                        public void onBindViewHolder(@NonNull CommonViewHolder holder, int position) {
                            super.onBindViewHolder(holder, position);

                            final CommonPackageInfo packageInfo = getCommonPackageInfos().get(position);

                            if (index == INDEX_OPS) {
                                holder.getLineTwoTextView().setText(packageInfo.getPayload()[0]);
                                if (getActivity() != null) {
                                    OpsItemViewHolder opsItemViewHolder = (OpsItemViewHolder) holder;
                                    opsItemViewHolder.getOpIconView().setImageResource(XAppOpsManagerRes
                                            .opToIconRes(packageInfo.getVersion()));
                                }
                            }
                            if (holder.getMoreBtn() != null) {
                                holder.getMoreBtn().setOnClickListener(v -> showPopMenu(packageInfo, v));
                            }
                        }

                        void showPopMenu(final CommonPackageInfo t, View anchor) {
                            PopupMenu popupMenu = new PopupMenu(getContext(), anchor);
                            popupMenu.inflate(getPopupMenuRes());
                            popupMenu.setOnMenuItemClickListener(onCreateOnMenuItemClickListener(t));
                            popupMenu.show();
                        }

                        int getPopupMenuRes() {
                            return R.menu.perm_ops_item;
                        }

                        PopupMenu.OnMenuItemClickListener onCreateOnMenuItemClickListener(final CommonPackageInfo t) {
                            if (index == INDEX_OPS) {
                                return item -> {
                                    OpLogViewerActivity.start(getContext(), null, t.getVersion());
                                    return true;
                                };
                            } else if (index == INDEX_APPS) {
                                return item -> {
                                    OpLogViewerActivity.start(getContext(), t.getPkgName(), -1);
                                    return true;
                                };
                            }
                            return null;
                        }

                        @Override
                        protected boolean imageLoadingEnabled() {
                            return index == INDEX_APPS;
                        }

                        @Override
                        protected int getTemplateLayoutRes() {
                            return index == INDEX_OPS ?
                                    R.layout.app_list_item_2_ops
                                    : R.layout.app_list_item_2_perm;
                        }
                    };

            adapter.setOnItemClickListener((parent, view, position, id) -> {
                CommonPackageInfo info = commonPackageInfoViewerAdapter.getCommonPackageInfos().get(position);
                if (index == INDEX_APPS) {
                    Apps2OpListActivity.start(getActivity(), info.getPkgName());
                } else if (index == INDEX_OPS) {
                    Op2AppsListActivity.start(getActivity(), info.getVersion(), true);
                }
            });
            return adapter;
        }

        private int indexOf(final CommonPackageInfo pkg) {
            return commonPackageInfoViewerAdapter.getCommonPackageInfos().indexOf(pkg);
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            this.index = getArguments().getInt(ARG_SECTION_NUMBER, -1);
            setHasOptionsMenu(true);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            inflater.inflate(R.menu.menu_fragment_perm_viewer, menu);
        }

        @Override
        public void onDetach() {
            super.onDetach();
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static OpsViewerFragment newInstance(int sectionNumber) {
            OpsViewerFragment fragment = new OpsViewerFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_perm_viewer, container, false);
            setupView(rootView);
            return rootView;
        }


        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            startLoading();
        }

        void setupView(View rootView) {
            recyclerView = rootView.findViewById(R.id.recycler_view);
            swipeRefreshLayout = rootView.findViewById(R.id.swipe);
            swipeRefreshLayout.setColorSchemeColors(getResources().getIntArray(R.array.polluted_waves));

            commonPackageInfoViewerAdapter = onCreateAdapter();

            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(),
                    LinearLayoutManager.VERTICAL, false));
            recyclerView.setAdapter(commonPackageInfoViewerAdapter);


            swipeRefreshLayout.setOnRefreshListener(this::startLoading);

            ViewGroup filterContainer = rootView.findViewById(R.id.apps_filter_spinner_container);
            onInitFilterSpinner(filterContainer);
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
            }
        }

        protected int getDefaultFilterSpinnerSelection() {
            return -1;
        }

        @NonNull
        @Override
        public ArrayList<CommonPackageInfo> findItem(String query, int page) {
            ArrayList<CommonPackageInfo> items = (ArrayList<CommonPackageInfo>)
                    commonPackageInfoViewerAdapter.getCommonPackageInfos();
            ArrayList<CommonPackageInfo> res = new ArrayList<>();
            for (CommonPackageInfo i : items) {
                if (i.getAppName().toLowerCase().contains(query.toLowerCase())) {
                    res.add(i);
                }
            }
            return res;
        }

        @Override
        public void onItemClicked(CommonPackageInfo item) {
            int index = indexOf(item);
            recyclerView.scrollToPosition(index);
            commonPackageInfoViewerAdapter.setSelection(index);
        }

        @Override
        public void onScroll() {

        }

        @Override
        public void error(String localizedMessage) {

        }

        private List<CommonPackageInfoListActivity.FilterOption> mFilterOptions;

        protected int mFilterOption = CommonPackageInfoListActivity.FilterOption.OPTION_3RD_APPS;

        protected SpinnerAdapter onCreateSpinnerAdapter(Spinner spinner) {
            if (getActivity() == null) return null;
            List<CommonPackageInfoListActivity.FilterOption> options = null;
            if (index == INDEX_APPS) {
                options = Lists.newArrayList(
                        new CommonPackageInfoListActivity.FilterOption(R.string.filter_third_party_apps,
                                CommonPackageInfoListActivity.FilterOption.OPTION_3RD_APPS),
                        new CommonPackageInfoListActivity.FilterOption(R.string.filter_system_apps,
                                CommonPackageInfoListActivity.FilterOption.OPTION_SYSTEM_APPS),
                        new CommonPackageInfoListActivity.FilterOption(R.string.filter_installed_apps,
                                CommonPackageInfoListActivity.FilterOption.OPTION_ALL_APPS));
            } else if (index == INDEX_OPS) {
                options = Lists.newArrayList(
                        new CommonPackageInfoListActivity.FilterOption(R.string.filter_ext_op,
                                CommonPackageInfoListActivity.FilterOption.OPTION_EXT_OP),
                        new CommonPackageInfoListActivity.FilterOption(R.string.filter_default_op,
                                CommonPackageInfoListActivity.FilterOption.OPTION_DEFAULT_OP),
                        new CommonPackageInfoListActivity.FilterOption(R.string.filter_all_op,
                                CommonPackageInfoListActivity.FilterOption.OPTION_ALL_OP));
            }
            mFilterOptions = options;
            if (options != null) {
                return new CommonPackageInfoListActivity.FilterSpinnerAdapter(getActivity(), options);
            } else {
                return null;
            }
        }

        protected AdapterView.OnItemSelectedListener onCreateSpinnerItemSelectListener() {
            return this;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Logger.d("onItemSelected: " + mFilterOptions.get(position));
            mFilterOption = mFilterOptions.get(position).getOption();
            startLoading();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a ComponentListFragment (defined as a static inner class below).
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return FRAGMENT_COUNT;
        }
    }

    @Getter
    static class OpsItemViewHolder extends CommonPackageInfoAdapter.CommonViewHolder {
        private ImageView opIconView;

        OpsItemViewHolder(View itemView) {
            super(itemView);
            opIconView = itemView.findViewById(R.id.op_icon_view);
        }
    }
}
