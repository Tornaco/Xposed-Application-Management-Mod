package github.tornaco.xposedmoduletest.xposed.service.opt.gcm;

/**
 * Created by Tornaco on 2018/4/19 9:25.
 * God bless no bug!
 */
public interface NotificationHandlerSettingsRetriever {

    boolean isPushMessageHandlerEnabled(java.lang.String pkg);

    boolean isPushMessageHandlerShowContentEnabled(java.lang.String pkg);

    boolean isPushMessageHandlerNotificationSoundEnabled(java.lang.String pkg);

    boolean isPushMessageHandlerNotificationVibrateEnabled(java.lang.String pkg);

    boolean isPushMessageHandlerMessageNotificationByAppEnabled(java.lang.String pkg);
}
