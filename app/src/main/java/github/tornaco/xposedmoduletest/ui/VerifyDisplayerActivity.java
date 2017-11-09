package github.tornaco.xposedmoduletest.ui;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.CancellationSignal;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.keyguard.KeyguardPatternView;
import com.android.keyguard.KeyguardSecurityCallback;

import org.newstand.logger.Logger;

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
import github.tornaco.xposedmoduletest.util.StopWatch;
import github.tornaco.xposedmoduletest.x.XSettings;
import github.tornaco.xposedmoduletest.x.app.XAppGuardManager;
import github.tornaco.xposedmoduletest.x.app.XMode;
import github.tornaco.xposedmoduletest.x.app.XWatcherAdapter;
import github.tornaco.xposedmoduletest.x.secure.XEnc;

import static github.tornaco.xposedmoduletest.x.app.XAppGuardManager.EXTRA_INJECT_HOME_WHEN_FAIL_ID;
import static github.tornaco.xposedmoduletest.x.app.XAppGuardManager.EXTRA_PKG_NAME;
import static github.tornaco.xposedmoduletest.x.app.XAppGuardManager.EXTRA_TRANS_ID;


/**
 * Created by guohao4 on 2017/10/23.
 * Email: Tornaco@163.com
 */

public class VerifyDisplayerActivity extends BaseActivity {

    private String pkg = null;
    // FIXME, this is not a good solution.
    // Tid is 0 or negative when this is performed when activity resume on user present?
    private int tid = -1;
    private boolean injectHome = false;

    private boolean testMode;

    private boolean mTakePhoto;

    private final Holder<String> mPsscode = new Holder<>();

    private CancellationSignal mCancellationSignal;

    private boolean mResNotified = false;
    private ScreenBroadcastReceiver mScreenBroadcastReceiver;

    public static void startAsTest(Context c) {
        Intent intent = new Intent(c, VerifyDisplayerActivity.class);
        intent.putExtra(EXTRA_PKG_NAME, c.getPackageName());
        intent.putExtra(EXTRA_TRANS_ID, 1024);
        intent.putExtra("extra.test", true);
        c.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        readSettings();
        setContentView(R.layout.verify_displayer);
        if (resolveIntent(getIntent())) {
            showVerifyView();
        }
    }

