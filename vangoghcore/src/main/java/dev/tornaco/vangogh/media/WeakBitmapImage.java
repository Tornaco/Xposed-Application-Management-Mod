package dev.tornaco.vangogh.media;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;

import lombok.ToString;

/**
 * Created by guohao4 on 2017/8/25.
 * Email: Tornaco@163.com
 */
@ToString
public class WeakBitmapImage implements Image {

    private WeakReference<Bitmap> reference;

    private boolean recycled;

    public WeakBitmapImage(Bitmap bitmap) {
        this.reference = new WeakReference<>(bitmap);
    }

    @Nullable
    @Override
    public Bitmap asBitmap(@NonNull Context context) {
        return reference.get();
    }

    @Nullable
    @Override
    public Drawable asDrawable(@NonNull Context context) {
        return null;
    }

    @Override
    public void recycle() {
        if (reference != null && reference.get() != null && !reference.get().isRecycled()) {
            reference.get().recycle();
            reference = null;
            recycled = true;
        }
    }

    @Override
    public boolean isRecycled() {
        return recycled;
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
