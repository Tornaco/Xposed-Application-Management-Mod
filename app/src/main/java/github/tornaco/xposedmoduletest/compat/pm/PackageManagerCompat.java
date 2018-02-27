package github.tornaco.xposedmoduletest.compat.pm;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;

import com.google.common.base.Preconditions;
import com.jaredrummler.android.shell.CommandResult;
import com.jaredrummler.android.shell.Shell;

import org.newstand.logger.Logger;

import java.io.File;

import github.tornaco.xposedmoduletest.util.XExecutor;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;

/**
 * Created by guohao4 on 2017/12/4.
 * Email: Tornaco@163.com
 */

public class PackageManagerCompat {

    public static final int NO_ERR = 0;
    public static final int ERR_NOT_SYSTEM_APP = 0x1;
    public static final int ERR_PATH_NOT_FOUND = 0x2;
    public static final int ERR_SHELL_FAILURE = 0x3;

    public interface UnInstallCallback {
        void onSuccess();

        void onFail(int err);

        void maybeSuccess();
    }

    public static void showAppDetails(Context context, String pkg) {
        String appName = String.valueOf(PkgUtil.loadNameByPkgName(context, pkg));
        String versionName = PkgUtil.loadVersionNameByPkgName(context, pkg);
        int versionCode = PkgUtil.loadVersionCodeByPkgName(context, pkg);
        String appPath = PkgUtil.pathOf(context, pkg);
        new AlertDialog.Builder(context)
                .setTitle(appName)
                .setMessage(String.format("Version code: %s \nVersion name: %s \nApk path: %s \nPackageName: %s",
                        versionCode, versionName, appPath, pkg))
                .setCancelable(true)
                .show();
    }

    public static void unInstallUserAppWithIntent(Context context, String pkg) {
        Uri packageURI = Uri.parse("package:" + pkg);
        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
        uninstallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(uninstallIntent);
    }

    @SuppressLint("StaticFieldLeak")
    public static void unInstallSystemApp(Context context, String pkg,
                                          final UnInstallCallback callback) {

        Preconditions.checkNotNull(pkg);
        Preconditions.checkNotNull(callback);

        if (!PkgUtil.isSystemApp(context, pkg)) {
            callback.onFail(ERR_NOT_SYSTEM_APP);
            return;
        }

        String appPath = PkgUtil.pathOf(context, pkg);
        if (appPath == null) {
            callback.onFail(ERR_PATH_NOT_FOUND);
            return;
        }

        final ProgressDialog p = new ProgressDialog(context);
        p.setMessage("...");

        new AsyncTask<String, Void, Integer>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                p.show();
            }

            @Override
            protected Integer doInBackground(String... paths) {

                File file = new File(paths[0]);
                boolean success;
                // mount -o rw,remount /system
                CommandResult c = Shell.SU.run(
                        "mount -o rw,remount /system",
                        "rm -rf " + file.getPath());
                success = c.isSuccessful();

                Logger.wtf(c.getStdout());
                Logger.wtf(c.getStderr());

                // Do do this too fast.
                callback.maybeSuccess();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {

                }

                return success ? NO_ERR : ERR_SHELL_FAILURE;
            }

            @Override
            protected void onPostExecute(Integer integer) {
                super.onPostExecute(integer);
                p.dismiss();
                if (integer == NO_ERR) {
                    callback.onSuccess();
                } else {
                    callback.onFail(integer);
                }
            }
        }.executeOnExecutor(XExecutor.getService(), appPath);


    }
}
