package github.tornaco.xposedmoduletest.xposed.util;

import android.util.LruCache;

import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.util.Singleton;
import github.tornaco.xposedmoduletest.xposed.bean.BlurTask;

/**
 * Created by Tornaco on 2018/5/9 16:30.
 * God bless no bug!
 */
public class BlurTaskCache {

    private static final boolean CACHE_ENABLED = BuildConfig.DEBUG;

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
    }

    public void put(String key, BlurTask task) {
        if (CACHE_ENABLED && key != null && task != null) {
            mCache.put(key, task);
        }
    }

    public BlurTask get(String key) {
        if (!CACHE_ENABLED) {
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
}
