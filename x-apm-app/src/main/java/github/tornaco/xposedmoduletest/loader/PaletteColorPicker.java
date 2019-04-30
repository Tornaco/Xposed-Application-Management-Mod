package github.tornaco.xposedmoduletest.loader;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v4.util.LruCache;
import android.support.v7.graphics.Palette;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import github.tornaco.android.common.util.ColorUtil;
import github.tornaco.xposedmoduletest.model.CommonPackageInfo;

/**
 * Created by guohao4 on 2017/10/28.
 * Email: Tornaco@163.com
 */

public abstract class PaletteColorPicker {
    public interface PickReceiver {
        void onColorReady(int color);
    }

    public static void pickPrimaryColor(Context context, final PickReceiver receiver,
                                        String pkg, final int defColor) {
        if (pkg == null) {
            receiver.onColorReady(defColor);
            return;
        }

        CommonPackageInfo c = new CommonPackageInfo();
        c.setPkgName(pkg);
        GlideApp.with(context)
                .asBitmap()
                .load(c)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource,
                                                Transition<? super Bitmap> transition) {
                        Palette.from(resource)
                                .generate(palette -> {
                                    int main = palette.getDominantColor(defColor);
                                    // Burn 3 time to make it darker!
                                    receiver.onColorReady(
                                            ColorUtil.colorBurn(
                                                    ColorUtil.colorBurn(
                                                            ColorUtil.colorBurn(main))));
                                });
                    }
                });
    }
}
