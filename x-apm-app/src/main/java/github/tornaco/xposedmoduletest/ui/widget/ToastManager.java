package github.tornaco.xposedmoduletest.ui.widget;

import android.content.Context;
import android.widget.Toast;

import lombok.Synchronized;

/**
 * Created by guohao4 on 2018/2/26.
 * Email: Tornaco@163.com
 */

public class ToastManager {

    private static Toast sToast;

    @Synchronized
    public static void show(Context context, String text) {
        if (sToast != null) {
            sToast.cancel();
        }
        sToast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        sToast.show();
    }
}
