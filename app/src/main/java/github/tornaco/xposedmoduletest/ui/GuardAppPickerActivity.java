package github.tornaco.xposedmoduletest.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;

import java.util.List;

import github.tornaco.android.common.Collections;
import github.tornaco.android.common.Consumer;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.bean.DaoManager;
import github.tornaco.xposedmoduletest.bean.DaoSession;
import github.tornaco.xposedmoduletest.bean.PackageInfo;
import github.tornaco.xposedmoduletest.loader.PackageLoader;
import github.tornaco.xposedmoduletest.service.AppService;
import github.tornaco.xposedmoduletest.ui.adapter.AppListAdapter;
import github.tornaco.xposedmoduletest.ui.adapter.AppPickerListAdapter;

public class GuardAppPickerActivity extends GuardAppNavActivity {

    @Override
    protected void initView() {
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        super.initView();
        findViewById(R.id.switchbar).setVisibility(View.GONE);
        fab.setImageResource(R.drawable.ic_check_black_24dp);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog p = new ProgressDialog(GuardAppPickerActivity.this);
                p.setCancelable(false);
                p.setMessage("HANDLING");
                p.setIndeterminate(true);
                p.show();
                final DaoSession session = DaoManager.getInstance().getSession(getApplicationContext());
                if (session != null) {
                    AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                        @Override
                        public void run() {
                            Collections.consumeRemaining(appListAdapter.getPackageInfos(),
                                    new Consumer<PackageInfo>() {
                                        @Override
                                        public void accept(PackageInfo packageInfo) {
                                            session.getPackageInfoDao().insertOrReplace(packageInfo);
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
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        startService(new Intent(this, AppService.class));
    }

    protected List<PackageInfo> performLoading() {
        return PackageLoader.Impl.create(this).loadInstalled();
    }

    @Override
    protected AppListAdapter onCreateAdapter() {
        return new AppPickerListAdapter(this);
    }
}
