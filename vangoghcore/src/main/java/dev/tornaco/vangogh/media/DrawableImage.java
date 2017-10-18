package dev.tornaco.vangogh.media;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import lombok.AllArgsConstructor;

/**
 * Created by guohao4 on 2017/8/27.
 * Email: Tornaco@163.com
 */
@AllArgsConstructor
public class DrawableImage implements Image {

    private Drawable drawable;

    @Nullable
    @Override
    public Bitmap asBitmap(@NonNull Context context) {
        return null;
    }

    @Nullable
    @Override
    public Drawable asDrawable(@NonNull Context context) {
        return this.drawable;
    }

    @Override
    public void recycle() {
        drawable = null;
    }

    @Override
    public boolean isRecycled() {
        return drawable == null;
    }

    @Override
    public boolean cachable() {
        return false;
    }

    @Override
    public long size() {
        return 1024;
    }
}
