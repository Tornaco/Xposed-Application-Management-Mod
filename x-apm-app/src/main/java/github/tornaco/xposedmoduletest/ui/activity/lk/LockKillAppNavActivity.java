package github.tornaco.xposedmoduletest.ui.activity.lk;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
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
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.compat.pm.PackageManagerCompat;
import github.tornaco.xposedmoduletest.loader.LockKillPackageLoader;
import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.provider.AppSettings;
import github.tornaco.xposedmoduletest.ui.activity.common.CommonPackageInfoListActivity;
import github.tornaco.xposedmoduletest.ui.adapter.common.CommonPackageInfoAdapter;
import github.tornaco.xposedmoduletest.ui.widget.SwitchBar;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;

public class LockKillAppNavActivity extends CommonPackageInfoListActivity implements SwitchBar.OnSwitchChangeListener, AdapterView.OnItemSelectedListener {

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
    protected void onRequestClearItemsInBackground() {
        Collections.consumeRemaining(getCommonPackageInfoAdapter().getCommonPackageInfos(),
                commonPackageInfo -> {
                    if (commonPackageInfo.isChecked()) {
                        XAPMManager.get().addOrRemoveLKApps(new String[]{commonPackageInfo.getPkgName()},
                                XAPMManager.Op.REMOVE);
                    }
                });
    }

    @Override
    protected void onInitSwitchBar(SwitchBar switchBar) {
        switchBar.setOnRes(R.string.summary_func_lk_enabled);
        switchBar.setOffRes(R.string.summary_func_lk_disabled);
        switchBar.show();
        switchBar.setChecked(XAPMManager.get().isLockKillEnabled());
        switchBar.addOnSwitchChangeListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initLKUEDialog();
    }

    private void initLKUEDialog() {
        if (AppSettings.isShowInfoEnabled(getContext(), "lk_ue_tip", true)) {
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.title_app_lk_tip)
                    .setMessage(getString(R.string.message_app_lk_tip))
                    .setCancelable(false)
                    .setNeutralButton(R.string.no_remind, (dialog, which) -> AppSettings.setShowInfo(getApplicationContext(), "lk_ue_tip", false))
                    .setPositiveButton(android.R.string.ok, null)
                    .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                        finishAffinity();
                        PackageManagerCompat.unInstallUserAppWithIntent(getContext(), getPackageName());
                    })
                    .show();

        }
    }

    @Override
    protected void onRequestPick() {
        LockKillAppPickerActivity.start(getActivity());
    }

    @Override
    protected int getSummaryRes() {
        return R.string.summary_lock_kill_app;
    }

    @Override
    protected CommonPackageInfoAdapter onCreateAdapter() {
        return new CommonPackageInfoAdapter(this);
    }

    @Override
    protected List<CommonPackageInfo> performLoading() {
        return LockKillPackageLoader.Impl.create(this).loadInstalled(mFilterOption, true);
    }

    @Override
    public void onSwitchChanged(SwitchCompat switchView, boolean isChecked) {
        XAPMManager.get().setLockKillEnabled(isChecked);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.lock_kill, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, LKSettingsDashboardActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}