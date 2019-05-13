package github.tornaco.xposedmoduletest.ui.activity.ag;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.CancellationSignal;
import android.support.v4.util.LruCache;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.andrognito.pinlockview.IndicatorDots;
import com.andrognito.pinlockview.PinLockListener;
import com.andrognito.pinlockview.PinLockView;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;

import org.newstand.logger.Logger;

import java.util.List;

import github.tornaco.android.common.util.ApkUtil;
import github.tornaco.android.common.util.ColorUtil;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.compat.fingerprint.FingerprintManagerCompat;
import github.tornaco.xposedmoduletest.loader.GlideApp;
import github.tornaco.xposedmoduletest.loader.PaletteColorPicker;
import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.provider.AppSettings;
import github.tornaco.xposedmoduletest.provider.LockStorage;
import github.tornaco.xposedmoduletest.provider.XSettings;
import github.tornaco.xposedmoduletest.ui.Themes;
import github.tornaco.xposedmoduletest.ui.activity.BaseActivity;
import github.tornaco.xposedmoduletest.util.PatternLockViewListenerAdapter;
import github.tornaco.xposedmoduletest.util.XExecutor;
import github.tornaco.xposedmoduletest.xposed.app.XAppLockManager;
import github.tornaco.xposedmoduletest.xposed.app.XAppVerifyMode;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static github.tornaco.xposedmoduletest.xposed.app.XAppLockManager.EXTRA_INJECT_HOME_WHEN_FAIL_ID;
import static github.tornaco.xposedmoduletest.xposed.app.XAppLockManager.EXTRA_PKG_NAME;
import static github.tornaco.xposedmoduletest.xposed.app.XAppLockManager.EXTRA_TRANS_ID;


/**
 * Created by guohao4 on 2017/10/23.
 * Email: Tornaco@163.com
 */

public class VerifyDisplayerActivity extends BaseActivity {
    private static final LruCache<String, Integer> sDominantCache = new LruCache<>(64);

    private String pkg = null;
    // FIXME, this is not a good solution.
    // Tid is 0 or negative when this is performed when activity resume on user present?
    private int tid = -1;
    private boolean injectHome = false;

    private boolean testMode;

    private boolean mTakePhoto;
    private LockStorage.LockMethod mLockMethod;

    private boolean mDynamicColor;
    private int mDefColor;

    private boolean mUseCustomBackground;
    private String mCustomBackgroundImagePath;

    private CancellationSignal mCancellationSignal;

    private boolean mResNotified = false;
    private ScreenBroadcastReceiver mScreenBroadcastReceiver;

