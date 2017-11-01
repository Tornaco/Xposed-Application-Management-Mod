package github.tornaco.xposedmoduletest.license;

import android.content.Context;
import android.text.TextUtils;

import org.newstand.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.util.XExecutor;
import github.tornaco.xposedmoduletest.x.XSettings;

/**
 * Created by guohao4 on 2017/10/17.
 * Email: Tornaco@163.com
 */

public class XActivation {

    private static final List<License> LICENSES = new ArrayList<>();

    public static boolean invalidate(License license) {
        return LICENSES.contains(license);
    }

    public static String deviceIdEnc() {
        return null;
    }

    public static void reloadAsync(final Context context) {
        XExecutor.execute(new Runnable() {
            @Override
            public void run() {
                reload(context);
            }
        });
    }

    private static void reload(Context context) {
        // Try read act code.
        String act = XSettings.getActivateCode(context);
        if (TextUtils.isEmpty(act)) {
            XSettings.setActivateCode(context, context.getString(R.string.prebuilt_activate_code));
        }

        LicenseService licenseService = LicenseService.Factory.create();
        try {
            List<License> licenseList = licenseService.all().execute().body();
            LICENSES.clear();
            if (licenseList != null) {
                LICENSES.addAll(licenseList);
            }
        } catch (Exception e) {
            Logger.e(Logger.getStackTraceString(e));
        }
    }
}
