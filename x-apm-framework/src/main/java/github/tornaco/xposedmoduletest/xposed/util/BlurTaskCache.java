package github.tornaco.xposedmoduletest.xposed.util;

import android.util.Log;
import android.util.LruCache;

import github.tornaco.xposedmoduletest.ITaskRemoveListener;
import github.tornaco.xposedmoduletest.util.Singleton;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.bean.BlurTask;

/**
 * Created by Tornaco on 2018/5/9 16:30.
 * God bless no bug!
 */
public class BlurTaskCache {

    private static final int MAX_ENTRY_SIZE = 12;

    private static final long EXPIRE_TIME_MILLS = 5 * 60 * 1000;

    private LruCache<String, BlurTask> mCache;

    private static final Singleton<BlurTaskCache> sMe
            = new Singleton<BlurTaskCache>() {
        @Override
        protected BlurTaskCache create() {
            return new BlurTaskCache();
        }
    };

    public static BlurTaskCache getInstance() {
        return sMe.get();
    }

    private BlurTaskCache() {
        mCache = new LruCache<>(MAX_ENTRY_SIZE);
        // Tracking the task removal.
        XAPMManager.get().registerTaskRemoveListener(new ITaskRemoveListener.Stub() {
            @Override
            public void onTaskRemoved(String packageName) {
                try {
                    mCache.remove(packageName);
                    Log.d(XposedLog.TAG, "BLUR, removing blur cache for: " + packageName);
                } catch (Throwable ignored) {
                }
            }
        });
    }

    public void put(String key, BlurTask task) {
        if (isBlurCacheEnabled() && key != null && task != null) {
            mCache.put(key, task);
        }
    }

    public BlurTask get(String key) {
        if (!isBlurCacheEnabled()) {
            return null;
        }
        BlurTask task = key == null ? null : mCache.get(key);
        if (task == null) return null;
        boolean isDirty = isDirtyTask(task);
        if (isDirty) {
            // Remove.
            mCache.remove(key);
            task.bitmap = null;
            task = null;
            return null;
        }
        return task;
    }

    private static boolean isDirtyTask(BlurTask task) {
        return task.bitmap == null || task.bitmap.isRecycled() || System.currentTimeMillis() - task.updateTimeMills > EXPIRE_TIME_MILLS;
    }

    private static boolean isBlurCacheEnabled() {
        return XAPMManager.get().isOptFeatureEnabled(XAPMManager.OPT.OPT_BLUR_CACHE.name());
    }
}
