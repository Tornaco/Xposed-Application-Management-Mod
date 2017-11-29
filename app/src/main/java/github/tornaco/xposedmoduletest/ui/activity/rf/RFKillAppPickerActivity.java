package github.tornaco.xposedmoduletest.ui.activity.rf;

import android.app.ProgressDialog;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import github.tornaco.android.common.Consumer;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.bean.RFKillPackage;
import github.tornaco.xposedmoduletest.loader.RFKillPackageLoader;
import github.tornaco.xposedmoduletest.provider.RFKillPackageProvider;
import github.tornaco.xposedmoduletest.ui.adapter.RFKillAppListAdapter;
import github.tornaco.xposedmoduletest.ui.adapter.RFKillAppPickerListAdapter;
import github.tornaco.xposedmoduletest.ui.widget.SwitchBar;
import github.tornaco.xposedmoduletest.util.XExecutor;

public class RFKillAppPickerActivity extends RFKillAppNavActivity {

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
                final ProgressDialog p = new ProgressDialog(RFKillAppPickerActivity.this);
                p.setCancelable(false);
                p.setMessage("SAVING...");
                p.setIndeterminate(true);
                p.show();
                XExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        List<RFKillPackage> packageInfoList = rFKillAppListAdapter.getRFKillPackages();
                        github.tornaco.android.common.Collections.consumeRemaining(packageInfoList,
                                new Consumer<RFKillPackage>() {
                                    @Override
                                    public void accept(RFKillPackage packageInfo) {
                                        if (!packageInfo.getKill()) {
                                            RFKillPackageProvider.insert(getApplicationContext(), packageInfo);
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

    protected List<RFKillPackage> performLoading() {
        return RFKillPackageLoader.Impl.create(this).loadInstalled(true);
    }

    @Override
    protected RFKillAppListAdapter onCreateAdapter() {
        return new RFKillAppPickerListAdapter(this);
    }
}
