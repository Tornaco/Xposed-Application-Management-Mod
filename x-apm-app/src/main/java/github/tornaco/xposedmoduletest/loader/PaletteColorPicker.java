package github.tornaco.xposedmoduletest.loader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.graphics.Palette;

import github.tornaco.android.common.util.ApkUtil;
import github.tornaco.android.common.util.ColorUtil;

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
        Drawable d = ApkUtil.loadIconByPkgName(context, pkg);
        if (d != null && d instanceof BitmapDrawable) {
            BitmapDrawable bd = (BitmapDrawable) d;
            Bitmap bm = bd.getBitmap();
            Palette.from(bm)
                    .generate(new Palette.PaletteAsyncListener() {
                        @Override
                        public void onGenerated(Palette palette) {
                            int main = palette.getDominantColor(defColor);
                            receiver.onColorReady(ColorUtil.colorBurn(main));
                        }
                    });
        } else {
            receiver.onColorReady(defColor);
        }
    }
}
