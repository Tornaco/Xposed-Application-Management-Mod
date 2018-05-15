package github.tornaco.xposedmoduletest.ui.activity;

import android.Manifest;
import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.CancellationSignal;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.andrognito.pinlockview.IndicatorDots;
import com.andrognito.pinlockview.PinLockListener;
import com.andrognito.pinlockview.PinLockView;

import org.newstand.logger.Logger;

import java.util.List;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.compat.fingerprint.FingerprintManagerCompat;
import github.tornaco.xposedmoduletest.provider.AppSettings;
import github.tornaco.xposedmoduletest.provider.LockStorage;
import github.tornaco.xposedmoduletest.provider.XSettings;
import github.tornaco.xposedmoduletest.ui.activity.ag.PatternSetupActivity;
import github.tornaco.xposedmoduletest.ui.activity.ag.PinSetupActivity;
import github.tornaco.xposedmoduletest.util.PatternLockViewListenerAdapter;
import github.tornaco.xposedmoduletest.util.ViewAnimatorUtil;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by guohao4 on 2017/11/15.
 * Email: Tornaco@163.com
 */

@SuppressLint("Registered")
public class NeedLockActivity<T> extends WithSearchActivity<T> {

    private PatternLockVerifyView mPatternLockVerifyView;
    private PinLockVerifyView mPinLockVerifyView;

