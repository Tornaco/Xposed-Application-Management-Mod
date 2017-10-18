package dev.tornaco.vangogh.loader;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import dev.tornaco.vangogh.media.DrawableImage;
import dev.tornaco.vangogh.media.Image;
import dev.tornaco.vangogh.media.ImageSource;

/**
 * Created by guohao4 on 2017/8/28.
 * Email: Tornaco@163.com
 */

public class FallbackLoader extends BaseImageLoader {

    @Override
    public int priority() {
        return Integer.MAX_VALUE;
    }

    @Override
    boolean canHandleType(@Nullable ImageSource.SourceType type) {
        return true;
    }

    @Nullable
    @Override
    Image doLoad(@NonNull ImageSource source, @Nullable LoaderObserver observer) {
        if (source.getFallback() > 0) {
            Drawable fallbackDrawable = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                fallbackDrawable = source.getContext().getResources()
                        .getDrawable(source.getFallback(), source.getContext().getTheme());
            } else {
                fallbackDrawable = source.getContext().getResources()
                        .getDrawable(source.getFallback());
            }
            if (fallbackDrawable != null) {
                Image image = new DrawableImage(fallbackDrawable);
                if (observer != null) {
                    observer.onImageReady(image);
                }
                return image;
            }
        }

        return null;
    }
}
