package github.tornaco.xposedmoduletest.ui.activity.resdient;

import android.content.Context;
import android.content.Intent;

import java.util.List;

import github.tornaco.android.common.Collections;
import github.tornaco.android.common.Consumer;
import github.tornaco.xposedmoduletest.loader.ResidentPackageLoader;
import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.ui.activity.common.CommonPackageInfoListPickerActivity;
import github.tornaco.xposedmoduletest.ui.adapter.common.CommonPackageInfoAdapter;
import github.tornaco.xposedmoduletest.ui.adapter.common.CommonPackageInfoPickerAdapter;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;

public class ResdientAppPickerActivity extends CommonPackageInfoListPickerActivity {

    public static void start(Context context) {
        Intent starter = new Intent(context, ResdientAppPickerActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void doOnFabClickInWorkThread() {
        Collections.consumeRemaining(getCommonPackageInfoAdapter().getCommonPackageInfos(),
                new Consumer<CommonPackageInfo>() {
                    @Override
                    public void accept(CommonPackageInfo commonPackageInfo) {
                        if (commonPackageInfo.isChecked()) {
                            XAPMManager.get().addOrRemoveResidentApps(commonPackageInfo.getPkgName(), true);
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
        return ResidentPackageLoader.Impl.create(this).loadInstalled(false);
    }
}
