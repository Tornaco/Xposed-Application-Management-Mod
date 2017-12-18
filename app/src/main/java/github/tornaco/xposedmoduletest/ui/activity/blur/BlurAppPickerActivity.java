package github.tornaco.xposedmoduletest.ui.activity.blur;

import android.content.Context;
import android.content.Intent;

import java.util.List;

import github.tornaco.android.common.Collections;
import github.tornaco.android.common.Consumer;
import github.tornaco.xposedmoduletest.loader.BlurPackageLoader;
import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.ui.activity.common.CommonPackageInfoListPickerActivity;
import github.tornaco.xposedmoduletest.ui.adapter.common.CommonPackageInfoAdapter;
import github.tornaco.xposedmoduletest.ui.adapter.common.CommonPackageInfoPickerAdapter;
import github.tornaco.xposedmoduletest.xposed.app.XAppGuardManager;

public class BlurAppPickerActivity extends CommonPackageInfoListPickerActivity {

    public static void start(Context context) {
        Intent starter = new Intent(context, BlurAppPickerActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void doOnFabClickInWorkThread() {
        Collections.consumeRemaining(getCommonPackageInfoAdapter().getCommonPackageInfos(),
                new Consumer<CommonPackageInfo>() {
                    @Override
                    public void accept(CommonPackageInfo commonPackageInfo) {
                        if (commonPackageInfo.isChecked()) {
                            XAppGuardManager.get().addOrRemoveBlurApps(new String[]{commonPackageInfo.getPkgName()}, true);
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
        return BlurPackageLoader.Impl.create(this).loadInstalled(false);
    }
}
