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

import java.util.Arrays;
import java.util.HashSet;

import github.tornaco.xposedmoduletest.util.ArrayUtil;
import github.tornaco.xposedmoduletest.util.Singleton1;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;


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
        if (mDeviceIdleService != null) {
            refreshList();
        } else {
            XposedLog.wtf("PowerWhitelistBackend construct, null proxy");
        }
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
            XposedLog.boot("Unable to reach IDeviceIdleController:" + e);
        }
    }

    public void removeApp(String pkg) {
        try {
            mDeviceIdleService.removePowerSaveWhitelistAppInternal(pkg);
            mWhitelistedApps.remove(pkg);
        } catch (Exception e) {
            XposedLog.boot("Unable to reach IDeviceIdleController:" + e);
        }
    }

    private void refreshList() {
        mSysWhitelistedApps.clear();
        mWhitelistedApps.clear();
        try {
            String[] whitelistedApps = mDeviceIdleService.getFullPowerWhitelistInternal();
            mWhitelistedApps.addAll(Arrays.asList(whitelistedApps));
            String[] sysWhitelistedApps = mDeviceIdleService.getSystemPowerWhitelistInternal();
            mSysWhitelistedApps.addAll(Arrays.asList(sysWhitelistedApps));
        } catch (Exception e) {
            XposedLog.boot("Unable to reach IDeviceIdleController:" + e);
        }
    }

    public String[] getSysWhitelistedApps() {
        return ArrayUtil.convertObjectArrayToStringArray(mSysWhitelistedApps.toArray());
    }

    public String[] getWhitelistedApps() {
        return ArrayUtil.convertObjectArrayToStringArray(mWhitelistedApps.toArray());
    }

    public static PowerWhitelistBackend getInstance(DeviceIdleControllerProxy proxy) {
        return sInstance.get(proxy);
    }

}
