package dev.tornaco.vangogh.loader;

import android.support.annotation.NonNull;

import dev.tornaco.vangogh.common.Error;
import dev.tornaco.vangogh.media.Image;
import dev.tornaco.vangogh.media.ImageSource;

/**
 * Created by guohao4 on 2017/8/25.
 * Email: Tornaco@163.com
 */

public class LoaderObserverAdapter implements LoaderObserver {
    @Override
    public void onImageLoading(@NonNull ImageSource source) {

    }

    @Override
    public void onProgressUpdate(float progress) {

    }

    @Override
    public void onImageReady(@NonNull Image image) {

    }

    @Override
    public void onImageFailure(@NonNull Error error) {

    }
}
