package github.tornaco.xposedmoduletest.ui.activity.ag;

import android.content.Intent;
import android.support.v7.widget.SwitchCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.google.common.collect.Lists;

import org.newstand.logger.Logger;

import java.util.List;

import github.tornaco.android.common.Collections;
import github.tornaco.android.common.Consumer;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.loader.LockPackageLoader;
import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.ui.activity.common.CommonPackageInfoListActivity;
import github.tornaco.xposedmoduletest.ui.adapter.common.CommonPackageInfoAdapter;
import github.tornaco.xposedmoduletest.ui.widget.SwitchBar;
import github.tornaco.xposedmoduletest.xposed.app.XAppLockManager;

public class GuardAppNavActivity extends CommonPackageInfoListActivity
        implements SwitchBar.OnSwitchChangeListener, AdapterView.OnItemSelectedListener {

    @Override
    protected boolean isLockNeeded() {
        return true;
    }

    @Override
    protected void onInitSwitchBar(SwitchBar switchBar) {
        switchBar.show();
        switchBar.setChecked(XAppLockManager.get().isServiceAvailable() && XAppLockManager.get().isEnabled());
        switchBar.addOnSwitchChangeListener(this);
    }

    private List<FilterOption> mFilterOptions;


    protected int mFilterOption = FilterOption.OPTION_ALL_APPS;

    @Override
    protected SpinnerAdapter onCreateSpinnerAdapter(Spinner spinner) {
        List<FilterOption> options = Lists.newArrayList(
                new FilterOption(R.string.filter_installed_apps, FilterOption.OPTION_ALL_APPS),
                new FilterOption(R.string.filter_third_party_apps, FilterOption.OPTION_3RD_APPS),
                new FilterOption(R.string.filter_system_apps, FilterOption.OPTION_SYSTEM_APPS)
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
        startLoading();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    protected void showSwitchBarIntro(SwitchBar switchBar) {
        super.showSwitchBarIntro(switchBar);
    }

    @Override
    protected void showFilterSpinnerIntro(Spinner spinner) {
        super.showFilterSpinnerIntro(spinner);
    }

    @Override
    protected void onRequestClearItemsInBackground() {
        Collections.consumeRemaining(getCommonPackageInfoAdapter().getCommonPackageInfos(),
                new Consumer<CommonPackageInfo>() {
                    @Override
                    public void accept(CommonPackageInfo commonPackageInfo) {
                        if (commonPackageInfo.isChecked()) {
                            XAppLockManager.get().addOrRemoveLockApps(new String[]{commonPackageInfo.getPkgName()}, false);
                        }
                    }
                });
    }

    @Override
    protected void onRequestPick() {
        GuardAppPickerActivity.start(getActivity());
    }

    @Override
    protected int getSummaryRes() {
        return R.string.summary_app_guard;
    }

    @Override
    protected CommonPackageInfoAdapter onCreateAdapter() {
        return new CommonPackageInfoAdapter(this){
            @Override
            protected void onItemClickNoneChoiceMode(CommonPackageInfo commonPackageInfo, View view) {
                super.onItemClickNoneChoiceMode(commonPackageInfo, view);
                showCommonItemPopMenu(commonPackageInfo, view);
            }
        };
    }

    @Override
    protected List<CommonPackageInfo> performLoading() {
        return LockPackageLoader.Impl.create(this).loadInstalled(mFilterOption, true);
    }

    @Override
    public void onSwitchChanged(SwitchCompat switchView, boolean isChecked) {
        if (XAppLockManager.get().isServiceAvailable()) {
            XAppLockManager.get().setEnabled(isChecked);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.guard_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, GuardSettingsDashboardActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

}
