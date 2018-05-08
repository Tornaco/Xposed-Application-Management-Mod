package github.tornaco.xposedmoduletest.xposed.service.pm;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;

import github.tornaco.xposedmoduletest.compat.os.AppOpsManagerCompat;
import github.tornaco.xposedmoduletest.xposed.service.AppResource;
import lombok.Getter;
import lombok.ToString;

/**
 * Created by guohao4 on 2018/2/2.
 * Email: Tornaco@163.com
 */
@Getter
@ToString
public class InstallDialog implements View.OnClickListener {

    private PackageInstallerManager.VerifyArgs args;
    private PackageInstallerManager.VerifyReceiver receiver;

    private Dialog d;

    InstallDialog(PackageInstallerManager.VerifyArgs args, PackageInstallerManager.VerifyReceiver receiver) {
        this.args = args;
        this.receiver = receiver;
    }

    public void display(Context context) {
        onCreateVerifierDialog(context);
        if (d != null) {
            d.show();
        } else {
            receiver.onVerifyResult("Dialog is null", AppOpsManagerCompat.MODE_ALLOWED);
        }
    }

    public void dismiss() {
        try {
            if (d != null) {
                d.dismiss();
            }
        } catch (Throwable ignored) {
        }
    }

    private View onCreateAndBindVerifierView(Context context) {
        CheckBox checkBox = new CheckBox(context);
        checkBox.setChecked(false);
        checkBox.setOnClickListener(this);
        return checkBox;
    }

    @SuppressWarnings("ConstantConditions")
    private void onCreateVerifierDialog(Context context) {
        AppResource appResource = new AppResource(context);
        Spanned dialogTitle = Html.fromHtml(appResource.loadStringFromAPMApp("package_install_verify_dialog_title"));
        Spanned dialogMessage = Html.fromHtml(appResource.loadStringFromAPMApp("package_install_verify_dialog_message",
                args.getInstallerAppLabel(), args.getAppLabel()));

        // Check res.
        if (dialogTitle == null || dialogMessage == null) {
            // Allow, we got err:(
            d = null;
            return;
        }

        d = new AlertDialog.Builder(context)
                .setTitle(dialogTitle)
                .setMessage(dialogMessage)
                .setIcon(args.getAppIcon())
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> receiver.onVerifyResult("OK", AppOpsManagerCompat.MODE_ALLOWED))
                .setNegativeButton(android.R.string.no, (dialog, which) -> receiver.onVerifyResult("CANCEL", AppOpsManagerCompat.MODE_IGNORED))
                .create();
        d.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
    }

    @Override
    public void onClick(View v) {
        CheckBox checkBox = (CheckBox) v;
    }
}
