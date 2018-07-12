package github.tornaco.xposedmoduletest.ui.activity.perm;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.collect.Lists;

import org.newstand.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import github.tornaco.android.common.Collections;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.compat.os.XAppOpsManager;
import github.tornaco.xposedmoduletest.compat.os.XAppOpsManagerRes;
import github.tornaco.xposedmoduletest.loader.PermissionLoader;
import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.ui.activity.WithSearchActivity;
import github.tornaco.xposedmoduletest.ui.activity.common.CommonPackageInfoListActivity;
import github.tornaco.xposedmoduletest.ui.adapter.PermissionAppsAdapter;
import github.tornaco.xposedmoduletest.util.XExecutor;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;

/**
 * Created by guohao4 on 2017/12/12.
 * Email: Tornaco@163.com
 */

public class Op2AppsListActivity extends WithSearchActivity<CommonPackageInfo> implements AdapterView.OnItemSelectedListener {

    private static final String EXTRA_OP = "extra.op";
    private static final String EXTRA_SHOW_SYSTEM = "extra.show_system";

    private SwipeRefreshLayout swipeRefreshLayout;
    private PermissionAppsAdapter permissionAppsAdapter;

    private int op;
    private String mRawTitle;

    private RecyclerView recyclerView;

    private boolean mShowSystem;

    public static void start(Context context, int op, boolean showSystem) {
        Intent starter = new Intent(context, Op2AppsListActivity.class);
        starter.putExtra(EXTRA_OP, op);
        starter.putExtra(EXTRA_SHOW_SYSTEM, showSystem);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.permission_list);
        setupToolbar();
        showHomeAsUp();

        op = getIntent().getIntExtra(EXTRA_OP, -1);
        mShowSystem = getIntent().getBooleanExtra(EXTRA_SHOW_SYSTEM, false);

        if (op < 0) return;


        Logger.w("Op2AppsListActivity, op " + op + ", mShowSystem " + mShowSystem);

        initView();

