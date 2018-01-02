package github.tornaco.xposedmoduletest.xposed.service.doze;

import android.content.Context;
import android.os.Build;
import android.os.PowerManager;

/**
 * Created by Nick on 2017/6/28 15:19
 */
public abstract class DozeStateRetriever {

    public static boolean isLightDeviceIdleMode(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return pm != null && pm.isLightDeviceIdleMode();
    }

    public static boolean isDeviceIdleMode(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && pm != null && pm.isDeviceIdleMode();
    }
}
