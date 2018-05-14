package github.tornaco.xposedmoduletest.xposed.service.am;

import android.content.Context;
import android.os.RemoteCallbackList;
import android.util.Log;

import github.tornaco.xposedmoduletest.ITaskRemoveListener;
import github.tornaco.xposedmoduletest.util.Singleton1;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;
import lombok.Getter;

/**
 * Created by Tornaco on 2018/5/10 10:02.
 * God bless no bug!
 */
public class PackageStateManager {

    private static final Singleton1<PackageStateManager, Context>
            sMe = new Singleton1<PackageStateManager, Context>() {
        @Override
        protected PackageStateManager create(Context context) {
            return new PackageStateManager(context);
        }
    };

    @Getter
    private Context context;

    private final RemoteCallbackList<ITaskRemoveListener> mTaskRemoveListeners = new RemoteCallbackList<>();

    private PackageStateManager(Context context) {
        this.context = context;
    }

    public static PackageStateManager from(Context context) {
        return sMe.get(context);
    }

    public void registerTaskRemoveListener(ITaskRemoveListener listener) {
        if (listener != null) try {
            mTaskRemoveListeners.register(listener);
            if (XposedLog.isVerboseLoggable()) {
                XposedLog.verbose(XposedLog.TAG_LAZY + "PackageStateManager registerTaskRemoveListener for: " + listener);
            }
        } catch (Exception ignored) {
            // Bad service?
            XposedLog.wtf(XposedLog.TAG_LAZY + "PackageStateManager Fail registerTaskRemoveListener: " + Log.getStackTraceString(ignored));
        }
    }

    public void unRegisterTaskRemoveListener(ITaskRemoveListener listener) {
        try {
            mTaskRemoveListeners.unregister(listener);
            if (XposedLog.isVerboseLoggable()) {
                XposedLog.verbose(XposedLog.TAG_LAZY + "PackageStateManager unRegisterTaskRemoveListener: " + listener);
            }
        } catch (Exception ignored) {
            // Bad service?
            XposedLog.wtf(XposedLog.TAG_LAZY + "PackageStateManager Fail unRegisterController: " + Log.getStackTraceString(ignored));
        }
    }

    public void onTaskRemoved(String taskOwnerPackageName) {
        XposedLog.verbose(XposedLog.TAG_LAZY + "PackageStateManager onTaskRemoved: " + taskOwnerPackageName);
        try {
            int itemCount = mTaskRemoveListeners.beginBroadcast();
            for (int i = 0; i < itemCount; i++) {
                try {
                    ITaskRemoveListener listener = mTaskRemoveListeners.getBroadcastItem(i);
                    try {
                        listener.onTaskRemoved(taskOwnerPackageName);
                    } catch (Throwable e) {
                        // Kik.
                        XposedLog.wtf(XposedLog.TAG_LAZY + "PackageStateManager Fail call onTaskRemoved, kik out! "
                                + Log.getStackTraceString(e));
                        unRegisterTaskRemoveListener(listener);
                    }
                } catch (Throwable ignored) {
                    // We tried...
                }
            }
            XposedLog.verbose(XposedLog.TAG_LAZY + "PackageStateManager onTaskRemoved finish");
        } catch (Throwable e) {
            XposedLog.wtf(XposedLog.TAG_LAZY + "PackageStateManager onTaskRemoved broadcast fail: " + Log.getStackTraceString(e));
        } finally {
            mTaskRemoveListeners.finishBroadcast();
            // If dead, go dead!!!!!
        }
    }
}