        mRawTitle = XAppOpsManagerRes.getOpLabel(this, op);
        setTitle(mRawTitle);
        setSubTitleChecked(XAppOpsManagerRes.getOpSummary(this, op));
    }

    private void warnIfSystemAppProtected() {
        boolean isSystemAppProtected = XAPMManager.get().isWhiteSysAppEnabled();
        if (isSystemAppProtected) {
            Toast.makeText(getContext(), R.string.perm_system_app_protected, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLoading();
    }

    protected void initView() {
        setSummaryView();

        recyclerView = findViewById(R.id.recycler_view);
        swipeRefreshLayout = findViewById(R.id.swipe);
        swipeRefreshLayout.setColorSchemeColors(getResources().getIntArray(R.array.polluted_waves));


        permissionAppsAdapter = onCreateAdapter();
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(permissionAppsAdapter);


        swipeRefreshLayout.setOnRefreshListener(this::startLoading);

        ViewGroup filterContainer = findViewById(R.id.apps_filter_spinner_container);
        onInitFilterSpinner(filterContainer);
    }

    protected void onInitFilterSpinner(ViewGroup filterContainer) {
        if (filterContainer == null) return;
        Spinner spinner = filterContainer.findViewById(R.id.filter_spinner);
        SpinnerAdapter adapter = onCreateSpinnerAdapter(spinner);
        if (adapter == null) {
            filterContainer.setVisibility(View.GONE);
        } else {
            filterContainer.setVisibility(View.VISIBLE);
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(onCreateSpinnerItemSelectListener());
            if (getDefaultFilterSpinnerSelection() > 0) {
                spinner.setSelection(getDefaultFilterSpinnerSelection());
            }
        }
    }

    protected int getDefaultFilterSpinnerSelection() {
        return -1;
    }

    private List<CommonPackageInfoListActivity.FilterOption> mFilterOptions;

    protected int mFilterOption = CommonPackageInfoListActivity.FilterOption.OPTION_3RD_APPS;

    protected SpinnerAdapter onCreateSpinnerAdapter(Spinner spinner) {
        if (getActivity() == null) return null;
        List<CommonPackageInfoListActivity.FilterOption> options = Lists.newArrayList(
                new CommonPackageInfoListActivity.FilterOption(R.string.filter_third_party_apps,
                        CommonPackageInfoListActivity.FilterOption.OPTION_3RD_APPS),
                new CommonPackageInfoListActivity.FilterOption(R.string.filter_system_apps,
                        CommonPackageInfoListActivity.FilterOption.OPTION_SYSTEM_APPS),
                new CommonPackageInfoListActivity.FilterOption(R.string.filter_installed_apps,
                        CommonPackageInfoListActivity.FilterOption.OPTION_ALL_APPS));
        mFilterOptions = options;
        return new CommonPackageInfoListActivity.FilterSpinnerAdapter(getActivity(), options);
    }

    protected AdapterView.OnItemSelectedListener onCreateSpinnerItemSelectListener() {
        return this;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Logger.d("onItemSelected: " + mFilterOptions.get(position));
        mFilterOption = mFilterOptions.get(position).getOption();
        if (mFilterOption == CommonPackageInfoListActivity.FilterOption.OPTION_ALL_APPS
                || mFilterOption == CommonPackageInfoListActivity.FilterOption.OPTION_SYSTEM_APPS) {
            warnIfSystemAppProtected();
        }
        startLoading();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    protected void startLoading() {
        swipeRefreshLayout.setRefreshing(true);
        XExecutor.execute(() -> {
            final List<CommonPackageInfo> res = performLoading();
            runOnUiThread(() -> {
                swipeRefreshLayout.setRefreshing(false);
                permissionAppsAdapter.update(res);
                setTitle(mRawTitle + "\t" + res.size());
            });
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_ops2apps, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            mSearchView.display();
            openKeyboard();
        }
        if (item.getItemId() == R.id.action_allow_all) {
            applyBatch(true);

        }
        if (item.getItemId() == R.id.action_ignore_all) {
            applyBatch(false);

        }
        return super.onOptionsItemSelected(item);
    }

    private void applyBatch(final boolean b) {
        final ProgressDialog p = new ProgressDialog(getActivity());
        p.setTitle(R.string.message_saving_changes);
        p.setIndeterminate(true);
        p.setCancelable(false);
        p.show();
        XExecutor.execute(() -> {
            try {
                Collections.consumeRemaining(permissionAppsAdapter.getData(),
                        info -> {
                            String pkg = info.getPkgName();
                            int mode = b ? XAppOpsManager.MODE_ALLOWED
                                    : XAppOpsManager.MODE_IGNORED;
                            XAPMManager.get().setPermissionControlBlockModeForPkg(op, pkg, mode);
                            runOnUiThreadChecked(() -> p.setMessage(info.getAppName()));
                        });
            } finally {
                runOnUiThreadChecked(() -> {
                    p.dismiss();

                    startLoading();
                });
            }
        });
    }

    protected List<CommonPackageInfo> performLoading() {
        return PermissionLoader.Impl.create(this).loadByOp(op, 0, mFilterOption);
    }

    protected PermissionAppsAdapter onCreateAdapter() {
        PermissionAppsAdapter a = new PermissionAppsAdapter(this);
        a.setOp(op);
        return a;
    }

    private void setSummaryView() {
        TextView textView = findViewById(R.id.summary);
        textView.setVisibility(View.GONE);
    }

    @Override
    public void onItemClicked(CommonPackageInfo item) {
        int index = indexOf(item);
        recyclerView.scrollToPosition(index);
        permissionAppsAdapter.setSelection(index);
    }

    @NonNull
    @Override
    public ArrayList<CommonPackageInfo> findItem(final String query, int page) {
        final ArrayList<CommonPackageInfo> res = new ArrayList<>();
        Collections.consumeRemaining(permissionAppsAdapter.getData(),
                info -> {
                    if (info.getAppName().toLowerCase().contains(query.toLowerCase())) {
                        res.add(info);
                    }
                });
        return res;
    }

    int indexOf(final CommonPackageInfo pkg) {
        return permissionAppsAdapter.getData().indexOf(pkg);
    }
}
