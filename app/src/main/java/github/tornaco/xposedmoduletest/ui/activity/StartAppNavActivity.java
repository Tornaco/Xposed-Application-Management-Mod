package github.tornaco.xposedmoduletest.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SwitchCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.newstand.logger.Logger;

import java.util.List;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.bean.AutoStartPackage;
import github.tornaco.xposedmoduletest.loader.StartPackageLoader;
import github.tornaco.xposedmoduletest.ui.adapter.StartAppListAdapter;
import github.tornaco.xposedmoduletest.ui.widget.SwitchBar;
import github.tornaco.xposedmoduletest.util.XExecutor;
import github.tornaco.xposedmoduletest.x.app.XAppGuardManager;
import in.myinnos.alphabetsindexfastscrollrecycler.IndexFastScrollRecyclerView;

public class StartAppNavActivity extends LockedActivity {

    protected FloatingActionButton fab;

    private SwipeRefreshLayout swipeRefreshLayout;

    protected StartAppListAdapter bootAppListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutRes());
        showHomeAsUp();
        initService();
        initView();
        startLoading();
    }

    private void initService() {
        boolean serviceConnected = XAppGuardManager.defaultInstance().isServiceAvailable();
        Logger.d("serviceConnected:" + serviceConnected);
        setTitle(serviceConnected ? R.string.title_service_connected : R.string.title_service_not_connected);
    }

    protected int getLayoutRes() {
        return R.layout.app_list;
    }

    @Override
    public void onResume() {
        super.onResume();
        startLoading();
    }

    protected void initView() {
        IndexFastScrollRecyclerView recyclerView = (IndexFastScrollRecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setIndexBarColor("#DADADA");
        recyclerView.setIndexBarTextColor("#333333");
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe);
        swipeRefreshLayout.setColorSchemeColors(getResources().getIntArray(R.array.polluted_waves));
        fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StartAppNavActivity.this, StartAppPickerActivity.class));
            }
        });

        bootAppListAdapter = onCreateAdapter();
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(bootAppListAdapter);


        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        startLoading();
                    }
                });

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SwitchBar switchBar = (SwitchBar) findViewById(R.id.switchbar);
                if (switchBar == null) return;
                switchBar.setChecked(XAppGuardManager.defaultInstance().isServiceAvailable()
                        && XAppGuardManager.defaultInstance().isEnabled());
                switchBar.addOnSwitchChangeListener(new SwitchBar.OnSwitchChangeListener() {
                    @Override
                    public void onSwitchChanged(SwitchCompat switchView, boolean isChecked) {
                        XAppGuardManager.defaultInstance().setEnabled(isChecked);
                    }
                });
                switchBar.show();
            }
        });
    }

    protected StartAppListAdapter onCreateAdapter() {
        return new StartAppListAdapter(this) {
            @Override
            protected void onPackageRemoved(String p) {
                super.onPackageRemoved(p);
                startLoading();
            }
        };
    }

    protected void startLoading() {
        swipeRefreshLayout.setRefreshing(true);
        XExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final List<AutoStartPackage> res = performLoading();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                        bootAppListAdapter.update(res);
                    }
                });
            }
        });
    }

    protected List<AutoStartPackage> performLoading() {
        return StartPackageLoader.Impl.create(this).loadStoredDisAllowed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.nav, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsDashboardActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected boolean showLockOnCreate() {
        return XAppGuardManager.defaultInstance().isServiceAvailable();
    }
}
