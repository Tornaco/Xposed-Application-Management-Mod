package github.tornaco.xposedmoduletest.ui.widget;

import android.app.Activity;
import android.support.v7.app.AlertDialog;

import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.R;

/**
 * Created by guohao4 on 2018/2/5.
 * Email: Tornaco@163.com
 */

public class UpdateLogDialog {

    public static void show(Activity context) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.title_app_update_log)
                .setMessage(BuildConfig.UPDATE_LOGS)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}
