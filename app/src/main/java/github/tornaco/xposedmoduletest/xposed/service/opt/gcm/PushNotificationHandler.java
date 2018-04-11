package github.tornaco.xposedmoduletest.xposed.service.opt.gcm;

import android.content.Intent;

/**
 * Created by Tornaco on 2018/4/10 17:24.
 * God bless no bug!
 */
public interface PushNotificationHandler {
    boolean handleIncomingIntent(String targetPackage, Intent intent);

    String getTag();

    void onSettingsChanged(String tag);
}
