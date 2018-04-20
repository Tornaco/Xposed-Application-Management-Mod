package github.tornaco.xposedmoduletest.ui.activity.helper;

import android.os.RemoteException;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.google.common.collect.Lists;

import org.newstand.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.cache.RunningServicesLoadingCache;
import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.ui.activity.common.CommonPackageInfoListActivity;
import github.tornaco.xposedmoduletest.ui.adapter.common.CommonPackageInfoAdapter;
import github.tornaco.xposedmoduletest.xposed.app.IProcessClearListenerAdapter;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;

/**
 * Created by guohao4 on 2017/12/19.
 * Email: Tornaco@163.com
 */

public class RunningServicesActivity
        extends CommonPackageInfoListActivity implements AdapterView.OnItemSelectedListener {

    private int mClearedPackageNum = 0;

    @Override
    protected void initView() {
        super.initView();
        fab.setImageResource(R.drawable.ic_clear_all_black_24dp);
    }

    @Override
    protected void onFabClick() {
        super.onFabClick();
        if (XAshmanManager.get().isServiceAvailable()) {
            XAshmanManager.get().clearProcess(
                    new IProcessClearListenerAdapter() {
                        @Override
                        public void onPrepareClearing() throws RemoteException {
                            super.onPrepareClearing();
                            mClearedPackageNum = 0;
                        }

                        @Override
                        public void onClearedPkg(String pkg) throws RemoteException {
                            super.onClearedPkg(pkg);
                            mClearedPackageNum++;
                        }

                        @Override
                        public void onAllCleared(final String[] pkg) throws RemoteException {
                            super.onAllCleared(pkg);
                            if (!isDestroyed()) {
                                runOnUiThreadChecked(() -> {
                                    Toast.makeText(getApplicationContext(),
                                            R.string.clear_process_complete, Toast.LENGTH_LONG).show();
                                    startLoading();
                                });
                            }
                        }

                        @Override
                        public boolean doNotClearWhenIntervative() throws RemoteException {
                            return false;
                        }
                    });
        }
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
            startLoading();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected List<? extends CommonPackageInfo> performLoading() {
        if (isDestroyed()) return new ArrayList<>();
        RunningServicesLoadingCache.getInstance().refresh();
        List<RunningState.MergedItem> items = RunningServicesLoadingCache.getInstance()
                .getRunningServiceCache().getList();

        ArrayList<RunningServiceInfoDisplay> displays = new ArrayList<>();
        for (RunningState.MergedItem m : items) {


            // Apply filter.
            if (mFilterOption == FilterOption.OPTION_BACKGROUND_PROCESS && !m.mBackground) continue;
            if (mFilterOption == FilterOption.OPTION_RUNNING_PROCESS && m.mBackground) continue;

            RunningServiceInfoDisplay d = new RunningServiceInfoDisplay(this, m);
            d.setSystemApp(PkgUtil.isSystemApp(getApplicationContext(), d.getPkgName()));

            if (!mShowSystemApps && d.isSystemApp()) continue;

            displays.add(d);
        }

        // Update activity title.
        int count = displays.size();
        FilterOption filterOption = mFilterOptions.get(mFilterOptionIndex);
        runOnUiThreadChecked(() -> setTitle(getString(filterOption.getTitleRes()) + count));

        return displays;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    // Filter.

    private List<FilterOption> mFilterOptions;

    protected int mFilterOption = FilterOption.OPTION_MERGED_PROCESS;
    private int mFilterOptionIndex = 0;
    private boolean mShowSystemApps = false;

    @Override
    protected SpinnerAdapter onCreateSpinnerAdapter(Spinner spinner) {
        List<FilterOption> options = Lists.newArrayList(
                new FilterOption(R.string.filter_merged_process, FilterOption.OPTION_MERGED_PROCESS),
                new FilterOption(R.string.filter_background_process, FilterOption.OPTION_BACKGROUND_PROCESS),
                new FilterOption(R.string.filter_running_process, FilterOption.OPTION_RUNNING_PROCESS)
        );
        mFilterOptions = options;
        return new FilterSpinnerAdapter(getActivity(), options);
    }

    @Override
    protected AdapterView.OnItemSelectedListener onCreateSpinnerItemSelectListener() {
        return this;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Logger.d("onItemSelected: " + mFilterOptions.get(position));
        mFilterOption = mFilterOptions.get(position).getOption();
        mFilterOptionIndex = position;
        startLoading();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    protected boolean onBindFilterAction(RelativeLayout container) {
        CheckBox showSystemAppsCheckBox = new CheckBox(getActivity());
        showSystemAppsCheckBox.setText(R.string.title_show_system_app);
        showSystemAppsCheckBox.setSoundEffectsEnabled(false);
        showSystemAppsCheckBox.setChecked(mShowSystemApps);
        showSystemAppsCheckBox.setOnClickListener(v -> {
            mShowSystemApps = showSystemAppsCheckBox.isChecked();
            startLoading();
        });
        container.removeAllViews();
        container.addView(showSystemAppsCheckBox, generateCenterParams());
        return true;
    }

    protected RelativeLayout.LayoutParams generateCenterParams() {
        return generateCenterParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    protected RelativeLayout.LayoutParams generateCenterParams(int w, int h) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(w, h);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        return params;
    }
}
