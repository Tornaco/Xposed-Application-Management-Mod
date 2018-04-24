package github.tornaco.xposedmoduletest.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.List;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.loader.BlockRecord2Loader;
import github.tornaco.xposedmoduletest.ui.Themes;
import github.tornaco.xposedmoduletest.ui.adapter.BlockRecord2ListAdapter;
import github.tornaco.xposedmoduletest.util.XExecutor;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;
import github.tornaco.xposedmoduletest.xposed.bean.BlockRecord2;

public class BlockRecordViewerActivity extends WithRecyclerView {

    private static final String EXTRA_PKG = "target_pkg";

    private SwipeRefreshLayout swipeRefreshLayout;

    protected BlockRecord2ListAdapter lockKillAppListAdapter;

    private String mTargetPkgName;

    public static void start(Context context, String pkg) {
        Intent intent = new Intent(context, BlockRecordViewerActivity.class);
        intent.putExtra(EXTRA_PKG, pkg);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected int getUserSetThemeResId(Themes themes) {
        return themes.getThemeStyleRes();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutRes());
        showHomeAsUp();
        initView();
        startLoading();
    }

    protected int getLayoutRes() {
        return R.layout.block_record_viewer;
    }

    @Override
    public void onResume() {
        super.onResume();
        startLoading();

        boolean powerSave = XAshmanManager.get().isPowerSaveModeEnabled();
        if (powerSave) {
            Toast.makeText(getContext(), R.string.power_save_no_logs, Toast.LENGTH_SHORT).show();
        }
    }

    protected void initView() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        swipeRefreshLayout = findViewById(R.id.swipe);
        swipeRefreshLayout.setColorSchemeColors(getResources().getIntArray(R.array.polluted_waves));

        lockKillAppListAdapter = onCreateAdapter();
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(lockKillAppListAdapter);


        swipeRefreshLayout.setOnRefreshListener(this::startLoading);
    }


    protected BlockRecord2ListAdapter onCreateAdapter() {
        return new BlockRecord2ListAdapter(this) {
            @Override
            protected void onListItemClick(BlockRecord2 blockRecord) {
                super.onListItemClick(blockRecord);
                String pkg = blockRecord.getPkgName();
                // Only start detailed when this page is not detailed.
                if (pkg != null && mTargetPkgName == null) {
                    BlockRecordViewerActivity.start(getContext(), pkg);
                }
            }
        };
    }

    protected void startLoading() {
        // Reslove intent.
        Intent intent = getIntent();
        mTargetPkgName = intent.getStringExtra(EXTRA_PKG);

        swipeRefreshLayout.setRefreshing(true);
        XExecutor.execute(() -> {
            final List<BlockRecord2> res = performLoading();
            runOnUiThread(() -> {
                swipeRefreshLayout.setRefreshing(false);
                lockKillAppListAdapter.update(res);
            });
        });
    }

    protected List<BlockRecord2> performLoading() {
        return BlockRecord2Loader.Impl.create().loadAll(mTargetPkgName);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
