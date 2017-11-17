package github.tornaco.xposedmoduletest.ui.activity.comp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.loader.ComponentLoader;
import github.tornaco.xposedmoduletest.ui.activity.BaseActivity;
import github.tornaco.xposedmoduletest.ui.adapter.ComponentListAdapter;
import github.tornaco.xposedmoduletest.ui.adapter.ReceiverSettingsAdapter;
import github.tornaco.xposedmoduletest.ui.adapter.ServiceSettingsAdapter;
import github.tornaco.xposedmoduletest.util.XExecutor;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;
import lombok.Getter;

public class ComponentEditorActivity extends BaseActivity {

    private static final String EXTRA_PKG = "ce_extra_pkg";

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

    private void initPages() {
        mFragments.clear();
        mFragments.add(ServiceListFragment.newInstance(mPackageName));
        mFragments.add(ReceiverListFragment.newInstance(mPackageName));
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private static final int FRAGMENT_COUNT = 2;
    private final List<ComponentListFragment> mFragments = new ArrayList<>(FRAGMENT_COUNT);

    public static class ReceiverListFragment extends ComponentListFragment {

        public static ReceiverListFragment newInstance(String pkg) {
            ReceiverListFragment fragment = new ReceiverListFragment();
            Bundle bundle = new Bundle(1);
            bundle.putString(EXTRA_PKG, pkg);
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        protected List performLoading() {
            return ComponentLoader.Impl.create(getActivity().getApplicationContext())
                    .loadReceiverSettings(getTargePpackageName());
        }

        @Override
        protected ComponentListAdapter onCreateAdapter() {
            return new ReceiverSettingsAdapter(getActivity());
        }
    }

    public static class ServiceListFragment extends ComponentListFragment {

        public static ServiceListFragment newInstance(String pkg) {
            ServiceListFragment fragment = new ServiceListFragment();
            Bundle bundle = new Bundle(1);
            bundle.putString(EXTRA_PKG, pkg);
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        protected List performLoading() {
            return ComponentLoader.Impl.create(getActivity().getApplicationContext())
                    .loadServiceSettings(getTargePpackageName());
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

        private String targePpackageName;

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
            this.targePpackageName = getArguments().getString(EXTRA_PKG);
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
