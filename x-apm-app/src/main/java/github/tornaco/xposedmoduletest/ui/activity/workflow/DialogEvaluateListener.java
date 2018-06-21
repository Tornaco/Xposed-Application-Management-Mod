package github.tornaco.xposedmoduletest.ui.activity.workflow;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;

import github.tornaco.xposedmoduletest.IJsEvaluateListener;
import github.tornaco.xposedmoduletest.R;
import lombok.Setter;

/**
 * Created by Tornaco on 2018/6/20 15:12.
 * This file is writen for project X-APM at host guohao4.
 */
public class DialogEvaluateListener extends IJsEvaluateListener.Stub {

    private Handler mUiHandler = new Handler(Looper.getMainLooper());

    private Context context;

    @Setter
    private boolean showDialogMessageOnFinish = false, showDialogMessageOnError = true;

    DialogEvaluateListener(Activity context) {
        this.context = context;
    }

    @Override
    public void onFinish(String res) {
        if (showDialogMessageOnFinish) mUiHandler.post(() -> new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.message_workflow_run_finish, ""))
                .setMessage(res)
                .setPositiveButton(android.R.string.ok, null)
                .show());
    }

    @Override
    public void onError(String message, String trace) {
        if (showDialogMessageOnError) mUiHandler.post(() -> new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.message_workflow_run_err, ""))
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .show());
    }
}
