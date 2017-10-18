package github.tornaco.xposedmoduletest.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Handler;
import android.os.RemoteException;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.andrognito.pinlockview.IndicatorDots;
import com.andrognito.pinlockview.PinLockListener;
import com.andrognito.pinlockview.PinLockView;

import org.newstand.logger.Logger;

import github.tornaco.xposedmoduletest.ICallback;
import github.tornaco.xposedmoduletest.R;

/**
 * Created by guohao4 on 2017/10/17.
 * Email: Tornaco@163.com
 */

@SuppressWarnings("ConstantConditions")
public class AppStartNoter {

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void note(Handler handler, final Context context,
                     final String callingAppName,
                     final String appName,
                     final ICallback callback) {

        handler.post(new Runnable() {
            @Override
            public void run() {

                @SuppressLint("InflateParams") View container = LayoutInflater.from(context)
                        .inflate(R.layout.app_noter, null, false);

                PinLockView pinLockView = (PinLockView) container.findViewById(R.id.pin_lock_view);
                IndicatorDots indicatorDots = (IndicatorDots) container.findViewById(R.id.indicator_dots);
                pinLockView.attachIndicatorDots(indicatorDots);


                final AlertDialog d = new AlertDialog.Builder(context)
                        .setTitle(appName)
                        .setView(container)
                        .setCancelable(true)
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                onFail(callback);
                            }
                        })
                        .create();
                d.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

                pinLockView.setPinLockListener(new PinLockListener() {
                    @Override
                    public void onComplete(String pin) {
                        if (pin.equals("6666")) {
                            d.dismiss();
                            onPass(callback);
                        }
                    }

                    @Override
                    public void onEmpty() {

                    }

                    @Override
                    public void onPinChange(int pinLength, String intermediatePin) {

                    }
                });

                try {
                    d.show();
                } catch (Exception e) {
                    Logger.e("Can not show dialog", Logger.getStackTraceString(e));
                    // We should tell the res here.
                    try {
                        callback.onRes(true); // BYPASS.
                    } catch (RemoteException e1) {
                        Logger.e(Logger.getStackTraceString(e1));
                    }
                }
            }
        });
    }

    private void onPass(ICallback callback) {
        try {
            callback.onRes(true);
        } catch (RemoteException e) {
            Logger.e(Logger.getStackTraceString(e));
        }
    }

    private void onFail(ICallback callback) {
        try {
            callback.onRes(false);
        } catch (RemoteException e) {
            Logger.e(Logger.getStackTraceString(e));
        }
    }
}
