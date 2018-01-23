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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.shahroz.svlibrary.interfaces.onSimpleSearchActionsListener;
import com.shahroz.svlibrary.widgets.SearchViewResults;

import java.util.ArrayList;
import java.util.List;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.loader.ComponentLoader;
import github.tornaco.xposedmoduletest.loader.PermissionLoader;
import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.provider.AppSettings;
import github.tornaco.xposedmoduletest.ui.activity.WithSearchActivity;
import github.tornaco.xposedmoduletest.ui.adapter.common.CommonPackageInfoViewerAdapter;
import github.tornaco.xposedmoduletest.ui.widget.SwitchBar;
import github.tornaco.xposedmoduletest.util.SpannableUtil;
import github.tornaco.xposedmoduletest.util.XExecutor;
import github.tornaco.xposedmoduletest.xposed.XApp;

public class PermViewerActivity extends WithSearchActivity<CommonPackageInfo> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.permission_viewer);

        setupToolbar();
        showHomeAsUp();

        setupViews();

        initPages();
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

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SwitchBar switchBar = findViewById(R.id.switchbar);
                if (switchBar == null) return;
                switchBar.hide();
            }
        });

        boolean donateOrPlay = XApp.isPlayVersion() || AppSettings.isDonated(getContext());
        if (!donateOrPlay) {
            tabLayout.getTabAt(INDEX_OPS).setText(R.string.donated_available);
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
            onSimpleSearchActionsListener<CommonPackageInfo> {

        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";


        private SwipeRefreshLayout swipeRefreshLayout;
        private CommonPackageInfoViewerAdapter commonPackageInfoViewerAdapter;

        private RecyclerView recyclerView;

        private int index;

        protected boolean mShowSystemApp = false;

        protected void startLoading() {
            swipeRefreshLayout.setRefreshing(true);
            XExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    final List res = performLoading();
                    if (getActivity() == null || isDetached()) return;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            swipeRefreshLayout.setRefreshing(false);
                            //noinspection unchecked
                            commonPackageInfoViewerAdapter.update(res);
                        }
                    });
                }
            });
        }

        public OpsViewerFragment() {
        }

        protected List performLoading() {
            switch (index) {
                case INDEX_APPS:
                    return ComponentLoader.Impl.create(getActivity()).loadInstalledApps(mShowSystemApp,
                            ComponentLoader.Sort.byName());
                case INDEX_OPS:
                    boolean donateOrPlay = XApp.isPlayVersion() || AppSettings.isDonated(getContext());
                    if (!donateOrPlay) return new ArrayList(0);
                    return PermissionLoader.Impl.create(getActivity())
                            .loadOps(0);
            }
            return new ArrayList(0);
        }

        protected CommonPackageInfoViewerAdapter onCreateAdapter() {
            CommonPackageInfoViewerAdapter adapter = new CommonPackageInfoViewerAdapter(getActivity()) {
                @Override
                public void onBindViewHolder(CommonViewHolder holder, int position) {
                    super.onBindViewHolder(holder, position);

                    if (index == INDEX_OPS) {
                        final CommonPackageInfo packageInfo = getCommonPackageInfos().get(position);
                        holder.getLineTwoTextView().setText(packageInfo.getPayload()[0]);
                        if (getActivity() != null) {
                            holder.getCheckableImageView().setImageDrawable(ContextCompat
                                    .getDrawable(getActivity(), github.tornaco.xposedmoduletest.compat.os.AppOpsManagerCompat
                                            .opToIconRes(packageInfo.getVersion())));
                        }
                    }
                }

                @Override
                protected boolean imageLoadingEnabled() {
                    return index == INDEX_APPS;
                }

                @Override
                protected int getTemplateLayoutRes() {
                    return index == INDEX_OPS ?
                            R.layout.app_list_item_2_ops
                            : super.getTemplateLayoutRes();
                }
            };

            adapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    CommonPackageInfo info = commonPackageInfoViewerAdapter.getCommonPackageInfos().get(position);
                    if (index == INDEX_APPS) {
                        Apps2OpListActivity.start(getActivity(), info.getPkgName());
                    } else if (index == INDEX_OPS) {
                        Op2AppsListActivity.start(getActivity(), info.getVersion(), mShowSystemApp);
                    }
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
        public void onPrepareOptionsMenu(Menu menu) {
            super.onPrepareOptionsMenu(menu);
            menu.findItem(R.id.show_system_app).setChecked(mShowSystemApp);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            if (item.getItemId() == R.id.show_system_app) {
                mShowSystemApp = !mShowSystemApp;
                getActivity().invalidateOptionsMenu();
                startLoading();
            }
            return super.onOptionsItemSelected(item);
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
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
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


            swipeRefreshLayout.setOnRefreshListener(
                    new SwipeRefreshLayout.OnRefreshListener() {
                        @Override
                        public void onRefresh() {
                            startLoading();
                        }
                    });
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
}
