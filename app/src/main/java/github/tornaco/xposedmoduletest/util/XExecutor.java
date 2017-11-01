package github.tornaco.xposedmoduletest.util;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Nick@NewStand.org on 2017/3/7 12:15
 * E-Mail: NewStand@163.com
 * All right reserved.
 */

public abstract class XExecutor {

    private static ExecutorService executorService = Executors.newCachedThreadPool();

    private static Handler mUIThreadHandler = new Handler(Looper.getMainLooper());

    public static void execute(Runnable runnable) {
        executorService.execute(runnable);
    }

    public static ExecutorService getService() {
        return executorService;
    }

    public static void runOnUIThread(Runnable runnable) {
        mUIThreadHandler.post(runnable);
    }

    public static void runOnUIThreadDelayed(Runnable runnable, long delayMills) {
        mUIThreadHandler.postDelayed(runnable, delayMills);
    }

    public static Handler getUIThreadHandler() {
        return mUIThreadHandler;
    }
}
