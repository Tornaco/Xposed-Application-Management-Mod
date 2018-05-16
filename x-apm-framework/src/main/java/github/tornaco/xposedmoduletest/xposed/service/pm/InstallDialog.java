package github.tornaco.xposedmoduletest.xposed.service.pm;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import github.tornaco.xposedmoduletest.compat.os.XAppOpsManager;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.service.AppResource;
import github.tornaco.xposedmoduletest.xposed.service.ErrorCatchRunnable;
import lombok.Getter;
import lombok.ToString;

import static github.tornaco.xposedmoduletest.xposed.service.pm.PackageInstallerManager.PACKAGE_INSTALL_VERIFY_TIMEOUT_MILLS;
import static github.tornaco.xposedmoduletest.xposed.service.pm.PackageInstallerManager.PACKAGE_INSTALL_VERIFY_TIMEOUT_S;

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
    private ValueAnimator animator;

    InstallDialog(PackageInstallerManager.VerifyArgs args, PackageInstallerManager.VerifyReceiver receiver) {
        this.args = args;
        this.receiver = receiver;
    }

    public void display(Context context) {
        onCreateVerifierDialog(context);
        if (d != null) {
            d.show();
            new ErrorCatchRunnable(() -> startCountdown(context), "startCountdown").run();
        } else {
            receiver.onVerifyResult("Dialog is null", XAppOpsManager.MODE_ALLOWED);
        }
    }

    @SuppressLint("SetTextI18n")
    private void startCountdown(Context context) {
        int titleId = context.getResources().getIdentifier("alertTitle", "id", "android");
        if (titleId > 0) {
            TextView dialogTitle = d.findViewById(titleId);
            if (dialogTitle != null) {
                CharSequence originText = dialogTitle.getText();
                if (originText != null) {
                    animator = ValueAnimator.ofInt((int) PACKAGE_INSTALL_VERIFY_TIMEOUT_S, 0);
                    animator.setDuration(PACKAGE_INSTALL_VERIFY_TIMEOUT_MILLS);
                    animator.setInterpolator(new LinearInterpolator());
                    animator.addUpdateListener(animation -> {
                        if (d != null && d.isShowing()) {
                            dialogTitle.setText(originText + "\t" + String.valueOf(animation.getAnimatedValue()));
                        }
                    });
                    animator.start();
                }
            }
        }
    }

    public void dismiss() {
        try {
            if (d != null) {
                d.dismiss();
            }
            cleanup();
        } catch (Throwable ignored) {
        }
    }

    private void cleanup() {
        try {
            if (animator != null && animator.isRunning()) {
                animator.cancel();
            }
        } catch (Throwable ignored) {
        }
    }

    private View onCreateAndBindVerifierView(Context context) {
        RelativeLayout container = new RelativeLayout(context);

        TextView switcher = new TextView(context);

        ValueAnimator animator = ValueAnimator.ofInt((int) PACKAGE_INSTALL_VERIFY_TIMEOUT_S, 0);
        animator.setDuration(PACKAGE_INSTALL_VERIFY_TIMEOUT_MILLS);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(animation -> switcher.setText(String.valueOf(animation.getAnimatedValue())));
        animator.start();

        container.addView(switcher, generateCenterParams());

        CheckBox checkBox = new CheckBox(context);
        checkBox.setChecked(false);
        checkBox.setText(android.R.string.cut);
        checkBox.setOnClickListener(this);
        return container;
    }

    private RelativeLayout.LayoutParams generateCenterParams() {
        return generateCenterParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private RelativeLayout.LayoutParams generateCenterParams(int w, int h) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(w, h);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        return params;
    }

    @SuppressWarnings("ConstantConditions")
    private void onCreateVerifierDialog(Context context) {

        AppResource appResource = new AppResource(context);

        Spanned dialogTitle = Html.fromHtml(appResource.loadStringFromAPMApp("package_install_verify_dialog_title"));
        Spanned dialogMessage = Html.fromHtml(appResource.loadStringFromAPMApp("package_install_verify_dialog_message",
                args.getInstallerAppLabel(),
                args.getSoucrePath() == null ? "UNKNOWN" : args.getSoucrePath(),
                args.getAppLabel()));

        String allow = appResource.loadStringFromAPMApp("package_install_verify_dialog_allow");
        String deny = appResource.loadStringFromAPMApp("package_install_verify_dialog_deny");
        String alwaysAllow = appResource.loadStringFromAPMApp("package_install_verify_dialog_always_allow");

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
                //.setView(onCreateAndBindVerifierView(context))
                .setPositiveButton(allow, (dialog, which) -> receiver.onVerifyResult("ALLOW", XAppOpsManager.MODE_ALLOWED))
                .setNegativeButton(deny, (dialog, which) -> receiver.onVerifyResult("DENY", XAppOpsManager.MODE_IGNORED))
                .setNeutralButton(alwaysAllow, (dialog, which) -> {
                    receiver.onVerifyResult("ALWAYS ALLOW", XAppOpsManager.MODE_ALLOWED);
                    XAPMManager.get().addOrRemovePackageInstallerVerifyRules("ALWAYS ALLOW " + args.getInstallerPackageName(), true);
                })
                .setOnDismissListener(dialog -> cleanup())
                .create();
        d.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
    }

    @Override
    public void onClick(View v) {
        CheckBox checkBox = (CheckBox) v;
    }
}
