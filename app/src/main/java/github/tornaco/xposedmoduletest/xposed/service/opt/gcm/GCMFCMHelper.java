package github.tornaco.xposedmoduletest.xposed.service.opt.gcm;

import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import java.util.HashSet;
import java.util.Set;

import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by Tornaco on 2018/3/20 13:31.
 * God bless no bug!
 */

public class GCMFCMHelper {

    private static final long GCM_INTENT_HANDLE_INTERVAL = 24 * 1000;

    // Some apps received GCM event, give it a little while to handle it.
    private static final Set<String> sGcmPendingApps = new HashSet<>();

    private static Handler sInternalHandler;

    // com.google.android.c2dm.intent.RECEIVE
    public static final String ACTION_GCM = "com.google.android.c2dm.intent.RECEIVE";
    public static final String ACTION_FCM = "com.google.firebase.MESSAGING_EVENT";

    public static boolean isGcmIntent(Intent intent) {
        return intent != null && ACTION_GCM.equals(intent.getAction());
    }

    public static boolean isFcmIntent(Intent intent) {
        return intent != null && ACTION_FCM.equals(intent.getAction());
    }

    public static boolean isGcmOrFcmIntent(Intent intent) {
        return isFcmIntent(intent) || isGcmIntent(intent);
    }

    public static boolean isHandlingGcmIntent(String who) {
        return sGcmPendingApps.contains(who);
    }

    public static void onGcmIntentReceived(String who) {
        XposedLog.verbose("onGcmIntentReceived: " + who);
        sGcmPendingApps.add(who);
        // Give it 10s to handle this.
        if (sInternalHandler == null) initInternalHandler();
        sInternalHandler.sendMessageDelayed(sInternalHandler.obtainMessage(0, who), GCM_INTENT_HANDLE_INTERVAL);
    }

    private static void onGcmIntentHandled(String who) {
        sGcmPendingApps.remove(who);
        XposedLog.verbose("onGcmIntentHandled: " + who);
    }

    private static void initInternalHandler() {
        HandlerThread hr = new HandlerThread("GCMFCMHelperHandler");
        hr.start();
        sInternalHandler = new Handler(hr.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                try {
                    String pkg = (String) msg.obj;
                    onGcmIntentHandled(pkg);
                } catch (Exception e) {
                    XposedLog.wtf("GCMFCMHelperHandler" + e.getLocalizedMessage());
                }
            }
        };
        XposedLog.verbose("initInternalHandler: " + sInternalHandler);
    }
}
