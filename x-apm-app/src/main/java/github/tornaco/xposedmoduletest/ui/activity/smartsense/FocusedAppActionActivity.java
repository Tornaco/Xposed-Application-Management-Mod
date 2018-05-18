package github.tornaco.xposedmoduletest.ui.activity.smartsense;

import android.content.Intent;

import java.util.List;

import github.tornaco.xposedmoduletest.loader.FocusedAppLoader;
import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.ui.activity.common.CommonPackageInfoListActivity;
import github.tornaco.xposedmoduletest.ui.activity.comp.PackageViewerActivity;
import github.tornaco.xposedmoduletest.ui.adapter.common.CommonPackageInfoAdapter;

/**
 * Created by guohao4 on 2017/12/28.
 * Email: Tornaco@163.com
 */

public class FocusedAppActionActivity
        extends CommonPackageInfoListActivity {

    @Override
    protected int getSummaryRes() {
        return 0;
    }

    @Override
    protected CommonPackageInfoAdapter onCreateAdapter() {
        return new CommonPackageInfoAdapter(this);
    }

    @Override
    protected List<? extends CommonPackageInfo> performLoading() {
        return FocusedAppLoader.Impl.create(this).load();
    }

    @Override
    protected void onRequestPick() {
        super.onRequestPick();
        startActivity(new Intent(this, PackageViewerActivity.class));
    }

    @Override
    protected void onRequestClearItemsInBackground() {
        super.onRequestClearItemsInBackground();
    }
}
