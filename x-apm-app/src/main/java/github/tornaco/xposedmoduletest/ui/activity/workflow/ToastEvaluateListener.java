package github.tornaco.xposedmoduletest.ui.activity.workflow;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.widget.Toast;

import github.tornaco.xposedmoduletest.IJsEvaluateListener;
import github.tornaco.xposedmoduletest.R;

/**
 * Created by Tornaco on 2018/6/20 15:12.
 * This file is writen for project X-APM at host guohao4.
 */
public class ToastEvaluateListener extends IJsEvaluateListener.Stub {

    private Handler mUiHandler = new Handler(Looper.getMainLooper());

    private Context context;

    public ToastEvaluateListener(Context context) {
        this.context = context;
    }

    @Override
    public void onFinish(String res) throws RemoteException {
        mUiHandler.post(() -> Toast.makeText(context, context.getString(R.string.message_workflow_run_finish, res), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onError(String message, String trace) throws RemoteException {
        mUiHandler.post(() -> Toast.makeText(context, context.getString(R.string.message_workflow_run_err, message), Toast.LENGTH_SHORT).show());
    }
}
