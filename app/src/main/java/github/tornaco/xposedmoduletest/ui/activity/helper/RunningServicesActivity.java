package github.tornaco.xposedmoduletest.ui.activity.helper;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.ui.activity.common.CommonPackageInfoListActivity;
import github.tornaco.xposedmoduletest.ui.adapter.common.CommonPackageInfoAdapter;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;

/**
 * Created by guohao4 on 2017/12/19.
 * Email: Tornaco@163.com
 */

public class RunningServicesActivity
        extends CommonPackageInfoListActivity {

    private RunningState mState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mState = RunningState.getInstance(getApplicationContext());
        super.onCreate(savedInstanceState);
        fab.hide();
    }

    @Override
    public void onResume() {
        super.onResume();
        // mState.resume();
    }

    @Override
    protected int getSummaryRes() {
        return 0;
    }

    @Override
    protected CommonPackageInfoAdapter onCreateAdapter() {
        return new RunningServiceAdapter(this);
    }

    private boolean mShowCache;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // getMenuInflater().inflate(R.menu.running_services, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_change_cached) {
            mShowCache = !mShowCache;
        }
        startLoading();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected List<? extends CommonPackageInfo> performLoading() {
        if (isDestroyed()) return new ArrayList<>();
        mState.updateNow();
        ArrayList<RunningState.MergedItem> items =
                mShowCache ?
                        mState.getCurrentBackgroundItems()
                        : mState.getCurrentMergedItems();
        ArrayList<RunningServiceInfoDisplay> displays = new ArrayList<>();
        for (RunningState.MergedItem m : items) {
            RunningServiceInfoDisplay d = new RunningServiceInfoDisplay(m);
            if (BuildConfig.APPLICATION_ID.equals(d.getPkgName())) continue;
            d.setSystemApp(PkgUtil.isSystemApp(getApplicationContext(), d.getPkgName()));
            displays.add(d);
        }
        return displays;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mState.reset();
    }
}
