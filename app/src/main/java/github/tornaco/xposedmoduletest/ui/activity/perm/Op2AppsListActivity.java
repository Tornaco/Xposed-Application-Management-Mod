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
import android.widget.TextView;

import org.newstand.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import github.tornaco.android.common.Collections;
import github.tornaco.android.common.Consumer;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.compat.os.AppOpsManagerCompat;
import github.tornaco.xposedmoduletest.loader.PermissionLoader;
import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.ui.activity.WithSearchActivity;
import github.tornaco.xposedmoduletest.ui.adapter.PermissionAppsAdapter;
import github.tornaco.xposedmoduletest.util.XExecutor;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;

/**
 * Created by guohao4 on 2017/12/12.
 * Email: Tornaco@163.com
 */

public class Op2AppsListActivity extends WithSearchActivity<CommonPackageInfo> {

    private static final String EXTRA_OP = "extra.op";

    private SwipeRefreshLayout swipeRefreshLayout;
    private PermissionAppsAdapter permissionAppsAdapter;

    private int op;
    private String mRawTitle;

    private RecyclerView recyclerView;

    public static void start(Context context, int op) {
        Intent starter = new Intent(context, Op2AppsListActivity.class);
        starter.putExtra(EXTRA_OP, op);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.permission_list);
        setupToolbar();
        showHomeAsUp();

        op = getIntent().getIntExtra(EXTRA_OP, -1);

        if (op < 0) return;


        Logger.w("Op2AppsListActivity, op " + op);

        initView();

        mRawTitle = AppOpsManagerCompat.getOpSummary(this, op);
        setTitle(mRawTitle);
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


        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        startLoading();
                    }
                });
    }

    protected void startLoading() {
        swipeRefreshLayout.setRefreshing(true);
        XExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final List<CommonPackageInfo> res = performLoading();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                        permissionAppsAdapter.update(res);
                        setTitle(mRawTitle + "\t" + res.size() + "个应用");
                    }
                });
            }
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
        XExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Collections.consumeRemaining(permissionAppsAdapter.getData(),
                            new Consumer<CommonPackageInfo>() {
                                @Override
                                public void accept(final CommonPackageInfo info) {
                                    String pkg = info.getPkgName();
                                    int mode = b ? AppOpsManagerCompat.MODE_ALLOWED
                                            : AppOpsManagerCompat.MODE_IGNORED;
                                    XAshmanManager.get().setPermissionControlBlockModeForPkg(op, pkg, mode);
                                    runOnUiThreadChecked(new Runnable() {
                                        @Override
                                        public void run() {
                                            p.setMessage(info.getAppName());
                                        }
                                    });
                                }
                            });
                } finally {
                    runOnUiThreadChecked(new Runnable() {
                        @Override
                        public void run() {
                            p.dismiss();
                        }
                    });
                }
            }
        });
    }

    protected List<CommonPackageInfo> performLoading() {
        return PermissionLoader.Impl.create(this).loadByOp(op, 0);
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
                new Consumer<CommonPackageInfo>() {
                    @Override
                    public void accept(CommonPackageInfo info) {
                        if (info.getAppName().toLowerCase().contains(query.toLowerCase())) {
                            res.add(info);
                        }
                    }
                });
        return res;
    }

    int indexOf(final CommonPackageInfo pkg) {
        return permissionAppsAdapter.getData().indexOf(pkg);
    }
}
