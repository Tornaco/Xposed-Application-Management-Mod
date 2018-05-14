package github.tornaco.xposedmoduletest.util;

import android.content.Context;

import org.newstand.logger.Logger;

import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;

/**
 * Created by Tornaco on 2018/3/23 12:35.
 * God bless no bug!
 */

public class GMSUtil {
    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    public static boolean checkPlayServices(Context context) {
        try {
            boolean support = PkgUtil.isPkgInstalled(context, "com.google.android.gms");
            Logger.d("GSM This device is supported: " + support);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }
}
