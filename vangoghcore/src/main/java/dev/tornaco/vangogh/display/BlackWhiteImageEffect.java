package dev.tornaco.vangogh.display;

import android.content.Context;
import android.support.annotation.NonNull;

import dev.tornaco.vangogh.media.BitmapImage;
import dev.tornaco.vangogh.media.Image;

/**
 * Created by guohao4 on 2017/8/27.
 * Email: Tornaco@163.com
 */

public class BlackWhiteImageEffect implements ImageEffect {
    @NonNull
    @Override
    public Image process(Context context, @NonNull Image image) {
        if (image.asBitmap(context) == null) return image;
        return new BitmapImage(BitmapUtil.blackAndWhited(image.asBitmap(context)), "bw");
    }
}
