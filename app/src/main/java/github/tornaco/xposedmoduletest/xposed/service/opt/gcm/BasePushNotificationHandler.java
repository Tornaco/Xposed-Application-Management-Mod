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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import github.tornaco.xposedmoduletest.model.PushMessage;
import github.tornaco.xposedmoduletest.util.OSUtil;
import github.tornaco.xposedmoduletest.xposed.repo.SettingsProvider;
import github.tornaco.xposedmoduletest.xposed.service.AppResource;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by Tornaco on 2018/4/10 17:31.
 * God bless no bug!
 */

@SuppressWarnings("WeakerAccess")
abstract class BasePushNotificationHandler implements PushNotificationHandler {

    @Setter
    @Getter
    private Context context;

    private Intent launchIntent;

    private Map<String, Integer> mBadgeMap = new HashMap<>();

    private Set<Integer> mPendingCancelNotifications = new HashSet<>();

    BasePushNotificationHandler(Context context) {
        this.context = context;
        readSettings(getTag());
    }

    @Setter
    @Getter
    private boolean enabled, showContentEnabled;

    @Getter
    @Setter
    private String topPackage;

    @Override
    public void onSettingsChanged(String tag) {
        readSettings(tag);

        XposedLog.verbose("onSettingsChanged: %s enabled: %s, showContentEnabled:%s",
                getClass(),
                isEnabled(),
                isShowContentEnabled());
    }

    private void readSettings(String tag) {
        setEnabled(SettingsProvider.get().getBoolean(getTag(), false));
        setShowContentEnabled(SettingsProvider.get().getBoolean(getTag() + "_show_content", false));
    }

    @Override
    public void onTopPackageChanged(String who) {
        setTopPackage(who);
        if (getTargetPackageName().equals(who)) {
            XposedLog.verbose("onTopPackageChanged: %s", getClass());
            // Reset badge.
            clearBadge();

            cancelPendingCancelNotifications();
        }
    }

    protected void clearBadge() {
        mBadgeMap.clear();
    }

    protected void updateBadge(String from) {
        Integer count = mBadgeMap.get(from);
        if (count == null) {
            count = 0;
        }
        mBadgeMap.put(from, count + 1);
    }

    protected int getBadgeFrom(String from) {
        Integer count = mBadgeMap.get(from);
        return count == null ? 0 : count;
    }

    protected int getFromCount() {
        return mBadgeMap.size();
    }

    protected int getAllBadge() {
        int res = 0;
        Map<String, Integer> tmp = new HashMap<>(mBadgeMap);
        for (String key : tmp.keySet()) {
            Integer count = tmp.get(key);
            if (count == null) {
                count = 0;
            }
            res = res + count;
        }
        return res;
    }

    protected boolean isTargetPackageRunningOnTop() {
        return getTargetPackageName().equals(getTopPackage());
    }

    private void addToPendingCancelNotifications(int id) {
        mPendingCancelNotifications.add(id);
    }

    private void clearPendingCancelNotifications() {
        mPendingCancelNotifications.clear();
    }

    private void cancelPendingCancelNotifications() {
        try {
            Set<Integer> temp = new HashSet<>(mPendingCancelNotifications);

            NotificationManagerCompat nm = NotificationManagerCompat.from(getContext());
            for (Integer id : temp) {
                nm.cancel(id);
            }
            clearPendingCancelNotifications();
        } catch (Throwable e) {
            // We tried...
        }
    }

    protected Intent getLaunchIntent() {
        ensureLauncherIntent();
        return launchIntent;
    }

    private void ensureLauncherIntent() {
        synchronized (this) {
            if (launchIntent == null) {
                launchIntent = getContext().getPackageManager().getLaunchIntentForPackage(getTargetPackageName());
                XposedLog.verbose("BasePushNotificationHandler, launchIntent=" + launchIntent);
            }
        }
    }

    protected void postNotification(PushMessage pushMessage) {

        createWeChatNotificationChannelForO(pushMessage.getChannelId(), pushMessage.getChannelName());

        Intent launchIntent = getLaunchIntent();
        if (launchIntent == null) {
            XposedLog.wtf("Fail retrieve launch intent when postNotification!");
            return;
        }

        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0x1, launchIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), pushMessage.getChannelId());

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
                .setLargeIcon(new AppResource(getContext()).loadBitmapFromAPMApp(pushMessage.getLargeIconResName()))
                .setVibrate(new long[]{200, 200})
                .setSound(defaultSoundUri)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(Notification.PRIORITY_HIGH)
                .build();

        if (OSUtil.isMOrAbove()) {
            n.setSmallIcon(new AppResource(getContext()).loadIconFromAPMApp(pushMessage.getSmallIconResName()));
        }

        addToPendingCancelNotifications(pushMessage.getFrom());

        NotificationManagerCompat.from(getContext())
                .notify(pushMessage.getFrom(), n);
    }

    private void createWeChatNotificationChannelForO(String channelId, String channelName) {
        if (OSUtil.isOOrAbove()) {
            NotificationManager notificationManager = (NotificationManager)
                    getContext().getSystemService(
                            Context.NOTIFICATION_SERVICE);
            NotificationChannel nc = null;
            if (notificationManager != null) {
                nc = notificationManager.getNotificationChannel(channelId);
            }
            if (nc != null) {
                return;
            }
            NotificationChannel notificationChannel;
            notificationChannel = new NotificationChannel(channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.GREEN);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{200, 200});
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
    }

    @Override
    public void systemReady() {

    }
}
