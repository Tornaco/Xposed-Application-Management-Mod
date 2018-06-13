package github.tornaco.xposedmoduletest.ui.activity.helper;

import android.support.v7.widget.AppCompatRadioButton;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.google.common.collect.Lists;

import org.newstand.logger.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import github.tornaco.android.common.Collections;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.cache.RunningServicesLoadingCache;
import github.tornaco.xposedmoduletest.loader.LoaderUtil;
import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.provider.AppSettings;
import github.tornaco.xposedmoduletest.ui.activity.common.CommonPackageInfoListActivity;
import github.tornaco.xposedmoduletest.ui.adapter.common.CommonPackageInfoAdapter;
import github.tornaco.xposedmoduletest.util.ArrayUtil;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.service.ErrorCatchRunnable;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;

/**
 * Created by guohao4 on 2017/12/19.
 * Email: Tornaco@163.com
 */

public class RunningServicesActivity
        extends CommonPackageInfoListActivity
        implements AdapterView.OnItemSelectedListener {

    @Override
    protected void initView() {
        super.initView();
        fab.setImageResource(R.drawable.ic_clear_all_black_24dp);
        fab.hide();
    }

    @Override
    protected boolean reloadOnResume() {
        return false;
    }

    @Override
    protected void onRequestClearItemsInBackground() {
        super.onRequestClearItemsInBackground();
        Set<String> candidates = new HashSet<>(getCommonPackageInfoAdapter().getItemCount());
        Collections.consumeRemaining(getCommonPackageInfoAdapter().getCommonPackageInfos(),
                commonPackageInfo -> {
                    if (commonPackageInfo.isChecked()) {
                        candidates.add(commonPackageInfo.getPkgName());
                    }
                });

        if (candidates.size() > 0) {
            XAPMManager.get().forceIdlePackages(ArrayUtil.convertObjectArrayToStringArray(candidates.toArray()));

            // Wait a while?
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {

            }
        }
    }

    @Override
    public void onEnterChoiceMode() {
        swipeRefreshLayout.setEnabled(false);
        fab.show();
    }

    @Override
    public void onLeaveChoiceMode() {
        swipeRefreshLayout.setEnabled(true);
        fab.hide();
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

        new ErrorCatchRunnable(() -> {
            LoaderUtil.commonSort(displays);
        }, "Sort RunningState").run();

        return displays;
    }

    // Filter.

    private List<FilterOption> mFilterOptions;

    protected int mFilterOption = FilterOption.OPTION_MERGED_PROCESS;
    private int mFilterOptionIndex = 0;
    private boolean mShowSystemApps = false;

    @Override
    protected void onInitFilterSpinner(ViewGroup filterContainer) {
        // Read option first.
        mFilterOption = AppSettings.getFilterOptions(getContext(), getClass().getName(), FilterOption.OPTION_ALL_APPS);
        // Fix.
        if (mFilterOption > FilterOption.OPTION_RUNNING_PROCESS) { // No more larger than this!!!
            mFilterOption = FilterOption.OPTION_MERGED_PROCESS;
            AppSettings.setFilterOptions(getContext(), getClass().getName(), mFilterOption);
        }
        Logger.i("onInitFilterSpinner: %s", mFilterOption);

        super.onInitFilterSpinner(filterContainer);
    }

    @Override
    protected int getDefaultFilterSpinnerSelection(SpinnerAdapter adapter) {
        FilterSpinnerAdapter filterSpinnerAdapter = (FilterSpinnerAdapter) adapter;
        return filterSpinnerAdapter.getIndex(mFilterOption);
    }

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
        // Save options.
        AppSettings.setFilterOptions(getContext(), getClass().getName(), mFilterOption);
        startLoading();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    protected boolean onBindFilterAction(RelativeLayout container) {
        AppCompatRadioButton showSystemAppsCheckBox = new AppCompatRadioButton(getActivity());
        showSystemAppsCheckBox.setText(R.string.title_show_system_app);
        showSystemAppsCheckBox.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        showSystemAppsCheckBox.setSoundEffectsEnabled(false);
        showSystemAppsCheckBox.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        showSystemAppsCheckBox.setChecked(mShowSystemApps);
        showSystemAppsCheckBox.setOnClickListener(v -> {
            showSystemAppsCheckBox.setChecked(!mShowSystemApps);
            mShowSystemApps = showSystemAppsCheckBox.isChecked();
            startLoading();
        });
        container.removeAllViews();
        container.addView(showSystemAppsCheckBox, generateCenterParams());
        return true;
    }

}
