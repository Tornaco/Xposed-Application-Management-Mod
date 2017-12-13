package github.tornaco.xposedmoduletest.ui.activity.perm;

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
import android.view.View;
import android.widget.TextView;

import java.util.List;

import github.tornaco.android.common.util.ColorUtil;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.loader.PaletteColorPicker;
import github.tornaco.xposedmoduletest.loader.PermissionLoader;
import github.tornaco.xposedmoduletest.model.Permission;
import github.tornaco.xposedmoduletest.provider.XSettings;
import github.tornaco.xposedmoduletest.ui.activity.WithRecyclerView;
import github.tornaco.xposedmoduletest.ui.adapter.PermissionAdapter;
import github.tornaco.xposedmoduletest.util.XExecutor;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;

/**
 * Created by guohao4 on 2017/12/12.
 * Email: Tornaco@163.com
 */

public class PermissionsListActivity extends WithRecyclerView {

    private static final String EXTRA_PKG = "extra.pkg";

    private SwipeRefreshLayout swipeRefreshLayout;
    private PermissionAdapter permissionAdapter;

    private String mPkg, mAppName;

    public static void start(Context context, String pkg) {
        Intent starter = new Intent(context, PermissionsListActivity.class);
        starter.putExtra(EXTRA_PKG, pkg);
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


        permissionAdapter = onCreateAdapter();
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(permissionAdapter);


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
        int color = ContextCompat.getColor(this, XSettings.getThemes(this).getThemeColorRes());

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
        getWindow().setStatusBarColor(ColorUtil.colorBurn(color));
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
                        permissionAdapter.update(res);
                        setTitle(mAppName + "\t" + res.size() + "项权限");
                    }
                });
            }
        });
    }

    protected List<Permission> performLoading() {
        return PermissionLoader.Impl.create(this).load(mPkg, 0);
    }

    protected PermissionAdapter onCreateAdapter() {
        return new PermissionAdapter(this);
    }

    private void setSummaryView() {
        TextView textView = findViewById(R.id.summary);
        textView.setVisibility(View.GONE);
    }

}
