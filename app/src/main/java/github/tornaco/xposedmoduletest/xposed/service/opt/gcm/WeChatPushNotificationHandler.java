package github.tornaco.xposedmoduletest.xposed.service.opt.gcm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.model.PushMessage;
import github.tornaco.xposedmoduletest.util.OSUtil;
import github.tornaco.xposedmoduletest.xposed.service.AppResource;
import github.tornaco.xposedmoduletest.xposed.util.ObjectToStringUtil;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by Tornaco on 2018/4/10 17:30.
 * God bless no bug!
 */
public class WeChatPushNotificationHandler extends BasePushNotificationHandler {

    private static final String WECHAT_PKG_NAME = "com.tencent.mm";

    private static final String NOTIFICATION_CHANNEL_ID_WECHAT = "dev.tornaco.notification.channel.id.X-APM-WECHAT";
    private static final AtomicInteger NOTIFICATION_ID_DYNAMIC = new AtomicInteger(6666);

    private static final String WECHAT_INTENT_KEY_ALERT = "alert";
    private static final String WECHAT_INTENT_KEY_BADGE = "badge";
    private static final String WECHAT_INTENT_KEY_FROM = "from";

    private Intent mWeChatLaunchIntent;

    public WeChatPushNotificationHandler(Context context) {
        super(context);
    }

    private Intent ensureLauncherIntent() {
        synchronized (this) {
            if (mWeChatLaunchIntent == null) {
                mWeChatLaunchIntent = getContext().getPackageManager().getLaunchIntentForPackage(WECHAT_PKG_NAME);
                XposedLog.verbose("WeChatPushNotificationHandler, mWeChatLaunchIntent=" + mWeChatLaunchIntent);
            }
            return mWeChatLaunchIntent;
        }
    }

    // Example. Assets/wechat_intent_dump
    @Override
    public boolean handleIncomingIntent(String targetPackage, Intent intent) {
        if (!isEnabled()) {
            XposedLog.verbose("WeChatPushNotificationHandler not enabled");
            return false;
        }
        if (XposedLog.isVerboseLoggable()) {
            XposedLog.verbose("handleIncomingIntent:" + intent);
        }
        if (!WECHAT_PKG_NAME.equals(targetPackage)) {
            return false;
        }

        if (BuildConfig.DEBUG) {
            XposedLog.verbose("WeChatPushNotificationHandler@ intent: "
                    + intent + "extra: " + intent.getExtras()
                    + ObjectToStringUtil.intentToString(intent));
        }

        postNotification(resolveWeChatPushIntent(intent));

        return false;
    }

    @Override
    public String getTag() {
        return "wechat";
    }

    @Override
    public String getTargetPackageName() {
        return WECHAT_PKG_NAME;
    }

    private static final PushMessage DUMMY_MSG = PushMessage.builder()
            .title("微信")
            .message("你收到了一条新消息")
            .from(NOTIFICATION_ID_DYNAMIC.getAndIncrement())
            .build();

    private PushMessage resolveWeChatPushIntent(Intent intent) {
        if (!isShowContentEnabled()) {
            return DUMMY_MSG;
        }
        try {
            String alert = intent.getStringExtra(WECHAT_INTENT_KEY_ALERT);
            String badge = intent.getStringExtra(WECHAT_INTENT_KEY_BADGE);
            String from = intent.getStringExtra(WECHAT_INTENT_KEY_FROM);
            // Increase message count.
            setBadge(getBadge() + 1);
            return PushMessage.builder()
                    .title(String.format("微信%s条消息", getBadge()))
                    .message(alert)
                    .from(MessageIdWrapper.id(WECHAT_PKG_NAME)) // Do not split for diff sender...
                    .build();
        } catch (Throwable e) {
            XposedLog.wtf("Fail resolveWeChatPushIntent: " + Log.getStackTraceString(e));
            return DUMMY_MSG;
        }
    }

    private void postNotification(PushMessage pushMessage) {
        createWeChatNotificationChannelForO();

        Intent launchIntent = ensureLauncherIntent();
        if (launchIntent == null) {
            XposedLog.wtf("Fail retrieve wechat launch intent!");
            return;
        }

        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0x1, launchIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(),
                NOTIFICATION_CHANNEL_ID_WECHAT);

        android.support.v4.app.NotificationCompat.BigTextStyle style =
                new android.support.v4.app.NotificationCompat.BigTextStyle();
        style.bigText(pushMessage.getMessage());
        style.setBigContentTitle(pushMessage.getTitle());

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Notification n = builder
                .setContentIntent(pendingIntent)
                .setContentTitle(pushMessage.getTitle())
                .setContentText(pushMessage.getMessage())
                .setAutoCancel(true)
                .setStyle(style)
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                //.setLargeIcon(new AppResource(getContext()).loadBitmapFromAPMApp("ic_stat_large_wechat"))
                .setVibrate(new long[]{100, 100})
                .setSound(defaultSoundUri)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                //.setFullScreenIntent(pendingIntent, false)
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(Notification.PRIORITY_HIGH)
                .build();

        if (OSUtil.isMOrAbove()) {
            n.setSmallIcon(new AppResource(getContext()).loadIconFromAPMApp("ic_stat_weixin"));
        }

        NotificationManagerCompat.from(getContext())
                .notify(pushMessage.getFrom(), n);
    }

    private void createWeChatNotificationChannelForO() {
        if (OSUtil.isOOrAbove()) {
            NotificationManager notificationManager = (NotificationManager)
                    getContext().getSystemService(
                            Context.NOTIFICATION_SERVICE);
            NotificationChannel nc = null;
            if (notificationManager != null) {
                nc = notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID_WECHAT);
            }
            if (nc != null) {
                return;
            }
            NotificationChannel notificationChannel;
            notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID_WECHAT,
                    "微信频道",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.GREEN);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 100});
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
    }

    static class MessageIdWrapper {
        static final Map<String, Integer> idMap = new HashMap<>();
        static final AtomicInteger NOTIFICATION_ID_DYNAMIC = new AtomicInteger(9999);

        static int id(String messageIdString) {
            Integer cache = idMap.get(messageIdString);
            if (cache != null) return cache;
            int idNew = NOTIFICATION_ID_DYNAMIC.getAndIncrement();
            idMap.put(messageIdString, idNew);
            return idNew;
        }
    }
}