    private void readSettings() {
        this.mTakePhoto = XSettings.get().takenPhotoEnabled(this);
        this.mPsscode.setData(XSettings.getPattern(this));
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
        if (!testMode && !XEnc.isPassCodeValid(mPsscode.getData())) {
            Logger.w("Pass code not valid, ignoring...");
            Toast.makeText(this, R.string.summary_setup_passcode_none_set, Toast.LENGTH_SHORT).show();
            onPass();
            return;
        }

        setupLabel();
        setupIcon();
        setupLockView();
        setupCamera();

        XAppGuardManager.defaultInstance().watch(new XWatcherAdapter() {
            @Override
            public void onUserLeaving(String reason) throws RemoteException {
                super.onUserLeaving(reason);
                XAppGuardManager.defaultInstance().unWatch(this);
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
        setupPatternLockView();
    }

    private void setupPatternLockView() {
        KeyguardPatternView keyguardPatternView = (KeyguardPatternView) findViewById(R.id.keyguard_pattern_view);
        keyguardPatternView.setKeyguardCallback(new KeyguardSecurityCallback() {
            @Override
            public void dismiss(boolean securityVerified) {
                onPass();
            }

            @Override
            public void userActivity() {

            }

            @Override
            public boolean isVerifyUnlockOnly() {
                return false;
            }

            @Override
            public void reportUnlockAttempt(boolean success) {

            }

            @Override
            public void reset() {

            }
        });
    }

    private void setupLabel() {
        TextView textView = (TextView) findViewById(R.id.label);
        textView.setText(getString(R.string.input_password, ApkUtil.loadNameByPkgName(this, pkg)));
    }

    private void setupCamera() {
        // Setup camera preview.
        View softwareCameraPreview = findViewById(R.id.surface);
        if (softwareCameraPreview != null)
            softwareCameraPreview.setVisibility(mTakePhoto ? View.VISIBLE : View.GONE);
    }

    private final class ScreenBroadcastReceiver extends BroadcastReceiver {
        private ScreenBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String strAction = null;
            if (intent != null) {
                strAction = intent.getAction();
            }
            if (Intent.ACTION_USER_PRESENT.equals(strAction)) {
                setupFP();
            }
        }
    }

    private void setupFP() {
        cancelFP();
        if (XSettings.get().fpEnabled(this)) {
            mCancellationSignal = setupFingerPrint(
                    new FingerprintManagerCompat.AuthenticationCallback() {
                        @Override
                        public void onAuthenticationSucceeded(
                                FingerprintManagerCompat.AuthenticationResult result) {
                            super.onAuthenticationSucceeded(result);
                            Logger.d("onAuthenticationSucceeded:" + result);
                            onPass();
                            vibrate();
                        }

                        @Override
                        public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                            super.onAuthenticationHelp(helpMsgId, helpString);
                            Logger.i("onAuthenticationHelp:" + helpString);
                            // mLabelView.setText(helpString);
                        }

                        @Override
                        public void onAuthenticationFailed() {
                            super.onAuthenticationFailed();
                            Logger.d("onAuthenticationFailed");
                            // vibrate();
                        }

                        @Override
                        public void onAuthenticationError(int errMsgId, CharSequence errString) {
                            super.onAuthenticationError(errMsgId, errString);
                            Logger.d("onAuthenticationError:" + errString);
                            // mLabelView.setText(errString);
                            // vibrate();
                        }
                    });
        }
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) VerifyDisplayerActivity.this.getSystemService(VIBRATOR_SERVICE);
        if (vibrator != null) {
            vibrator.vibrate(200);
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

    private boolean isKeyguard() {
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        if (keyguardManager != null && keyguardManager.inKeyguardRestrictedInputMode()) {
            Logger.d("in keyguard true");
            return true;
        }
        Logger.d("in keyguard false");
        return false;
    }


    private void onPass() {
        if (testMode || mResNotified) return;
        StopWatch stopWatch = StopWatch.start("onPass");
        mResNotified = true;
        cancelFP();
        stopWatch.split("cancelFP");
        XAppGuardManager.defaultInstance().setResult(tid, XMode.MODE_ALLOWED);
        stopWatch.split("setResult");
        finish();
    }

    private void cancelFP() {
        if (mCancellationSignal != null && !mCancellationSignal.isCanceled()) {
            mCancellationSignal.cancel();
            mCancellationSignal = null;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        onFail();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cancelFP();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isKeyguard()) {
            this.mScreenBroadcastReceiver = new ScreenBroadcastReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_USER_PRESENT);
            registerReceiver(this.mScreenBroadcastReceiver, intentFilter);
            return;
        }

        KeyguardPatternView keyguardPatternView =
                (KeyguardPatternView) findViewById(R.id.keyguard_pattern_view);
        if (keyguardPatternView != null) keyguardPatternView.startAppearAnimation();

        setupFP();
    }

    private void onFail() {
        if (testMode || mResNotified) {
            return;
        }
        mResNotified = true;
        cancelFP();
        XAppGuardManager.defaultInstance().setResult(tid, XMode.MODE_DENIED);
        if (injectHome) {
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory("android.intent.category.HOME");
            startActivity(intent);
        }
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
        tid = intent.getIntExtra(EXTRA_TRANS_ID, -1);
        injectHome = intent.getBooleanExtra(EXTRA_INJECT_HOME_WHEN_FAIL_ID, false);
        Logger.d("after resolveIntent: %s, %s, %s", pkg, tid, injectHome);
        testMode = intent.getBooleanExtra("extra.test", false);
        return (pkg != null) || testMode;
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
        try {
            CameraManager.get().closeCamera();

            if (mScreenBroadcastReceiver != null) {
                unregisterReceiver(mScreenBroadcastReceiver);
            }
        } catch (Throwable e) {
            Logger.e("Error onDestroy: " + e);
        }
    }
}
