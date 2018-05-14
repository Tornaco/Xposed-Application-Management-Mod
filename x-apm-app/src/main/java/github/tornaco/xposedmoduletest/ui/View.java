package github.tornaco.xposedmoduletest.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

/**
 * Created by guohao4 on 2017/9/21.
 * Email: Tornaco@163.com
 */

public interface View {
    void setTitle(@StringRes int title);

    void setTitle(CharSequence title);

    void showHomeAsUp();

    void finish();

    void showSimpleDialog(String title, String message);

    void showDialog(String title, String message,
                    String positive, String negative,
                    boolean cancelable,
                    @Nullable Runnable ok,
                    @Nullable Runnable cancel);

    void showDialog(@StringRes int title, String message,
                    int positive, int negative, int neutral,
                    boolean cancelable,
                    @Nullable Runnable ok,
                    @Nullable Runnable cancel,
                    @Nullable Runnable net);

    void showTips(CharSequence tips, boolean infinite, String actionTitle, Runnable action);

    void showTips(@StringRes int tipsRes, boolean infinite, String actionTitle, Runnable action);

    void showProgress(@StringRes int progressTitle, @StringRes int progressMessage);

    void showProgress(CharSequence progressTitle, CharSequence progressMessage);

    @NonNull
    Context getContext();

    void checkRuntimePermissions();
}
