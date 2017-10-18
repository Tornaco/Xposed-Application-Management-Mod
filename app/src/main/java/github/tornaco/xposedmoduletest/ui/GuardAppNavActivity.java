package github.tornaco.xposedmoduletest.ui;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.List;

import ezy.assist.compat.SettingsCompat;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.bean.PackageInfo;
import github.tornaco.xposedmoduletest.loader.PackageLoader;
import github.tornaco.xposedmoduletest.ui.adapter.AppListAdapter;

public class GuardAppNavActivity extends AppCompatActivity {

    protected FloatingActionButton fab;

    private SwipeRefreshLayout swipeRefreshLayout;

    protected AppListAdapter appListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler_view_with_fab_template);
        SettingsCompat.manageDrawOverlays(this);
        initView();
        startLoading();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLoading();
    }

    protected void initView() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
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
    }

    protected AppListAdapter onCreateAdapter() {
        return new AppListAdapter(this);
    }

    private void startLoading() {
        swipeRefreshLayout.setRefreshing(true);
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
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
}
