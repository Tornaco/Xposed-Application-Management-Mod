package github.tornaco.xposedmoduletest.provider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.StringRes;
import android.text.TextUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import github.tornaco.xposedmoduletest.R;
import lombok.Getter;

/**
 * Created by guohao4 on 2017/11/15.
 * Email: Tornaco@163.com
 */

public class LockStorage {

    public enum LockMethod {
        Pin(R.string.lock_method_pin), Pattern(R.string.lock_method_pattern);

        @Getter
        @StringRes
        private int nameRes;

        LockMethod(int nameRes) {
            this.nameRes = nameRes;
        }
    }

    private static ExecutorService checkService = Executors.newCachedThreadPool();

    private static final String KEY_PATTERN_SEC = "key_pattern_enc";
    private static final String KEY_PIN_SEC = "key_pin_enc";
    private static final String KEY_PREFER = "key_prefer_unlock_method";

    // Used by secure settings.
    public static final String KEY_HIDE_PATTERN = "key_hide_pattern";

    private LockStorage() {
    }

    public static LockMethod getLockMethod(Context context) {
        return LockMethod.valueOf(PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString(KEY_PREFER, LockMethod.Pattern.name()));
    }

    public static void setLockMethod(Context context, LockMethod method) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(KEY_PREFER, method.name())
                .apply();
    }

    public static void setHidePatternEnabled(Context context, boolean sp) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_HIDE_PATTERN, sp)
                .apply();
    }

    public static boolean isShowPatternEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_HIDE_PATTERN, false);
    }


    public static boolean iaPatternSet(Context context) {
        return !TextUtils.isEmpty(getPattern(context));
    }

    public static boolean isPinSet(Context context) {
        return !TextUtils.isEmpty(getPin(context));
    }

    private static String getPattern(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_PATTERN_SEC, null);
    }

    private static String getPin(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_PIN_SEC, null);
    }

    public static AsyncTask checkPatternAsync(final Context context, String input,
                                              final PatternCheckListener listener) {
        @SuppressLint("StaticFieldLeak") AsyncTask<String, Void, Boolean> asyncTask
                = new AsyncTask<String, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(String... strings) {
                String pwd = getPattern(context);
                return pwd.equals(strings[0]);
            }

            @Override
            protected void onPostExecute(Boolean res) {
                super.onPostExecute(res);
                if (res) {
                    listener.onMatch();
                } else {
                    listener.onMisMatch();
                }
            }
        };

        asyncTask.executeOnExecutor(checkService, input);

        return asyncTask;
    }

    public static AsyncTask checkPinAsync(final Context context, String input,
                                          final PatternCheckListener listener) {
        @SuppressLint("StaticFieldLeak") AsyncTask<String, Void, Boolean> asyncTask
                = new AsyncTask<String, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(String... strings) {
                String pwd = getPin(context);
                return pwd.equals(strings[0]);
            }

            @Override
            protected void onPostExecute(Boolean res) {
                super.onPostExecute(res);
                if (res) {
                    listener.onMatch();
                } else {
                    listener.onMisMatch();
                }
            }
        };

        asyncTask.executeOnExecutor(checkService, input);

        return asyncTask;
    }

    public static void setPattern(Context context, String code) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit().putString(KEY_PATTERN_SEC, code)
                .apply();
    }

    public static void setPin(Context context, String code) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit().putString(KEY_PIN_SEC, code)
                .apply();
    }

    public interface PatternCheckListener {
        void onMatch();

        void onMisMatch();
    }
}
