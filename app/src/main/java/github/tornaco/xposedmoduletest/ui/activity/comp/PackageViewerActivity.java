package github.tornaco.xposedmoduletest.ui.activity.comp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.PopupMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.io.Files;
import com.nononsenseapps.filepicker.FilePickerActivity;
import com.nononsenseapps.filepicker.Utils;

import org.newstand.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;

import github.tornaco.permission.requester.RequiresPermission;
import github.tornaco.permission.requester.RuntimePermissions;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.bean.PackageInfo;
import github.tornaco.xposedmoduletest.compat.os.PowerManagerCompat;
import github.tornaco.xposedmoduletest.compat.pm.PackageManagerCompat;
import github.tornaco.xposedmoduletest.loader.PackageLoader;
import github.tornaco.xposedmoduletest.provider.AppSettings;
import github.tornaco.xposedmoduletest.ui.activity.ag.GuardAppPickerActivity;
import github.tornaco.xposedmoduletest.ui.activity.res.ConfigurationSettingActivity;
import github.tornaco.xposedmoduletest.ui.adapter.GuardAppListAdapter;
import github.tornaco.xposedmoduletest.util.XExecutor;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;

/**
 * Created by guohao4 on 2017/11/18.
 * Email: Tornaco@163.com
 */
@RuntimePermissions
public class PackageViewerActivity extends GuardAppPickerActivity {

    private String disabledString = null;

    private String mAppPackageToExport = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        disabledString = getString(R.string.title_package_disabled);
        findViewById(R.id.fab).setVisibility(View.GONE);
    }

    @Override
    protected GuardAppListAdapter onCreateAdapter() {
        return new GuardAppListAdapter(this) {

            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(final AppViewHolder holder, final int position) {
                super.onBindViewHolder(holder, position);

                // Block all when xash is not running.
                if (!XAshmanManager.get().isServiceAvailable()) return;

                final PackageInfo packageInfo = getPackageInfos().get(position);
                final boolean disabled = packageInfo.isDisabled();
                if (disabled) {
                    holder.getLineOneTextView().setTextColor(Color.RED);
                    holder.getLineOneTextView().setText(packageInfo.getAppName() + disabledString);
                } else {
                    holder.getLineOneTextView().setTextColor(Color.BLACK);
                }
                holder.itemView.setOnLongClickListener(null);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (XAshmanManager.get().isServiceAvailable())
                            showPopMenu(packageInfo, disabled, v);
                    }
                });
            }
        };
    }

    private void showPopMenu(final PackageInfo packageInfo, boolean isDisabledCurrently, View anchor) {
        PopupMenu popupMenu = new PopupMenu(PackageViewerActivity.this, anchor);
        popupMenu.inflate(R.menu.package_viewer_pop);
        if (isDisabledCurrently) {
            popupMenu.getMenu().findItem(R.id.action_enable_app).setVisible(true);
        } else {
            popupMenu.getMenu().findItem(R.id.action_disable_app).setVisible(true);
        }
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_disable_app:
                        XAshmanManager.get().setApplicationEnabledSetting(
                                packageInfo.getPkgName(), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
                        startLoading();
                        break;
                    case R.id.action_enable_app:
                        XAshmanManager.get().setApplicationEnabledSetting(
                                packageInfo.getPkgName(), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 0);
                        startLoading();
                        break;
                    case R.id.action_comp_edit:
                        ComponentEditorActivity.start(getActivity(), packageInfo.getPkgName());
                        break;
                    case R.id.action_comp_uninstall:

                        if (!PkgUtil.isSystemApp(getContext(), packageInfo.getPkgName())) {
                            PackageManagerCompat.unInstallUserAppWithIntent(getContext(), packageInfo.getPkgName());
                        } else {
                            PackageManagerCompat.unInstallSystemApp(PackageViewerActivity.this,
                                    packageInfo.getPkgName(), new PackageManagerCompat.UnInstallCallback() {
                                        @Override
                                        public void onSuccess() {
                                            showTips(R.string.tips_uninstall_sys_app_success,
                                                    true,
                                                    getString(R.string.title_restart_android),
                                                    new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            PowerManagerCompat.restartAndroid();
                                                        }
                                                    });
                                        }

                                        @Override
                                        public void onFail(int err) {
                                            showTips(getString(R.string.tips_uninstall_sys_app_fail) + err,
                                                    true,
                                                    null,
                                                    null);
                                        }

                                        @Override
                                        public void maybeSuccess() {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    startLoading();
                                                }
                                            });
                                        }
                                    });
                        }
                        break;
                    case R.id.action_comp_details:
                        PackageManagerCompat.showAppDetails(getActivity(), packageInfo.getPkgName());
                        break;
                    case R.id.action_comp_export_apk:
                        mAppPackageToExport = packageInfo.getPkgName();
                        PackageViewerActivityPermissionRequester.pickSingleFileChecked
                                (getActivity(), REQUEST_CODE_PICK_APK_EXPORT_PATH,
                                        PackageViewerActivity.this);
                        break;

                    case R.id.action_config_setting:
                        ConfigurationSettingActivity.start(getActivity(), packageInfo.getPkgName());
                        break;
                }
                return true;
            }
        });
        popupMenu.show();
    }

    private static final int REQUEST_CODE_PICK_APK_EXPORT_PATH = 0x111;

    // FIXME Copy to File utils.
    @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    static void pickSingleFile(Activity activity, int requestCode) {
        // This always works
        Intent i = new Intent(activity, FilePickerActivity.class);
        // This works if you defined the intent filter
        // Intent i = new Intent(Intent.ACTION_GET_CONTENT);

        // Set these depending on your use case. These are the defaults.
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
        i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);

        // Configure initial directory by specifying a String.
        // You could specify a String like "/storage/emulated/0/", but that can
        // dangerous. Always use Android's API calls to getSingleton paths to the SD-card or
        // internal memory.
        i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());

        activity.startActivityForResult(i, requestCode);
    }

    @Override
    protected void setSummaryView() {
        super.setSummaryView();
        String who = getClass().getSimpleName();
        boolean showInfo = AppSettings.isShowInfoEnabled(this, who);
        TextView textView = findViewById(R.id.summary);
        if (!showInfo) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setText(R.string.summary_comp_edit);
            textView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.package_viewer_nav, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.show_system_app).setChecked(mShowSystemApp);
        menu.findItem(R.id.action_info).setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings_package_viewer) {
            startActivity(new Intent(this, CompSettingsDashboardActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected List<PackageInfo> performLoading() {
        return PackageLoader.Impl.create(this).loadInstalled(mShowSystemApp);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_APK_EXPORT_PATH && resultCode == Activity.RESULT_OK) {
            // Use the provided utility method to parse the result
            List<Uri> files = Utils.getSelectedFilesFromResult(data);
            File file = Utils.getFileForUri(files.get(0));
            onApkExportPathPick(file);
        }
    }

    private void onApkExportPathPick(final File file) {
        if (mAppPackageToExport == null) {
            Toast.makeText(getContext(), R.string.err_file_not_found, Toast.LENGTH_LONG).show();
            return;
        }
        final String appPath = PkgUtil.pathOf(getContext(), mAppPackageToExport);
        if (appPath == null) {
            Toast.makeText(getContext(), R.string.err_file_not_found, Toast.LENGTH_LONG).show();
            return;
        }
        XExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Files.copy(new File(appPath),
                            new File(file, PkgUtil.loadNameByPkgName(getContext(),
                                    mAppPackageToExport) + ".apk"));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showTips(R.string.title_export_success, false, null, null);
                        }
                    });
                } catch (final IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), Logger.getStackTraceString(e), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }
}
