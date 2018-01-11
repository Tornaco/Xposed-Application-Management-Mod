package github.tornaco.xposedmoduletest.util;

import android.os.Build;
import android.os.SystemProperties;
import android.text.TextUtils;

/**
 * Created by guohao4 on 2017/10/24.
 * Email: Tornaco@163.com
 */

public abstract class OSUtil {

    private static final String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code";
    private static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
    private static final String KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage";

    public static boolean isFlyme() {
        String id = SystemProperties.get("ro.build.display.id", "");
        return !TextUtils.isEmpty(id) && (id.contains("flyme")
                || id.toLowerCase().contains("flyme"));
    }

    public static boolean isMIUI() {
        return Build.FINGERPRINT.startsWith("Xiaomi");
    }

    public static boolean isLOS() {
        return false;
    }

    public static boolean isMOrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static boolean isNOrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    }

    public static boolean isOOrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }
}
