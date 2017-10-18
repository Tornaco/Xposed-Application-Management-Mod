package dev.tornaco.vangogh.loader;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import junit.framework.Assert;

import org.newstand.logger.Logger;

import java.io.File;
import java.util.concurrent.ExecutorService;

import dev.tornaco.vangogh.media.Image;
import dev.tornaco.vangogh.media.ImageSource;

/**
 * Created by guohao4 on 2017/8/25.
 * Email: Tornaco@163.com
 */

abstract class BaseImageLoader implements Loader<Image> {

    @Override
    @Nullable
    public Image load(@NonNull ImageSource source, @Nullable LoaderObserver observer) {
        if (canHandleType(getSourceType(source))) {
            Logger.v("BaseImageLoader, %s, canHandleType: %s", getClass().getSimpleName(), source);
            return doLoad(source, observer);
        }
        return null;
    }

    @Override
    public ExecutorService getExecutor() {
        return null;
    }

    abstract boolean canHandleType(@Nullable ImageSource.SourceType type);

    @Nullable
    abstract Image doLoad(@NonNull ImageSource source, @Nullable LoaderObserver observer);

    @Nullable
    private ImageSource.SourceType getSourceType(ImageSource source) {
        String url = source.getUrl();
        Assert.assertNotNull("Url is null", url);
        ImageSource.SourceType[] types = ImageSource.SourceType.values();
        for (ImageSource.SourceType t : types) {
            if (url.startsWith(t.prefix)) {
                return t;
            }
        }
        // Check if it is a file.
        File file = new File(source.getUrl());
        if (file.exists()) return ImageSource.SourceType.File;
        return null;
    }

    String getSplitPath(ImageSource source, ImageSource.SourceType sourceType) {
        if (source.getUrl().startsWith(sourceType.prefix))
            return source.getUrl().substring(sourceType.prefix.length(), source.getUrl().length());
        return source.getUrl();
    }
}
