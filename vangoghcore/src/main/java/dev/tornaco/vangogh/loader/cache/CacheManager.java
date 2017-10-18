package dev.tornaco.vangogh.loader.cache;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.newstand.logger.Logger;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dev.tornaco.vangogh.VangoghConfigManager;
import dev.tornaco.vangogh.media.Image;
import dev.tornaco.vangogh.media.ImageSource;
import dev.tornaco.vangogh.request.ImageManager;

/**
 * Created by guohao4 on 2017/8/28.
 * Email: Tornaco@163.com
 */

public class CacheManager {

    private Cache<ImageSource, Image> diskCache, memCache;

    private Observer imageReadyObserver = new Observer() {
        @Override
        public void update(Observable o, Object arg) {
            onImageReady(((ImageManager.ImageArgs) arg).getSource(),
                    ((ImageManager.ImageArgs) arg).getImage());
        }
    };

    private Observer confObserver = new Observer() {
        @Override
        public void update(Observable o, Object arg) {
            // Recreate caches.
            diskCache = new DiskCache(VangoghConfigManager.getInstance().getConfig().getDiskCacheDir());
            memCache.clear();
            memCache = new MemoryCache(VangoghConfigManager.getInstance().getConfig().getMemCachePoolSize());
        }
    };

    private static CacheManager cacheManager;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public synchronized static CacheManager getInstance() {
        if (cacheManager == null) cacheManager = new CacheManager();
        return cacheManager;
    }

    private CacheManager() {
        this.diskCache = new DiskCache(VangoghConfigManager.getInstance().getConfig().getDiskCacheDir());
        this.memCache = new MemoryCache(VangoghConfigManager.getInstance().getConfig().getMemCachePoolSize());

        ImageManager.getInstance().addObserver(imageReadyObserver);
        VangoghConfigManager.getInstance().addObserver(confObserver);
    }

    private void onImageReady(final ImageSource source, final Image image) {
        if (!image.cachable()) return;
        if (image.asBitmap(source.getContext()) == null) return;
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                diskCache.put(source, image);
                memCache.put(source, image);
            }
        });
    }

    public
    @Nullable
    Image get(@NonNull ImageSource source) {
        Image image = source.isSkipMemoryCache() ? null : memCache.get(source);
        Logger.v("CacheManager, get from mem: %s", image);
        if (image == null) {
            image = source.isSkipDiskCache() ? null : diskCache.get(source);
        }
        return image;
    }

    public void quit() {
        executorService.shutdownNow();
        ImageManager.getInstance().deleteObserver(imageReadyObserver);
        VangoghConfigManager.getInstance().deleteObserver(confObserver);
    }
}
