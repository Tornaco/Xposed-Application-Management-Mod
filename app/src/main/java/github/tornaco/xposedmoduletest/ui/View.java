package github.tornaco.xposedmoduletest.ui;

import android.content.Context;
import android.support.annotation.NonNull;
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

    void showTips(CharSequence tips, boolean infinite, String actionTitle, Runnable action);

    void showTips(@StringRes int tipsRes, boolean infinite, String actionTitle, Runnable action);

    void showProgress(@StringRes int progressTitle, @StringRes int progressMessage);

    void showProgress(CharSequence progressTitle, CharSequence progressMessage);

    @NonNull
    Context getContext();

    void checkRuntimePermissions();
}
