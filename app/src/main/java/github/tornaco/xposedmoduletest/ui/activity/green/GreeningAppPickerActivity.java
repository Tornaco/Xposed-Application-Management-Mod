package github.tornaco.xposedmoduletest.ui.activity.green;

import android.app.ProgressDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import github.tornaco.android.common.Consumer;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.bean.AutoStartPackage;
import github.tornaco.xposedmoduletest.loader.GreeningPackageLoader;
import github.tornaco.xposedmoduletest.ui.adapter.StartAppListAdapter;
import github.tornaco.xposedmoduletest.ui.adapter.StartAppPickerListAdapter;
import github.tornaco.xposedmoduletest.ui.widget.SwitchBar;
import github.tornaco.xposedmoduletest.util.XExecutor;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;

public class GreeningAppPickerActivity extends GreeningAppNavActivity {

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
                final ProgressDialog p = new ProgressDialog(GreeningAppPickerActivity.this);
                p.setCancelable(false);
                p.setMessage(getString(R.string.message_saving_changes));
                p.setIndeterminate(true);
                p.show();
                XExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        List<AutoStartPackage> packageInfoList = startAppListAdapter.getAutoStartPackages();
                        github.tornaco.android.common.Collections.consumeRemaining(packageInfoList,
                                new Consumer<AutoStartPackage>() {
                                    @Override
                                    public void accept(AutoStartPackage packageInfo) {
                                        if (packageInfo.getAllow()) {
                                            XAshmanManager.get()
                                                    .addOrRemoveGreeningApps(
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
            startAppListAdapter.selectAll(selectAll);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected List<AutoStartPackage> performLoading() {
        return GreeningPackageLoader.Impl.create(this).loadInstalled(false);
    }

    @Override
    protected StartAppListAdapter onCreateAdapter() {
        return new StartAppPickerListAdapter(this);
    }
}
