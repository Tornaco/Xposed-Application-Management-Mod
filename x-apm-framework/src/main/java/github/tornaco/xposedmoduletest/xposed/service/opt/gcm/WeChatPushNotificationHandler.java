package github.tornaco.xposedmoduletest.xposed.service.opt.gcm;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import github.tornaco.xposedmoduletest.model.PushMessage;
import github.tornaco.xposedmoduletest.service.PushMessageNotificationService;
import github.tornaco.xposedmoduletest.xposed.service.notification.UniqueIdFactory;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by Tornaco on 2018/4/10 17:30.
 * God bless no bug!
 */
public class WeChatPushNotificationHandler extends BasePushNotificationHandler {

    public static final String WECHAT_PKG_NAME = "com.tencent.mm";

    private static final String NOTIFICATION_CHANNEL_ID_WECHAT = "dev.tornaco.notification.channel.id.X-APM-WECHAT";
    private static final String NOTIFICATION_CHANNEL_NAME_WECHAT = "WeChat";

    private static final String WECHAT_INTENT_KEY_ALERT = "alert";
    private static final String WECHAT_INTENT_KEY_BADGE = "badge";
    private static final String WECHAT_INTENT_KEY_FROM = "from";

    public WeChatPushNotificationHandler(Context context, NotificationHandlerSettingsRetriever retriever) {
        super(context, retriever);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static boolean launchNotificationChannelSettingsForOreo(Context context,
                                                                   boolean android/*App layer or FW layer*/) {
        try {
            Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, android ? "android" : context.getPackageName())
                    .putExtra(Settings.EXTRA_CHANNEL_ID, NOTIFICATION_CHANNEL_ID_WECHAT)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            return false;
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

        if (isTargetPackageRunningOnTop()) {
            // Reset all when package is in front.
            XposedLog.verbose("WeChatPushNotificationHandler target is running on top");
            clearBadge();
            return true;
        }

        if (isNotificationPostByAppEnabled() && PushMessageNotificationService.start(getContext(),
                resolveWeChatPushIntent(intent))) {
            XposedLog.verbose("WeChatPushNotificationHandler posted by app!");
        } else {
            postNotification(resolveWeChatPushIntent(intent));
        }


        return false;
    }

    @Override
    public String getTargetPackageName() {
        return WECHAT_PKG_NAME;
    }

    private PushMessage resolveWeChatPushIntent(Intent intent) {

        try {

            // If this is a test.
            boolean isTestMessage = intent.hasExtra(KEY_MOCK_MESSAGE);
            if (isTestMessage) {
                return createAlertMessage("X-APM", intent.getStringExtra(KEY_MOCK_MESSAGE));
            }

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
                .smallIconResName("ic_wechat_2_fill")
                .largeIconResName("ic_stat_large_wechat")
                .title(String.format("%s条消息", getBadgeFrom(from)))
                .message(alert)
                // Diff sender with diff id.
                .from(MessageIdWrapper.id(from))
                .targetPackageName(getTargetPackageName())
                .build();
    }

    // No content.
    private PushMessage createSecretPushMessage(String from) {
        return PushMessage.builder()
                .channelId(NOTIFICATION_CHANNEL_ID_WECHAT)
                .channelName(NOTIFICATION_CHANNEL_NAME_WECHAT)
                .smallIconResName("ic_wechat_2_fill")
                .largeIconResName("ic_stat_large_wechat")
                .title("微信")
                .message(String.format("%s个联系人发来%s条消息", getFromCount(), getAllBadge()))
                .from(MessageIdWrapper.id(WECHAT_PKG_NAME)) // Do not split for diff sender...
                .targetPackageName(getTargetPackageName())
                .build();
    }


    // Some err occurred, we post this message.
    private PushMessage createDefaultPushMessage() {
        return PushMessage.builder()
                .channelId(NOTIFICATION_CHANNEL_ID_WECHAT)
                .channelName(NOTIFICATION_CHANNEL_NAME_WECHAT)
                .smallIconResName("ic_wechat_2_fill")
                .largeIconResName("ic_stat_large_wechat")
                .message("你收到了一条新消息")
                .title("微信")
                .from(MessageIdWrapper.id(WECHAT_PKG_NAME)) // Do not split for diff sender...
                .targetPackageName(getTargetPackageName())
                .build();
    }

    static class MessageIdWrapper {
        static final Map<String, Integer> idMap = new HashMap<>();

        static int id(String messageIdString) {
            Integer cache = idMap.get(messageIdString);
            if (cache != null) return cache;
            int idNew = UniqueIdFactory.getNextId();
            idMap.put(messageIdString, idNew);
            return idNew;
        }
    }
}
