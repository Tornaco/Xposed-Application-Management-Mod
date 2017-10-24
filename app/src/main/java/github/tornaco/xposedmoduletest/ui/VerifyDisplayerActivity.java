package github.tornaco.xposedmoduletest.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.CancellationSignal;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.andrognito.pinlockview.IndicatorDots;
import com.andrognito.pinlockview.PinLockListener;
import com.andrognito.pinlockview.PinLockView;

import org.newstand.logger.Logger;

import dev.tornaco.vangogh.Vangogh;
import dev.tornaco.vangogh.display.appliers.ScaleInXYApplier;
import github.tornaco.android.common.Holder;
import github.tornaco.android.common.util.ColorUtil;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.camera.CameraManager;
import github.tornaco.xposedmoduletest.compat.fingerprint.FingerprintManagerCompat;
import github.tornaco.xposedmoduletest.loader.VangoghAppLoader;
import github.tornaco.xposedmoduletest.x.XAppGuardManager;
import github.tornaco.xposedmoduletest.x.XEnc;
import github.tornaco.xposedmoduletest.x.XKey;
import github.tornaco.xposedmoduletest.x.XMode;
import github.tornaco.xposedmoduletest.x.XSettings;

import static github.tornaco.xposedmoduletest.x.XKey.EXTRA_PKG_NAME;
import static github.tornaco.xposedmoduletest.x.XKey.EXTRA_TRANS_ID;

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

    private Handler mHandler;

    private Runnable expireRunnable = new Runnable() {
        @Override
        public void run() {
            onFail();
            finish();
        }
    };

    public static void startAsTest(Context c) {
        Intent intent = new Intent(c, VerifyDisplayerActivity.class);
        intent.putExtra(EXTRA_PKG_NAME, c.getPackageName());
        intent.putExtra(EXTRA_TRANS_ID, 1024);
        c.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_noter_fullscreen);
        resolveIntent(getIntent());
        init(this);
    }

    @SuppressWarnings("ConstantConditions")
    private void init(Context context) {
        AppBarLayout appBar = (AppBarLayout) findViewById(R.id.appbar);
        int color = ContextCompat.getColor(this, XSettings.getThemes(this).getThemeColorRes());
        if (appBar != null) appBar.setBackgroundColor(color);
        ViewGroup infoContainer = (ViewGroup) findViewById(R.id.info);
        infoContainer.setBackgroundColor(color);
        getWindow().setStatusBarColor(ColorUtil.colorBurn(color));
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setBackgroundColor(color);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setTitle(null);
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
                .skipDiskCache(true)
                .skipMemoryCache(true)
                .into(imageView);

        // Setup timeout.
        mHandler = new Handler();
        mHandler.postDelayed(expireRunnable, XAppGuardManager.TRANSACTION_EXPIRE_TIME);
    }

    private void onPass() {
        if (mCancellationSignal != null) {
            mCancellationSignal.cancel();
        }
        XAppGuardManager.from().setResult(tid, XMode.MODE_ALLOWED);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        onFail();
    }

    private void onFail() {
        mHandler.removeCallbacksAndMessages(null);
        if (mCancellationSignal != null) {
            mCancellationSignal.cancel();
        }
        XAppGuardManager.from().setResult(tid, XMode.MODE_DENIED);
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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Logger.d("onNewIntent: %s, %s", pkg, tid);
    }

    private void resolveIntent(Intent intent) {
        Logger.d("before resolveIntent: %s, %s", pkg, tid);
        if (intent == null) return;
        pkg = intent.getStringExtra(EXTRA_PKG_NAME);
        tid = intent.getIntExtra(XKey.EXTRA_TRANS_ID, -1);
        Logger.d("after resolveIntent: %s, %s", pkg, tid);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onFail();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
