package github.tornaco.xposedmoduletest.ui.activity.prop;

import android.content.Context;
import android.content.Intent;

import java.util.List;

import github.tornaco.android.common.Collections;
import github.tornaco.xposedmoduletest.loader.PropPackageLoader;
import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.ui.activity.common.CommonPackageInfoListPickerActivity;
import github.tornaco.xposedmoduletest.ui.adapter.common.CommonPackageInfoAdapter;
import github.tornaco.xposedmoduletest.ui.adapter.common.CommonPackageInfoPickerAdapter;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;

public class PropAppListPickerActivity extends CommonPackageInfoListPickerActivity {

    public static void start(Context context) {
        Intent starter = new Intent(context, PropAppListPickerActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void doOnFabClickInWorkThread() {
        Collections.consumeRemaining(getCommonPackageInfoAdapter().getCommonPackageInfos(),
                commonPackageInfo -> {
                    if (commonPackageInfo.isChecked()) {
                        XAPMManager.get().addOrRemoveSystemPropProfileApplyApps(
                                new String[]{commonPackageInfo.getPkgName()},
                                true);
                    }
                });
    }

    @Override
    protected CommonPackageInfoAdapter onCreateAdapter() {
        return new CommonPackageInfoPickerAdapter(this);
    }

    @Override
    protected List<CommonPackageInfo> performLoading() {
        return PropPackageLoader.Impl.create(this).loadInstalled(false);
    }
}
