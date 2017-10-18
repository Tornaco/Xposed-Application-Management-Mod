package dev.tornaco.vangogh.display;

import android.support.v4.util.LruCache;

import dev.tornaco.vangogh.media.Image;

/**
 * Created by guohao4 on 2017/8/29.
 * Email: Tornaco@163.com
 */

class DisplayManager {

    private LruCache<Integer, Image> mLruCache;

    private static DisplayManager sMe = new DisplayManager();

    static DisplayManager getManager() {
        return sMe;
    }

    private DisplayManager() {
        mLruCache = new LruCache<Integer, Image>(128) {
            @Override
            protected void entryRemoved(boolean evicted, Integer key, Image oldValue, Image newValue) {
                super.entryRemoved(evicted, key, oldValue, newValue);
                System.gc();
            }
        };
    }

    Image remove(Integer key) {
        return mLruCache.remove(key);
    }

    void put(Integer key, Image value) {
        mLruCache.put(key, value);
    }

    public Image get(Integer view) {
        return mLruCache.get(view);
    }

    public void clear() {
        mLruCache.evictAll();
    }
}
