package github.tornaco.xposedmoduletest.xposed.service.notification;

import android.app.Notification;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

/**
 * Created by Tornaco on 2018/5/24 14:48.
 * God bless no bug!
 */
public abstract class SystemUI {

    public static void overrideNotificationAppName(Context context, Notification.Builder n, String name) {
        final Bundle extras = new Bundle();
        extras.putString(Notification.EXTRA_SUBSTITUTE_APP_NAME,
                name == null ? context.getString(com.android.internal.R.string.android_system_label)
                        : name);

        n.addExtras(extras);
    }

    public static void overrideNotificationAppName(Context context, NotificationCompat.Builder n, String name) {
        final Bundle extras = new Bundle();
        extras.putString(Notification.EXTRA_SUBSTITUTE_APP_NAME,
                name == null ? context.getString(com.android.internal.R.string.android_system_label)
                        : name);

        n.addExtras(extras);
    }
}
