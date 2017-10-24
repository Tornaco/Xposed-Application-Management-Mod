package github.tornaco.xposedmoduletest.ui;

import android.app.ProgressDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.List;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.bean.PackageInfo;
import github.tornaco.xposedmoduletest.loader.PackageLoader;
import github.tornaco.xposedmoduletest.ui.adapter.AppListAdapter;
import github.tornaco.xposedmoduletest.ui.adapter.AppPickerListAdapter;
import github.tornaco.xposedmoduletest.ui.widget.SwitchBar;
import github.tornaco.xposedmoduletest.x.XAppGuardManager;
import github.tornaco.xposedmoduletest.x.XExecutor;

public class GuardAppPickerActivity extends GuardAppNavActivity {

    private boolean mShowSystemApp;

    @Override
    protected int getLayoutRes() {
        return R.layout.app_list;
    }

    @Override
    protected void initView() {
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        super.initView();
        SwitchBar switchBar = (SwitchBar) findViewById(R.id.switchbar);
        switchBar.hide();
        fab.setImageResource(R.drawable.ic_check_black_24dp);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog p = new ProgressDialog(GuardAppPickerActivity.this);
                p.setCancelable(false);
                p.setMessage("HANDLING");
                p.setIndeterminate(true);
                p.show();
                XExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        List<PackageInfo> packageInfoList = appListAdapter.getPackageInfos();
                        String pkgs[] = new String[packageInfoList.size()];
                        for (int i = 0; i < packageInfoList.size(); i++) {
                            if (packageInfoList.get(i).getGuard())
                                pkgs[i] = packageInfoList.get(i).getPkgName();
                        }
                        XAppGuardManager.from().addPackages(pkgs);
                        XAppGuardManager.from().forceWriteState();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                p.dismiss();
                                finish();
                            }
                        });
                    }
                });
            }
        });
    }

    protected List<PackageInfo> performLoading() {
        return PackageLoader.Impl.create(this).loadInstalled(mShowSystemApp);
    }

    @Override
    protected AppListAdapter onCreateAdapter() {
        return new AppPickerListAdapter(this);
    }

    @Override
    protected boolean showLockOnCreate() {
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.picker, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.show_system_app).setChecked(mShowSystemApp);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mShowSystemApp = !mShowSystemApp;
        invalidateOptionsMenu();
        startLoading();
        return super.onOptionsItemSelected(item);
    }
}
