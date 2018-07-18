package github.tornaco.xposedmoduletest.xposed.service.opt.gcm;

import android.content.Intent;

import java.util.HashMap;
import java.util.Map;

import lombok.Synchronized;

/**
 * Created by Tornaco on 2018/3/20 13:31.
 * God bless no bug!
 */

public class PushMessageHelper {

    public static final long PUSH_INTENT_HANDLE_INTERVAL_MILLS = 30 * 1000;

    // Some apps received Push event, give it a little while to handle it.
    private static final Map<String, GcmEvent> PUSH_EVENT_MAP = new HashMap<>();

    // com.google.android.c2dm.intent.RECEIVE
    public static final String ACTION_GCM = "com.google.android.c2dm.intent.RECEIVE";
    public static final String ACTION_FCM = "com.google.firebase.MESSAGING_EVENT";

    public static final String ACTION_MIPUSH = "com.xiaomi.mipush.RECEIVE_MESSAGE";

    public static boolean isGcmIntent(Intent intent) {
        return intent != null && ACTION_GCM.equals(intent.getAction());
    }

    public static boolean isFcmIntent(Intent intent) {
        return intent != null && ACTION_FCM.equals(intent.getAction());
    }

    public static boolean isMIPushIntent(Intent intent) {
        return intent != null && ACTION_MIPUSH.equals(intent.getAction());
    }

    public static boolean isPushIntent(Intent intent) {
        return isFcmIntent(intent) || isGcmIntent(intent) || isMIPushIntent(intent);
    }

    @Synchronized
    public static boolean isHandlingPushIntent(String who) {
        final GcmEvent event = PUSH_EVENT_MAP.get(who);
        if (event == null) return false;

        long interval = System.currentTimeMillis() - event.getEventTime();
        // Handle invalid.
        if (interval < 0) {
            // This is bad!!!
            event.setEventTime(0);
            return false;
        }
        return interval <= PUSH_INTENT_HANDLE_INTERVAL_MILLS;
    }

    @Synchronized
    public static void onGcmIntentReceived(String who) {
        GcmEvent event = PUSH_EVENT_MAP.get(who);
        if (event == null) event = new GcmEvent();

        // Update event.
        event.setEventTime(System.currentTimeMillis());
        event.setEventPackage(who);
        PUSH_EVENT_MAP.put(who, event);
    }
}
