package github.tornaco.xposedmoduletest.ui.activity.comp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

import github.tornaco.android.common.util.ColorUtil;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.loader.ComponentLoader;
import github.tornaco.xposedmoduletest.loader.PaletteColorPicker;
import github.tornaco.xposedmoduletest.provider.XSettings;
import github.tornaco.xposedmoduletest.ui.activity.BaseActivity;
import github.tornaco.xposedmoduletest.ui.adapter.ComponentListAdapter;
import github.tornaco.xposedmoduletest.ui.adapter.ReceiverSettingsAdapter;
import github.tornaco.xposedmoduletest.ui.adapter.ServiceSettingsAdapter;
import github.tornaco.xposedmoduletest.util.XExecutor;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;
import lombok.Getter;

public class ComponentEditorActivity extends BaseActivity implements LoadingListener {

    private static final String EXTRA_PKG = "ce_extra_pkg";
    private static final String EXTRA_INDEX = "ce_extra_index";

    public static void start(Context context, String pkg) {
        Intent intent = new Intent(context, ComponentEditorActivity.class);
        intent.putExtra(EXTRA_PKG, pkg);
        context.startActivity(intent);
    }

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private String mPackageName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.component_editor);

        setupToolbar();
        showHomeAsUp();

        if (!internalResolveIntent()) {
            finish();
            return;
        }
        initView();

        initColor();

        initPages();
    }

    private void initView() {
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        // Post this fucking work, do not block ui work.
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setTitle(PkgUtil.loadNameByPkgName(getApplicationContext(), mPackageName));
            }
        });
    }

    private static final int INDEX_SERVICE = 0;
    private static final int INDEX_BROADCAST = 1;

    @SuppressWarnings("ConstantConditions")
    void setTabTitle(int index, String title) {
        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.getTabAt(index).setText(title);
    }

    private void initPages() {
        mFragments.clear();
        mFragments.add(ServiceListFragment.newInstance(mPackageName, 0));
        mFragments.add(ReceiverListFragment.newInstance(mPackageName, 1));
    }

    private void initColor() {
        // Apply theme color.
        int color = ContextCompat.getColor(this, XSettings.getThemes(this).getThemeColorRes());

        // Apply palette color.
        PaletteColorPicker.pickPrimaryColor(this, new PaletteColorPicker.PickReceiver() {
            @Override
            public void onColorReady(int color) {
                applyColor(color);
            }
        }, mPackageName, color);
    }

    @SuppressWarnings("ConstantConditions")
    private void applyColor(int color) {
        AppBarLayout appBar = findViewById(R.id.appbar);
        if (appBar != null) appBar.setBackgroundColor(color);
        getWindow().setStatusBarColor(ColorUtil.colorBurn(color));
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setBackgroundColor(color);
        }
    }

    private boolean internalResolveIntent() {
        Intent intent = getIntent();
        if (intent == null) return false;
        mPackageName = intent.getStringExtra(EXTRA_PKG);
        return !TextUtils.isEmpty(mPackageName);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.component_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_export_service_settings) {
            showSimpleDialog(null, ComponentLoader.Impl.create(this).formatServiceSettings(mPackageName));
            return true;
        }

        if (id == R.id.action_export_broadcast_settings) {
            showSimpleDialog(null, ComponentLoader.Impl.create(this).formatReceiverSettings(mPackageName));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private static final int FRAGMENT_COUNT = 2;
    private final List<ComponentListFragment> mFragments = new ArrayList<>(FRAGMENT_COUNT);

    @Override
    public void onLoadingComplete(int index, List data) {
        setTabTitle(index, index == INDEX_SERVICE ?
                getString(R.string.tab_text_1) + "[" + data.size() + "]"
                : getString(R.string.tab_text_2) + "[" + data.size() + "]");
    }

    public static class ReceiverListFragment extends ComponentListFragment {

        public static ReceiverListFragment newInstance(String pkg, int index) {
            ReceiverListFragment fragment = new ReceiverListFragment();
            Bundle bundle = new Bundle(2);
            bundle.putString(EXTRA_PKG, pkg);
            bundle.putInt(EXTRA_INDEX, index);
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        protected List performLoading() {
            return ComponentLoader.Impl.create(getActivity().getApplicationContext())
                    .loadReceiverSettings(getTargetPackageName());
        }

        @Override
        protected ComponentListAdapter onCreateAdapter() {
            return new ReceiverSettingsAdapter(getActivity());
        }
    }

    public static class ServiceListFragment extends ComponentListFragment {

        public static ServiceListFragment newInstance(String pkg, int index) {
            ServiceListFragment fragment = new ServiceListFragment();
            Bundle bundle = new Bundle(2);
            bundle.putString(EXTRA_PKG, pkg);
            bundle.putInt(EXTRA_INDEX, index);
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        protected List performLoading() {
            return ComponentLoader.Impl.create(getActivity().getApplicationContext())
                    .loadServiceSettings(getTargetPackageName());
        }

        @Override
        protected ServiceSettingsAdapter onCreateAdapter() {
            return new ServiceSettingsAdapter(getActivity());
        }
    }

    @Getter
    public static class ComponentListFragment extends Fragment {

        private SwipeRefreshLayout swipeRefreshLayout;
        private ComponentListAdapter componentListAdapter;

        private String targetPackageName;
        private int index;

        @Getter
        private LoadingListener loadingListener;

        public ComponentListFragment() {
        }

        protected void startLoading() {
            swipeRefreshLayout.setRefreshing(true);
            XExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    final List res = performLoading();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            swipeRefreshLayout.setRefreshing(false);
                            //noinspection unchecked
                            componentListAdapter.update(res);
                            loadingListener.onLoadingComplete(index, res);
                        }
                    });
                }
            });
        }

        protected List performLoading() {
            return Lists.newArrayListWithCapacity(0);
        }

        protected ComponentListAdapter onCreateAdapter() {
            return null;
        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            this.targetPackageName = getArguments().getString(EXTRA_PKG);
            this.index = getArguments().getInt(EXTRA_INDEX, -1);
            this.loadingListener = (LoadingListener) getActivity();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.component_list, container, false);
            setupView(rootView);
            return rootView;
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            startLoading();
        }

        void setupView(View rootView) {
            RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view);
            swipeRefreshLayout = rootView.findViewById(R.id.swipe);
            swipeRefreshLayout.setColorSchemeColors(getResources().getIntArray(R.array.polluted_waves));

            componentListAdapter = onCreateAdapter();
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(),
                    LinearLayoutManager.VERTICAL, false));
            recyclerView.setAdapter(componentListAdapter);


            swipeRefreshLayout.setOnRefreshListener(
                    new SwipeRefreshLayout.OnRefreshListener() {
                        @Override
                        public void onRefresh() {
                            startLoading();
                        }
                    });
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

interface LoadingListener {
    void onLoadingComplete(int tab, List data);
}