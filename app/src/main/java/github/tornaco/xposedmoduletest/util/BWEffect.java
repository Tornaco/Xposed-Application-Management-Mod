package github.tornaco.xposedmoduletest.util;

import android.content.Context;
import android.support.annotation.NonNull;

import dev.tornaco.vangogh.display.ImageEffect;
import dev.tornaco.vangogh.media.BitmapImage;
import dev.tornaco.vangogh.media.Image;

/**
 * Created by guohao4 on 2017/10/24.
 * Email: Tornaco@163.com
 */

public class BWEffect implements ImageEffect {
    @NonNull
    @Override
    public Image process(Context context, @NonNull Image image) {
        return new BitmapImage(BitmapUtil.convertToBlackWhite(image.asBitmap(context)), "BW");
    }
}
