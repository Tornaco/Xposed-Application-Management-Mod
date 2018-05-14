package github.tornaco.xposedmoduletest.util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * Created by Tornaco on 2018/3/26 15:46.
 * God bless no bug!
 */

public abstract class MarketUtil {

    public static void goToMarket(Context context, String packageName) {
        try {
            context.startActivity(buildIntent(packageName));
        } catch (ActivityNotFoundException ignored) {
        }
    }

    public static Intent buildIntent(String packageName) {
        Uri uri = Uri.parse("market://details?id=" + packageName);
        return new Intent(Intent.ACTION_VIEW, uri);
    }
}
