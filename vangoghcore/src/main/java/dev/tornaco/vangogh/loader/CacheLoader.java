package dev.tornaco.vangogh.loader;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.newstand.logger.Logger;

import dev.tornaco.vangogh.loader.cache.CacheManager;
import dev.tornaco.vangogh.media.Image;
import dev.tornaco.vangogh.media.ImageSource;

/**
 * Created by guohao4 on 2017/8/25.
 * Email: Tornaco@163.com
 */

class CacheLoader extends BaseImageLoader {

    private CacheManager cacheManager;

    public CacheLoader() {
        cacheManager = CacheManager.getInstance();
    }

    @Override
    public int priority() {
        return 0;
    }

    @Override
    boolean canHandleType(@Nullable ImageSource.SourceType type) {
        return type != null;
    }

    @Nullable
    @Override
    Image doLoad(@NonNull ImageSource source, @Nullable LoaderObserver observer) {
        Image image = cacheManager.get(source);
        Logger.v("CacheLoader, got from cache: %s", image);
        if (observer != null && image != null) {
            observer.onImageReady(image);
        }
        return image;
    }
}
