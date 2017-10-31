/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.keyguard;

import android.content.Context;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;

import java.util.List;

import github.tornaco.keyguard.BuildConfig;
import github.tornaco.keyguard.LockPatternChecker;
import github.tornaco.keyguard.LockPatternView;
import github.tornaco.keyguard.R;
import github.tornaco.settingslib.animation.AppearAnimationCreator;
import github.tornaco.settingslib.animation.AppearAnimationUtils;
import github.tornaco.settingslib.animation.DisappearAnimationUtils;

public class KeyguardPatternView extends LinearLayout implements KeyguardSecurityView,
        AppearAnimationCreator<LockPatternView.CellState> {

    private static final String TAG = "SecurityPatternView";
    private static final boolean DEBUG = BuildConfig.DEBUG;

    // how long before we clear the wrong pattern
    private static final int PATTERN_CLEAR_TIMEOUT_MS = 2000;

    // how long we stay awake after each key beyond MIN_PATTERN_BEFORE_POKE_WAKELOCK
    private static final int UNLOCK_PATTERN_WAKE_INTERVAL_MS = 7000;

    // how many cells the user has to cross before we poke the wakelock
    private static final int MIN_PATTERN_BEFORE_POKE_WAKELOCK = 2;

    // How much we scale up the duration of the disappear animation when the current user is locked
    public static final float DISAPPEAR_MULTIPLIER_LOCKED = 1.5f;

    private static final int MIN_PATTERN_REGISTER_FAIL = 4;

    private final AppearAnimationUtils mAppearAnimationUtils;
    private final DisappearAnimationUtils mDisappearAnimationUtils;
    private final DisappearAnimationUtils mDisappearAnimationUtilsLocked;

    private AsyncTask<?, ?, ?> mPendingLockCheck;
    private LockPatternView mLockPatternView;
    private KeyguardSecurityCallback mCallback;

    /**
     * Keeps track of the last time we poked the wake lock during dispatching of the touch event.
     * Initialized to something guaranteed to make us poke the wakelock when the user starts
     * drawing the pattern.
     *
     * @see #dispatchTouchEvent(android.view.MotionEvent)
     */
    private long mLastPokeTime = -UNLOCK_PATTERN_WAKE_INTERVAL_MS;

    /**
     * Useful for clearing out the wrong pattern after a delay
     */
    private Runnable mCancelPatternRunnable = new Runnable() {
        @Override
        public void run() {
            mLockPatternView.clearPattern();
        }
    };
    private Rect mTempRect = new Rect();
    private KeyguardMessageArea mSecurityMessageDisplay;
    private ViewGroup mContainer;

    enum FooterMode {
        Normal,
        ForgotLockPattern,
        VerifyUnlocked
    }

    public KeyguardPatternView(Context context) {
        this(context, null);
    }

    public KeyguardPatternView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mAppearAnimationUtils = new AppearAnimationUtils(context,
                AppearAnimationUtils.DEFAULT_APPEAR_DURATION, 1.5f /* translationScale */,
                2.0f /* delayScale */, AnimationUtils.loadInterpolator(
                context, android.R.interpolator.linear_out_slow_in));
        mDisappearAnimationUtils = new DisappearAnimationUtils(context,
                125, 1.2f /* translationScale */,
                0.6f /* delayScale */, AnimationUtils.loadInterpolator(
                context, android.R.interpolator.fast_out_linear_in));
        mDisappearAnimationUtilsLocked = new DisappearAnimationUtils(context,
                (long) (125 * DISAPPEAR_MULTIPLIER_LOCKED), 1.2f /* translationScale */,
                0.6f /* delayScale */, AnimationUtils.loadInterpolator(
                context, android.R.interpolator.fast_out_linear_in));
    }

    @Override
    public void setKeyguardCallback(KeyguardSecurityCallback callback) {
        mCallback = callback;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mLockPatternView = (LockPatternView) findViewById(R.id.lockPatternView);
        mLockPatternView.setSaveEnabled(false);
        mLockPatternView.setOnPatternListener(new UnlockPatternListener());

        setFocusableInTouchMode(true);

        // vibrate mode will be the same for the life of this screen
        mLockPatternView.setTactileFeedbackEnabled(true);

        mSecurityMessageDisplay =
                (KeyguardMessageArea) KeyguardMessageArea.findSecurityMessageDisplay(this);
        mContainer = (ViewGroup) findViewById(R.id.container);

    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean result = super.onTouchEvent(ev);
        // as long as the user is entering a pattern (i.e sending a touch event that was handled
        // by this screen), keep poking the wake lock so that the screen will stay on.
        final long elapsed = SystemClock.elapsedRealtime() - mLastPokeTime;
        if (result && (elapsed > (UNLOCK_PATTERN_WAKE_INTERVAL_MS - 100))) {
            mLastPokeTime = SystemClock.elapsedRealtime();
        }
        mTempRect.set(0, 0, 0, 0);
        offsetRectIntoDescendantCoords(mLockPatternView, mTempRect);
        ev.offsetLocation(mTempRect.left, mTempRect.top);
        result = mLockPatternView.dispatchTouchEvent(ev) || result;
        ev.offsetLocation(-mTempRect.left, -mTempRect.top);
        return result;
    }

    @Override
    public void reset() {
        // reset lock pattern
        mLockPatternView.setInStealthMode(false);
        mLockPatternView.enableInput();
        mLockPatternView.setEnabled(true);
        mLockPatternView.clearPattern();

        // if the user is currently locked out, enforce it.
        displayDefaultSecurityMessage();
    }

    private void displayDefaultSecurityMessage() {
        mSecurityMessageDisplay.setMessage(getMessageWithCount(R.string.kg_pattern_instructions), false);
    }

    @Override
    public void showUsabilityHint() {
    }

    /**
     * TODO: hook this up
     */
    public void cleanUp() {
        if (DEBUG) Log.v(TAG, "Cleanup() called on " + this);
        mLockPatternView.setOnPatternListener(null);
    }

    private class UnlockPatternListener implements LockPatternView.OnPatternListener {

        @Override
        public void onPatternStart() {
            mLockPatternView.removeCallbacks(mCancelPatternRunnable);
            mSecurityMessageDisplay.setMessage("", false);
        }

        @Override
        public void onPatternCleared() {
        }

        @Override
        public void onPatternCellAdded(List<LockPatternView.Cell> pattern) {
            mCallback.userActivity();
        }

        @Override
        public void onPatternDetected(final List<LockPatternView.Cell> pattern) {
            mLockPatternView.disableInput();
            if (mPendingLockCheck != null) {
                mPendingLockCheck.cancel(false);
            }

            if (pattern.size() < MIN_PATTERN_REGISTER_FAIL) {
                mLockPatternView.enableInput();
                onPatternChecked(false, false /* not valid - too short */);
                return;
            }

            mPendingLockCheck = LockPatternChecker.checkPattern(
                    pattern,
                    new LockPatternChecker.OnCheckCallback() {


                        @Override
                        public void onChecked(boolean matched) {
                            mLockPatternView.enableInput();
                            mPendingLockCheck = null;
                            if (!matched) {
                                onPatternChecked(false /* matched */,
                                        true /* isValidPattern */);
                            }
                        }
                    });
            if (pattern.size() > MIN_PATTERN_BEFORE_POKE_WAKELOCK) {
                mCallback.userActivity();
            }
        }

        private void onPatternChecked(boolean matched,
                                      boolean isValidPattern) {
            if (matched) {
                mCallback.reportUnlockAttempt(true);
                mLockPatternView.setDisplayMode(LockPatternView.DisplayMode.Correct);
                mCallback.dismiss(true);
            } else {
                mLockPatternView.setDisplayMode(LockPatternView.DisplayMode.Wrong);
                if (isValidPattern) {
                    mCallback.reportUnlockAttempt(false);
                }
                mSecurityMessageDisplay.
                        setMessage(getMessageWithCount(R.string.kg_wrong_pattern), true);
                mLockPatternView.postDelayed(mCancelPatternRunnable, PATTERN_CLEAR_TIMEOUT_MS);
            }
        }
    }

    private String getMessageWithCount(int msgId) {
        return getContext().getString(msgId);
    }

    @Override
    public boolean needsInput() {
        return false;
    }

    @Override
    public void onPause() {
        if (mPendingLockCheck != null) {
            mPendingLockCheck.cancel(false);
            mPendingLockCheck = null;
        }
    }

    @Override
    public void onResume(int reason) {
        reset();
    }

    @Override
    public KeyguardSecurityCallback getCallback() {
        return mCallback;
    }

    @Override
    public void showPromptReason(int reason) {
        switch (reason) {
            default:
                mSecurityMessageDisplay.setMessage(R.string.kg_enter_confirm_pin_hint,
                        true /* important */);
                break;
        }
    }

    @Override
    public void showMessage(String message, int color) {
        mSecurityMessageDisplay.setNextMessageColor(color);
        mSecurityMessageDisplay.setMessage(message, true /* important */);
    }

    @Override
    public void startAppearAnimation() {
        enableClipping(false);
        setAlpha(1f);
        setTranslationY(mAppearAnimationUtils.getStartTranslation());
        AppearAnimationUtils.startTranslationYAnimation(this, 0 /* delay */, 500 /* duration */,
                0, mAppearAnimationUtils.getInterpolator());
        mAppearAnimationUtils.startAnimation2d(
                mLockPatternView.getCellStates(),
                new Runnable() {
                    @Override
                    public void run() {
                        enableClipping(true);
                    }
                },
                this);
        if (!TextUtils.isEmpty(mSecurityMessageDisplay.getText())) {
            mAppearAnimationUtils.createAnimation(mSecurityMessageDisplay, 0,
                    AppearAnimationUtils.DEFAULT_APPEAR_DURATION,
                    mAppearAnimationUtils.getStartTranslation(),
                    true /* appearing */,
                    mAppearAnimationUtils.getInterpolator(),
                    null /* finishRunnable */);
        }
    }

    @Override
    public boolean startDisappearAnimation(final Runnable finishRunnable) {
        float durationMultiplier = true
                ? DISAPPEAR_MULTIPLIER_LOCKED
                : 1f;
        mLockPatternView.clearPattern();
        enableClipping(false);
        setTranslationY(0);
        AppearAnimationUtils.startTranslationYAnimation(this, 0 /* delay */,
                (long) (300 * durationMultiplier),
                -mDisappearAnimationUtils.getStartTranslation(),
                mDisappearAnimationUtils.getInterpolator());

        DisappearAnimationUtils disappearAnimationUtils = true
                ? mDisappearAnimationUtilsLocked
                : mDisappearAnimationUtils;
        disappearAnimationUtils.startAnimation2d(mLockPatternView.getCellStates(),
                new Runnable() {
                    @Override
                    public void run() {
                        enableClipping(true);
                        if (finishRunnable != null) {
                            finishRunnable.run();
                        }
                    }
                }, KeyguardPatternView.this);
        if (!TextUtils.isEmpty(mSecurityMessageDisplay.getText())) {
            mDisappearAnimationUtils.createAnimation(mSecurityMessageDisplay, 0,
                    (long) (200 * durationMultiplier),
                    -mDisappearAnimationUtils.getStartTranslation() * 3,
                    false /* appearing */,
                    mDisappearAnimationUtils.getInterpolator(),
                    null /* finishRunnable */);
        }
        return true;
    }

    private void enableClipping(boolean enable) {
        setClipChildren(enable);
        mContainer.setClipToPadding(enable);
        mContainer.setClipChildren(enable);
    }

    @Override
    public void createAnimation(final LockPatternView.CellState animatedCell, long delay,
                                long duration, float translationY, final boolean appearing,
                                Interpolator interpolator,
                                final Runnable finishListener) {
        mLockPatternView.startCellStateAnimation(animatedCell,
                1f, appearing ? 1f : 0f, /* alpha */
                appearing ? translationY : 0f, appearing ? 0f : translationY, /* translation */
                appearing ? 0f : 1f, 1f /* scale */,
                delay, duration, interpolator, finishListener);
    }

    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }
}
