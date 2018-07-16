package github.tornaco.xposedmoduletest.loader;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.xposed.util.BlurUtil;

/**
 * Created by guohao4 on 2017/10/28.
 * Email: Tornaco@163.com
 */

public abstract class AppIconBlurImagePicker {

    public interface PickReceiver {
        void onColorReady(Bitmap bitmap);
    }

    public static void pickBlurImage(Context context, final PickReceiver receiver,
                                     String pkg, final Bitmap def) {
        if (pkg == null) {
            receiver.onColorReady(def);
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
                        Bitmap bluredBitmap = BlurUtil.rs(context, resource, BlurUtil.BLUR_RADIUS_MAX);
                        receiver.onColorReady(bluredBitmap);
                    }
                });
    }
}
