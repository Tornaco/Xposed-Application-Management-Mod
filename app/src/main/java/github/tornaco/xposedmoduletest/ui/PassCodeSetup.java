package github.tornaco.xposedmoduletest.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.andrognito.pinlockview.IndicatorDots;
import com.andrognito.pinlockview.PinLockListener;
import com.andrognito.pinlockview.PinLockView;

import org.newstand.logger.Logger;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.x.secure.XEnc;
import github.tornaco.xposedmoduletest.x.XSettings;

/**
 * Created by guohao4 on 2017/10/21.
 * Email: Tornaco@163.com
 */

class PassCodeSetup {

    interface SetupListener {
        void onSetSuccess();

        void onSetFail(String reason);
    }

    private String mCurrentPin, mRepeatPin;
    private Context mContext;
    private SetupListener mSetupListener;

    PassCodeSetup(Context context) {
        this.mContext = context;
    }

    void setSetupListener(SetupListener setupListener) {
        this.mSetupListener = setupListener;
    }

    void setup(final boolean isRepeatPhase) {

        @SuppressLint("InflateParams") final View container = LayoutInflater.from(mContext)
                .inflate(R.layout.app_noter, null, false);

        final PinLockView pinLockView = (PinLockView) container.findViewById(R.id.pin_lock_view);
        IndicatorDots indicatorDots = (IndicatorDots) container.findViewById(R.id.indicator_dots);
        pinLockView.attachIndicatorDots(indicatorDots);

        TextView labelView = (TextView) container.findViewById(R.id.label);
        labelView.setText(isRepeatPhase
                ? R.string.title_setup_code_repeat
                : R.string.title_setup_coce_new);

        ImageView imageView = (ImageView) container.findViewById(R.id.icon);
        imageView.setVisibility(View.GONE);

        final Dialog md =
                new AlertDialog.Builder(mContext, R.style.NoterLight)
                        .setView(container)
                        .setCancelable(false)
                        .setNegativeButton(android.R.string.cancel, new
                                DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                        .create();

        pinLockView.setPinLockListener(new PinLockListener() {
            @Override
            public void onComplete(String pin) {
                Logger.v("onComplete:" + pin);
                if (!isRepeatPhase) {
                    mCurrentPin = pin;
                    md.dismiss();
                    if (!XEnc.isPassCodeValid(mCurrentPin)) {
                        mSetupListener.onSetFail(mContext.getString(R.string.title_setup_code_invalid));
                        return;
                    }
                    setup(true);
                } else {
                    mRepeatPin = pin;
                    if (mRepeatPin.equals(mCurrentPin) && XEnc.isPassCodeValid(mRepeatPin)) {
                        XSettings.get().setPassCodeEncrypt(mContext, mCurrentPin);
                        md.dismiss();
                        mSetupListener.onSetSuccess();
                    } else {
                        md.dismiss();
                        mSetupListener.onSetFail(mContext.getString(R.string.title_setup_code_mismatch));
                    }
                }
            }

            @Override
            public void onEmpty() {

            }

            @Override
            public void onPinChange(int pinLength, String intermediatePin) {

            }
        });

        md.show();
    }
}
