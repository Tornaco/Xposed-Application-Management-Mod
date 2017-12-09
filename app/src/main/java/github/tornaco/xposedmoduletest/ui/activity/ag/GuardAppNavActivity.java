package github.tornaco.xposedmoduletest.ui.activity.ag;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.newstand.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.bean.PackageInfo;
import github.tornaco.xposedmoduletest.loader.PackageLoader;
import github.tornaco.xposedmoduletest.provider.AppSettings;
import github.tornaco.xposedmoduletest.provider.LockStorage;
import github.tornaco.xposedmoduletest.ui.activity.NeedLockActivity;
import github.tornaco.xposedmoduletest.ui.adapter.GuardAppListAdapter;
import github.tornaco.xposedmoduletest.ui.widget.SwitchBar;
import github.tornaco.xposedmoduletest.util.XExecutor;
import github.tornaco.xposedmoduletest.xposed.app.XAppGuardManager;
import ir.mirrajabi.searchdialog.SimpleSearchDialogCompat;
import ir.mirrajabi.searchdialog.core.BaseSearchDialogCompat;
import ir.mirrajabi.searchdialog.core.SearchResultListener;
import lombok.Getter;

public class GuardAppNavActivity extends NeedLockActivity {

    protected FloatingActionButton fab;

    private SwipeRefreshLayout swipeRefreshLayout;

    @Getter
    protected GuardAppListAdapter guardAppListAdapter;

    @Getter
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutRes());
        setupToolbar();
        showHomeAsUp();
        initService();
        initView();
        startLoading();
    }

    private void initService() {
        boolean serviceConnected = XAppGuardManager.get().isServiceAvailable();
        Logger.d("serviceConnected:" + serviceConnected);
    }

    protected int getLayoutRes() {
        return R.layout.app_list;
    }

    @Override
    protected String getLockLabel() {
        return getString(R.string.app_lock_need_unlock);
    }

    @Override
    public void onResume() {
        super.onResume();
        startLoading();

        // Check up the pwd.
        if (!LockStorage.iaPatternSet(getApplicationContext())) {
            showPasswordSetupTips();
        }
    }

    @SuppressWarnings("unchecked")
    void onRequestSearch() {
        final ArrayList<PackageInfo> adapterData = (ArrayList<PackageInfo>)
                getGuardAppListAdapter().getPackageInfos();

        final SimpleSearchDialogCompat<PackageInfo> searchDialog =
                new SimpleSearchDialogCompat(getActivity(), getString(R.string.title_search),
                        getString(R.string.title_search_hint), null, adapterData,
                        new SearchResultListener<PackageInfo>() {

                            @Override
                            public void onSelected(BaseSearchDialogCompat baseSearchDialogCompat,
                                                   PackageInfo info, int i) {
                                int index = indexOf(info);
                                getRecyclerView().scrollToPosition(index);
                                getGuardAppListAdapter().setSelection(index);
                                baseSearchDialogCompat.dismiss();
                            }
                        });


        searchDialog.show();
        searchDialog.getSearchBox().setTypeface(Typeface.SERIF);
    }


    private int indexOf(final PackageInfo info) {
        return getGuardAppListAdapter().getPackageInfos().indexOf(info);
    }

    protected void initView() {
        recyclerView = findViewById(R.id.recycler_view);
        swipeRefreshLayout = findViewById(R.id.swipe);
        swipeRefreshLayout.setColorSchemeColors(getResources().getIntArray(R.array.polluted_waves));
        fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(GuardAppNavActivity.this, GuardAppPickerActivity.class));
            }
        });

        guardAppListAdapter = onCreateAdapter();
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(guardAppListAdapter);


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
                SwitchBar switchBar = findViewById(R.id.switchbar);
                if (switchBar == null) return;
                switchBar.setChecked(XAppGuardManager.get().isServiceAvailable()
                        && XAppGuardManager.get().isEnabled());
                switchBar.addOnSwitchChangeListener(new SwitchBar.OnSwitchChangeListener() {
                    @Override
                    public void onSwitchChanged(SwitchCompat switchView, boolean isChecked) {
                        if (XAppGuardManager.get().isServiceAvailable())
                            XAppGuardManager.get().setEnabled(isChecked);
                        else showTips(R.string.title_service_not_connected_settings, false,
                                null, null);
                    }
                });
                switchBar.show();
            }
        });
        setSummaryView();
    }

    protected void setSummaryView() {
        String who = getClass().getSimpleName();
        boolean showInfo = AppSettings.isShowInfoEnabled(this, who);
        TextView textView = findViewById(R.id.summary);
        if (!showInfo) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setText(R.string.summary_app_guard);
            textView.setVisibility(View.VISIBLE);
        }
    }

    private void showPasswordSetupTips() {
        showTips(R.string.summary_setup_passcode_none_set,
                true, getString(R.string.title_setup_passcode_now),
                new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(getApplicationContext(),
                                PatternSetupActivity.class));
                    }
                });
    }

    protected GuardAppListAdapter onCreateAdapter() {
        return new GuardAppListAdapter(this) {
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
                        guardAppListAdapter.update(res);
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
        getMenuInflater().inflate(R.menu.guard_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, GuardSettingsDashboardActivity.class));
        }

        if (item.getItemId() == R.id.action_info) {
            String who = getClass().getSimpleName();
            AppSettings.setShowInfo(this, who, !AppSettings.isShowInfoEnabled(this, who));
            setSummaryView();
        }

        if (item.getItemId() == R.id.action_search) {
            onRequestSearch();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected boolean isLockNeeded() {
        return LockStorage.iaPatternSet(this.getApplicationContext());
    }
}
