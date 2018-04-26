package github.tornaco.xposedmoduletest.util;

import android.content.Context;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.newstand.logger.Logger;

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
            GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
            int resultCode = apiAvailability.isGooglePlayServicesAvailable(context);
            if (resultCode != ConnectionResult.SUCCESS) {
//            if (apiAvailability.isUserResolvableError(resultCode)) {
//                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
//                        .show();
//            } else {
//                Logger.d("GSM This device is not supported.");
//            }
                Logger.d("GSM This device is not supported.");
                return false;
            }
            Logger.d("GSM This device is supported.");
            return true;
        } catch (Throwable e) {
            // We tried...
            return false;
        }
    }
}
