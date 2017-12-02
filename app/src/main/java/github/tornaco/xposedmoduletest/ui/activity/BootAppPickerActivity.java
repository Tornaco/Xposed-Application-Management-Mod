package github.tornaco.xposedmoduletest.ui.activity;

import android.app.ProgressDialog;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import github.tornaco.android.common.Consumer;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.bean.BootCompletePackage;
import github.tornaco.xposedmoduletest.loader.BootPackageLoader;
import github.tornaco.xposedmoduletest.ui.adapter.BootAppPickerListAdapter;
import github.tornaco.xposedmoduletest.ui.widget.SwitchBar;
import github.tornaco.xposedmoduletest.util.XExecutor;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;

public class BootAppPickerActivity extends BootAppNavActivity {

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
                final ProgressDialog p = new ProgressDialog(BootAppPickerActivity.this);
                p.setCancelable(false);
                p.setMessage(getString(R.string.message_saving_changes));
                p.setIndeterminate(true);
                p.show();
                XExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        List<BootCompletePackage> packageInfoList = bootAppListAdapter.getBootCompletePackages();
                        github.tornaco.android.common.Collections.consumeRemaining(packageInfoList,
                                new Consumer<BootCompletePackage>() {
                                    @Override
                                    public void accept(BootCompletePackage packageInfo) {
                                        if (packageInfo.getAllow()) {
                                            XAshmanManager.singleInstance()
                                                    .addOrRemoveBootBlockApps(
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
        return super.onPrepareOptionsMenu(menu);
    }

    protected List<BootCompletePackage> performLoading() {
        return BootPackageLoader.Impl.create(this).loadInstalled(true);
    }

    @Override
    protected BootAppPickerListAdapter onCreateAdapter() {
        return new BootAppPickerListAdapter(this);
    }
}
