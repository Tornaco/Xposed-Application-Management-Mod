package github.tornaco.xposedmoduletest.ui.activity.workflow;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.support.v7.app.AlertDialog;

import github.tornaco.xposedmoduletest.IJsEvaluateListener;
import github.tornaco.xposedmoduletest.R;

/**
 * Created by Tornaco on 2018/6/20 15:12.
 * This file is writen for project X-APM at host guohao4.
 */
public class DialogEvaluateListener extends IJsEvaluateListener.Stub {

    private Handler mUiHandler = new Handler(Looper.getMainLooper());

    private Context context;

    DialogEvaluateListener(Activity context) {
        this.context = context;
    }

    @Override
    public void onFinish(String res) throws RemoteException {
        mUiHandler.post(() -> new AlertDialog.Builder(context)
                .setMessage(context.getString(R.string.message_workflow_run_finish, res))
                .setPositiveButton(android.R.string.ok, null)
                .show());
    }

    @Override
    public void onError(String message, String trace) throws RemoteException {
        mUiHandler.post(() -> new AlertDialog.Builder(context)
                .setMessage(context.getString(R.string.message_workflow_run_err, message))
                .setPositiveButton(android.R.string.ok, null)
                .show());
    }
}
