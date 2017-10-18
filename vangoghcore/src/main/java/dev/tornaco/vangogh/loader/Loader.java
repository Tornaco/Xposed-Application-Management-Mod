package dev.tornaco.vangogh.loader;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.concurrent.ExecutorService;

import dev.tornaco.vangogh.media.ImageSource;

/**
 * Created by guohao4 on 2017/8/24.
 * Email: Tornaco@163.com
 */

public interface Loader<T> {
    /**
     * @param source   {@link ImageSource} source of the {@link dev.tornaco.vangogh.media.Image} to load.
     * @param observer The instance of the {@link LoaderObserver}, may be null.
     * @return Loading result.
     */
    @Nullable
    T load(@NonNull ImageSource source, @Nullable LoaderObserver observer);

    /**
     * Range from -{@link Integer#MAX_VALUE} to {@link Integer#MAX_VALUE},
     * You git high priority with smaller value.
     *
     * @return Your loader's.
     */
    int priority();

    ExecutorService getExecutor();
}
