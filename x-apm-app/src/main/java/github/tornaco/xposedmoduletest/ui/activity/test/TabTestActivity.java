package github.tornaco.xposedmoduletest.ui.activity.test;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.TabLayout;

import java.util.List;

import github.tornaco.xposedmoduletest.ui.activity.common.MultipleTabListActivity;

/**
 * Created by Tornaco on 2018/6/20 9:59.
 * This file is writen for project X-APM at host guohao4.
 */
public class TabTestActivity extends MultipleTabListActivity {

    public static void start(Context context) {
        Intent starter = new Intent(context, TabTestActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onInitPages(List<TabListFragment> fragments) {
        super.onInitPages(fragments);
        fragments.add(new TabListFragment());
        fragments.add(new TabListFragment());
        fragments.add(new TabListFragment());
    }

    @Override
    protected void onSetupTabLayout(TabLayout tabLayout) {
        super.onSetupTabLayout(tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText("A"));
        tabLayout.addTab(tabLayout.newTab().setText("B"));
        tabLayout.addTab(tabLayout.newTab().setText("C"));
    }
}
