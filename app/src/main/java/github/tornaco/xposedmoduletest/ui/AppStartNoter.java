package github.tornaco.xposedmoduletest.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.os.CancellationSignal;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.andrognito.pinlockview.IndicatorDots;
import com.andrognito.pinlockview.PinLockListener;
import com.andrognito.pinlockview.PinLockView;

import junit.framework.Assert;

import org.newstand.logger.Logger;

import java.util.Observable;
import java.util.Observer;

import github.tornaco.android.common.Holder;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.camera.CameraManager;
import github.tornaco.xposedmoduletest.compat.fingerprint.FingerprintManagerCompat;
import github.tornaco.xposedmoduletest.x.XEnc;
import github.tornaco.xposedmoduletest.x.XMode;
import github.tornaco.xposedmoduletest.x.XSettings;

/**
 * Created by guohao4 on 2017/10/17.
 * Email: Tornaco@163.com
 */

@SuppressWarnings("ConstantConditions")
public class AppStartNoter {

    private Handler mUiHandler;
    private Context mContext;

    private boolean mTakePhoto, mUseFP;

    private Animation mErrorAnim;

    private final Holder<String> mPsscode = new Holder<>();

    private CancellationSignal mCancellationSignal;

    public AppStartNoter(Handler uiHandler, Context context) {
        this.mUiHandler = uiHandler;
        this.mContext = context;
        Assert.assertTrue(
                "MainLopper is needed",
                this.mUiHandler.getLooper() == Looper.getMainLooper());
        this.mTakePhoto = XSettings.get().takenPhotoEnabled(context);
        this.mPsscode.setData(XSettings.getPassCodeEncrypt(context));//FIXME Enc-->NoneEnc
        this.mErrorAnim = AnimationUtils.loadAnimation(context, R.anim.shake);
        this.mUseFP = XSettings.get().fpEnabled(context);
        registerObserver();
    }

    private void registerObserver() {
        XSettings.get().addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                Logger.d("AppStart noter, settings changed.");
                mTakePhoto = XSettings.get().takenPhotoEnabled(mContext);
                mUseFP = XSettings.get().fpEnabled(mContext);
                mPsscode.setData(XSettings.getPassCodeEncrypt(mContext));//FIXME Enc-->NoneEnc
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void note(
            final String callingAppName,
            final String targetPkg,
            final String appName,
            final Callback callback) {
        mUiHandler.post(new LockDialog(callingAppName, targetPkg, appName, callback));
    }

    public interface Callback {
        void onRes(int res);
    }

    private class LockDialog implements Runnable {

        final String callingAppName;
        final String targetPkg;
        final String appName;
        final Callback callback;

        LockDialog(String callingAppName, String targetPkg, String appName, Callback callback) {
            this.callingAppName = callingAppName;
            this.targetPkg = targetPkg;
            this.appName = appName;
            this.callback = callback;
        }

        @Override
        public void run() {

            try {
                // Check if our passcode has been set.
                if (!XEnc.isPassCodeValid(mPsscode.getData())) {
                    Logger.w("Pass code not valid, ignoring...");
                    Toast.makeText(mContext, R.string.summary_setup_passcode_none_set, Toast.LENGTH_SHORT).show();
                    onPass(callback);
                    return;
                }

                Logger.v("Init note dialog, mFP:" + mUseFP);

                @SuppressLint("InflateParams") final View container = LayoutInflater.from(mContext)
                        .inflate(R.layout.app_noter, null, false);

                final PinLockView pinLockView = (PinLockView) container.findViewById(R.id.pin_lock_view);
                IndicatorDots indicatorDots = (IndicatorDots) container.findViewById(R.id.indicator_dots);
                pinLockView.attachIndicatorDots(indicatorDots);

                TextView labelView = (TextView) container.findViewById(R.id.label);
                labelView.setText(mContext.getString(R.string.input_password, appName));

                final Dialog md =
                        new AlertDialog.Builder(mContext,
                                R.style.NoterLight)
                                .setView(container)
                                .setCancelable(true)
                                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        onFail(callback);
                                    }
                                })
                                .create();

                if (mUseFP)
                    mCancellationSignal = setupFingerPrint(
                            new FingerprintManagerCompat.AuthenticationCallback() {
                                @Override
                                public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
                                    super.onAuthenticationSucceeded(result);
                                    Logger.d("onAuthenticationSucceeded:" + result);
                                    md.dismiss();
                                    onPass(callback);
                                }

                                @Override
                                public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                                    super.onAuthenticationHelp(helpMsgId, helpString);
                                    Logger.i("onAuthenticationHelp:" + helpString);
                                }

                                @Override
                                public void onAuthenticationFailed() {
                                    super.onAuthenticationFailed();
                                    Logger.d("onAuthenticationFailed");
                                }

                                @Override
                                public void onAuthenticationError(int errMsgId, CharSequence errString) {
                                    super.onAuthenticationError(errMsgId, errString);
                                    Logger.d("onAuthenticationError:" + errString);
                                }
                            });

                pinLockView.setPinLockListener(new PinLockListener() {
                    @Override
                    public void onComplete(String pin) {
                        if (XEnc.isPassCodeCorrect(mPsscode.getData(), pin)) {
                            md.dismiss();
                            onPass(callback);
                        } else {
                            container.clearAnimation();
                            container.startAnimation(mErrorAnim);

                            pinLockView.resetPinLockView();

                            if (mTakePhoto) {
                                CameraManager.get().captureSaveAsync(new CameraManager.PictureCallback() {
                                    @Override
                                    public void onImageReady(String path) {
                                        Logger.v("onImageReady:" + path);
                                    }

                                    @Override
                                    public void onFail(Exception e) {
                                        Logger.v("onImageFail:" + e);
                                    }
                                });
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

                Logger.d("Show note dialog...");
                md.show();

                // Setup camera preview.
                View softwareCameraPreview = container.findViewById(R.id.surface);
                if (softwareCameraPreview != null)
                    softwareCameraPreview.setVisibility(mTakePhoto ? View.VISIBLE : View.GONE);
            } catch (Exception e) {
                Logger.e("Can not show dialog:" + Logger.getStackTraceString(e));
                Toast.makeText(mContext, "FATAL- Fail show lock dialog:\n" + Logger.getStackTraceString(e),
                        Toast.LENGTH_LONG).show();
                if (mCancellationSignal != null) mCancellationSignal.cancel();
                // We should tell the res here.
                callback.onRes(XMode.MODE_IGNORED); // BYPASS.
            }
        }
    }

    private void onPass(Callback callback) {
        if (mCancellationSignal != null) {
            mCancellationSignal.cancel();
        }
        callback.onRes(XMode.MODE_ALLOWED);
    }

    private void onFail(Callback callback) {
        if (mCancellationSignal != null) {
            mCancellationSignal.cancel();
        }
        callback.onRes(XMode.MODE_DENIED);
    }

    private CancellationSignal setupFingerPrint(FingerprintManagerCompat.AuthenticationCallback callback) {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.USE_FINGERPRINT)
                != PackageManager.PERMISSION_GRANTED) {
            Logger.w("FP Permission is missing...");
            return null;
        }
        if (!FingerprintManagerCompat.from(mContext).isHardwareDetected()) {
            Logger.w("FP HW is missing...");
            return null;
        }
        CancellationSignal cancellationSignal = new CancellationSignal();
        FingerprintManagerCompat.from(mContext)
                .authenticate(null, 0, cancellationSignal, callback, mUiHandler);
        Logger.i("FP authenticate");
        return cancellationSignal;
    }
}