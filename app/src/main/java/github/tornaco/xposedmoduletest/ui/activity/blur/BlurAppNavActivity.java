package github.tornaco.xposedmoduletest.ui.activity.blur;

import android.support.v7.widget.SwitchCompat;

import java.util.List;

import github.tornaco.android.common.Collections;
import github.tornaco.android.common.Consumer;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.loader.BlurPackageLoader;
import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.ui.activity.common.CommonPackageInfoListActivity;
import github.tornaco.xposedmoduletest.ui.adapter.common.CommonPackageInfoAdapter;
import github.tornaco.xposedmoduletest.ui.widget.SwitchBar;
import github.tornaco.xposedmoduletest.xposed.app.XAppGuardManager;

public class BlurAppNavActivity extends CommonPackageInfoListActivity implements SwitchBar.OnSwitchChangeListener {

    @Override
    protected void onRequestClearItemsInBackground() {
        Collections.consumeRemaining(getCommonPackageInfoAdapter().getCommonPackageInfos(),
                new Consumer<CommonPackageInfo>() {
                    @Override
                    public void accept(CommonPackageInfo commonPackageInfo) {
                        if (commonPackageInfo.isChecked()) {
                            XAppGuardManager.get().addOrRemoveBlurApps(new String[]{commonPackageInfo.getPkgName()}, false);
                        }
                    }
                });
    }

    @Override
    protected void onInitSwitchBar(SwitchBar switchBar) {
        switchBar.show();
        switchBar.setChecked(XAppGuardManager.get().isBlurEnabled());
        switchBar.addOnSwitchChangeListener(this);
    }

    @Override
    protected void onRequestPick() {
        BlurAppPickerActivity.start(getActivity());
    }

    @Override
    protected int getSummaryRes() {
        return R.string.summary_app_recent_blur;
    }

    @Override
    protected CommonPackageInfoAdapter onCreateAdapter() {
        return new CommonPackageInfoAdapter(this);
    }

    @Override
    protected List<CommonPackageInfo> performLoading() {
        return BlurPackageLoader.Impl.create(this).loadInstalled(true);
    }

    @Override
    public void onSwitchChanged(SwitchCompat switchView, boolean isChecked) {
        XAppGuardManager.get().setBlurEnabled(isChecked);
    }
}