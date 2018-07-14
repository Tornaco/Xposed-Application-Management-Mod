package github.tornaco.xposedmoduletest.ui.activity.boot;

import android.support.v7.widget.SwitchCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.google.common.collect.Lists;

import org.newstand.logger.Logger;

import java.util.List;

import github.tornaco.android.common.Collections;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.loader.BootPackageLoader;
import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.ui.activity.BlockRecordViewerActivity;
import github.tornaco.xposedmoduletest.ui.activity.common.CommonPackageInfoListActivity;
import github.tornaco.xposedmoduletest.ui.adapter.common.CommonPackageInfoAdapter;
import github.tornaco.xposedmoduletest.ui.widget.SwitchBar;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;

public class BootAppNavActivity extends CommonPackageInfoListActivity implements SwitchBar.OnSwitchChangeListener, AdapterView.OnItemSelectedListener {

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
        warnIfSystemProtectedButSelected();
    }

    private void warnIfSystemProtectedButSelected() {
        if (mFilterOption == FilterOption.OPTION_SYSTEM_APPS || mFilterOption == FilterOption.OPTION_ALL_APPS) {
            if (XAPMManager.get().isWhiteSysAppEnabled()) {
                Toast.makeText(getActivity(), R.string.nav_list_warn_system_app_protected, Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    protected void onRequestClearItemsInBackground() {
        Collections.consumeRemaining(getCommonPackageInfoAdapter().getCommonPackageInfos(),
                commonPackageInfo -> {
                    if (commonPackageInfo.isChecked()) {
                        XAPMManager.get().addOrRemoveBootBlockApps(new String[]{commonPackageInfo.getPkgName()},
                                XAPMManager.Op.REMOVE);
                    }
                });
    }

    @Override
    protected void onInitSwitchBar(SwitchBar switchBar) {
        switchBar.show();
        switchBar.setChecked(XAPMManager.get().isBlockBlockEnabled());
        switchBar.addOnSwitchChangeListener(this);
    }

    @Override
    protected void onRequestPick() {
        BootAppPickerActivity.start(getActivity());
    }

    @Override
    protected int getSummaryRes() {
        return R.string.summary_boot_app;
    }

    @Override
    protected CommonPackageInfoAdapter onCreateAdapter() {
        return new CommonPackageInfoAdapter(this) {
            @Override
            protected void onItemClickNoneChoiceMode(CommonPackageInfo commonPackageInfo, View view) {
                super.onItemClickNoneChoiceMode(commonPackageInfo, view);
                showCommonItemPopMenu(commonPackageInfo, view);
            }
        };
    }

    @Override
    protected List<CommonPackageInfo> performLoading() {
        return BootPackageLoader.Impl.create(this).loadInstalled(mFilterOption, true);
    }

    @Override
    public void onSwitchChanged(SwitchCompat switchView, boolean isChecked) {
        XAPMManager.get().setBootBlockEnabled(isChecked);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.boot_block, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_block_record_viewer) {
            BlockRecordViewerActivity.start(this, null);
        }
        return super.onOptionsItemSelected(item);
    }
}