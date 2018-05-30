package github.tornaco.xposedmoduletest.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.nononsenseapps.filepicker.FilePickerActivity;
import com.nononsenseapps.filepicker.Utils;

import org.newstand.logger.Logger;

import java.io.File;
import java.util.List;

import dev.nick.tiles.tile.Category;
import github.tornaco.permission.requester.RequiresPermission;
import github.tornaco.permission.requester.RuntimePermissions;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.backup.DataBackup;
import github.tornaco.xposedmoduletest.compat.pm.PackageManagerCompat;
import github.tornaco.xposedmoduletest.ui.AppCustomDashboardFragment;
import github.tornaco.xposedmoduletest.ui.tiles.app.Backup;
import github.tornaco.xposedmoduletest.ui.tiles.app.Restore;
import github.tornaco.xposedmoduletest.ui.tiles.app.RestoreDefault;
import github.tornaco.xposedmoduletest.ui.tiles.app.UnInstallAPM;
import github.tornaco.xposedmoduletest.util.XExecutor;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import lombok.Synchronized;

/**
 * Created by guohao4 on 2017/9/7.
 * Email: Tornaco@163.com
 */
@RuntimePermissions
public class BackupRestoreSettingsActivity extends BaseActivity implements
        DataBackup.BackupRestoreListener {

    public static void start(Context context) {
        Intent starter = new Intent(context, BackupRestoreSettingsActivity.class);
        starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_with_appbar_template);
        setupToolbar();
        showHomeAsUp();
        replaceV4(R.id.container, onCreateSettingsFragment(), null, false);
    }

    // Settings block.
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
        XExecutor.execute(() -> DataBackup.performBackup(getContext(), dir, BackupRestoreSettingsActivity.this));
    }

    @RequiresPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE})
    void performRestore(final File zipFile) {
        XExecutor.execute(() -> DataBackup.performRestore(getContext(), zipFile, BackupRestoreSettingsActivity.this));
    }

    public void onRequestBackup() {
        BackupRestoreSettingsActivityPermissionRequester.onRequestBackupInternalChecked(this);
    }


    public void onRequestRestore() {
        BackupRestoreSettingsActivityPermissionRequester.onRequestRestoreInternalChecked(this);
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
        BackupRestoreSettingsActivityPermissionRequester.performRestoreChecked(file, this);
    }

    private void onBackupDirPicked(File file) {
        BackupRestoreSettingsActivityPermissionRequester.performBackupChecked(file, this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        BackupRestoreSettingsActivityPermissionRequester.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onDataBackupFail(final Throwable e) {
        runOnUiThreadChecked(() -> {
            cancelProgressDialog();
            Toast.makeText(getActivity(),
                    getString(R.string.title_export_or_import_fail) + "\n" +
                            Logger.getStackTraceString(e), Toast.LENGTH_SHORT).show();
        });
        Logger.e("Fail backup: " + Logger.getStackTraceString(e));
    }

    @Override
    public void onDataBackupSuccess() {
        runOnUiThreadChecked(() -> {
            cancelProgressDialog();
            Toast.makeText(getActivity(), R.string.title_backup_restore_success, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onProgress(float progress) {
        if (progress == 0f) {
            runOnUiThreadChecked(this::showProgressDialog);
        }
    }

    public void onRequestUninstalledAPM() {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.title_uninstall_apm)
                .setMessage(getString(R.string.message_uninstall_apm))
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    if (XAPMManager.get().isServiceAvailable()) {
                        XAPMManager.get().restoreDefaultSettings();
                        Toast.makeText(getContext(), R.string.summary_restore_done, Toast.LENGTH_SHORT).show();
                    }
                    PackageManagerCompat.unInstallUserAppWithIntent(getContext(), BuildConfig.APPLICATION_ID);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    protected Fragment onCreateSettingsFragment() {
        return new SettingsNavFragment();
    }

    public static class SettingsNavFragment
            extends AppCustomDashboardFragment {

        @Override
        protected boolean androidPStyleIcon() {
            return false;
        }

        @Override
        protected void onCreateDashCategories(List<Category> categories) {
            super.onCreateDashCategories(categories);

            Category restore = new Category();
            restore.titleRes = R.string.title_tile_restore;

            restore.addTile(new UnInstallAPM(getActivity()));
            restore.addTile(new RestoreDefault(getActivity()));
            restore.addTile(new Restore(getActivity()));

            Category backup = new Category();
            backup.titleRes = R.string.title_tile_backup;
            backup.addTile(new Backup(getActivity()));

            categories.add(restore);
            categories.add(backup);
        }
    }
}
