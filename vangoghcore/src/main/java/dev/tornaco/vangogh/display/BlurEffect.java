package dev.tornaco.vangogh.display;

import android.content.Context;
import android.support.annotation.NonNull;

import dev.tornaco.vangogh.media.BitmapImage;
import dev.tornaco.vangogh.media.Image;

/**
 * Created by guohao4 on 2017/8/25.
 * Email: Tornaco@163.com
 */

public class BlurEffect implements ImageEffect {

    private int radius = 25;

    public BlurEffect() {
    }

    public BlurEffect(int radius) {
        this.radius = radius;
    }

    @NonNull
    @Override
    public Image process(Context context, @NonNull Image image) {
        return new BitmapImage(BitmapUtil.createBlurredBitmap(image.asBitmap(context), this.radius), "blur");
    }
}
