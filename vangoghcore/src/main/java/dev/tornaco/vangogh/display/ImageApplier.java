package dev.tornaco.vangogh.display;

import android.support.annotation.NonNull;

import dev.tornaco.vangogh.media.Image;

/**
 * Created by guohao4 on 2017/8/25.
 * Email: Tornaco@163.com
 */

public interface ImageApplier {
    void apply(@NonNull ImageDisplayer displayer, @NonNull Image image);

    /**
     * @return The total time for your animation, if you apply with any.
     */
    long duration();
}
