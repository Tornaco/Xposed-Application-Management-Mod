package github.tornaco.xposedmoduletest.xposed.repo;

import android.os.Environment;
import android.os.HandlerThread;
import android.util.Log;

import java.io.File;

import github.tornaco.xposedmoduletest.util.Singleton;

/**
 * Created by guohao4 on 2017/12/19.
 * Email: Tornaco@163.com
 */

public class SettingsProvider {

    private static final String TAG = "SettingsProvider";

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

    private final Object mLock = new Object();

    public SettingsProvider() {

        HandlerThread handlerThread = new HandlerThread("SettingsProvider@Tor");
        handlerThread.start();

        File dir = getBaseDataDir();
        mSettingsState = new SettingsState(mLock,
                new File(dir, SETTINGS_NAME_TOR_AG),
                SETTINGS_KEY_TOR_AG,
                -1, // No limit.
                handlerThread.getLooper());
    }

    private static File getBaseDataDir() {
        File systemFile = new File(Environment.getDataDirectory(), "system");
        File dir = new File(systemFile, "tor_apm");
        if (!dir.exists()) {
            dir = new File(systemFile, "tor");
        }
        return dir;
    }

    private boolean insertSettingLocked(String name, String value) {
        synchronized (mLock) {
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
}
