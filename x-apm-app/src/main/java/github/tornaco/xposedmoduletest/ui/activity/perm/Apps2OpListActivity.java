package github.tornaco.xposedmoduletest.ui.activity.perm;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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

import java.util.List;

import github.tornaco.android.common.Collections;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.compat.os.XAppOpsManager;
import github.tornaco.xposedmoduletest.loader.PermissionLoader;
import github.tornaco.xposedmoduletest.model.Permission;
import github.tornaco.xposedmoduletest.ui.activity.WithRecyclerView;
import github.tornaco.xposedmoduletest.ui.activity.common.CommonPackageInfoListActivity;
import github.tornaco.xposedmoduletest.ui.adapter.PermissionOpsAdapter;
import github.tornaco.xposedmoduletest.util.XExecutor;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.bean.AppOpsTemplate;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;

/**
 * Created by guohao4 on 2017/12/12.
 * Email: Tornaco@163.com
 */

public class Apps2OpListActivity extends WithRecyclerView implements AdapterView.OnItemSelectedListener {

    private static final String EXTRA_PKG = "extra.pkg";

    private SwipeRefreshLayout swipeRefreshLayout;
    private PermissionOpsAdapter permissionOpsAdapter;

    private String mPkg, mAppName;

    public static void start(Context context, String pkg) {
        Intent starter = new Intent(context, Apps2OpListActivity.class);
        starter.putExtra(EXTRA_PKG, pkg);
        starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.permission_list);
        setupToolbar();
        showHomeAsUp();

        mPkg = getIntent().getStringExtra(EXTRA_PKG);
        if (TextUtils.isEmpty(mPkg)) return;

        initView();

        mAppName = String.valueOf(PkgUtil.loadNameByPkgName(this, mPkg));
        setTitle(mAppName);

        warnIfSystemAppProtected();
    }

    private void warnIfSystemAppProtected() {
        boolean isSystemAppProtected = XAPMManager.get().isWhiteSysAppEnabled();
        if (isSystemAppProtected) {
            boolean isSystemApp = PkgUtil.isSystemApp(getContext(), mPkg);
            if (isSystemApp) {
                Toast.makeText(getContext(), R.string.perm_system_app_protected, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLoading();
    }

    protected void initView() {
        setSummaryView();

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        swipeRefreshLayout = findViewById(R.id.swipe);
        swipeRefreshLayout.setColorSchemeColors(getResources().getIntArray(R.array.polluted_waves));


        permissionOpsAdapter = onCreateAdapter();
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(permissionOpsAdapter);


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

    protected int mFilterOption = CommonPackageInfoListActivity.FilterOption.OPTION_ALL_OP;

    protected SpinnerAdapter onCreateSpinnerAdapter(Spinner spinner) {
        if (getActivity() == null) return null;
        List<CommonPackageInfoListActivity.FilterOption> options = Lists.newArrayList(
                new CommonPackageInfoListActivity.FilterOption(R.string.filter_all_op,
                        CommonPackageInfoListActivity.FilterOption.OPTION_ALL_OP),
                new CommonPackageInfoListActivity.FilterOption(R.string.filter_ext_op,
                        CommonPackageInfoListActivity.FilterOption.OPTION_EXT_OP),
                new CommonPackageInfoListActivity.FilterOption(R.string.filter_default_op,
                        CommonPackageInfoListActivity.FilterOption.OPTION_DEFAULT_OP));
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
        startLoading();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    protected void startLoading() {
        swipeRefreshLayout.setRefreshing(true);
        XExecutor.execute(() -> {
            final List<Permission> res = performLoading();
            runOnUiThread(() -> {
                swipeRefreshLayout.setRefreshing(false);
                permissionOpsAdapter.update(res);
                setTitle(mAppName + "\t" + res.size());
            });
        });
    }

    protected List<Permission> performLoading() {
        return PermissionLoader.Impl.create(this).load(mPkg, 0, mFilterOption);
    }

    protected PermissionOpsAdapter onCreateAdapter() {
        return new PermissionOpsAdapter(this);
    }

    private void setSummaryView() {
        TextView textView = findViewById(R.id.summary);
        textView.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_apps2ops, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_allow_all) {
            applyBatch(true);
        }
        if (item.getItemId() == R.id.action_ignore_all) {
            applyBatch(false);
        }

        if (item.getItemId() == R.id.action_select_from_ops_template) {
            AppOpsTemplatePicker.chooseOne(getActivity(), null, template -> {
                if (template != null) {
                    applyFromTemplate(template);
                }
            });
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
                Collections.consumeRemaining(permissionOpsAdapter.getData(),
                        permission -> {
                            int mode = b ? XAppOpsManager.MODE_ALLOWED
                                    : XAppOpsManager.MODE_IGNORED;
                            XAPMManager.get().setPermissionControlBlockModeForPkg(permission.getCode(), mPkg, mode);
                            runOnUiThreadChecked(() -> p.setMessage(permission.getName()));
                        });
            } finally {
                runOnUiThreadChecked(() -> {
                    p.dismiss();
                    startLoading();
                });
            }
        });
    }

    private void applyFromTemplate(final AppOpsTemplate appOpsTemplate) {
        final ProgressDialog p = new ProgressDialog(getActivity());
        p.setTitle(R.string.message_saving_changes);
        p.setIndeterminate(true);
        p.setCancelable(false);
        p.show();
        XExecutor.execute(() -> {
            try {
                Collections.consumeRemaining(permissionOpsAdapter.getData(),
                        permission -> {
                            int mode = appOpsTemplate.getMode(permission.getCode());
                            XAPMManager.get().setPermissionControlBlockModeForPkg(permission.getCode(), mPkg, mode);
                            runOnUiThreadChecked(() -> p.setMessage(permission.getName()));
                        });
            } finally {
                runOnUiThreadChecked(() -> {
                    p.dismiss();
                    startLoading();
                });
            }
        });
    }
}
