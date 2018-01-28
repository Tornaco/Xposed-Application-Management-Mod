package github.tornaco.xposedmoduletest.ui.activity.perm;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import github.tornaco.android.common.Collections;
import github.tornaco.android.common.Consumer;
import github.tornaco.android.common.util.ColorUtil;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.compat.os.AppOpsManagerCompat;
import github.tornaco.xposedmoduletest.loader.PaletteColorPicker;
import github.tornaco.xposedmoduletest.loader.PermissionLoader;
import github.tornaco.xposedmoduletest.model.Permission;
import github.tornaco.xposedmoduletest.provider.XSettings;
import github.tornaco.xposedmoduletest.ui.activity.WithRecyclerView;
import github.tornaco.xposedmoduletest.ui.adapter.PermissionOpsAdapter;
import github.tornaco.xposedmoduletest.util.XExecutor;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;

/**
 * Created by guohao4 on 2017/12/12.
 * Email: Tornaco@163.com
 */

public class Apps2OpListActivity extends WithRecyclerView {

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
        initColor();

        mAppName = String.valueOf(PkgUtil.loadNameByPkgName(this, mPkg));
        setTitle(mAppName);
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


        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        startLoading();
                    }
                });
    }

    private void initColor() {
        // Apply theme color.
        int color = ContextCompat.getColor(this, XSettings.getThemes(this).getThemeColor());

        // Apply palette color.
        PaletteColorPicker.pickPrimaryColor(this, new PaletteColorPicker.PickReceiver() {
            @Override
            public void onColorReady(int color) {
                applyColor(color);
            }
        }, mPkg, color);
    }

    @SuppressWarnings("ConstantConditions")
    private void applyColor(int color) {
        AppBarLayout appBar = findViewById(R.id.appbar);
        if (appBar != null) appBar.setBackgroundColor(color);
        int dark = ColorUtil.colorBurn(color);
        getWindow().setStatusBarColor(dark);
        getWindow().setNavigationBarColor(dark);
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setBackgroundColor(color);
        }
    }

    protected void startLoading() {
        swipeRefreshLayout.setRefreshing(true);
        XExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final List<Permission> res = performLoading();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                        permissionOpsAdapter.update(res);
                        setTitle(mAppName + "\t" + res.size() + "项权限");
                    }
                });
            }
        });
    }

    protected List<Permission> performLoading() {
        return PermissionLoader.Impl.create(this).load(mPkg, 0);
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
                    Collections.consumeRemaining(permissionOpsAdapter.getData(),
                            new Consumer<Permission>() {
                                @Override
                                public void accept(final Permission permission) {
                                    int mode = b ? AppOpsManagerCompat.MODE_ALLOWED
                                            : AppOpsManagerCompat.MODE_IGNORED;
                                    XAshmanManager.get().setPermissionControlBlockModeForPkg(permission.getCode(), mPkg, mode);
                                    runOnUiThreadChecked(new Runnable() {
                                        @Override
                                        public void run() {
                                            p.setMessage(permission.getName());
                                        }
                                    });
                                }
                            });
                } finally {
                    runOnUiThreadChecked(new Runnable() {
                        @Override
                        public void run() {
                            p.dismiss();

                            startLoading();
                        }
                    });
                }
            }
        });
    }
}
