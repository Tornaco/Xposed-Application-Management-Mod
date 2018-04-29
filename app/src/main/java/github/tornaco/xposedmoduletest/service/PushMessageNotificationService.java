package github.tornaco.xposedmoduletest.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.newstand.logger.Logger;

import java.util.HashMap;
import java.util.Map;

import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.ITopPackageChangeListener;
import github.tornaco.xposedmoduletest.model.PushMessage;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;
import github.tornaco.xposedmoduletest.xposed.service.ErrorCatchRunnable;
import github.tornaco.xposedmoduletest.xposed.service.opt.gcm.BasePushNotificationHandler;
import github.tornaco.xposedmoduletest.xposed.service.opt.gcm.NotificationHandlerSettingsRetriever;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by Tornaco on 2018/4/19 9:01.
 * God bless no bug!
 */
public class PushMessageNotificationService extends Service implements NotificationHandlerSettingsRetriever {

    private static final String SERVICE_ACTION = "github.tornaco.xposedmoduletest.action.StartPushMessageNotificationService";
    private static final String INTENT_EXTRA_KEY_PUSH_MESSAGE = "github.tornaco.xposedmoduletest.key.push_message";

    public static Intent getIntent(PushMessage pushMessage) {
        Intent intent = new Intent(SERVICE_ACTION);
        intent.setPackage(BuildConfig.APPLICATION_ID);
        intent.putExtra(INTENT_EXTRA_KEY_PUSH_MESSAGE, pushMessage);
        return intent;
    }

    public static PushMessage resolvePushMessageFromIntent(Intent intent) {
        if (intent != null && intent.hasExtra(INTENT_EXTRA_KEY_PUSH_MESSAGE)) {
            return intent.getParcelableExtra(INTENT_EXTRA_KEY_PUSH_MESSAGE);
        }
        return null;
    }

    public static boolean start(Context context, PushMessage pushMessage) {
        try {
            context.startService(getIntent(pushMessage));
            return true;
        } catch (Throwable e) {
            Log.e(XposedLog.TAG, "Fail start PushMessageNotificationService: " + Log.getStackTraceString(e));
            return false;
        }
    }

    // Key: pkg.
    private final Map<String, BasePushNotificationHandler> mNotificationHandlers = new HashMap<>();

    private Handler mUIThreadHandler;

    private final ITopPackageChangeListener mTopPackageListener = new ITopPackageChangeListener.Stub() {
        @Override
        public void onChange(String from, String to) {
            notifyTopPackageChanged(from, to);
        }

        @Override
        public String hostPackageName() {
            return null;
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mUIThreadHandler = new Handler();

        XAshmanManager.get().registerOnTopPackageChangeListener(mTopPackageListener);
    }

    private void notifyTopPackageChanged(String from, String to) {
        if (BuildConfig.DEBUG) {
            Logger.d("PushMessageNotificationService, notifyTopPackageChanged, to: " + to);
        }
        mUIThreadHandler.post(new ErrorCatchRunnable(() -> {
            Object[] handlers = mNotificationHandlers.values().toArray();
            for (Object o : handlers) {
                BasePushNotificationHandler h = (BasePushNotificationHandler) o;
                h.onTopPackageChanged(to);
            }
        }, "notifyTopPackageChanged"));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        PushMessage pushMessage = resolvePushMessageFromIntent(intent);

        if (pushMessage == null) {
            return START_NOT_STICKY;
        }

        BasePushNotificationHandler handler = mNotificationHandlers.get(pushMessage.getTargetPackageName());

        if (handler == null) {
            handler =
                    new BasePushNotificationHandler(this, this) {
                        @Override
                        public boolean handleIncomingIntent(String targetPackage, Intent intent) {
                            // Noop.
                            return false;
                        }

                        @Override
                        public String getTargetPackageName() {
                            return pushMessage.getTargetPackageName();
                        }
                    };
            handler.setCustomRingtoneEnabled(false);
            mNotificationHandlers.put(pushMessage.getTargetPackageName(), handler);
        }

        if (BuildConfig.DEBUG) {
            Logger.d("PushMessageNotificationService, posting: " + pushMessage);
        }

        // Force update settings.
        handler.onSettingsChanged(pushMessage.getTargetPackageName());
        handler.postNotification(pushMessage);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        XAshmanManager.get().unRegisterOnTopPackageChangeListener(mTopPackageListener);
    }

    @Override
    public boolean isPushMessageHandlerEnabled(String pkg) {
        return XAshmanManager.get().isPushMessageHandlerEnabled(pkg);
    }

    @Override
    public boolean isPushMessageHandlerShowContentEnabled(String pkg) {
        return XAshmanManager.get().isPushMessageHandlerShowContentEnabled(pkg);
    }

    @Override
    public boolean isPushMessageHandlerNotificationSoundEnabled(String pkg) {
        return XAshmanManager.get().isPushMessageHandlerNotificationSoundEnabled(pkg);
    }

    @Override
    public boolean isPushMessageHandlerNotificationVibrateEnabled(String pkg) {
        return XAshmanManager.get().isPushMessageHandlerNotificationVibrateEnabled(pkg);
    }

    @Override
    public boolean isPushMessageHandlerMessageNotificationByAppEnabled(String pkg) {
        return XAshmanManager.get().isPushMessageHandlerMessageNotificationByAppEnabled(pkg);
    }
}
