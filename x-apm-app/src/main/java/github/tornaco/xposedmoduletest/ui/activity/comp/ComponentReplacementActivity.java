package github.tornaco.xposedmoduletest.ui.activity.comp;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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

import com.nononsenseapps.filepicker.FilePickerActivity;
import com.nononsenseapps.filepicker.Utils;

import org.newstand.logger.Logger;

import java.io.File;
import java.util.List;

import github.tornaco.android.common.Collections;
import github.tornaco.android.common.Consumer;
import github.tornaco.permission.requester.RequiresPermission;
import github.tornaco.permission.requester.RuntimePermissions;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.bean.ComponentReplacement;
import github.tornaco.xposedmoduletest.bean.ComponentReplacementList;
import github.tornaco.xposedmoduletest.loader.ComponentReplacementsLoader;
import github.tornaco.xposedmoduletest.provider.AppSettings;
import github.tornaco.xposedmoduletest.remote.ComponentReplacements;
import github.tornaco.xposedmoduletest.ui.activity.WithRecyclerView;
import github.tornaco.xposedmoduletest.ui.adapter.ComponentReplacementListAdapter;
import github.tornaco.xposedmoduletest.util.XExecutor;
import github.tornaco.xposedmoduletest.xposed.app.XAppLockManager;
import github.tornaco.xposedmoduletest.xposed.util.FileUtil;

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

    private boolean firstLoading = true;

    protected void startLoading() {
        swipeRefreshLayout.setRefreshing(true);
        XExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final List<ComponentReplacement> res = performLoading();
                runOnUiThreadChecked(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                        componentReplacementListAdapter.update(res);

                        // Parse the clip.
                        if (firstLoading) parseClipboard();
                        firstLoading = false;
                    }
                });
            }
        });
    }

    private void parseClipboard() {
        ClipboardManager cmb = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (cmb != null && cmb.hasPrimaryClip()) {
            try {
                ClipData.Item item = cmb.getPrimaryClip().getItemAt(0);
                if (item == null) return;
                String content = item.getText().toString();
                Logger.w("content: " + content);

                // Parse.
                ComponentReplacement replacement = ComponentReplacement.fromJson(content);
                if (replacement == null) {
                    return;
                }
                if (replacement.getCompFromClassName() == null || replacement.getCompFromPackageName() == null) {
                    return;
                }
                int index = componentReplacementListAdapter.getData().indexOf(replacement);
                if (index < 0) {
                    onNewComponentReplacementFromClipboardFound(replacement);
                }
            } catch (Exception e) {
                Logger.e(Logger.getStackTraceString(e));
            }
        }
    }

    private void onNewComponentReplacementFromClipboardFound(final ComponentReplacement replacement) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.title_comp_replacement_found)
                        .setMessage(R.string.message_comp_replacement_found)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ComponentName fromCompName = ComponentName.unflattenFromString(replacement.fromFlattenToString());
                                ComponentName toCompName = ComponentName.unflattenFromString(replacement.toFlattenToString());
                                XAppLockManager.get().addOrRemoveComponentReplacement(fromCompName, toCompName, true);
                                startLoading();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ClipboardManager cmb = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                                if (cmb != null) {
                                    cmb.setPrimaryClip(ClipData.newPlainText("ComponentReplacement", "Hooked"));
                                }
                            }
                        })
                        .setCancelable(false)
                        .show();
            }
        });
    }

    protected List<ComponentReplacement> performLoading() {
        return ComponentReplacementsLoader.Impl.create(this).loadAllFromAPMS();
    }

    protected ComponentReplacementListAdapter onCreateAdapter() {
        return
                new ComponentReplacementListAdapter(this) {
                    @Override
                    protected PopupMenu.OnMenuItemClickListener
                    onCreateOnMenuItemClickListener(final ComponentReplacement t) {
                        return XAppLockManager.get().isServiceAvailable() ? new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {

                                if (item.getItemId() == R.id.action_remove) {
                                    ComponentName from = ComponentName.unflattenFromString(t.fromFlattenToString());
                                    if (from != null) {
                                        XAppLockManager.get().addOrRemoveComponentReplacement(from, null, false);
                                        startLoading();
                                    } else {
                                        Toast.makeText(getActivity(), R.string.title_from_comp_null, Toast.LENGTH_SHORT).show();
                                    }
                                } else if (item.getItemId() == R.id.action_edit) {
                                    showAddRuleDialog(t.fromFlattenToString(), t.toFlattenToString());
                                } else if (item.getItemId() == R.id.action_copy_to_clipboard) {
                                    ClipboardManager cmb = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                                    if (cmb != null) {
                                        String content = t.toJson();
                                        Logger.w("content: " + content);
                                        cmb.setPrimaryClip(ClipData.newPlainText("  ComponentReplacement", content));
                                    }
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
            return true;
        }


        if (item.getItemId() == R.id.action_export) {
            onRequestExport();
            return true;
        }

        if (item.getItemId() == R.id.action_import) {
            ComponentReplacementActivityPermissionRequester.importFromFileChecked(this);
            return true;
        }

        if (item.getItemId() == R.id.action_import_old) {
            onRequestMergeOldData();
            return true;
        }

        if (item.getItemId() == R.id.action_pull_from_server) {

            final ProgressDialog d = new ProgressDialog(getActivity());
            d.setIndeterminate(true);
            d.setMessage("....");

            ComponentReplacements.getSingleton().loadAsync(
                    new ComponentReplacements.LoaderListener() {
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

                            performImport(list);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    d.dismiss();
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

    private void onRequestMergeOldData() {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.title_import_old_comp_replacements)
                .setMessage(R.string.message_import_old_comp_replacements)
                .setPositiveButton(R.string.title_import, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mergeOldData();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create()
                .show();
    }

    private void mergeOldData() {
        final ProgressDialog p = new ProgressDialog(getActivity());
        p.setCancelable(false);
        p.setIndeterminate(true);
        XExecutor.execute(new Runnable() {
            @Override
            public void run() {

                try {
                    runOnUiThreadChecked(new Runnable() {
                        @Override
                        public void run() {
                            p.show();
                        }
                    });

                    Collections.consumeRemaining(ComponentReplacementsLoader.Impl.create(getContext())
                            .loadAllFromProvider(), new Consumer<ComponentReplacement>() {
                        @Override
                        public void accept(ComponentReplacement replacement) {
                            ComponentName fromCompName = ComponentName.unflattenFromString(replacement.fromFlattenToString());
                            ComponentName toCompName = ComponentName.unflattenFromString(replacement.toFlattenToString());
                            XAppLockManager.get().addOrRemoveComponentReplacement(fromCompName, toCompName, true);
                        }
                    });

                    if (!isDestroyed()) {
                        runOnUiThreadChecked(new Runnable() {
                            @Override
                            public void run() {
                                startLoading();
                            }
                        });
                    }

                } catch (Exception e) {
                    if (!isDestroyed()) {
                        Toast.makeText(getActivity(), getString(R.string.title_import_fail)
                                + Logger.getStackTraceString(e), Toast.LENGTH_SHORT).show();
                    }
                } finally {
                    p.dismiss();
                }
            }
        });
    }

    private void performImport(ComponentReplacementList list) {
        final int[] count = {0};
        Collections.consumeRemaining(list.getList(), new Consumer<ComponentReplacement>() {
            @Override
            public void accept(ComponentReplacement componentReplacement) {
                try {
                    ComponentName fromCompName = ComponentName.unflattenFromString(componentReplacement.fromFlattenToString());
                    ComponentName toCompName = ComponentName.unflattenFromString(componentReplacement.toFlattenToString());
                    XAppLockManager.get().addOrRemoveComponentReplacement(fromCompName, toCompName, true);
                    count[0]++;
                } catch (Throwable e) {
                    Logger.e("Error add replacement: " + Logger.getStackTraceString(e));
                }
            }
        });

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(),
                        getString(R.string.title_comp_replacement_remote_success,
                                String.valueOf(count[0])),
                        Toast.LENGTH_LONG).show();
                startLoading();
            }
        });
    }

    private void onRequestExport() {

        String[] items = new String[]{"剪贴板", "选择文件位置"};

        AlertDialog dialog = new AlertDialog.Builder(this).setTitle(R.string.title_export)
                .setSingleChoiceItems(items, -1,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();

                                if (which == 0) {
                                    exportToClipboard();
                                } else {
                                    ComponentReplacementActivityPermissionRequester.exportToFileChecked(ComponentReplacementActivity.this);
                                }
                            }
                        }).create();
        dialog.show();
    }

    private void exportToClipboard() {
        XExecutor.execute(new Runnable() {
            @Override
            public void run() {
                List<ComponentReplacement> replacements = componentReplacementListAdapter.getData();
                ComponentReplacementList list = new ComponentReplacementList(replacements);
                final String content = list.toJson();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ClipboardManager cmb = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                        if (cmb != null) {
                            Logger.w("content: " + content);
                            cmb.setPrimaryClip(ClipData.newPlainText("ComponentReplacements", content));
                        }
                    }
                });
            }
        });

    }

    @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void exportToFile() {
        pickSingleDir(this, REQUEST_CODE_PICK_EXPORT_PATH);
    }

    @RequiresPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    void importFromFile() {
        pickSingleFile(this, REQUEST_CODE_PICK_IMPORT_PATH);
    }

    private static final int REQUEST_CODE_PICK_EXPORT_PATH = 0x110;
    private static final int REQUEST_CODE_PICK_IMPORT_PATH = 0x111;

    // FIXME Copy to File utils.
    private static void pickSingleDir(Activity activity, int code) {
        // This always works
        Intent i = new Intent(activity, FilePickerActivity.class);
        // This works if you defined the intent filter
        // Intent i = new Intent(Intent.ACTION_GET_CONTENT);

        // Set these depending on your use case. These are the defaults.
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
        i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);

        // Configure initial directory by specifying a String.
        // You could specify a String like "/storage/emulated/0/", but that can
        // dangerous. Always use Android's API calls to getSingleton paths to the SD-card or
        // internal memory.
        i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());

        activity.startActivityForResult(i, code);
    }

    private static void pickSingleFile(Activity activity, int code) {
        // This always works
        Intent i = new Intent(activity, FilePickerActivity.class);
        // This works if you defined the intent filter
        // Intent i = new Intent(Intent.ACTION_GET_CONTENT);

        // Set these depending on your use case. These are the defaults.
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
        i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);

        // Configure initial directory by specifying a String.
        // You could specify a String like "/storage/emulated/0/", but that can
        // dangerous. Always use Android's API calls to getSingleton paths to the SD-card or
        // internal memory.
        i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());

        activity.startActivityForResult(i, code);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_EXPORT_PATH && resultCode == Activity.RESULT_OK) {
            // Use the provided utility method to parse the result
            List<Uri> files = Utils.getSelectedFilesFromResult(data);
            File file = Utils.getFileForUri(files.get(0));
            onExportFilePick(file);
        }

        if (requestCode == REQUEST_CODE_PICK_IMPORT_PATH && resultCode == Activity.RESULT_OK) {
            // Use the provided utility method to parse the result
            List<Uri> files = Utils.getSelectedFilesFromResult(data);
            File file = Utils.getFileForUri(files.get(0));
            onImportFilePick(file);
        }
    }

    private void onExportFilePick(final File file) {
        XExecutor.execute(new Runnable() {
            @Override
            public void run() {
                List<ComponentReplacement> replacements = componentReplacementListAdapter.getData();
                ComponentReplacementList list = new ComponentReplacementList(replacements);
                final String content = list.toJson();
                final File destFile = new File(file, "component_replacements.json");
                final boolean ok = FileUtil.writeString(content, destFile.getPath());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), ok ? getString(R.string.title_export_success)
                                        + "\t" + destFile.getPath()
                                        : getString(R.string.title_export_fail),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void onImportFilePick(final File file) {
        XExecutor.execute(new Runnable() {
            @Override
            public void run() {
                String content = FileUtil.readString(file.getPath());
                if (TextUtils.isEmpty(content)) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showTips(R.string.title_import_fail_invalid_file, false, null, null);
                        }
                    });

                    return;
                }

                ComponentReplacementList list = ComponentReplacementList.fromJson(content);
                if (list == null || list.getList() == null) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showTips(R.string.title_import_fail_invalid_file, false, null, null);
                        }
                    });

                    return;
                }
                performImport(list);
            }
        });
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

        XAppLockManager.get().addOrRemoveComponentReplacement(fromCompName, toCompName, true);

        startLoading();
    }
}
