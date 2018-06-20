package github.tornaco.xposedmoduletest.ui.activity.common;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import java.util.ArrayList;
import java.util.List;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.activity.BaseActivity;
import github.tornaco.xposedmoduletest.ui.widget.SwitchBar;

/**
 * Created by Tornaco on 2018/6/20 9:46.
 * This file is writen for project X-APM at host guohao4.
 */
@SuppressLint("Registered")
public class MultipleTabListActivity extends BaseActivity {

    private final List<TabListFragment> mFragments = new ArrayList<>();
    private Fragment mCurrentFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_with_tab_list);
        setupToolbar();
        showHomeAsUp();
        initPages();
        setupViews();
    }

    private void setupViews() {
        PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager());

        ViewPager viewPager = findViewById(R.id.container);
        viewPager.setAdapter(pagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);

        onSetupTabLayout(tabLayout);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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

        runOnUiThread(() -> {
            SwitchBar switchBar = findViewById(R.id.switchbar);
            if (switchBar == null) return;
            switchBar.hide();
        });
    }

    protected void onSetupTabLayout(TabLayout tabLayout) {

    }

    private void initPages() {
        mFragments.clear();
        onInitPages(mFragments);
    }

    protected void onInitPages(List<TabListFragment> fragments) {

    }

    public class PagerAdapter extends FragmentPagerAdapter {

        PagerAdapter(FragmentManager fm) {
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
            return mFragments.size();
        }
    }

    public static class TabListFragment extends Fragment {

    }
}
