package github.tornaco.xposedmoduletest.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.SystemProperties;
import android.text.TextUtils;

/**
 * Created by guohao4 on 2017/10/24.
 * Email: Tornaco@163.com
 */

public abstract class OSUtil {

    public static boolean isFlyme() {
        String id = SystemProperties.get("ro.build.display.id", "");
        return !TextUtils.isEmpty(id) && (id.contains("flyme")
                || id.toLowerCase().contains("flyme"));
    }

    public static boolean isMIUI() {
        return Build.FINGERPRINT.startsWith("Xiaomi") || Build.FINGERPRINT.startsWith("xiaomi");
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

    public static boolean isO() {
        return Build.VERSION.SDK_INT == Build.VERSION_CODES.O
                || Build.VERSION.SDK_INT == (Build.VERSION_CODES.O + 1);
    }

    public static boolean isPOrAbove() {
        return Build.VERSION.SDK_INT > (Build.VERSION_CODES.O + 1);
    }

    public static boolean isQOrAbove() {
        return Build.VERSION.SDK_INT >= 29;
    }

    public static boolean hasTvFeature(Context context) {
        PackageManager pm = context.getPackageManager();
        return pm.hasSystemFeature(PackageManager.FEATURE_LIVE_TV);
    }

    public static boolean isLenovoDevice() {
        return Build.MANUFACTURER.contains("ZUK") || Build.MANUFACTURER.contains("Lenovo");
    }

    public static boolean isNTDDevice() {
        return Build.MANUFACTURER.contains("NTD");
    }

    public static boolean isHuaWeiDevice() {
        return Build.MANUFACTURER.contains("HUAWEI");
    }

    public static boolean isEMUI() {
        return !TextUtils.isEmpty(SystemProperties.get("ro.build.version.emui", ""));
    }

    public static boolean isNubiaDevice() {
        return Build.FINGERPRINT.contains("nubia");
    }
}
