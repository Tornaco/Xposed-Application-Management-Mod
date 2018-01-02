package github.tornaco.xposedmoduletest.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import org.newstand.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.bean.AccessInfo;
import github.tornaco.xposedmoduletest.bean.DaoManager;
import github.tornaco.xposedmoduletest.bean.DaoSession;
import github.tornaco.xposedmoduletest.provider.AppSettings;
import github.tornaco.xposedmoduletest.ui.Themes;
import github.tornaco.xposedmoduletest.ui.adapter.PhotoListAdapter;
import github.tornaco.xposedmoduletest.util.XExecutor;

/**
 * Created by guohao4 on 2017/10/20.
 * Email: Tornaco@163.com
 */

public class PhotoViewerActivity extends BaseActivity {

    private SwipeRefreshLayout swipeRefreshLayout;

    protected PhotoListAdapter appListAdapter;

    @Override
    protected int getUserSetThemeResId(Themes themes) {
        return themes.getThemeStyleRes();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_viewer);
        initView();
        startLoading();
    }

    @SuppressWarnings("ConstantConditions")
    protected void initView() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        swipeRefreshLayout = findViewById(R.id.swipe);
        swipeRefreshLayout.setColorSchemeColors(getResources().getIntArray(R.array.polluted_waves));

        appListAdapter = onCreateAdapter();
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1,
                LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(appListAdapter);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startLoading();
            }
        });
    }

    protected PhotoListAdapter onCreateAdapter() {
        return new PhotoListAdapter(this) {
            @Override
            protected void onAccessInfoDetele(AccessInfo info) {
                super.onAccessInfoDetele(info);
                Snackbar.make(swipeRefreshLayout, R.string.title_delete_success, Snackbar.LENGTH_SHORT).show();
                startLoading();
            }
        };
    }

    private void startLoading() {
        swipeRefreshLayout.setRefreshing(true);
        XExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final List<AccessInfo> res = performLoading();
                Logger.v("Photo load complete:" + res.size());
                AppSettings.setShowInfo(getContext(), "new_photo", false);
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

    private List<AccessInfo> performLoading() {
        DaoSession session = DaoManager.getInstance().getSession(this);
        if (session == null) return new ArrayList<>();
        return session.getAccessInfoDao().loadAll();
    }
}
