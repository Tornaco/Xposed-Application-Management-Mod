package github.tornaco.keyguard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Helper class to check/verify PIN/Password/Pattern asynchronously.
 */
@SuppressLint("StaticFieldLeak")
public class LockPatternChecker {

    static abstract class CheckerExecutor {

        private static ExecutorService executorService = Executors.newCachedThreadPool();

        public static ExecutorService getService() {
            return executorService;
        }
    }

    /**
     * Interface for a callback to be invoked after security check.
     */
    public interface OnCheckCallback {
        void onChecked(boolean matched);
    }

    /**
     * Interface for a callback to be invoked after security verification.
     */
    public interface OnRecordCallback {
        void onRecord(boolean ok);
    }

    /**
     * Verify a pattern asynchronously.
     *
     * @param pattern  The pattern to check.
     * @param callback The callback to be invoked with the verification result.
     */
    public static AsyncTask<?, ?, ?> recordPattern(
            final Context context,
            final List<LockPatternView.Cell> pattern,
            final OnRecordCallback callback) {
        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... args) {
                KeyguardStorage.setPattern(context, LockPatternUtils.patternToString(pattern));
                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                callback.onRecord(result);
            }
        };
        task.executeOnExecutor(CheckerExecutor.getService());
        return task;
    }

    /**
     * Checks a pattern asynchronously.
     *
     * @param pattern  The pattern to check.
     * @param callback The callback to be invoked with the check result.
     */
    public static void checkPattern(
            final Context context,
            final List<LockPatternView.Cell> pattern,
            final OnCheckCallback callback) {

        CheckerExecutor.getService().execute(new Runnable() {
            @Override
            public void run() {
                Log.d("XAppGuard", "checkPattern doInBackground.");
                String saved = KeyguardStorage.getPattern(context);
                boolean ok = !TextUtils.isEmpty(saved) && saved.equals(LockPatternUtils.patternToString(pattern));
                Log.d("XAppGuard", "checkPattern onPostExecute.");
                callback.onChecked(ok);
            }
        });
        Log.d("XAppGuard", "checkPattern return.");
    }

    /**
     * Verify a password asynchronously.
     *
     * @param password  The password to check.
     * @param challenge The challenge to verify against the pattern.
     * @param userId    The user to check against the pattern.
     * @param callback  The callback to be invoked with the verification result.
     */
    public static AsyncTask<?, ?, ?> recordPassword(
            final String password,
            final long challenge,
            final int userId,
            final OnRecordCallback callback) {
        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... args) {
                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                callback.onRecord(result);
            }
        };
        task.executeOnExecutor(CheckerExecutor.getService());
        return task;
    }

    /**
     * Checks a password asynchronously.
     *
     * @param password The password to check.
     * @param callback The callback to be invoked with the check result.
     */
    public static AsyncTask<?, ?, ?> checkPassword(
            final String password,
            final OnCheckCallback callback) {
        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... args) {
                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                callback.onChecked(result);
            }
        };
        task.executeOnExecutor(CheckerExecutor.getService());
        return task;
    }
}
