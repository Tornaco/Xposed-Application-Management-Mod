package dev.tornaco.vangogh.request;

import android.content.Context;

import dev.tornaco.vangogh.display.ImageApplier;
import dev.tornaco.vangogh.display.ImageDisplayer;
import dev.tornaco.vangogh.display.ImageEffect;
import dev.tornaco.vangogh.loader.Loader;
import dev.tornaco.vangogh.loader.LoaderObserver;
import dev.tornaco.vangogh.media.Image;
import dev.tornaco.vangogh.media.ImageSource;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Created by guohao4 on 2017/8/24.
 * Email: Tornaco@163.com
 */
@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class ImageRequest {
    private Context context;

    private int id;
    private String alias;
    private long requestTimeMills;
    private ImageSource imageSource;
    private ImageDisplayer displayer;
    private ImageApplier applier;

    private LoaderObserver observer;

    private boolean dirty;

    private Loader<Image> loader;

    void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ImageRequest that = (ImageRequest) o;

        return displayer.equals(that.displayer);

    }

    @Override
    public int hashCode() {
        return displayer.hashCode();
    }
}
