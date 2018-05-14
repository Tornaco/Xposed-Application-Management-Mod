package github.tornaco.xposedmoduletest.ui.activity.perm;

import android.view.Menu;
import android.view.MenuItem;

import java.util.List;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.loader.ComponentLoader;
import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.ui.activity.common.CommonPackageInfoListActivity;
import github.tornaco.xposedmoduletest.ui.adapter.common.CommonPackageInfoAdapter;
import github.tornaco.xposedmoduletest.ui.adapter.common.CommonPackageInfoViewerAdapter;
import github.tornaco.xposedmoduletest.ui.widget.SwitchBar;

/**
 * Created by guohao4 on 2017/11/18.
 * Email: Tornaco@163.com
 */
public class PackageViewerActivity extends CommonPackageInfoListActivity {

    private boolean mShowSystemApp;

    @Override
    protected void initView() {
        super.initView();
        fab.hide();
    }

    @Override
    protected void onInitSwitchBar(SwitchBar switchBar) {
        switchBar.hide();
    }

    @Override
    protected int getSummaryRes() {
        return R.string.summary_perm_control;
    }

    @Override
    protected CommonPackageInfoAdapter onCreateAdapter() {

        CommonPackageInfoViewerAdapter adapter = new CommonPackageInfoViewerAdapter(this) {
            @Override
            protected boolean enableLongPressTriggerAllSelection() {
                // No need.
                return false;
            }
        };

        adapter.setOnItemClickListener((parent, view, position, id) -> {
            CommonPackageInfo info = getCommonPackageInfoAdapter().getCommonPackageInfos().get(position);
            Apps2OpListActivity.start(getActivity(), info.getPkgName());
        });
        return adapter;
    }

    @Override
    protected List<CommonPackageInfo> performLoading() {
        return ComponentLoader.Impl.create(this).loadInstalledApps(mShowSystemApp,
                ComponentLoader.Sort.byName(), FilterOption.OPTION_ALL_APPS);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.perm_viewer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.show_system_app) {
            mShowSystemApp = !mShowSystemApp;
            item.setChecked(mShowSystemApp);
            startLoading();
        }
        return super.onOptionsItemSelected(item);
    }
}
