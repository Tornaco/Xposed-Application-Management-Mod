package dev.tornaco.vangogh.loader;

import android.support.annotation.NonNull;

import dev.tornaco.vangogh.common.Error;
import dev.tornaco.vangogh.media.Image;
import dev.tornaco.vangogh.media.ImageSource;

/**
 * Created by guohao4 on 2017/8/24.
 * Email: Tornaco@163.com
 */

public interface LoaderObserver {

    void onImageLoading(@NonNull ImageSource source);

    void onProgressUpdate(float progress);

    void onImageReady(@NonNull Image image);

    void onImageFailure(@NonNull Error error);
}
