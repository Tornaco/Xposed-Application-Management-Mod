package github.tornaco.xposedmoduletest.xposed.service.notification;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import java.util.Objects;

import github.tornaco.xposedmoduletest.util.OSUtil;
import github.tornaco.xposedmoduletest.xposed.service.AppResource;
import github.tornaco.xposedmoduletest.xposed.service.ProtectedBroadcastReceiver;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;
import lombok.Getter;

/**
 * Created by Tornaco on 2018/5/29 16:24.
 * This file is writen for project X-APM at host guohao4.
 */
public class RebootNotification {

    private int mLastRebootNotificationId;

    @Getter
    private Context context;
    private static final String ACTION_DISABLE_DEBUG_MODE = "github.tornaco.broadcast.action.reboot";

    public RebootNotification(Context context, Handler mainHandler) {
        this.context = context;
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null && ACTION_DISABLE_DEBUG_MODE.equals(intent.getAction())) {
                    XposedLog.wtf("RebootNotification Reboot!!!!");
                    mainHandler.post(() -> {
                        PowerManager pm = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
                        Objects.requireNonNull(pm).reboot(null);
                    });
                }
            }
        };
        context.registerReceiver(new ProtectedBroadcastReceiver(broadcastReceiver), new IntentFilter(ACTION_DISABLE_DEBUG_MODE));
    }

    public void show(String channelId, int notificationId) {
        if (getContext() == null) return;
        XposedLog.verbose("RebootNotification show");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId);
        try {
            SystemUI.overrideNotificationAppName(getContext(), builder, "X-APM");
        } catch (Throwable ignored) {
        }

        Intent disableBroadcastIntent = new Intent(ACTION_DISABLE_DEBUG_MODE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), UniqueIdFactory.getNextId(), disableBroadcastIntent,
                0);

        Notification n = builder
                .addAction(0, "立即重启", pendingIntent)
                .setContentTitle("需要重启")
                .setContentText("你现在需要重启你的设备已完成更新。")
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .build();

        if (OSUtil.isMOrAbove()) {
            n.setSmallIcon(new AppResource(getContext()).loadIconFromAPMApp("ic_refresh_fill_server"));
        }

        if (mLastRebootNotificationId > 0) {
            NotificationManagerCompat.from(context)
                    .cancel(mLastRebootNotificationId);
        }
        NotificationManagerCompat.from(context)
                .notify(notificationId, n);
        mLastRebootNotificationId = notificationId;
    }
}
