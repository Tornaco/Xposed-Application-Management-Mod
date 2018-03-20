package github.tornaco.xposedmoduletest.xposed.service.opt.gcm;

import android.content.Intent;

/**
 * Created by Tornaco on 2018/3/20 13:31.
 * God bless no bug!
 */

public class GCMFCMHelper {

    private static final String ACTION_GCM = "com.google.android.c2dm.intent.RECEIVE";
    private static final String ACTION_FCM = "com.google.firebase.MESSAGING_EVENT";

    public static boolean isGcmIntent(Intent intent) {
        return intent != null && ACTION_GCM.equals(intent.getAction());
    }

    public static boolean isFcmIntent(Intent intent) {
        return intent != null && ACTION_FCM.equals(intent.getAction());
    }

    public static boolean isGcmOrFcmIntent(Intent intent) {
        return isFcmIntent(intent) || isGcmIntent(intent);
    }
}
