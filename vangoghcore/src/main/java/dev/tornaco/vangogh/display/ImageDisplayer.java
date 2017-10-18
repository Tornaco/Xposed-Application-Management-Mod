package dev.tornaco.vangogh.display;

import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.view.View;

import dev.tornaco.vangogh.media.Image;

/**
 * Created by guohao4 on 2017/8/25.
 * Email: Tornaco@163.com
 */

public interface ImageDisplayer {
    @UiThread
    void display(@Nullable Image image);

    @Nullable
    View getView();

    int getWidth();

    int getHeight();

    String getLabel();
}
