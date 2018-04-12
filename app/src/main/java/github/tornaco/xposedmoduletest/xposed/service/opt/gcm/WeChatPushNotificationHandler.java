package github.tornaco.xposedmoduletest.xposed.service.opt.gcm;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import github.tornaco.xposedmoduletest.model.PushMessage;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by Tornaco on 2018/4/10 17:30.
 * God bless no bug!
 */
public class WeChatPushNotificationHandler extends BasePushNotificationHandler {

    private static final String WECHAT_PKG_NAME = "com.tencent.mm";

    private static final String NOTIFICATION_CHANNEL_ID_WECHAT = "dev.tornaco.notification.channel.id.X-APM-WECHAT";
    private static final String NOTIFICATION_CHANNEL_NAME_WECHAT = "WeChat";

    private static final String WECHAT_INTENT_KEY_ALERT = "alert";
    private static final String WECHAT_INTENT_KEY_BADGE = "badge";
    private static final String WECHAT_INTENT_KEY_FROM = "from";

    public WeChatPushNotificationHandler(Context context) {
        super(context);
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

    private PushMessage resolveWeChatPushIntent(Intent intent) {

        try {

            String from = intent.getStringExtra(WECHAT_INTENT_KEY_FROM);

            if (from == null) {
                return createDefaultPushMessage();
            }

            // Increase message count.
            updateBadge(from);

            if (!isShowContentEnabled()) {
                return createSecretPushMessage(from);
            }

            String alert = intent.getStringExtra(WECHAT_INTENT_KEY_ALERT);

            if (alert == null) {
                return createSecretPushMessage(from);
            }

            return createAlertMessage(from, alert);
        } catch (Throwable e) {
            XposedLog.wtf("Fail resolveWeChatPushIntent, use default: " + Log.getStackTraceString(e));
            return createDefaultPushMessage();
        }
    }

    private PushMessage createAlertMessage(String from, String alert) {
        return PushMessage.builder()
                .channelId(NOTIFICATION_CHANNEL_ID_WECHAT)
                .channelName(NOTIFICATION_CHANNEL_NAME_WECHAT)
                .smallIconResName("ic_stat_weixin")
                .largeIconResName("ic_stat_weixin")
                .title(String.format("%s条消息", getBadgeFrom(from)))
                .message(alert)
                // Diff sender with diff id.
                .from(MessageIdWrapper.id(from))
                .build();
    }

    // No content.
    private PushMessage createSecretPushMessage(String from) {
        return PushMessage.builder()
                .channelId(NOTIFICATION_CHANNEL_ID_WECHAT)
                .channelName(NOTIFICATION_CHANNEL_NAME_WECHAT)
                .smallIconResName("ic_stat_weixin")
                .largeIconResName("ic_stat_weixin")
                .title("微信")
                .message(String.format("%s个联系人发来%s条消息", getFromCount(), getAllBadge()))
                .from(MessageIdWrapper.id(WECHAT_PKG_NAME)) // Do not split for diff sender...
                .build();
    }


    // Some err occurred, we post this message.
    private PushMessage createDefaultPushMessage() {
        return PushMessage.builder()
                .channelId(NOTIFICATION_CHANNEL_ID_WECHAT)
                .channelName(NOTIFICATION_CHANNEL_NAME_WECHAT)
                .smallIconResName("ic_stat_weixin")
                .largeIconResName("ic_stat_weixin")
                .message("你收到了一条新消息")
                .title("微信")
                .from(MessageIdWrapper.id(WECHAT_PKG_NAME)) // Do not split for diff sender...
                .build();
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
