package github.tornaco.xposedmoduletest.ui.activity.app;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.nononsenseapps.filepicker.FilePickerActivity;
import com.nononsenseapps.filepicker.Utils;

import org.newstand.logger.Logger;

import java.io.File;
import java.util.List;

import dev.nick.tiles.tile.Category;
import dev.nick.tiles.tile.DashboardFragment;
import github.tornaco.permission.requester.RequiresPermission;
import github.tornaco.permission.requester.RuntimePermissions;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.backup.DataBackup;
import github.tornaco.xposedmoduletest.ui.activity.WithWithCustomTabActivity;
import github.tornaco.xposedmoduletest.ui.tiles.app.AutoApplyAppSettingsTemplate;
import github.tornaco.xposedmoduletest.ui.tiles.app.AutoBlackNotification;
import github.tornaco.xposedmoduletest.ui.tiles.app.Backup;
import github.tornaco.xposedmoduletest.ui.tiles.app.IconPack;
import github.tornaco.xposedmoduletest.ui.tiles.app.PowerSave;
import github.tornaco.xposedmoduletest.ui.tiles.app.Restore;
import github.tornaco.xposedmoduletest.ui.tiles.app.RestoreDefault;
import github.tornaco.xposedmoduletest.ui.tiles.app.ShowTileDivider;
import github.tornaco.xposedmoduletest.ui.tiles.app.ThemeChooser;
import github.tornaco.xposedmoduletest.ui.tiles.app.WhiteSystemApp;
import github.tornaco.xposedmoduletest.util.XExecutor;
import lombok.Synchronized;

/**
 * Created by guohao4 on 2017/11/2.
 * Email: Tornaco@163.com
 */
@RuntimePermissions
public class SettingsDashboardActivity extends WithWithCustomTabActivity
        implements DataBackup.BackupRestoreListener {

    private ProgressDialog mProgressDialog;

    @Synchronized
    private void showProgressDialog() {
        cancelProgressDialog();
        if (isDestroyed()) return;

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage(getString(R.string.message_saving_changes));
        mProgressDialog.show();
    }

    @Synchronized
    private void cancelProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @RequiresPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE})
    void performBackup(final File dir) {
        XExecutor.execute(new Runnable() {
            @Override
            public void run() {
                DataBackup.performBackup(getContext(), dir, SettingsDashboardActivity.this);
            }
        });
    }

    @RequiresPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE})
    void performRestore(final File zipFile) {
        XExecutor.execute(new Runnable() {
            @Override
            public void run() {
                DataBackup.performRestore(getContext(), zipFile, SettingsDashboardActivity.this);
            }
        });
    }

    public void onRequestBackup() {
        SettingsDashboardActivityPermissionRequester.onRequestBackupInternalChecked(this);
    }


    public void onRequestRestore() {
        SettingsDashboardActivityPermissionRequester.onRequestRestoreInternalChecked(this);
    }

    @RequiresPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE})
    public void onRequestBackupInternal() {
        pickSingleFile(this, REQUEST_CODE_PICK_BACKUP_DIR, FilePickerActivity.MODE_DIR);
    }

    @RequiresPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE})
    public void onRequestRestoreInternal() {
        pickSingleFile(this, REQUEST_CODE_PICK_RESTORE_FILE, FilePickerActivity.MODE_FILE);
    }

    private static final int REQUEST_CODE_PICK_BACKUP_DIR = 0x111;
    private static final int REQUEST_CODE_PICK_RESTORE_FILE = 0x112;

    private static void pickSingleFile(Activity activity, int code, int mode) {
        // This always works
        Intent i = new Intent(activity, FilePickerActivity.class);
        // This works if you defined the intent filter
        // Intent i = new Intent(Intent.ACTION_GET_CONTENT);

        // Set these depending on your use case. These are the defaults.
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
        i.putExtra(FilePickerActivity.EXTRA_MODE, mode);

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
        if (requestCode == REQUEST_CODE_PICK_BACKUP_DIR && resultCode == Activity.RESULT_OK) {
            // Use the provided utility method to parse the result
            List<Uri> files = Utils.getSelectedFilesFromResult(data);
            File file = Utils.getFileForUri(files.get(0));
            onBackupDirPicked(file);
        }

        if (requestCode == REQUEST_CODE_PICK_RESTORE_FILE && resultCode == Activity.RESULT_OK) {
            // Use the provided utility method to parse the result
            List<Uri> files = Utils.getSelectedFilesFromResult(data);
            File file = Utils.getFileForUri(files.get(0));
            onRestoreFilePicked(file);
        }
    }

    private void onRestoreFilePicked(File file) {
        SettingsDashboardActivityPermissionRequester.performRestoreChecked(file, this);
    }

    private void onBackupDirPicked(File file) {
        SettingsDashboardActivityPermissionRequester.performBackupChecked(file, this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        SettingsDashboardActivityPermissionRequester.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_with_appbar_template);
        setupToolbar();
        showHomeAsUp();
        replaceV4(R.id.container, new SettingsNavFragment(), null, false);
    }

    @Override
    public void onDataBackupFail(final Throwable e) {
        runOnUiThreadChecked(new Runnable() {
            @Override
            public void run() {
                cancelProgressDialog();
                Toast.makeText(getActivity(),
                        getString(R.string.title_export_or_import_fail) + "\n" +
                                Logger.getStackTraceString(e), Toast.LENGTH_SHORT).show();
            }
        });
        Logger.e("Fail backup: " + Logger.getStackTraceString(e));
    }

    @Override
    public void onDataBackupSuccess() {
        runOnUiThreadChecked(new Runnable() {
            @Override
            public void run() {
                cancelProgressDialog();
                Toast.makeText(getActivity(), R.string.title_backup_restore_success, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onProgress(float progress) {
        if (progress == 0f) {
            runOnUiThreadChecked(new Runnable() {
                @Override
                public void run() {
                    showProgressDialog();
                }
            });
        }
    }

    public static class SettingsNavFragment extends DashboardFragment {
        @Override
        protected void onCreateDashCategories(List<Category> categories) {
            super.onCreateDashCategories(categories);

            Category system = new Category();
            system.titleRes = R.string.title_opt;
            system.addTile(new PowerSave(getActivity()));

            Category systemProtect = new Category();
            systemProtect.titleRes = R.string.title_app_settings;
            systemProtect.addTile(new WhiteSystemApp(getActivity()));
            systemProtect.addTile(new AutoApplyAppSettingsTemplate(getActivity()));
            systemProtect.addTile(new AutoBlackNotification(getActivity()));

            Category data = new Category();
            data.titleRes = R.string.title_data;
            data.addTile(new RestoreDefault(getActivity()));
            data.addTile(new Restore(getActivity()));
            Category dataHook = new Category();
            dataHook.addTile(new Backup(getActivity()));

            Category theme = new Category();
            theme.titleRes = R.string.title_style;
            theme.addTile(new ThemeChooser(getActivity()));
            theme.addTile(new ShowTileDivider(getActivity()));
            theme.addTile(new IconPack(getActivity()));

            categories.add(system);
            categories.add(systemProtect);
            categories.add(data);
            categories.add(dataHook);
            categories.add(theme);
        }
    }


}
