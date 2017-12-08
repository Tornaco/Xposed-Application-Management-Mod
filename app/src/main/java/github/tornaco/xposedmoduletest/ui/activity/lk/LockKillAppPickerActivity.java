package github.tornaco.xposedmoduletest.ui.activity.lk;

import android.app.ProgressDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import github.tornaco.android.common.Consumer;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.bean.LockKillPackage;
import github.tornaco.xposedmoduletest.loader.LockKillPackageLoader;
import github.tornaco.xposedmoduletest.ui.adapter.LockKillAppListAdapter;
import github.tornaco.xposedmoduletest.ui.adapter.LockKillAppPickerListAdapter;
import github.tornaco.xposedmoduletest.ui.widget.SwitchBar;
import github.tornaco.xposedmoduletest.util.XExecutor;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;

public class LockKillAppPickerActivity extends LockKillAppNavActivity {

    @Override
    protected int getLayoutRes() {
        return R.layout.app_list;
    }

    @Override
    protected void initView() {
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        super.initView();
        SwitchBar switchBar = findViewById(R.id.switchbar);
        switchBar.hide();
        fab.setImageResource(R.drawable.ic_check_black_24dp);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog p = new ProgressDialog(LockKillAppPickerActivity.this);
                p.setCancelable(false);
                p.setMessage("SAVING...");
                p.setIndeterminate(true);
                p.show();
                XExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        List<LockKillPackage> packageInfoList = lockKillAppListAdapter.getLockKillPackages();
                        github.tornaco.android.common.Collections.consumeRemaining(packageInfoList,
                                new Consumer<LockKillPackage>() {
                                    @Override
                                    public void accept(LockKillPackage packageInfo) {
                                        if (!packageInfo.getKill()) {
                                            XAshmanManager.get()
                                                    .addOrRemoveLKApps(
                                                            new String[]{packageInfo.getPkgName()},
                                                            XAshmanManager.Op.ADD
                                                    );
                                        }
                                    }
                                });
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
        setSummaryView();
    }

    protected void setSummaryView() {
        TextView textView = findViewById(R.id.summary);
        textView.setVisibility(View.GONE);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_info).setVisible(false);
        menu.findItem(R.id.action_select_all).setVisible(true);
        return super.onPrepareOptionsMenu(menu);
    }

    private boolean selectAll = false;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_select_all) {
            selectAll = !selectAll;
            lockKillAppListAdapter.selectAll(selectAll);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected List<LockKillPackage> performLoading() {
        return LockKillPackageLoader.Impl.create(this).loadInstalled(true);
    }

    @Override
    protected LockKillAppListAdapter onCreateAdapter() {
        return new LockKillAppPickerListAdapter(this);
    }
}
