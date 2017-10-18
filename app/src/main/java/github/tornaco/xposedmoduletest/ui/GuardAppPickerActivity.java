package github.tornaco.xposedmoduletest.ui;

import android.view.View;

import java.util.List;

import github.tornaco.android.common.Collections;
import github.tornaco.android.common.Consumer;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.bean.DaoManager;
import github.tornaco.xposedmoduletest.bean.DaoSession;
import github.tornaco.xposedmoduletest.bean.PackageInfo;
import github.tornaco.xposedmoduletest.loader.PackageLoader;
import github.tornaco.xposedmoduletest.ui.adapter.AppListAdapter;
import github.tornaco.xposedmoduletest.ui.adapter.AppPickerListAdapter;

public class GuardAppPickerActivity extends GuardAppNavActivity {

    @Override
    protected void initView() {
        super.initView();
        fab.setImageResource(R.drawable.ic_check_black_24dp);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DaoSession session = DaoManager.getInstance().getSession(getApplicationContext());
                if (session != null) {
                    Collections.consumeRemaining(appListAdapter.getPackageInfos(),
                            new Consumer<PackageInfo>() {
                                @Override
                                public void accept(PackageInfo packageInfo) {
                                    session.getPackageInfoDao().insertOrReplace(packageInfo);
                                }
                            });
                }
                finish();
            }
        });
    }

    protected List<PackageInfo> performLoading() {
        return PackageLoader.Impl.create(this).loadInstalled();
    }

    @Override
    protected AppListAdapter onCreateAdapter() {
        return new AppPickerListAdapter(this);
    }
}
