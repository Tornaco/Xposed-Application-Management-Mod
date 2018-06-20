package github.tornaco.xposedmoduletest.ui.activity.nf;

import android.content.Context;
import android.content.DialogInterface;
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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.google.common.collect.Lists;
import com.shahroz.svlibrary.interfaces.onSimpleSearchActionsListener;
import com.shahroz.svlibrary.widgets.SearchViewResults;

import org.newstand.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import github.tornaco.android.common.Collections;
import github.tornaco.android.common.Consumer;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.loader.NetworkRestrictLoader;
import github.tornaco.xposedmoduletest.model.NetworkRestrictionItem;
import github.tornaco.xposedmoduletest.provider.AppSettings;
import github.tornaco.xposedmoduletest.ui.activity.WithSearchActivity;
import github.tornaco.xposedmoduletest.ui.activity.common.CommonPackageInfoListActivity;
import github.tornaco.xposedmoduletest.ui.adapter.NetworkRestrictListAdapter;
import github.tornaco.xposedmoduletest.ui.widget.SwitchBar;
import github.tornaco.xposedmoduletest.util.SpannableUtil;
import github.tornaco.xposedmoduletest.util.XExecutor;

public class NetworkRestrictActivity extends WithSearchActivity<NetworkRestrictionItem>
        implements AdapterView.OnItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.network_restrict);

        setupToolbar();
        showHomeAsUp();

        setupViews();

        initPages();
    }


    private void setupViews() {
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        /*
      The {@link ViewPager} that will host the section contents.
     */
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

        setTitle(R.string.title_nf);

        setSummaryView();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SwitchBar switchBar = findViewById(R.id.switchbar);
                if (switchBar == null) return;
                switchBar.hide();
            }
        });

        // showAlertDialog();

        ViewGroup filterContainer = findViewById(R.id.apps_filter_spinner_container);
        onInitFilterSpinner(filterContainer);
    }

    private void showAlertDialog() {
        int normalColor = ContextCompat.getColor(getActivity(), R.color.white);
        int highlightColor = ContextCompat.getColor(getActivity(), R.color.amber);
        int strId = R.string.summary_nf;
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.title_nf_warn)
                .setMessage(SpannableUtil.buildHighLightString(getActivity(), normalColor, highlightColor, strId))
                .setCancelable(false)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setPositiveButton(android.R.string.ok, null)
                .show();
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
            int strId = R.string.summary_nf;
            textView.setText(SpannableUtil.buildHighLightString(getActivity(), normalColor, highlightColor, strId));
            textView.setVisibility(View.VISIBLE);
        }
    }

    private List<CommonPackageInfoListActivity.FilterOption> mFilterOptions;

    protected int mFilterOption = CommonPackageInfoListActivity.FilterOption.OPTION_ALL_APPS;

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

    protected SpinnerAdapter onCreateSpinnerAdapter(Spinner spinner) {
        List<CommonPackageInfoListActivity.FilterOption> options = Lists.newArrayList(
                new CommonPackageInfoListActivity.FilterOption(R.string.filter_installed_apps, CommonPackageInfoListActivity.FilterOption.OPTION_ALL_APPS),
                new CommonPackageInfoListActivity.FilterOption(R.string.filter_third_party_apps, CommonPackageInfoListActivity.FilterOption.OPTION_3RD_APPS),
                new CommonPackageInfoListActivity.FilterOption(R.string.filter_system_apps, CommonPackageInfoListActivity.FilterOption.OPTION_SYSTEM_APPS)
        );
        mFilterOptions = options;
        return new CommonPackageInfoListActivity.FilterSpinnerAdapter(getActivity(), options);
    }

    protected AdapterView.OnItemSelectedListener onCreateSpinnerItemSelectListener() {
        return this;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Logger.d("onItemSelected: " + mFilterOptions.get(position));
        mFilterOption = mFilterOptions.get(position).getOption();
        Collections.consumeRemaining(mFragments, new Consumer<RestrictAppListFragment>() {
            @Override
            public void accept(RestrictAppListFragment restrictAppListFragment) {
                restrictAppListFragment.startLoading();
            }
        });
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_network_restrict, menu);
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

    private static final int INDEX_DATA = 0;
    private static final int INDEX_WIFI = 1;
    private static final int TAB_COUNT = 2;

    private static final int FRAGMENT_COUNT = TAB_COUNT;
    private final List<RestrictAppListFragment> mFragments = new ArrayList<>(FRAGMENT_COUNT);

    private void initPages() {
        mFragments.clear();
        mFragments.add(RestrictAppListFragment.newInstance(INDEX_DATA));
        mFragments.add(RestrictAppListFragment.newInstance(INDEX_WIFI));
    }

    @NonNull
    @Override
    public ArrayList<NetworkRestrictionItem> findItem(String query, int page) {
        return mCurrentFragment.findItem(query, page);
    }

    @Override
    public void onItemClicked(NetworkRestrictionItem item) {
        super.onItemClicked(item);
        mCurrentFragment.onItemClicked(item);
    }

    private RestrictAppListFragment mCurrentFragment;

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class RestrictAppListFragment extends Fragment
            implements SearchViewResults.SearchPerformer<NetworkRestrictionItem>,
            onSimpleSearchActionsListener<NetworkRestrictionItem> {

        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";


        private SwipeRefreshLayout swipeRefreshLayout;
        private NetworkRestrictListAdapter networkRestrictListAdapter;

        private RecyclerView recyclerView;

        private int index;

        protected boolean mShowSystemApp = true;

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
                            networkRestrictListAdapter.update(res);
                        }
                    });
                }
            });
        }

        public RestrictAppListFragment() {
        }

        protected List performLoading() {
            NetworkRestrictActivity activity = (NetworkRestrictActivity) getActivity();
            if (activity == null) return new ArrayList(0);
            return NetworkRestrictLoader.Impl.create(getActivity()).loadAll(activity.mFilterOption, mShowSystemApp);
        }

        protected NetworkRestrictListAdapter onCreateAdapter() {
            return new NetworkRestrictListAdapter(getActivity());
        }

        private int indexOf(final NetworkRestrictionItem pkg) {
            return networkRestrictListAdapter.getNetworkRestrictionItems().indexOf(pkg);
        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            this.index = getArguments().getInt(ARG_SECTION_NUMBER, -1);
            setHasOptionsMenu(true);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            inflater.inflate(R.menu.menu_fragment_network_restrict, menu);
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
        public static RestrictAppListFragment newInstance(int sectionNumber) {
            RestrictAppListFragment fragment = new RestrictAppListFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_network_restrict, container, false);
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

            networkRestrictListAdapter = onCreateAdapter();
            networkRestrictListAdapter.setRestrictWifi(index == INDEX_WIFI);
            networkRestrictListAdapter.setRestrictData(index == INDEX_DATA);

            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(),
                    LinearLayoutManager.VERTICAL, false));
            recyclerView.setAdapter(networkRestrictListAdapter);


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
        public ArrayList<NetworkRestrictionItem> findItem(String query, int page) {
            ArrayList<NetworkRestrictionItem> items = (ArrayList<NetworkRestrictionItem>)
                    networkRestrictListAdapter.getNetworkRestrictionItems();
            ArrayList<NetworkRestrictionItem> res = new ArrayList<>();
            for (NetworkRestrictionItem i : items) {
                if (i.getAppName().toLowerCase().contains(query.toLowerCase())) {
                    res.add(i);
                }
            }
            return res;
        }

        @Override
        public void onItemClicked(NetworkRestrictionItem item) {
            int index = indexOf(item);
            recyclerView.scrollToPosition(index);
            networkRestrictListAdapter.setSelection(index);
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
