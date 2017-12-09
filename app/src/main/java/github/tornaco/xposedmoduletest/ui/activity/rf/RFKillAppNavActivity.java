package github.tornaco.xposedmoduletest.ui.activity.rf;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.bean.RFKillPackage;
import github.tornaco.xposedmoduletest.loader.RFKillPackageLoader;
import github.tornaco.xposedmoduletest.provider.AppSettings;
import github.tornaco.xposedmoduletest.ui.activity.WithRecyclerView;
import github.tornaco.xposedmoduletest.ui.activity.lk.LKSettingsDashboardActivity;
import github.tornaco.xposedmoduletest.ui.adapter.RFKillAppListAdapter;
import github.tornaco.xposedmoduletest.ui.widget.SwitchBar;
import github.tornaco.xposedmoduletest.util.SpannableUtil;
import github.tornaco.xposedmoduletest.util.XExecutor;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;
import ir.mirrajabi.searchdialog.SimpleSearchDialogCompat;
import ir.mirrajabi.searchdialog.core.BaseSearchDialogCompat;
import ir.mirrajabi.searchdialog.core.SearchResultListener;
import lombok.Getter;

public class RFKillAppNavActivity extends WithRecyclerView {

    protected FloatingActionButton fab;

    private SwipeRefreshLayout swipeRefreshLayout;

    @Getter
    protected RFKillAppListAdapter rFKillAppListAdapter;

    @Getter
    protected RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutRes());
        setupToolbar();
        showHomeAsUp();
        initView();
        startLoading();
    }

    protected int getLayoutRes() {
        return R.layout.app_list;
    }

    @Override
    public void onResume() {
        super.onResume();
        startLoading();
    }

    @SuppressWarnings("unchecked")
    void onRequestSearch() {
        final ArrayList<RFKillPackage> adapterData = (ArrayList<RFKillPackage>)
                getRFKillAppListAdapter().getRFKillPackages();

        final SimpleSearchDialogCompat<RFKillPackage> searchDialog =
                new SimpleSearchDialogCompat(getActivity(), getString(R.string.title_search),
                        getString(R.string.title_search_hint), null, adapterData,
                        new SearchResultListener<RFKillPackage>() {

                            @Override
                            public void onSelected(BaseSearchDialogCompat baseSearchDialogCompat,
                                                   RFKillPackage info, int i) {
                                int index = indexOf(info);
                                getRecyclerView().scrollToPosition(index);
                                getRFKillAppListAdapter().setSelection(index);
                                baseSearchDialogCompat.dismiss();
                            }
                        });


        searchDialog.show();
        searchDialog.getSearchBox().setTypeface(Typeface.SERIF);
    }

    private int indexOf(final RFKillPackage info) {
        return getRFKillAppListAdapter().getRFKillPackages().indexOf(info);
    }

    protected void initView() {
        recyclerView = findViewById(R.id.recycler_view);
        swipeRefreshLayout = findViewById(R.id.swipe);
        swipeRefreshLayout.setColorSchemeColors(getResources().getIntArray(R.array.polluted_waves));
        fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RFKillAppNavActivity.this, RFKillAppPickerActivity.class));
            }
        });

        rFKillAppListAdapter = onCreateAdapter();
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(rFKillAppListAdapter);


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
                switchBar.setChecked(XAshmanManager.get().isServiceAvailable()
                        && XAshmanManager.get().isRFKillEnabled());
                switchBar.addOnSwitchChangeListener(new SwitchBar.OnSwitchChangeListener() {
                    @Override
                    public void onSwitchChanged(SwitchCompat switchView, boolean isChecked) {
                        if (XAshmanManager.get().isServiceAvailable())
                            XAshmanManager.get().setRFKillEnabled(isChecked);
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
            int normalColor = ContextCompat.getColor(getActivity(), R.color.white);
            int highlightColor = ContextCompat.getColor(getActivity(), R.color.amber);
            int strId = XAshmanManager.get().isServiceAvailable()
                    && XAshmanManager.get().isWhiteSysAppEnabled()
                    ? R.string.summary_rf_kill_app_include_system : R.string.summary_rf_kill_app;
            textView.setText(SpannableUtil.buildHighLightString(getActivity(), normalColor, highlightColor, strId));
            textView.setVisibility(View.VISIBLE);
        }
    }

    protected RFKillAppListAdapter onCreateAdapter() {
        return new RFKillAppListAdapter(this) {
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
                final List<RFKillPackage> res = performLoading();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                        rFKillAppListAdapter.update(res);
                    }
                });
            }
        });
    }

    protected List<RFKillPackage> performLoading() {
        return RFKillPackageLoader.Impl.create(this).loadInstalled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.rf, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, LKSettingsDashboardActivity.class));
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
}
