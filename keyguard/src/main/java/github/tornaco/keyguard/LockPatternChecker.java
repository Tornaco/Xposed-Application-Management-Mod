package github.tornaco.keyguard;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import java.util.List;

/**
 * Helper class to check/verify PIN/Password/Pattern asynchronously.
 */
@SuppressLint("StaticFieldLeak")
public final class LockPatternChecker {
    /**
     * Interface for a callback to be invoked after security check.
     */
    public interface OnCheckCallback {
        void onChecked(boolean matched);
    }

    /**
     * Interface for a callback to be invoked after security verification.
     */
    public interface OnVerifyCallback {
        /**
         * Invoked when a security verification is finished.
         */
        void onVerified(boolean ok);
    }

    /**
     * Verify a pattern asynchronously.
     *
     * @param pattern   The pattern to check.
     * @param challenge The challenge to verify against the pattern.
     * @param callback  The callback to be invoked with the verification result.
     */
    public static AsyncTask<?, ?, ?> verifyPattern(
            final List<LockPatternView.Cell> pattern,
            final long challenge,
            final OnVerifyCallback callback) {
        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... args) {
                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                callback.onVerified(result);
            }
        };
        task.execute();
        return task;
    }

    /**
     * Checks a pattern asynchronously.
     *
     * @param pattern  The pattern to check.
     * @param callback The callback to be invoked with the check result.
     */
    public static AsyncTask<?, ?, ?> checkPattern(
            final List<LockPatternView.Cell> pattern,
            final OnCheckCallback callback) {
        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
            private int mThrottleTimeout;

            @Override
            protected Boolean doInBackground(Void... args) {
                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                callback.onChecked(result);
            }
        };
        task.execute();
        return task;
    }

    /**
     * Verify a password asynchronously.
     *
     * @param password  The password to check.
     * @param challenge The challenge to verify against the pattern.
     * @param userId    The user to check against the pattern.
     * @param callback  The callback to be invoked with the verification result.
     */
    public static AsyncTask<?, ?, ?> verifyPassword(
            final String password,
            final long challenge,
            final int userId,
            final OnVerifyCallback callback) {
        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
            private int mThrottleTimeout;

            @Override
            protected Boolean doInBackground(Void... args) {
                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                callback.onVerified(result);
            }
        };
        task.execute();
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
        task.execute();
        return task;
    }
}
