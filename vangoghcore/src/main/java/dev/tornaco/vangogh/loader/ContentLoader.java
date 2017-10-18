package dev.tornaco.vangogh.loader;

import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.newstand.logger.Logger;

import java.io.IOException;

import dev.tornaco.vangogh.VangoghConfig;
import dev.tornaco.vangogh.common.Error;
import dev.tornaco.vangogh.media.BitmapImage;
import dev.tornaco.vangogh.media.Image;
import dev.tornaco.vangogh.media.ImageSource;

/**
 * Created by guohao4 on 2017/8/27.
 * Email: Tornaco@163.com
 */

class ContentLoader extends BaseImageLoader {
    @Override
    public int priority() {
        return 3;
    }

    @Override
    boolean canHandleType(@Nullable ImageSource.SourceType type) {
        return type == ImageSource.SourceType.Content;
    }

    @Nullable
    @Override
    Image doLoad(@NonNull ImageSource source, @Nullable LoaderObserver observer) {
        Logger.v("ContentLoader, doLoad: %s", source);
        if (observer != null) observer.onImageLoading(source);

        Bitmap bitmap = null;
        try {
            bitmap = BitmapUtil.decodeUri(source.getContext(), Uri.parse(source.getUrl()));
        } catch (IOException e) {
            Error error = Error.io(e);
            if (observer != null) {
                observer.onImageFailure(error);
            }
            return null;
        }

        Logger.i("decodeFile bitmap: %s", bitmap);

        Image image = new BitmapImage(bitmap, "content");
        if (observer != null) {
            observer.onImageReady(image);
        }
        return image;
    }
}
