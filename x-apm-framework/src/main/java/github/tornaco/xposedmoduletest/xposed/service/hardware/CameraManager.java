package github.tornaco.xposedmoduletest.xposed.service.hardware;

import android.app.KeyguardManager;
import android.app.Notification;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import github.tornaco.xposedmoduletest.util.OSUtil;
import github.tornaco.xposedmoduletest.util.Singleton;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.service.AppResource;
import github.tornaco.xposedmoduletest.xposed.service.notification.SystemUI;
import github.tornaco.xposedmoduletest.xposed.service.notification.UniqueIdFactory;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by Tornaco on 2018/6/22 9:56.
 * This file is writen for project X-APM at host guohao4.
 */
public class CameraManager {

    private static final Singleton<CameraManager> sManager
            = new Singleton<CameraManager>() {
        @Override
        protected CameraManager create() {
            return new CameraManager();
        }
    };

    public static CameraManager getInstance() {
        return sManager.get();
    }

    private Handler mHandler;

    private String mCameraOpenNotificationChannelId;

    private CameraManager() {
        HandlerThread handlerThread = new HandlerThread("X-APM-CameraManager");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                // Wrap with try catch to make it safe.
                try {
                    super.handleMessage(msg);
                } catch (Throwable e) {
                    XposedLog.wtf("Handler @CameraManager err: " + Log.getStackTraceString(e));
                }
            }
        };

        // Watch settings.
    }

    public void watchCameraDevice(Context context) {

        try {
            android.hardware.camera2.CameraManager cameraManager =
                    (android.hardware.camera2.CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            if (cameraManager != null) {
                cameraManager.registerAvailabilityCallback(new android.hardware.camera2.CameraManager.AvailabilityCallback() {
                    @Override
                    public void onCameraAvailable(@NonNull String cameraId) {
                        super.onCameraAvailable(cameraId);
                        XposedLog.verbose("CameraManager onCameraAvailable: " + cameraId);
                    }

                    @Override
                    public void onCameraUnavailable(@NonNull String cameraId) {
                        super.onCameraUnavailable(cameraId);
                        XposedLog.verbose("CameraManager onCameraUnavailable: " + cameraId);
                        try {
                            if (XAPMManager.get().isOptFeatureEnabled(XAPMManager.OPT.CAMERA_OPEN_NOTIFICATION.name())) {
                                showCameraOpenNotification(context, null, cameraId);
                            }
                        } catch (Throwable e) {
                            XposedLog.wtf("Error showCameraOpenNotification " + Log.getStackTraceString(e));
                        }
                    }
                }, mHandler);
            }
        } catch (Throwable e) {
            XposedLog.wtf("Fail watchCameraDevice " + Log.getStackTraceString(e));
        }
    }

    public void enableCameraOpenNotification(String notificationChannelId) {
        mCameraOpenNotificationChannelId = notificationChannelId;
    }

    private void showCameraOpenNotification(Context context, String openByPackageName, String cameraId) {
        if (context == null) return;
        XposedLog.verbose("showCameraOpenNotification show");

        if (isKeyguard(context)) {
            XposedLog.verbose("showCameraOpenNotification skip on keyguard");
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, mCameraOpenNotificationChannelId);
        try {
            SystemUI.overrideNotificationAppName(context, builder, "X-APM");
        } catch (Throwable ignored) {
        }

        Notification n = builder
                .setContentTitle("相机被打开")
                .setContentText(String.format("序号%s的相机被打开了。", cameraId))
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .build();

        if (OSUtil.isMOrAbove()) {
            n.setSmallIcon(new AppResource(context).loadIconFromAPMApp("ic_camera_alt_black_24dp"));
        }

        NotificationManagerCompat.from(context)
                .notify(UniqueIdFactory.getIdByTag("X-APM-CAMERA-OPEN-" + cameraId), n);
    }

    private boolean isKeyguard(Context context) {
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        return keyguardManager != null && keyguardManager.inKeyguardRestrictedInputMode();
    }

}
