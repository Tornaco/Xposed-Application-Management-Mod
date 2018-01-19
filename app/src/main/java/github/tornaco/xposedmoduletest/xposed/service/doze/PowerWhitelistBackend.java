/*
 * Copyright (C) 2015 The Android Open Source Project
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
package github.tornaco.xposedmoduletest.xposed.service.doze;

import android.util.Log;

import java.util.HashSet;

import github.tornaco.xposedmoduletest.util.Singleton1;


/**
 * Handles getting/changing the whitelist for the exceptions to battery saving features.
 */
public class PowerWhitelistBackend {

    private static final String TAG = "X-APM-POWER";

    private static Singleton1<PowerWhitelistBackend, DeviceIdleControllerProxy> sInstance
            = new Singleton1<PowerWhitelistBackend, DeviceIdleControllerProxy>() {
        @Override
        protected PowerWhitelistBackend create(DeviceIdleControllerProxy proxy) {
            return new PowerWhitelistBackend(proxy);
        }
    };

    private final DeviceIdleControllerProxy mDeviceIdleService;
    private final HashSet<String> mWhitelistedApps = new HashSet<>();
    private final HashSet<String> mSysWhitelistedApps = new HashSet<>();

    public PowerWhitelistBackend(DeviceIdleControllerProxy proxy) {
        mDeviceIdleService = proxy;
        refreshList();
    }

    public int getWhitelistSize() {
        return mWhitelistedApps.size();
    }

    public boolean isSysWhitelisted(String pkg) {
        return mSysWhitelistedApps.contains(pkg);
    }

    public boolean isWhitelisted(String pkg) {
        return mWhitelistedApps.contains(pkg);
    }

    public void addApp(String pkg) {
        try {
            mDeviceIdleService.addPowerSaveWhitelistAppInternal(pkg);
            mWhitelistedApps.add(pkg);
        } catch (Exception e) {
            Log.w(TAG, "Unable to reach IDeviceIdleController", e);
        }
    }

    public void removeApp(String pkg) {
        try {
            mDeviceIdleService.removePowerSaveWhitelistAppInternal(pkg);
            mWhitelistedApps.remove(pkg);
        } catch (Exception e) {
            Log.w(TAG, "Unable to reach IDeviceIdleController", e);
        }
    }

    private void refreshList() {
        mSysWhitelistedApps.clear();
        mWhitelistedApps.clear();
        try {
            String[] whitelistedApps = mDeviceIdleService.getFullPowerWhitelistInternal();
            for (String app : whitelistedApps) {
                mWhitelistedApps.add(app);
            }
            String[] sysWhitelistedApps = mDeviceIdleService.getSystemPowerWhitelistInternal();
            for (String app : sysWhitelistedApps) {
                mSysWhitelistedApps.add(app);
            }
        } catch (Exception e) {
            Log.w(TAG, "Unable to reach IDeviceIdleController", e);
        }
    }

    public static PowerWhitelistBackend getInstance(DeviceIdleControllerProxy proxy) {
        return sInstance.get(proxy);
    }

}
