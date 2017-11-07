package github.tornaco.xposedmoduletest.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SwitchCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.newstand.logger.Logger;

import java.util.List;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.bean.PackageInfo;
import github.tornaco.xposedmoduletest.loader.PackageLoader;
import github.tornaco.xposedmoduletest.ui.activity.SettingsDashboardActivity;
import github.tornaco.xposedmoduletest.ui.adapter.AppListAdapter;
import github.tornaco.xposedmoduletest.ui.widget.SwitchBar;
import github.tornaco.xposedmoduletest.util.XExecutor;
import github.tornaco.xposedmoduletest.x.XSettings;
import github.tornaco.xposedmoduletest.x.app.XAppGuardManager;
import in.myinnos.alphabetsindexfastscrollrecycler.IndexFastScrollRecyclerView;

public class GuardAppNavActivity extends LockedActivity {

    protected FloatingActionButton fab;

    private SwipeRefreshLayout swipeRefreshLayout;

    protected AppListAdapter appListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutRes());
        initService();
        initView();
        initFirstRun();
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

    private void initFirstRun() {
        boolean first = XSettings.isFirstRun(this);
        if (first) {
            new AlertDialog.Builder(GuardAppNavActivity.this)
                    .setTitle(R.string.first_run_title)
                    .setMessage(R.string.message_first_run)
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            XSettings.setFirstRun(getApplicationContext());
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .show();
        }
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
                startActivity(new Intent(GuardAppNavActivity.this, GuardAppPickerActivity.class));
            }
        });

        appListAdapter = onCreateAdapter();
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(appListAdapter);


        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
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
                switchBar.setChecked(XAppGuardManager.defaultInstance().isServiceAvailable() && XAppGuardManager.defaultInstance().isEnabled());
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

    protected AppListAdapter onCreateAdapter() {
        return new AppListAdapter(this) {
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
                final List<PackageInfo> res = performLoading();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                        appListAdapter.update(res);
                    }
                });
            }
        });
    }

    protected List<PackageInfo> performLoading() {
        return PackageLoader.Impl.create(this).loadStoredGuarded();
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
        if (item.getItemId() == R.id.action_help) {
            navigateToWebPage(getString(R.string.app_wiki_url));
        }
        if (item.getItemId() == R.id.action_open_source) {
            navigateToWebPage(getString(R.string.app_source_url));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected boolean showLockOnCreate() {
        return XAppGuardManager.defaultInstance().isServiceAvailable();
    }
}
