package github.tornaco.xposedmoduletest.util;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;

import org.newstand.logger.Logger;

/**
 * Created by guohao4 on 2017/12/19.
 * Email: Tornaco@163.com
 */

public class RunningServiceLauncher {

    private static final String ACTIVITY_NAME = "com.android.settings.Settings$RunningServicesActivity";
    private static final String PKG_NAME = "com.android.settings";

    public static boolean launch(Activity context) {
        try {
            Intent intent = new Intent("/");
            ComponentName cm = new ComponentName("com.android.settings", "com.android.settings.RunningServices");
            intent.setComponent(cm);
            intent.setAction("android.intent.action.VIEW");
            context.startActivityForResult(intent, 0);
            return true;
        } catch (Throwable e) {
            Logger.e("RunningServiceLauncher: " + Logger.getStackTraceString(e));
            return false;
        }
    }
}
