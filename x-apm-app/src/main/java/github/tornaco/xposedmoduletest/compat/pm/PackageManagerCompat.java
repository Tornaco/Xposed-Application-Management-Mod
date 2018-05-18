package github.tornaco.xposedmoduletest.compat.pm;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;

import com.google.common.base.Preconditions;
import com.jaredrummler.android.shell.CommandResult;
import com.jaredrummler.android.shell.Shell;

import org.newstand.logger.Logger;

import java.io.File;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.util.XExecutor;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;

/**
 * Created by guohao4 on 2017/12/4.
 * Email: Tornaco@163.com
 */

public class PackageManagerCompat {

    /**
     * Extra field name for the URI to a verification file. Passed to a package
     * verifier.
     */
    public static final String EXTRA_VERIFICATION_URI = "android.content.pm.extra.VERIFICATION_URI";

    /**
     * Extra field name for the ID of a package pending verification. Passed to
     * a package verifier and is used to call back to
     * {@link PackageManager#verifyPendingInstall(int, int)}
     */
    public static final String EXTRA_VERIFICATION_ID = "android.content.pm.extra.VERIFICATION_ID";

    /**
     * Extra field name for the package identifier which is trying to install
     * the package.
     */
    public static final String EXTRA_VERIFICATION_INSTALLER_PACKAGE
            = "android.content.pm.extra.VERIFICATION_INSTALLER_PACKAGE";

    /**
     * Extra field name for the requested install flags for a package pending
     * verification. Passed to a package verifier.
     */
    public static final String EXTRA_VERIFICATION_INSTALL_FLAGS
            = "android.content.pm.extra.VERIFICATION_INSTALL_FLAGS";

    /**
     * Extra field name for the uid of who is requesting to install
     * the package.
     */
    public static final String EXTRA_VERIFICATION_INSTALLER_UID
            = "android.content.pm.extra.VERIFICATION_INSTALLER_UID";

    /**
     * Extra field name for the package name of a package pending verification.
     */
    public static final String EXTRA_VERIFICATION_PACKAGE_NAME
            = "android.content.pm.extra.VERIFICATION_PACKAGE_NAME";
    /**
     * Extra field name for the result of a verification, either
     * {@link #VERIFICATION_ALLOW}, or {@link #VERIFICATION_REJECT}.
     * Passed to package verifiers after a package is verified.
     */
    public static final String EXTRA_VERIFICATION_RESULT
            = "android.content.pm.extra.VERIFICATION_RESULT";

    /**
     * Extra field name for the version code of a package pending verification.
     */
    public static final String EXTRA_VERIFICATION_VERSION_CODE
            = "android.content.pm.extra.VERIFICATION_VERSION_CODE";

    public static final String EXTRA_INTENT_FILTER_VERIFICATION_ID
            = "android.content.pm.extra.INTENT_FILTER_VERIFICATION_ID";

    /**
     * Extra field name for the scheme used for an intent filter pending verification. Passed to
     * an intent filter verifier and is used to construct the URI to verify against.
     * <p>
     * Usually this is "https"
     */
    public static final String EXTRA_INTENT_FILTER_VERIFICATION_URI_SCHEME
            = "android.content.pm.extra.INTENT_FILTER_VERIFICATION_URI_SCHEME";

    /**
     * Extra field name for the host names to be used for an intent filter pending verification.
     * Passed to an intent filter verifier and is used to construct the URI to verify the
     * intent filter.
     * <p>
     * This is a space delimited list of hosts.
     */
    public static final String EXTRA_INTENT_FILTER_VERIFICATION_HOSTS
            = "android.content.pm.extra.INTENT_FILTER_VERIFICATION_HOSTS";

    /**
     * Extra field name for the package name to be used for an intent filter pending verification.
     * Passed to an intent filter verifier and is used to check the verification responses coming
     * from the hosts. Each host response will need to include the package name of APK containing
     * the intent filter.
     */
    public static final String EXTRA_INTENT_FILTER_VERIFICATION_PACKAGE_NAME
            = "android.content.pm.extra.INTENT_FILTER_VERIFICATION_PACKAGE_NAME";

    /**
     * Usable by the required verifier as the {@code verificationCode} argument
     * for {@link PackageManager#verifyPendingInstall} to indicate that it will
     * allow the installation to proceed without any of the optional verifiers
     * needing to vote.
     */
    public static final int VERIFICATION_ALLOW_WITHOUT_SUFFICIENT = 2;

    /**
     * Used as the {@code verificationCode} argument for
     * {@link PackageManager#verifyPendingInstall} to indicate that the calling
     * package verifier allows the installation to proceed.
     */
    public static final int VERIFICATION_ALLOW = 1;

    /**
     * Used as the {@code verificationCode} argument for
     * {@link PackageManager#verifyPendingInstall} to indicate the calling
     * package verifier does not vote to allow the installation to proceed.
     */
    public static final int VERIFICATION_REJECT = -1;

    /**
     * Used as the {@code verificationCode} argument for
     * {@link PackageManager#verifyIntentFilter} to indicate that the calling
     * IntentFilter Verifier confirms that the IntentFilter is verified.
     */
    public static final int INTENT_FILTER_VERIFICATION_SUCCESS = 1;

    /**
     * Used as the {@code verificationCode} argument for
     * {@link PackageManager#verifyIntentFilter} to indicate that the calling
     * IntentFilter Verifier confirms that the IntentFilter is NOT verified.
     */
    public static final int INTENT_FILTER_VERIFICATION_FAILURE = -1;

    /**
     * Internal status code to indicate that an IntentFilter verification result is not specified.
     */
    public static final int INTENT_FILTER_DOMAIN_VERIFICATION_STATUS_UNDEFINED = 0;

    public static final int INTENT_FILTER_DOMAIN_VERIFICATION_STATUS_ASK = 1;

    public static final int INTENT_FILTER_DOMAIN_VERIFICATION_STATUS_ALWAYS = 2;

    public static final int INTENT_FILTER_DOMAIN_VERIFICATION_STATUS_NEVER = 3;

    public static final int INTENT_FILTER_DOMAIN_VERIFICATION_STATUS_ALWAYS_ASK = 4;

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
                .setMessage(String.format("Version code: %s \nVersion name: %s \nApk path: %s \nPackageName: %s \nuid: %s",
                        versionCode, versionName, appPath, pkg, PkgUtil.uidForPkg(context, pkg)))
                .setCancelable(true)
                .setPositiveButton(context.getString(R.string.title_goto_system_settings), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", pkg, null);
                        intent.setData(uri);
                        context.startActivity(intent);
                    }
                })
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
