package github.tornaco.xposedmoduletest.ui.activity.common;

import android.app.ProgressDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.newstand.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.provider.AppSettings;
import github.tornaco.xposedmoduletest.ui.activity.NeedLockActivity;
import github.tornaco.xposedmoduletest.ui.adapter.common.CommonPackageInfoAdapter;
import github.tornaco.xposedmoduletest.ui.widget.SwitchBar;
import github.tornaco.xposedmoduletest.util.XExecutor;
import ir.mirrajabi.searchdialog.SimpleSearchDialogCompat;
import ir.mirrajabi.searchdialog.core.BaseSearchDialogCompat;
import ir.mirrajabi.searchdialog.core.SearchResultListener;
import lombok.Getter;

/**
 * Created by guohao4 on 2017/12/18.
 * Email: Tornaco@163.com
 */

public abstract class CommonPackageInfoListActivity extends NeedLockActivity
        implements CommonPackageInfoAdapter.ChoiceModeListener {

    protected FloatingActionButton fab;

    private SwipeRefreshLayout swipeRefreshLayout;

    @Getter
    protected CommonPackageInfoAdapter commonPackageInfoAdapter;

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

    @Override
    protected boolean isLockNeeded() {
        return false;
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
        final ArrayList<CommonPackageInfo> adapterData = (ArrayList<CommonPackageInfo>)
                getCommonPackageInfoAdapter().getCommonPackageInfos();

        final SimpleSearchDialogCompat<CommonPackageInfo> searchDialog =
                new SimpleSearchDialogCompat(getActivity(), getString(R.string.title_search),
                        getString(R.string.title_search_hint), null, adapterData,
                        new SearchResultListener<CommonPackageInfo>() {

                            @Override
                            public void onSelected(BaseSearchDialogCompat baseSearchDialogCompat,
                                                   CommonPackageInfo info, int i) {
                                int index = indexOf(info);
                                getRecyclerView().scrollToPosition(index);
                                getCommonPackageInfoAdapter().setSelection(index);
                                baseSearchDialogCompat.dismiss();
                            }
                        });


        searchDialog.show();
        searchDialog.getSearchBox().setTypeface(Typeface.SERIF);
    }

    private int indexOf(final CommonPackageInfo pkg) {
        return getCommonPackageInfoAdapter().getCommonPackageInfos().indexOf(pkg);
    }

    protected void initView() {
        recyclerView = findViewById(R.id.recycler_view);
        swipeRefreshLayout = findViewById(R.id.swipe);
        swipeRefreshLayout.setColorSchemeColors(getResources().getIntArray(R.array.polluted_waves));
        fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFabClick();
            }
        });

        commonPackageInfoAdapter = onCreateAdapter();
        commonPackageInfoAdapter.setChoiceModeListener(this);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(commonPackageInfoAdapter);


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
                onInitSwitchBar(switchBar);
            }
        });

        setupSummaryView();
    }

    protected void onFabClick() {
        if (commonPackageInfoAdapter.isChoiceMode()) {
            onRequestClearItems();
        } else {
            onRequestPick();
        }
    }

    private void onRequestClearItems() {
        final ProgressDialog p = new ProgressDialog(getActivity());
        p.setCancelable(false);
        p.setIndeterminate(true);
        p.setMessage(getString(R.string.message_saving_changes));
        p.show();

        XExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (isDestroyed()) return;
                    onRequestClearItemsInBackground();
                } catch (final Throwable e) {
                    Logger.e("onRequestClearItems: " + Logger.getStackTraceString(e));
                    if (isDestroyed()) return;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showSimpleDialog(getString(R.string.title_error_occur), Logger.getStackTraceString(e));
                        }
                    });
                } finally {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            p.dismiss();
                            getCommonPackageInfoAdapter().onBackPressed();// Leave choice mode ugly.
                        }
                    });
                }

            }
        });
    }

    protected void onRequestClearItemsInBackground() {
    }

    protected void onInitSwitchBar(SwitchBar switchBar) {
    }

    protected void onRequestPick() {
    }

    protected void setupSummaryView() {
        String who = getClass().getSimpleName();
        boolean showInfo = AppSettings.isShowInfoEnabled(this, who);
        TextView textView = findViewById(R.id.summary);
        if (!showInfo) {
            textView.setVisibility(View.GONE);
        } else {
            @StringRes
            int strId = getSummaryRes();
            if (strId > 0) {
                textView.setVisibility(View.VISIBLE);
                textView.setText(strId);
            } else {
                textView.setVisibility(View.GONE);
            }
        }
    }

    protected @StringRes
    abstract int getSummaryRes();

    protected abstract CommonPackageInfoAdapter onCreateAdapter();

    protected void startLoading() {
        swipeRefreshLayout.setRefreshing(true);
        XExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final List<? extends CommonPackageInfo> res = performLoading();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                        commonPackageInfoAdapter.update(res);
                    }
                });
            }
        });
    }

    protected abstract List<? extends CommonPackageInfo> performLoading();

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        if (item.getItemId() == R.id.action_info) {
            String who = getClass().getSimpleName();
            AppSettings.setShowInfo(this, who, !AppSettings.isShowInfoEnabled(this, who));
            setupSummaryView();
        }
        if (item.getItemId() == R.id.action_search) {
            onRequestSearch();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (getCommonPackageInfoAdapter().onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onEnterChoiceMode() {
        fab.hide(new FloatingActionButton.OnVisibilityChangedListener() {
            @Override
            public void onHidden(FloatingActionButton fab) {
                super.onHidden(fab);
                fab.setImageResource(R.drawable.ic_clear_all_black_24dp);
                fab.show();
            }
        });
    }

    @Override
    public void onLeaveChoiceMode() {
        fab.hide(new FloatingActionButton.OnVisibilityChangedListener() {
            @Override
            public void onHidden(FloatingActionButton fab) {
                super.onHidden(fab);
                fab.setImageResource(R.drawable.ic_add_black_24dp);
                fab.show();

                startLoading();
            }
        });
    }
}
