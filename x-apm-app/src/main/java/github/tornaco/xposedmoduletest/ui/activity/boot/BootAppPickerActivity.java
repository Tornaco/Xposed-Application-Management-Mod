package github.tornaco.xposedmoduletest.ui.activity.boot;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.google.common.collect.Lists;

import org.newstand.logger.Logger;

import java.util.List;

import github.tornaco.android.common.Collections;
import github.tornaco.android.common.Consumer;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.loader.BootPackageLoader;
import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.ui.activity.common.CommonPackageInfoListPickerActivity;
import github.tornaco.xposedmoduletest.ui.adapter.common.CommonPackageInfoAdapter;
import github.tornaco.xposedmoduletest.ui.adapter.common.CommonPackageInfoPickerAdapter;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;

public class BootAppPickerActivity extends CommonPackageInfoListPickerActivity implements AdapterView.OnItemSelectedListener {

    public static void start(Context context) {
        Intent starter = new Intent(context, BootAppPickerActivity.class);
        context.startActivity(starter);
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
    protected void doOnFabClickInWorkThread() {
        Collections.consumeRemaining(getCommonPackageInfoAdapter().getCommonPackageInfos(),
                new Consumer<CommonPackageInfo>() {
                    @Override
                    public void accept(CommonPackageInfo commonPackageInfo) {
                        if (commonPackageInfo.isChecked()) {
                            XAPMManager.get().addOrRemoveBootBlockApps(new String[]{commonPackageInfo.getPkgName()},
                                    XAPMManager.Op.ADD);
                        }
                    }
                });
    }

    @Override
    protected CommonPackageInfoAdapter onCreateAdapter() {
        return new CommonPackageInfoPickerAdapter(this);
    }

    @Override
    protected List<CommonPackageInfo> performLoading() {
        return BootPackageLoader.Impl.create(this).loadInstalled(mFilterOption, false);
    }
}
