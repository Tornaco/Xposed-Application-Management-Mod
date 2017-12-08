package github.tornaco.xposedmoduletest.ui.activity.comp;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.newstand.logger.Logger;

import java.util.List;

import github.tornaco.android.common.Collections;
import github.tornaco.android.common.Consumer;
import github.tornaco.permission.requester.RuntimePermissions;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.bean.ComponentReplacement;
import github.tornaco.xposedmoduletest.bean.ComponentReplacementList;
import github.tornaco.xposedmoduletest.license.ComponentReplacements;
import github.tornaco.xposedmoduletest.loader.ComponentReplacementsLoader;
import github.tornaco.xposedmoduletest.provider.AppSettings;
import github.tornaco.xposedmoduletest.provider.ComponentsReplacementProvider;
import github.tornaco.xposedmoduletest.ui.activity.WithRecyclerView;
import github.tornaco.xposedmoduletest.ui.adapter.ComponentReplacementListAdapter;
import github.tornaco.xposedmoduletest.util.XExecutor;
import github.tornaco.xposedmoduletest.xposed.app.XAppGuardManager;

/**
 * Created by guohao4 on 2017/11/18.
 * Email: Tornaco@163.com
 */
@RuntimePermissions
public class ComponentReplacementActivity extends WithRecyclerView {

    private SwipeRefreshLayout swipeRefreshLayout;

    protected ComponentReplacementListAdapter componentReplacementListAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.component_replacement_list);
        setupToolbar();
        showHomeAsUp();
        initView();
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

        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddRuleDialog(null, null);
            }
        });


        componentReplacementListAdapter = onCreateAdapter();
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(componentReplacementListAdapter);


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
                final List<ComponentReplacement> res = performLoading();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                        componentReplacementListAdapter.update(res);
                    }
                });
            }
        });
    }

    protected List<ComponentReplacement> performLoading() {
        return ComponentReplacementsLoader.Impl.create(this).loadAll();
    }

    protected ComponentReplacementListAdapter onCreateAdapter() {
        return
                new ComponentReplacementListAdapter(this) {
                    @Override
                    protected PopupMenu.OnMenuItemClickListener
                    onCreateOnMenuItemClickListener(final ComponentReplacement t) {
                        return XAppGuardManager.get().isServiceAvailable() ? new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {

                                if (item.getItemId() == R.id.action_remove) {
                                    ComponentsReplacementProvider.delete(getApplicationContext(), t);

                                    startLoading();
                                } else if (item.getItemId() == R.id.action_edit) {
                                    showAddRuleDialog(t.fromFlattenToString(), t.toFlattenToString());
                                }

                                return true;
                            }
                        } : new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                return false;
                            }
                        };
                    }
                };
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.component_replace, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_info) {
            String who = getClass().getSimpleName();
            AppSettings.setShowInfo(this, who, !AppSettings.isShowInfoEnabled(this, who));
            setSummaryView();
        }

        final ProgressDialog d = new ProgressDialog(getActivity());
        d.setIndeterminate(true);
        d.setMessage("....");

        if (item.getItemId() == R.id.action_pull_from_server) {
            ComponentReplacements.getSingleton().loadAsync(new ComponentReplacements.LoaderListener() {
                @Override
                public void onStartLoading() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            d.show();
                        }
                    });
                }

                @Override
                public void onLoadingComplete(ComponentReplacementList list) {
                    if (list.getList() == null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                d.dismiss();
                                Toast.makeText(getActivity(), R.string.title_comp_replacement_remote_empty, Toast.LENGTH_LONG).show();
                            }
                        });
                        return;
                    }

                    Collections.consumeRemaining(list.getList(), new Consumer<ComponentReplacement>() {
                        @Override
                        public void accept(ComponentReplacement componentReplacement) {
                            try {
                                ComponentName fromCompName = ComponentName.unflattenFromString(componentReplacement.fromFlattenToString());
                                ComponentName toCompName = ComponentName.unflattenFromString(componentReplacement.toFlattenToString());
                                XAppGuardManager.get().addOrRemoveComponentReplacement(fromCompName, toCompName, true);
                            } catch (Throwable e) {
                                Logger.e("Error add replacement: " + Logger.getStackTraceString(e));
                            }
                        }
                    });

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            d.dismiss();
                            Toast.makeText(getActivity(), R.string.title_comp_replacement_remote_success, Toast.LENGTH_LONG).show();
                        }
                    });
                }

                @Override
                public void onError(final Throwable e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            d.dismiss();
                            Toast.makeText(getActivity(), Logger.getStackTraceString(e), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
        }
        return super.onOptionsItemSelected(item);
    }

    private void setSummaryView() {
        String who = getClass().getSimpleName();
        boolean showInfo = AppSettings.isShowInfoEnabled(this, who);
        TextView textView = findViewById(R.id.summary);
        if (!showInfo) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setText(R.string.summary_comp_replacement);
            textView.setVisibility(View.VISIBLE);
        }
    }

    private void showAddRuleDialog(String from, String to) {
        View layout = LayoutInflater.from(this).inflate(R.layout.comp_replace_editor, null, false);

        final AppCompatEditText fromEditText = layout.findViewById(R.id.from_comp);
        fromEditText.setText(from);

        final AppCompatEditText toEditText = layout.findViewById(R.id.to_comp);
        toEditText.setText(to);

        AlertDialog d = new AlertDialog.Builder(ComponentReplacementActivity.this)
                .setTitle(R.string.title_comp_replacement)
                .setView(layout)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onRequestNewRule(fromEditText.getEditableText().toString(), toEditText.getEditableText().toString());
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        d.show();
    }

    private void onRequestNewRule(String from, String to) {
        if (TextUtils.isEmpty(from)) {
            showTips(R.string.title_from_comp_null, false, null, null);
            return;
        }
        ComponentName fromCompName = ComponentName.unflattenFromString(from);

        if (fromCompName == null) {
            showTips(R.string.title_from_comp_invalid, false, null, null);
            return;
        }
        Logger.d("fromCompName: " + fromCompName);

        ComponentName toCompName = ComponentName.unflattenFromString(to);
        if (toCompName == null) {
            showTips(R.string.title_from_comp_to_null, false, null, null);
        }
        Logger.d("toCompName: " + toCompName);

        XAppGuardManager.get().addOrRemoveComponentReplacement(fromCompName, toCompName, true);

        startLoading();
    }
}
