package github.tornaco.xposedmoduletest.loader;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.newstand.logger.Logger;

import java.util.concurrent.ExecutorService;

import dev.tornaco.vangogh.loader.Loader;
import dev.tornaco.vangogh.loader.LoaderObserver;
import dev.tornaco.vangogh.media.BitmapImage;
import dev.tornaco.vangogh.media.Image;
import dev.tornaco.vangogh.media.ImageSource;
import github.tornaco.android.common.util.ApkUtil;

/**
 * Created by guohao4 on 2017/10/23.
 * Email: Tornaco@163.com
 */

public class VangoghAppLoader implements Loader<Image> {

    private Context context;

    public VangoghAppLoader(Context context) {
        this.context = context;
    }

    @Nullable
    @Override
    public Image load(@NonNull ImageSource source, @Nullable LoaderObserver observer) {
        String pkgName = source.getUrl();
        Drawable d = ApkUtil.loadIconByPkgName(context, pkgName);
        BitmapDrawable bd = (BitmapDrawable) d;
        Logger.v("XXX- Loading COMPLETE for: " + pkgName);
        BitmapImage bitmapImage = new BitmapImage(bd.getBitmap());
        if (observer != null) {
            observer.onImageReady(bitmapImage);
        }
        return bitmapImage;
    }

    @Override
    public int priority() {
        return 3;
    }

    @Override
    public ExecutorService getExecutor() {
        return null;
    }
}
