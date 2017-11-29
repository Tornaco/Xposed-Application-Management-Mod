package github.tornaco.xposedmoduletest.ui.activity.lk;

import android.app.ProgressDialog;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import github.tornaco.android.common.Consumer;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.bean.LockKillPackage;
import github.tornaco.xposedmoduletest.loader.LockKillPackageLoader;
import github.tornaco.xposedmoduletest.provider.LockKillPackageProvider;
import github.tornaco.xposedmoduletest.ui.adapter.LockKillAppListAdapter;
import github.tornaco.xposedmoduletest.ui.adapter.LockKillAppPickerListAdapter;
import github.tornaco.xposedmoduletest.ui.widget.SwitchBar;
import github.tornaco.xposedmoduletest.util.XExecutor;

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
                                            LockKillPackageProvider.insert(getApplicationContext(), packageInfo);
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

    protected List<LockKillPackage> performLoading() {
        return LockKillPackageLoader.Impl.create(this).loadInstalled(true);
    }

    @Override
    protected LockKillAppListAdapter onCreateAdapter() {
        return new LockKillAppPickerListAdapter(this);
    }
}