    private AsyncTask mCheckTask;

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
        setContentView(mLockMethod == LockStorage.LockMethod.Pattern ?
                R.layout.verify_displayer_pattern
                : R.layout.verify_displayer_pin);
        if (resolveIntent(getIntent())) {
            showVerifyView();
        }
    }

    @Override
    public Themes getUserSetTheme() {
        return Themes.DEFAULT;
    }

    private void readSettings() {
        this.mTakePhoto = XSettings.takenPhotoEnabled(this);
        this.mLockMethod = LockStorage.getLockMethod(this);
        this.mDynamicColor = XSettings.dynamicColorEnabled(this);
        this.mDefColor = XSettings.defaultVerifierColor(this);
        this.mUseCustomBackground = XSettings.customBackgroundEnabled(this);
    }

    private void showVerifyView() {
        if (mDynamicColor) {
            // Apply theme color.
            int color = ContextCompat.getColor(this, XSettings.getThemes(this).getThemeColor());
            // Apply palette color.
            // Workaround.
            Integer cachedDominant = sDominantCache.get(pkg);
            if (cachedDominant != null) {
                applyColor(cachedDominant);
            } else {
                PaletteColorPicker.pickPrimaryColor(this, color1 -> {
                    sDominantCache.put(pkg, color1);
                    applyColor(color1);
                }, pkg, color);
            }

        } else if (mDefColor != 0) {
            applyColor(mDefColor);
        }

        setTitle(null);

        // Check if our passcode has been set.
        boolean pwdSet = (LockStorage.getLockMethod(this) == LockStorage.LockMethod.Pattern && LockStorage.iaPatternSet(this))
                || (LockStorage.getLockMethod(this) == LockStorage.LockMethod.Pin && LockStorage.isPinSet(this));
        if (!testMode && !pwdSet) {
            Toast.makeText(this, R.string.summary_setup_passcode_none_set, Toast.LENGTH_SHORT).show();
            onPass();
            return;
        }

        setupLabel(getString(R.string.input_password, ApkUtil.loadNameByPkgName(this, pkg)));
        setupIcon();
        setupLockView();
    }

    private void setupIcon() {
        if (XSettings.showAppIconEnabled(this)) {
            boolean useRoundIcon = XSettings.cropEnabled(this);

            ImageView imageView = findViewById(R.id.icon);

            CommonPackageInfo c = new CommonPackageInfo();
            c.setPkgName(pkg);

            if (useRoundIcon) GlideApp.with(this)
                    .load(c)
                    .placeholder(0)
                    .error(R.mipmap.ic_launcher_round)
                    .fallback(R.mipmap.ic_launcher_round)
                    .transition(withCrossFade())
                    .transform(new CircleCrop())
                    .into(imageView);
            else GlideApp.with(this)
                    .load(c)
                    .placeholder(0)
                    .error(R.mipmap.ic_launcher_round)
                    .fallback(R.mipmap.ic_launcher_round)
                    .transition(withCrossFade())
                    .into(imageView);
        }
    }

    private void setupLockView() {
        if (mLockMethod == LockStorage.LockMethod.Pattern) {
            setupPatternLockView();
        } else {
            setupPinLockView();
        }
    }

    private void setupPatternLockView() {
        final PatternLockView patternLockView = findViewById(R.id.pattern_lock_view);
        patternLockView.setTactileFeedbackEnabled(false);
        patternLockView.setEnableHapticFeedback(false);

        patternLockView.setInStealthMode(LockStorage.isShowPatternEnabled(getActivity()));

        patternLockView.setDrawableVibrateEnabled(AppSettings.isDrawVibrateEnabled(this));
        patternLockView.addPatternLockListener(new PatternLockViewListenerAdapter() {
            @Override
            public void onComplete(List<PatternLockView.Dot> pattern) {
                cancelCheckTask();
                // Check pattern.
                mCheckTask = LockStorage.checkPatternAsync(getApplicationContext(),
                        PatternLockUtils.patternToString(patternLockView, pattern),
                        new LockStorage.PatternCheckListener() {
                            @Override
                            public void onMatch() {
                                patternLockView.setViewMode(PatternLockView.PatternViewMode.CORRECT);
                                patternLockView.clearPattern();
                                onPass();
                            }

                            @Override
                            public void onMisMatch() {
                                patternLockView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.shake));
                                patternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG);
                                takePhoto();
                                setupLabel(getString(R.string.title_wrong_pwd));
                                patternLockView.clearPattern();
                            }
                        });
            }

            @Override
            public void onCleared() {

            }
        });
        patternLockView.setEnableHapticFeedback(true);
    }

    private void setupPinLockView() {
        final PinLockView pinLockView = findViewById(R.id.pin_lock_view);
        IndicatorDots indicatorDots = findViewById(R.id.indicator_dots);
        pinLockView.attachIndicatorDots(indicatorDots);
        pinLockView.setPinLockListener(new PinLockListener() {
            @Override
            public void onComplete(String pin) {
                cancelCheckTask();
                // Check pattern.
                mCheckTask = LockStorage.checkPinAsync(getApplicationContext(),
                        pin,
                        new LockStorage.PatternCheckListener() {
                            @Override
                            public void onMatch() {
                                onPass();
                            }

                            @Override
                            public void onMisMatch() {
                                pinLockView.resetPinLockView();
                                takePhoto();

                                setupLabel(getString(R.string.title_wrong_pwd));
                            }
                        });
            }

            @Override
            public void onEmpty() {

            }

            @Override
            public void onPinChange(int pinLength, String intermediatePin) {

            }
        });
    }

    private void takePhoto() {
        Logger.d("takePhoto, enabled: " + mTakePhoto);
//        if (mTakePhoto) {
//            try {
//                setupCamera();
//                CameraManager.get().captureSaveAsync(new CameraManager.PictureCallback() {
//                    @Override
//                    public void onImageReady(String path) {
//                        Logger.d("CameraManager- onImageReady@" + path);
//                    }
//
//                    @Override
//                    public void onDataBackupFail(Exception e) {
//                        Logger.d("CameraManager- onDataBackupFail@" + e);
//                    }
//                });
//            } catch (Throwable e) {
//                Logger.e("Fail take photo: " + Logger.getStackTraceString(e));
//            }
//        }
    }

    private void cancelCheckTask() {
        if (mCheckTask != null) {
            mCheckTask.cancel(true);
        }
    }

    private void setupLabel(String label) {
        TextView textView = findViewById(R.id.label);
        textView.setText(label);
    }

    private void setupCamera() {
        //  Setup camera preview.
//        View softwareCameraPreview = findViewById(R.id.surface);
//        if (softwareCameraPreview != null)
//            softwareCameraPreview.setVisibility(mTakePhoto ? View.VISIBLE : View.GONE);
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
        if (XSettings.fpEnabled(this)) {
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
                        }

                        @Override
                        public void onAuthenticationFailed() {
                            super.onAuthenticationFailed();
                            Logger.d("onAuthenticationFailed");
                            takePhoto();
                        }

                        @Override
                        public void onAuthenticationError(int errMsgId, CharSequence errString) {
                            super.onAuthenticationError(errMsgId, errString);
                            Logger.d("onAuthenticationError:" + errString);
                        }
                    });
        }
    }

    private void vibrate() {
//        if (XAppLockManager.get().isInterruptFPEventVBEnabled(XAppLockManager.FPEvent.SUCCESS)) {
//            Logger.w("vibrating...");
//            Vibrator vibrator = (Vibrator) VerifyDisplayerActivity.this.getSystemService(VIBRATOR_SERVICE);
//            if (vibrator != null) {
//                vibrator.vibrate(new long[]{10, 20, 20}, -1);
//            }
//        }
    }

    @SuppressWarnings("ConstantConditions")
    private void applyColor(int color) {
        AppBarLayout appBar = findViewById(R.id.appbar);
        if (appBar != null) appBar.setBackgroundColor(color);
        ViewGroup infoContainer = findViewById(R.id.info);
        infoContainer.setBackgroundColor(color);
        int dark = ColorUtil.colorBurn(color);
        getWindow().setStatusBarColor(dark);
        getWindow().setNavigationBarColor(dark);
        Toolbar toolbar = findViewById(R.id.toolbar);
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
        setupLabel(getString(R.string.title_launching_apps));
        if (testMode || mResNotified) return;

        mResNotified = true;
        cancelFP();
        try {
            if (!checkTransaction()) {
                return;
            }

            final boolean needFix = AppSettings.isAppLockWorkaroundEnabled(getContext());
            long resDelay = needFix ? 800 : 100;
            Handler h = XExecutor.getUIThreadHandler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Logger.w("set Res delayed: " + needFix);
                    // Delay res.
                    XAppLockManager.get().setResult(tid, XAppVerifyMode.MODE_ALLOWED);
                }
            }, resDelay);

        } finally {
            try {
                // Finish first.
                finish();
            } catch (Throwable ignored) {
            }
        }
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

        if (!checkTransaction()) {
            finish();
            return;
        }

        if (isKeyguard()) {
            this.mScreenBroadcastReceiver = new ScreenBroadcastReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_USER_PRESENT);
            registerReceiver(this.mScreenBroadcastReceiver, intentFilter);
            return;
        }

        setupFP();
    }

    private boolean checkTransaction() {
        if (!testMode && tid < 0) {
            setupLabel(getString(R.string.title_transaction_expire));
            finish();
            return false;
        }
        return XAppLockManager.get().isServiceAvailable()
                && XAppLockManager.get().isTransactionValid(tid);
    }

    private void onFail() {
        if (testMode || mResNotified) {
            return;
        }
        mResNotified = true;
        cancelFP();
        XAppLockManager.get().setResult(tid, XAppVerifyMode.MODE_DENIED);
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
        // Service is not available, ignore.
        if (!XAppLockManager.get().isServiceAvailable()) {
            return false;
        }
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
//            CameraManager.get().closeCamera();

            if (mScreenBroadcastReceiver != null) {
                unregisterReceiver(mScreenBroadcastReceiver);
            }

            cancelCheckTask();
        } catch (Throwable e) {
            Logger.e("Error onDestroy: " + e);
        }
    }
}
