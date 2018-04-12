package github.tornaco.xposedmoduletest.provider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by guohao4 on 2017/11/15.
 * Email: Tornaco@163.com
 */

public class LockStorage {

    public enum LockMethod {
        Pin, Pattern
    }

    private static ExecutorService checkService = Executors.newCachedThreadPool();

    private static final String PATTERN_SEC = "key_pattern_enc";
    private static final String PIN_SEC = "key_pin_enc";
    private static final String PREFER = "key_prefer_unlock_method";
    private static final String SP = "key_sp";

    private LockStorage() {
    }

    public static LockMethod getLockMethod(Context context) {
        return LockMethod.valueOf(PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString(PREFER, LockMethod.Pattern.name()));
    }

    public static void setLockMethod(Context context, LockMethod method) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREFER, method.name())
                .apply();
    }

    public static void setSP(Context context, boolean sp) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(SP, sp)
                .apply();
    }

    public static boolean checkSP(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(SP, false);
    }


    public static boolean iaPatternSet(Context context) {
        return !TextUtils.isEmpty(getPattern(context));
    }

    public static boolean isPinSet(Context context) {
        return !TextUtils.isEmpty(getPin(context));
    }

    private static String getPattern(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PATTERN_SEC, null);
    }

    private static String getPin(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PIN_SEC, null);
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
                .edit().putString(PATTERN_SEC, code)
                .apply();
    }

    public static void setPin(Context context, String code) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit().putString(PIN_SEC, code)
                .apply();
    }

    public interface PatternCheckListener {
        void onMatch();

        void onMisMatch();
    }
}
