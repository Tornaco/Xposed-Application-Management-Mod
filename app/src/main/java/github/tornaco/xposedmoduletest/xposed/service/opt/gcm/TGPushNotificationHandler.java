package github.tornaco.xposedmoduletest.xposed.service.opt.gcm;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import github.tornaco.xposedmoduletest.model.PushMessage;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by Tornaco on 2018/4/10 17:30.
 * God bless no bug!
 */
public class TGPushNotificationHandler extends BasePushNotificationHandler {

    private static final String TG_PKG_NAME = "org.telegram.messenger";

    private static final String NOTIFICATION_CHANNEL_ID_WECHAT = "dev.tornaco.notification.channel.id.X-APM-TG";
    private static final String NOTIFICATION_CHANNEL_NAME_WECHAT = "Telegram";

    private static final String TG_INTENT_KEY_FROM = "from";

    private static final int NOTIFICATION_ID_TG = 12000;

    public TGPushNotificationHandler(Context context) {
        super(context);
    }

    // Example. Assets/tg_intent_dump
    @Override
    public boolean handleIncomingIntent(String targetPackage, Intent intent) {
        if (!isEnabled()) {
            XposedLog.verbose("TGPushNotificationHandler not enabled");
            return false;
        }
        if (XposedLog.isVerboseLoggable()) {
            XposedLog.verbose("handleIncomingIntent:" + intent);
        }
        if (!TG_PKG_NAME.equals(targetPackage)) {
            return false;
        }

        postNotification(resolvePushIntent(intent));

        return false;
    }

    @Override
    public String getTag() {
        return "tg";
    }

    @Override
    public String getTargetPackageName() {
        return TG_PKG_NAME;
    }

    private PushMessage resolvePushIntent(Intent intent) {

        try {

            String from = intent.getStringExtra(TG_INTENT_KEY_FROM);

            if (from == null) {
                return createDefaultPushMessage();
            }

            // Increase message count.
            updateBadge(from);

            return createSecretPushMessage(from);
        } catch (Throwable e) {
            XposedLog.wtf("Fail resolvePushIntent, use default: " + Log.getStackTraceString(e));
            return createDefaultPushMessage();
        }
    }

    // No content.
    private PushMessage createSecretPushMessage(String from) {
        return PushMessage.builder()
                .channelId(NOTIFICATION_CHANNEL_ID_WECHAT)
                .channelName(NOTIFICATION_CHANNEL_NAME_WECHAT)
                .smallIconResName("ic_stat_tg")
                .largeIconResName("ic_stat_tg")
                .title("Telegram")
                .message(String.format("%s个联系人发来%s条消息", getFromCount(), getAllBadge()))
                .from(NOTIFICATION_ID_TG) // Do not split for diff sender...
                .build();
    }


    // Some err occurred, we post this message.
    private PushMessage createDefaultPushMessage() {
        return PushMessage.builder()
                .channelId(NOTIFICATION_CHANNEL_ID_WECHAT)
                .channelName(NOTIFICATION_CHANNEL_NAME_WECHAT)
                .smallIconResName("ic_stat_tg")
                .largeIconResName("ic_stat_tg")
                .message("你收到了一条新消息")
                .title("Telegram")
                .from(NOTIFICATION_ID_TG) // Do not split for diff sender...
                .build();
    }
}
