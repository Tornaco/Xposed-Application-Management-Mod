package github.tornaco.xposedmoduletest.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v4.os.CancellationSignal;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.andrognito.pinlockview.IndicatorDots;
import com.andrognito.pinlockview.PinLockListener;
import com.andrognito.pinlockview.PinLockView;

import org.newstand.logger.Logger;

import dev.tornaco.vangogh.Vangogh;
import dev.tornaco.vangogh.display.CircleImageEffect;
import dev.tornaco.vangogh.display.appliers.ScaleInXYApplier;
import github.tornaco.android.common.Holder;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.camera.CameraManager;
import github.tornaco.xposedmoduletest.loader.VangoghAppLoader;
import github.tornaco.xposedmoduletest.x.XAppGuardManager;
import github.tornaco.xposedmoduletest.x.XEnc;
import github.tornaco.xposedmoduletest.x.XKey;
import github.tornaco.xposedmoduletest.x.XMode;
import github.tornaco.xposedmoduletest.x.XSettings;

/**
 * Created by guohao4 on 2017/10/23.
 * Email: Tornaco@163.com
 */

public class VerifyDisplayerActivity extends AppCompatActivity {

    private String pkg;
    private int tid;

    private boolean mTakePhoto;

    private final Holder<String> mPsscode = new Holder<>();

    private CancellationSignal mCancellationSignal;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_noter_fullscreen);
        resolveIntent(getIntent());
        init(this);
    }

    private void init(Context context) {
        this.mTakePhoto = XSettings.get().takenPhotoEnabled(context);
        this.mPsscode.setData(XSettings.getPassCodeEncrypt(context));//FIXME Enc-->NoneEnc
        boolean fpEnabled = XSettings.get().fpEnabled(context);

        // Check if our passcode has been set.
        if (!XEnc.isPassCodeValid(mPsscode.getData())) {
            Logger.w("Pass code not valid, ignoring...");
            Toast.makeText(this, R.string.summary_setup_passcode_none_set, Toast.LENGTH_SHORT).show();
            onPass();
            return;
        }

        final PinLockView pinLockView = (PinLockView) findViewById(R.id.pin_lock_view);
        IndicatorDots indicatorDots = (IndicatorDots) findViewById(R.id.indicator_dots);
        pinLockView.attachIndicatorDots(indicatorDots);

        final TextView labelView = (TextView) findViewById(R.id.label);
        ImageView imageView = (ImageView) findViewById(R.id.icon);

        if (fpEnabled)
            mCancellationSignal = setupFingerPrint(
                    new FingerprintManagerCompat.AuthenticationCallback() {
                        @Override
                        public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
                            super.onAuthenticationSucceeded(result);
                            Logger.d("onAuthenticationSucceeded:" + result);
                            onPass();
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
                    onPass();
                } else {
                    pinLockView.resetPinLockView();
                    labelView.setText(R.string.title_passcode_wrong);
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

        // Setup camera preview.
        View softwareCameraPreview = findViewById(R.id.surface);
        if (softwareCameraPreview != null)
            softwareCameraPreview.setVisibility(mTakePhoto ? View.VISIBLE : View.GONE);

        Vangogh.with(this)
                .load(pkg)
                .placeHolder(0)
                .fallback(R.mipmap.ic_header_avatar)
                .usingLoader(new VangoghAppLoader(this))
                .applier(new ScaleInXYApplier())
                .effect(new CircleImageEffect())
                .into(imageView);
    }

    private void onPass() {
        if (mCancellationSignal != null) {
            mCancellationSignal.cancel();
        }
        XAppGuardManager.get().setResult(tid, XMode.MODE_ALLOWED);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        onFail();
    }

    private void onFail() {
        if (mCancellationSignal != null) {
            mCancellationSignal.cancel();
        }
        XAppGuardManager.get().setResult(tid, XMode.MODE_DENIED);
        finish();
    }

    private CancellationSignal setupFingerPrint(FingerprintManagerCompat.AuthenticationCallback callback) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT)
                != PackageManager.PERMISSION_GRANTED) {
            Logger.w("FP Permission is missing...");
            return null;
        }
        if (!FingerprintManagerCompat.from(this).isHardwareDetected()) {
            Logger.w("FP HW is missing...");
            return null;
        }
        CancellationSignal cancellationSignal = new CancellationSignal();
        FingerprintManagerCompat.from(this)
                .authenticate(null, 0, cancellationSignal, callback, null);
        Logger.i("FP authenticate");
        return cancellationSignal;
    }

    private void resolveIntent(Intent intent) {
        if (intent == null) return;
        pkg = intent.getStringExtra(XKey.EXTRA_PKG_NAME);
        tid = intent.getIntExtra(XKey.EXTRA_TRANS_ID, -1);
        Logger.d("resolveIntent: %s, %s", pkg, tid);
    }
}
