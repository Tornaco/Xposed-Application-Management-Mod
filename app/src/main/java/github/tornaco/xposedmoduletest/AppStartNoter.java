package github.tornaco.xposedmoduletest;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.RemoteException;
import android.support.annotation.RequiresApi;
import android.view.WindowManager;

/**
 * Created by guohao4 on 2017/10/17.
 * Email: Tornaco@163.com
 */

@SuppressWarnings("ConstantConditions")
public class AppStartNoter {

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void note(Context context, String pkg, final ICallback callback) {
        AlertDialog d = new AlertDialog.Builder(context)
                .setTitle("XXXXXXX")
                .setMessage(pkg)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            callback.onRes(true);
                        } catch (RemoteException e) {

                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            callback.onRes(false);
                        } catch (RemoteException e) {

                        }
                    }
                })
                .setView(R.layout.app_noter)
                .setCancelable(false)
                .create();
        d.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        d.show();
    }
}
