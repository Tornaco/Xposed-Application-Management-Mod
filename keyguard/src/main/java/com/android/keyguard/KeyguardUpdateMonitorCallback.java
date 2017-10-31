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

import android.hardware.fingerprint.FingerprintManager;
import android.os.SystemClock;

/**
 * Callback for general information relevant to lock screen.
 */
public class KeyguardUpdateMonitorCallback {

    private static final long VISIBILITY_CHANGED_COLLAPSE_MS = 1000;
    private long mVisibilityChangedCalled;
    private boolean mShowing;

    /**
     * Called once per minute or when the time changes.
     */
    public void onTimeChanged() {
    }

    /**
     * Called when the visibility of the keyguard changes.
     *
     * @param showing Indicates if the keyguard is now visible.
     */
    public void onKeyguardVisibilityChanged(boolean showing) {
    }

    public void onKeyguardVisibilityChangedRaw(boolean showing) {
        final long now = SystemClock.elapsedRealtime();
        if (showing == mShowing
                && (now - mVisibilityChangedCalled) < VISIBILITY_CHANGED_COLLAPSE_MS) return;
        onKeyguardVisibilityChanged(showing);
        mVisibilityChangedCalled = now;
        mShowing = showing;
    }

    /**
     * Called when the keyguard enters or leaves bouncer mode.
     *
     * @param bouncer if true, keyguard is now in bouncer mode.
     */
    public void onKeyguardBouncerChanged(boolean bouncer) {
    }

    /**
     * Called when visibility of lockscreen clock changes, such as when
     * obscured by a widget.
     */
    public void onClockVisibilityChanged() {
    }

    /**
     * Called when the screen has been turned on.
     */
    public void onScreenTurnedOn() {
    }

    /**
     * Called when the screen has been turned off.
     */
    public void onScreenTurnedOff() {
    }

    /**
     * Called when a finger has been acquired.
     * <p>
     * It is guaranteed that either {@link #onFingerprintAuthenticated} or
     * {@link #onFingerprintAuthFailed()} is called after this method eventually.
     */
    public void onFingerprintAcquired() {
    }

    /**
     * Called when a fingerprint couldn't be authenticated.
     */
    public void onFingerprintAuthFailed() {
    }

    /**
     * Called when a fingerprint is recognized.
     *
     * @param userId the user id for which the fingerprint was authenticated
     */
    public void onFingerprintAuthenticated(int userId) {
    }

    /**
     * Called when fingerprint provides help string (e.g. "Try again")
     *
     * @param msgId
     * @param helpString
     */
    public void onFingerprintHelp(int msgId, String helpString) {
    }

    /**
     * Called when fingerprint provides an semi-permanent error message
     * (e.g. "Hardware not available").
     *
     * @param msgId     one of the error messages listed in {@link FingerprintManager}
     * @param errString
     */
    public void onFingerprintError(int msgId, String errString) {
    }

    /**
     * Called when the state of face unlock changed.
     */
    public void onFaceUnlockStateChanged(boolean running, int userId) {
    }

    /**
     * Called when the fingerprint running state changed.
     */
    public void onFingerprintRunningStateChanged(boolean running) {
    }

    /**
     * Called when the state whether we have a lockscreen wallpaper has changed.
     */
    public void onHasLockscreenWallpaperChanged(boolean hasLockscreenWallpaper) {
    }

}
