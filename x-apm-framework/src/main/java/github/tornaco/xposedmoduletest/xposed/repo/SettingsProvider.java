package github.tornaco.xposedmoduletest.xposed.repo;

import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import java.io.File;

import github.tornaco.android.common.BlackHole;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.util.Singleton;
import github.tornaco.xposedmoduletest.xposed.DefaultConfiguration;

/**
 * Created by guohao4 on 2017/12/19.
 * Email: Tornaco@163.com
 */

public class SettingsProvider {

    private static final String TAG = DefaultConfiguration.LOG_TAG_PREFIX + "-SP";

    private static final int SETTINGS_KEY_TOR_AG = 0x1;
    private static final String SETTINGS_NAME_TOR_AG = "settings_common.xml";

    private static final Singleton<SettingsProvider> sProvider =
            new Singleton<SettingsProvider>() {
                @Override
                protected SettingsProvider create() {
                    return new SettingsProvider();
                }
            };

    public static SettingsProvider get() {
        return sProvider.get();
    }

    private SettingsState mSettingsState;

    private Looper mLooper;

    private final Object mLock = new Object();

    private SettingsProvider() {

        HandlerThread handlerThread = new HandlerThread("SettingsProvider@Tor");
        handlerThread.start();
        mLooper = handlerThread.getLooper();

        initSettingsState(mLooper);
    }

    private void initSettingsState(Looper looper) {
        File dir = getBaseDataDir();
        mSettingsState = new SettingsState(mLock,
                new File(dir, SETTINGS_NAME_TOR_AG),
                SETTINGS_KEY_TOR_AG,
                -1, // No limit.
                looper);
    }

    private static File getBaseDataDir() {
        return RepoProxy.getBaseDataDir();
    }

    private boolean insertSettingLocked(String name, String value) {
        synchronized (mLock) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "insertSettingLocked: " + name + " " + value);
            }
            return mSettingsState.insertSettingLocked(name, value, "tornaco", true, "android");
        }
    }

    private String getSettingLocked(String name) {
        synchronized (mLock) {
            SettingsState.Setting setting = mSettingsState.getSettingLocked(name);
            if (setting.isNull()) {
                return null;
            }
            return setting.getValue();
        }
    }

    public boolean putString(String name, String value) {
        try {
            return insertSettingLocked(name, value);
        } catch (Throwable e) {
            Log.e(TAG, "putString" + Log.getStackTraceString(e));
            return false;
        }
    }

    public String getString(String name, String def) {
        try {
            return getSettingLocked(name);
        } catch (Throwable e) {
            Log.e(TAG, "getString" + Log.getStackTraceString(e));
            return def;
        }
    }

    public boolean putInt(String name, int value) {
        return putString(name, String.valueOf(value));
    }

    public int getInt(String name, int def) {
        try {
            String s = getString(name, String.valueOf(def));
            if (s == null) return def;
            return Integer.parseInt(s);
        } catch (Throwable e) {
            Log.e(TAG, "getInt" + Log.getStackTraceString(e));
            return def;
        }
    }

    public boolean getBoolean(String name, boolean def) {
        String v = getSettingLocked(name);
        if (v == null) return def;
        try {
            return Boolean.parseBoolean(v);
        } catch (Throwable e) {
            Log.e(TAG, "getBoolean" + Log.getStackTraceString(e));
            return def;
        }
    }

    public boolean putBoolean(String name, boolean value) {
        try {
            return insertSettingLocked(name, String.valueOf(value));
        } catch (Throwable e) {
            Log.e(TAG, "putBoolean" + Log.getStackTraceString(e));
            return false;
        }
    }

    public long getLong(String name, long def) {
        String v = getSettingLocked(name);
        if (v == null) return def;
        try {
            return Long.parseLong(v);
        } catch (Throwable e) {
            Log.e(TAG, "getLong" + Log.getStackTraceString(e));
            return def;
        }
    }

    public boolean putLong(String name, long value) {
        try {
            return insertSettingLocked(name, String.valueOf(value));
        } catch (Throwable e) {
            Log.e(TAG, "putLong" + Log.getStackTraceString(e));
            return false;
        }
    }

    public void reset() {
        try {
            synchronized (mLock) {
                mSettingsState.reset();
                File dir = getBaseDataDir();
                BlackHole.eat(new File(dir, SETTINGS_NAME_TOR_AG).delete());
                initSettingsState(mLooper);
                Log.d(TAG, "Settings state has been reset!");
            }
        } catch (Throwable e) {
            Log.e(TAG, "Fail reset settings state: " + Log.getStackTraceString(e));
        }
    }
}
