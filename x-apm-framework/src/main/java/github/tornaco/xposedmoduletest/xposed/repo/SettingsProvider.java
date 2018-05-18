package github.tornaco.xposedmoduletest.xposed.repo;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.util.Log;

import java.io.File;

import github.tornaco.android.common.BlackHole;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.ISettingsChangeListener;
import github.tornaco.xposedmoduletest.util.Singleton;
import github.tornaco.xposedmoduletest.xposed.DefaultConfiguration;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/12/19.
 * Email: Tornaco@163.com
 */

public class SettingsProvider {

    private static final String TAG = DefaultConfiguration.LOG_TAG_PREFIX + "-SP";

    private static final int MSG_SETTINGS_UPDATE = 0x1;

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

    private Looper mStateLooper;
    private Handler mNotifyHandler;

    private final Object mLock = new Object();

    private final RemoteCallbackList<ISettingsChangeListener> mListeners = new RemoteCallbackList<>();

    private SettingsProvider() {

        HandlerThread stateThread = new HandlerThread("SettingsState@APM");
        stateThread.start();
        mStateLooper = stateThread.getLooper();

        initSettingsState(mStateLooper);

        HandlerThread notifyThread = new HandlerThread("SettingsState@APM");
        notifyThread.start();
        mNotifyHandler = new Handler(notifyThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MSG_SETTINGS_UPDATE:
                        try {
                            notifySettingsChangeListener((String) msg.obj);
                        } catch (Throwable e) {
                            // Fuck it.
                            XposedLog.wtf("Fail notifySettingsChangeListener! " + Log.getStackTraceString(e));
                        }
                        break;
                }
            }
        };
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
            boolean success = mSettingsState.insertSettingLocked(name, value, "tornaco", true, "android");
            if (success) {
                notifyForSettingChange(name);
            }
            return success;
        }
    }

    private void notifyForSettingChange(String name) {
        mNotifyHandler.obtainMessage(MSG_SETTINGS_UPDATE, name).sendToTarget();
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
                initSettingsState(mStateLooper);
                Log.d(TAG, "Settings state has been reset!");
            }
        } catch (Throwable e) {
            Log.e(TAG, "Fail reset settings state: " + Log.getStackTraceString(e));
        }
    }

    public boolean registerSettingsChangeListener(ISettingsChangeListener listener) {
        return listener != null && mListeners.register(listener);
    }

    public boolean unRegisterSettingsChangeListener(ISettingsChangeListener listener) {
        return listener != null && mListeners.unregister(listener);
    }

    private void notifySettingsChangeListener(String name) {
        XposedLog.verbose("notifySettingsChangeListener: " + name);
        try {
            int itemCount = mListeners.beginBroadcast();
            for (int i = 0; i < itemCount; i++) {
                try {
                    ISettingsChangeListener listener = mListeners.getBroadcastItem(i);
                    try {
                        listener.onChange(name);
                    } catch (Throwable e) {
                        XposedLog.wtf(XposedLog.TAG_LAZY + "notifySettingsChangeListener fail call onChange! "
                                + Log.getStackTraceString(e));
                    }
                } catch (Throwable ignored) {
                    // We tried...
                }
            }
            XposedLog.verbose("notifySettingsChangeListener notifySettingsChangeListener finish");
        } catch (Throwable e) {
            XposedLog.wtf("notifySettingsChangeListener notifySettingsChangeListener broadcast fail: " + Log.getStackTraceString(e));
        } finally {
            mListeners.finishBroadcast();
            // If dead, go dead!!!!!
        }
    }
}
