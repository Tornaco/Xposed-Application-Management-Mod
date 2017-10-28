package github.tornaco.xposedmoduletest.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.pinlockview.IndicatorDots;
import com.andrognito.pinlockview.PinLockListener;
import com.andrognito.pinlockview.PinLockView;

import org.newstand.logger.Logger;

import java.util.List;

import dev.tornaco.vangogh.Vangogh;
import dev.tornaco.vangogh.display.CircleImageEffect;
import dev.tornaco.vangogh.display.ImageEffect;
import dev.tornaco.vangogh.display.appliers.ScaleInXYApplier;
import dev.tornaco.vangogh.media.Image;
import github.tornaco.android.common.Holder;
import github.tornaco.android.common.util.ApkUtil;
import github.tornaco.android.common.util.ColorUtil;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.camera.CameraManager;
import github.tornaco.xposedmoduletest.compat.fingerprint.FingerprintManagerCompat;
import github.tornaco.xposedmoduletest.loader.PaletteColorPicker;
import github.tornaco.xposedmoduletest.loader.VangoghAppLoader;
import github.tornaco.xposedmoduletest.x.XAppGuardManager;
import github.tornaco.xposedmoduletest.x.XEnc;
import github.tornaco.xposedmoduletest.x.XKey;
import github.tornaco.xposedmoduletest.x.XMode;
import github.tornaco.xposedmoduletest.x.XSettings;
import github.tornaco.xposedmoduletest.x.XWatcherMainThreadAdapter;

import static github.tornaco.xposedmoduletest.x.XKey.EXTRA_PKG_NAME;
import static github.tornaco.xposedmoduletest.x.XKey.EXTRA_TRANS_ID;

/**
 * Created by guohao4 on 2017/10/23.
 * Email: Tornaco@163.com
 */

public class VerifyDisplayerActivity extends AppCompatActivity {

    private String pkg;
    private int tid;

    private boolean mTakePhoto, mUsePatternLock;

    private final Holder<String> mPsscode = new Holder<>();

    private CancellationSignal mCancellationSignal;

    private boolean mResNotified = false;

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
        readSettings();
        setContentView(mUsePatternLock ?
                R.layout.app_noter_fullscreen_pattern
                : R.layout.app_noter_fullscreen);
        if (resolveIntent(getIntent())) {
            showVerifyView();
        }
    }

    private void readSettings() {
        this.mTakePhoto = XSettings.get().takenPhotoEnabled(this);
        this.mPsscode.setData(XSettings.getPassCodeEncrypt(this));//FIXME Enc-->NoneEnc
        this.mUsePatternLock = XSettings.get().patternLockEnabled(this);
    }

    private void showVerifyView() {

        // Apply theme color.
        int color = ContextCompat.getColor(this, XSettings.getThemes(this).getThemeColorRes());

        // Apply palette color.
        PaletteColorPicker.pickPrimaryColor(this, new PaletteColorPicker.PickReceiver() {
            @Override
            public void onColorReady(int color) {
                applyColor(color);
            }
        }, pkg, color);

        setTitle(null);

        // Check if our passcode has been set.
        if (!XEnc.isPassCodeValid(mPsscode.getData())) {
            Logger.w("Pass code not valid, ignoring...");
            Toast.makeText(this, R.string.summary_setup_passcode_none_set, Toast.LENGTH_SHORT).show();
            onPass();
            return;
        }

        setupLabel();
        setupIcon();
        setupLockView();
        setupFP();
        setupCamera();

        XAppGuardManager.from().watch(new XWatcherMainThreadAdapter() {
            @Override
            protected void onUserLeavingMainThread(String reason) {
                super.onUserLeavingMainThread(reason);
                XAppGuardManager.from().unWatch(this);
                postDelayed(expireRunnable, 800);
            }
        });
    }

    private void setupIcon() {
        if (XSettings.get().showAppIconEnabled(this)) {
            ImageView imageView = (ImageView) findViewById(R.id.icon);
            Vangogh.with(this)
                    .load(pkg)
                    .placeHolder(0)
                    .fallback(R.mipmap.ic_header_avatar)
                    .usingLoader(new VangoghAppLoader(this))
                    .applier(new ScaleInXYApplier())
                    // FIXME Make it simple.
                    .effect(XSettings.get().cropEnabled(this)
                            ? new CircleImageEffect() : new ImageEffect() {
                        @NonNull
                        @Override
                        public Image process(Context context, @NonNull Image image) {
                            return image;
                        }
                    })
                    .skipDiskCache(true)
                    .skipMemoryCache(true)
                    .into(imageView);
        }
    }

    private void setupLockView() {
        if (mUsePatternLock) {
            setupPatternLockView();
        } else {
            setupPinLockView();
        }
    }

    private void setupPatternLockView() {
        PatternLockView patternLockView =
                (PatternLockView) findViewById(R.id.pattern_lock_view);
        patternLockView.setInputEnabled(true);
        patternLockView.addPatternLockListener(new PatternLockViewListener() {
            @Override
            public void onStarted() {

            }

            @Override
            public void onProgress(List<PatternLockView.Dot> progressPattern) {
                Logger.e("onProgress:" + progressPattern.toString());
            }

            @Override
            public void onComplete(List<PatternLockView.Dot> pattern) {
                Logger.e(pattern.toString());
            }

            @Override
            public void onCleared() {

            }
        });
    }

    private void setupPinLockView() {

        final PinLockView pinLockView = (PinLockView) findViewById(R.id.pin_lock_view);
        IndicatorDots indicatorDots = (IndicatorDots) findViewById(R.id.indicator_dots);
        pinLockView.attachIndicatorDots(indicatorDots);

        pinLockView.setPinLockListener(new PinLockListener() {
            @Override
            public void onComplete(String pin) {
                if (XEnc.isPassCodeCorrect(mPsscode.getData(), pin)) {
                    onPass();
                } else {
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
    }

    private void setupLabel() {
        final TextView labelView = (TextView) findViewById(R.id.label);
        labelView.setText(ApkUtil.loadNameByPkgName(this, pkg));
    }

    private void setupCamera() {
        // Setup camera preview.
        View softwareCameraPreview = findViewById(R.id.surface);
        if (softwareCameraPreview != null)
            softwareCameraPreview.setVisibility(mTakePhoto ? View.VISIBLE : View.GONE);
    }

    private void setupFP() {
        if (XSettings.get().fpEnabled(this)) {
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
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void applyColor(int color) {
        AppBarLayout appBar = (AppBarLayout) findViewById(R.id.appbar);
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
    }

    private void onPass() {
        if (mResNotified) return;
        mResNotified = true;
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
        if (mResNotified) return;
        mResNotified = true;
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

    private boolean resolveIntent(Intent intent) {
        Logger.d("before resolveIntent: %s, %s", pkg, tid);
        if (intent == null) return false;
        pkg = intent.getStringExtra(EXTRA_PKG_NAME);
        tid = intent.getIntExtra(XKey.EXTRA_TRANS_ID, -1);
        Logger.d("after resolveIntent: %s, %s", pkg, tid);
        return pkg != null && tid > 0;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onFail();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
