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

public class KeyguardStorage {

    private static ExecutorService checkService = Executors.newCachedThreadPool();

    private static final String PATTERN_SEC = "key_pattern_enc";

    private KeyguardStorage() {
    }

    public static boolean iaPatternSet(Context context) {
        return !TextUtils.isEmpty(getPattern(context));
    }

    public static String getPattern(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PATTERN_SEC, null);
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

    public static void setPattern(Context context, String code) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit().putString(PATTERN_SEC, code)
                .apply();
    }

    public interface PatternCheckListener {
        void onMatch();

        void onMisMatch();
    }
}
