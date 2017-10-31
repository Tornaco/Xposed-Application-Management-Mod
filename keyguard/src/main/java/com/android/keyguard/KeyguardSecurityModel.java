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

import android.app.admin.DevicePolicyManager;
import android.content.Context;

public class KeyguardSecurityModel {

    /**
     * The different types of security available.
     */
    public enum SecurityMode {
        Invalid, // NULL state
        None, // No security enabled
        Pattern, // Unlock by drawing a pattern.
        Password, // Unlock by entering an alphanumeric password
        PIN, // Strictly numeric password
    }

    private final Context mContext;

    KeyguardSecurityModel(Context context) {
        mContext = context;
    }

    SecurityMode getSecurityMode() {
        // FIXME Read from Settings.
        final int security = DevicePolicyManager.PASSWORD_QUALITY_NUMERIC;
        switch (security) {
            case DevicePolicyManager.PASSWORD_QUALITY_NUMERIC:
            case DevicePolicyManager.PASSWORD_QUALITY_NUMERIC_COMPLEX:
                return SecurityMode.PIN;

            case DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC:
            case DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC:
            case DevicePolicyManager.PASSWORD_QUALITY_COMPLEX:
            case DevicePolicyManager.PASSWORD_QUALITY_MANAGED:
                return SecurityMode.Password;

            case DevicePolicyManager.PASSWORD_QUALITY_SOMETHING:
                return SecurityMode.Pattern;
            case DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED:
                return SecurityMode.None;

            default:
                throw new IllegalStateException("Unknown security quality:" + security);
        }
    }
}