    @Setter
    @Getter
    private boolean isLocking;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupLockState();
    }

    private void setupLockState() {
        if (isLockNeeded()) {

            boolean pwdSet = (LockStorage.getLockMethod(this) == LockStorage.LockMethod.Pattern && LockStorage.iaPatternSet(this))
                    || (LockStorage.getLockMethod(this) == LockStorage.LockMethod.Pin && LockStorage.isPinSet(this));
            if (!pwdSet) {
                showPasswordSetupTips();
                return;
            }

            LockStorage.LockMethod lm = LockStorage.getLockMethod(this);
            if (lm == LockStorage.LockMethod.Pattern) {
                if (mPatternLockVerifyView != null && mPatternLockVerifyView.isAttached()) {
                    return;
                }
                mPatternLockVerifyView = new PatternLockVerifyView();
                try {
                    mPatternLockVerifyView.attach(NeedLockActivity.this);
                } catch (Exception e) {
                    String msg = Logger.getStackTraceString(e);
                    showSimpleDialog(getString(R.string.fail_show_internal_lockview), msg);
                }
            } else {
                if (mPinLockVerifyView != null && mPinLockVerifyView.isAttached()) {
                    return;
                }
                mPinLockVerifyView = new PinLockVerifyView();
                try {
                    mPinLockVerifyView.attach(NeedLockActivity.this);
                } catch (Exception e) {
                    String msg = Logger.getStackTraceString(e);
                    showSimpleDialog(getString(R.string.fail_show_internal_lockview), msg);
                }
            }
        }
    }

    private void showPasswordSetupTips() {
        showTips(R.string.summary_setup_passcode_none_set,
                true, getString(R.string.title_setup_passcode_now),
                () -> onRequestSetupSecurePassport());
    }

    private void onRequestSetupSecurePassport() {
        final int[] selection = {0};
        String[] choice = new String[LockStorage.LockMethod.values().length];
        for (int i = 0; i < choice.length; i++) {
            choice[i] = getString(LockStorage.LockMethod.values()[i].getNameRes());
        }
        new AlertDialog.Builder(getActivity())
                .setSingleChoiceItems(choice, selection[0], (dialog, which) -> {
                    selection[0] = which;
                    if (selection[0] == 0) {
                        PinSetupActivity.start(getActivity());
                    } else {
                        PatternSetupActivity.start(getActivity());
                    }
                    dialog.dismiss();
                })
                .show();
    }

    protected String getLockLabel() {
        return getString(R.string.input_password,
                getString(R.string.app_name));
    }

    protected boolean isLockNeeded() {
        return LockStorage.iaPatternSet(this);
    }


    protected void onUnLock() {
        setLocking(false);
    }

    private class PatternLockVerifyView {

        private Activity activity;

        private CancellationSignal mCancellationSignal;

        private ScreenBroadcastReceiver mScreenBroadcastReceiver;

        private AsyncTask mCheckTask;

        private boolean mTakePhoto;

        private View mRootView;

        private ViewGroup mDecor;

        boolean isAttached() {
            return (mRootView != null && mRootView.isAttachedToWindow());
        }

        @SuppressLint("InflateParams")
        public void attach(Activity activity) {
            this.activity = activity;

            setLocking(true);

            readSettings();

            mRootView = LayoutInflater.from(activity)
                    .inflate(R.layout.verify_displayer_pattern, null, false);
            mRootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Hook click.
                }
            });
            mRootView.findViewById(R.id.appbar).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            mRootView.requestFocus();

            setupLabel(getLockLabel());
            setupFP();
            setupLockView();

            if (isKeyguard()) {
                this.mScreenBroadcastReceiver = new ScreenBroadcastReceiver();
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(Intent.ACTION_USER_PRESENT);
                registerReceiver(this.mScreenBroadcastReceiver, intentFilter);
                return;
            }

            ViewGroup.LayoutParams params
                    = new WindowManager.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG,
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                    PixelFormat.RGB_565);

            Window w = activity.getWindow();
            mDecor = (ViewGroup) w.getDecorView();

            mDecor.addView(mRootView, params);
        }

        public void detach(boolean withAnim) {
            try {
//                CameraManager.get().closeCamera();

                if (mScreenBroadcastReceiver != null) {
                    unregisterReceiver(mScreenBroadcastReceiver);
                }

                cancelCheckTask();
            } catch (Throwable e) {
                Logger.e("Error onDestroy: " + e);
            }

            if (isAttached()) {

                if (withAnim) {
                    ViewAnimatorUtil.circularHide(mRootView, new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if (isAttached()) {
                                mDecor.removeView(mRootView);
                            }
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                } else {
                    WindowManager wm = getWindowManager();
                    wm.removeView(mRootView);
                }
            }
        }


        private void readSettings() {
            this.mTakePhoto = XSettings.takenPhotoEnabled(activity);
        }

        private void setupLabel(String label) {
            TextView textView = mRootView.findViewById(R.id.label);
            textView.setText(label);
        }

        private void setupCamera() {
            // Setup camera preview.
//            View softwareCameraPreview = mRootView.findViewById(R.id.surface);
//            if (softwareCameraPreview != null)
//                softwareCameraPreview.setVisibility(mTakePhoto ? View.VISIBLE : View.GONE);
        }

        private void takePhoto() {
            Logger.d("takePhoto, enabled: " + mTakePhoto);
//            if (mTakePhoto) {
//                try {
//                    setupCamera();
//                    CameraManager.get().captureSaveAsync(new CameraManager.PictureCallback() {
//                        @Override
//                        public void onImageReady(String path) {
//                            Logger.d("CameraManager- onImageReady@" + path);
//                        }
//
//                        @Override
//                        public void onDataBackupFail(Exception e) {
//                            Logger.d("CameraManager- onDataBackupFail@" + e);
//                        }
//                    });
//                } catch (Throwable e) {
//                    Logger.e("Fail take photo: " + Logger.getStackTraceString(e));
//                }
//            }
        }

        private void setupFP() {
            cancelFP();
            if (XSettings.fpEnabled(activity)) {
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
                                takePhoto();
                            }

                            @Override
                            public void onAuthenticationError(int errMsgId, CharSequence errString) {
                                super.onAuthenticationError(errMsgId, errString);
                                Logger.d("onAuthenticationError:" + errString);
                                // mLabelView.setText(errString);
                                // vibrate();
                                takePhoto();
                            }
                        });
            }
        }

        private void vibrate() {
//            if (XAppLockManager.get().isServiceAvailable()
//                    && XAppLockManager.get().isInterruptFPEventVBEnabled(XAppLockManager.FPEvent.SUCCESS)) {
//                Vibrator vibrator = (Vibrator) activity.getSystemService(VIBRATOR_SERVICE);
//                if (vibrator != null) {
//                    vibrator.vibrate(new long[]{10, 20, 20}, -1);
//                }
//            }
        }

        private boolean isKeyguard() {
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
            return keyguardManager != null && keyguardManager.inKeyguardRestrictedInputMode();
        }


        private void onPass() {
            cancelFP();
            detach(true);
            onUnLock();
        }

        private void cancelFP() {
            if (mCancellationSignal != null && !mCancellationSignal.isCanceled()) {
                mCancellationSignal.cancel();
                mCancellationSignal = null;
            }
        }

        private CancellationSignal setupFingerPrint(FingerprintManagerCompat.AuthenticationCallback callback) {
            if (ActivityCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.USE_FINGERPRINT)
                    != PackageManager.PERMISSION_GRANTED) {
                Logger.w("FP Permission is missing...");
                return null;
            }
            if (!FingerprintManagerCompat.from(activity.getApplicationContext()).isHardwareDetected()) {
                Logger.w("FP HW is missing...");
                return null;
            }
            CancellationSignal cancellationSignal = new CancellationSignal();
            FingerprintManagerCompat.from(activity.getApplicationContext())
                    .authenticate(null, 0, cancellationSignal, callback, null);
            Logger.i("FP authenticate");
            return cancellationSignal;
        }

        private void setupLockView() {
            setupPatternLockView();
        }

        private void setupPatternLockView() {
            final com.andrognito.patternlockview.PatternLockView patternPatternLockView = mRootView.findViewById(R.id.pattern_lock_view);

            if (mUserTheme.isReverseTheme()) {
                ImageView imageView = mRootView.findViewById(R.id.icon);
                imageView.setColorFilter(ContextCompat.getColor(getContext(),
                        mUserTheme.getThemeColor()), android.graphics.PorterDuff.Mode.MULTIPLY);
            }

            patternPatternLockView.setInStealthMode(LockStorage.isShowPatternEnabled(getContext()));

            patternPatternLockView.setDrawableVibrateEnabled(AppSettings.isDrawVibrateEnabled(getContext()));
            patternPatternLockView.addPatternLockListener(new PatternLockViewListenerAdapter() {
                @Override
                public void onComplete(List<com.andrognito.patternlockview.PatternLockView.Dot> pattern) {
                    cancelCheckTask();
                    // Check pattern.
                    mCheckTask = LockStorage.checkPatternAsync(getApplicationContext(),
                            PatternLockUtils.patternToString(patternPatternLockView, pattern),
                            new LockStorage.PatternCheckListener() {
                                @Override
                                public void onMatch() {
                                    patternPatternLockView.setViewMode(com.andrognito.patternlockview.PatternLockView.PatternViewMode.CORRECT);
                                    onPass();
                                }

                                @Override
                                public void onMisMatch() {
                                    patternPatternLockView.setViewMode(com.andrognito.patternlockview.PatternLockView.PatternViewMode.WRONG);
                                    patternPatternLockView.clearPattern();
                                    takePhoto();
                                    setupLabel(getString(R.string.title_wrong_pwd));
                                }
                            });
                }

                @Override
                public void onCleared() {

                }
            });
            patternPatternLockView.setEnableHapticFeedback(true);
        }

        private void cancelCheckTask() {
            if (mCheckTask != null) {
                mCheckTask.cancel(true);
            }
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
    }


    private class PinLockVerifyView {

        private Activity activity;

        private CancellationSignal mCancellationSignal;

        private ScreenBroadcastReceiver mScreenBroadcastReceiver;

        private AsyncTask mCheckTask;

        private boolean mTakePhoto;

        private View mRootView;

        private ViewGroup mDecor;

        boolean isAttached() {
            return (mRootView != null && mRootView.isAttachedToWindow());
        }

        @SuppressLint("InflateParams")
        public void attach(Activity activity) {
            this.activity = activity;

            setLocking(true);

            readSettings();

            mRootView = LayoutInflater.from(activity)
                    .inflate(R.layout.verify_displayer_pin, null, false);
            mRootView.setOnClickListener(v -> {
                // Hook click.
            });
            mRootView.findViewById(R.id.appbar).setOnClickListener(v -> {

            });

            mRootView.requestFocus();

            setupLabel(getLockLabel());
            setupFP();
            setupLockView();

            if (isKeyguard()) {
                this.mScreenBroadcastReceiver = new ScreenBroadcastReceiver();
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(Intent.ACTION_USER_PRESENT);
                registerReceiver(this.mScreenBroadcastReceiver, intentFilter);
                return;
            }

            ViewGroup.LayoutParams params
                    = new WindowManager.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG,
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                    PixelFormat.RGB_565);

            Window w = activity.getWindow();
            mDecor = (ViewGroup) w.getDecorView();

            mDecor.addView(mRootView, params);
        }

        public void detach(boolean withAnim) {
            try {
//                CameraManager.get().closeCamera();

                if (mScreenBroadcastReceiver != null) {
                    unregisterReceiver(mScreenBroadcastReceiver);
                }

                cancelCheckTask();
            } catch (Throwable e) {
                Logger.e("Error onDestroy: " + e);
            }

            if (isAttached()) {

                if (withAnim) {
                    ViewAnimatorUtil.circularHide(mRootView, new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if (isAttached()) {
                                mDecor.removeView(mRootView);
                            }
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                } else {
                    WindowManager wm = getWindowManager();
                    wm.removeView(mRootView);
                }
            }
        }


        private void readSettings() {
            this.mTakePhoto = XSettings.takenPhotoEnabled(activity);
        }

        private void setupLabel(String label) {
            TextView textView = mRootView.findViewById(R.id.label);
            textView.setText(label);
        }

        private void setupCamera() {
            // Setup camera preview.
//            View softwareCameraPreview = mRootView.findViewById(R.id.surface);
//            if (softwareCameraPreview != null)
//                softwareCameraPreview.setVisibility(mTakePhoto ? View.VISIBLE : View.GONE);
        }

        private void takePhoto() {
            Logger.d("takePhoto, enabled: " + mTakePhoto);
//            if (mTakePhoto) {
//                try {
//                    setupCamera();
//                    CameraManager.get().captureSaveAsync(new CameraManager.PictureCallback() {
//                        @Override
//                        public void onImageReady(String path) {
//                            Logger.d("CameraManager- onImageReady@" + path);
//                        }
//
//                        @Override
//                        public void onDataBackupFail(Exception e) {
//                            Logger.d("CameraManager- onDataBackupFail@" + e);
//                        }
//                    });
//                } catch (Throwable e) {
//                    Logger.e("Fail take photo: " + Logger.getStackTraceString(e));
//                }
//            }
        }

        private void setupFP() {
            cancelFP();
            if (XSettings.fpEnabled(activity)) {
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
                                takePhoto();
                            }

                            @Override
                            public void onAuthenticationError(int errMsgId, CharSequence errString) {
                                super.onAuthenticationError(errMsgId, errString);
                                Logger.d("onAuthenticationError:" + errString);
                                // mLabelView.setText(errString);
                                // vibrate();
                                takePhoto();
                            }
                        });
            }
        }

        private void vibrate() {
//            if (XAppGuardManager.get().isServiceAvailable()
//                    && XAppLockManager.get().isInterruptFPEventVBEnabled(XAppLockManager.FPEvent.SUCCESS)) {
//                Vibrator vibrator = (Vibrator) activity.getSystemService(VIBRATOR_SERVICE);
//                if (vibrator != null) {
//                    vibrator.vibrate(new long[]{10, 20, 20}, -1);
//                }
//            }
        }

        private boolean isKeyguard() {
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
            return keyguardManager != null && keyguardManager.inKeyguardRestrictedInputMode();
        }


        private void onPass() {
            cancelFP();
            detach(true);
            onUnLock();
        }

        private void cancelFP() {
            if (mCancellationSignal != null && !mCancellationSignal.isCanceled()) {
                mCancellationSignal.cancel();
                mCancellationSignal = null;
            }
        }

        private CancellationSignal setupFingerPrint(FingerprintManagerCompat.AuthenticationCallback callback) {
            if (ActivityCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.USE_FINGERPRINT)
                    != PackageManager.PERMISSION_GRANTED) {
                Logger.w("FP Permission is missing...");
                return null;
            }
            if (!FingerprintManagerCompat.from(activity.getApplicationContext()).isHardwareDetected()) {
                Logger.w("FP HW is missing...");
                return null;
            }
            CancellationSignal cancellationSignal = new CancellationSignal();
            FingerprintManagerCompat.from(activity.getApplicationContext())
                    .authenticate(null, 0, cancellationSignal, callback, null);
            Logger.i("FP authenticate");
            return cancellationSignal;
        }

        private void setupLockView() {
            setupPatternLockView();
        }

        private void setupPatternLockView() {
            final PinLockView pinLockView = mRootView.findViewById(R.id.pin_lock_view);
            IndicatorDots indicatorDots = mRootView.findViewById(R.id.indicator_dots);
            pinLockView.attachIndicatorDots(indicatorDots);
            if (mUserTheme.isReverseTheme()) {
                ImageView imageView = mRootView.findViewById(R.id.icon);
                imageView.setColorFilter(ContextCompat.getColor(getContext(),
                        mUserTheme.getThemeColor()), android.graphics.PorterDuff.Mode.MULTIPLY);
            }

            pinLockView.setPinLockListener(new PinLockListener() {
                @Override
                public void onComplete(String pin) {
                    cancelCheckTask();
                    // Check pattern.
                    mCheckTask = LockStorage.checkPinAsync(getApplicationContext(), pin,
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

        private void cancelCheckTask() {
            if (mCheckTask != null) {
                mCheckTask.cancel(true);
            }
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
    }
}
