package dev.tornaco.vangogh.media;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by guohao4 on 2017/8/24.
 * Email: Tornaco@163.com
 */

public interface Image {

    @Nullable
    Bitmap asBitmap(@NonNull Context context);

    @Nullable
    Drawable asDrawable(@NonNull Context context);

    void recycle();

    boolean isRecycled();

    boolean cachable();

    long size();
}
