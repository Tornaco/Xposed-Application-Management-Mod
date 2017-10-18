package dev.tornaco.vangogh.loader;

import android.content.Context;
import android.content.res.AssetManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.newstand.logger.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import dev.tornaco.vangogh.media.Image;
import dev.tornaco.vangogh.media.ImageSource;

/**
 * Created by guohao4 on 2017/8/28.
 * Email: Tornaco@163.com
 */

class AssetsImageLoader extends BaseImageLoader {

    private FileLoader fileLoader;

    AssetsImageLoader() {
        fileLoader = new FileLoader();
    }

    @Override
    public int priority() {
        return 3;
    }

    @Override
    boolean canHandleType(@Nullable ImageSource.SourceType type) {
        return type == ImageSource.SourceType.Assets;
    }

    @Nullable
    @Override
    Image doLoad(@NonNull ImageSource source, @Nullable LoaderObserver observer) {
        String path = getSplitPath(source, ImageSource.SourceType.Assets);
        String tmpImagePath = source.getContext().getCacheDir().getPath() + File.separator
                + UUID.randomUUID().toString();
        if (!copyFromAssetsTo(source.getContext(), path, tmpImagePath))
            return null;

        try {
            ImageSource newSource = source.duplicate();
            newSource.setUrl(tmpImagePath);
            return fileLoader.doLoad(newSource, observer);
        } catch (CloneNotSupportedException ignored) {
            return null;
        } finally {
            //noinspection ResultOfMethodCallIgnored
            new File(tmpImagePath).delete();
        }
    }

    private boolean copyFromAssetsTo(Context context, String from, String to) {

        Logger.v("copyFromAssetsTo: %s, %s", from, to);

        if (!new File(to).getParentFile().exists() && !new File(to).getParentFile().mkdirs()) {
            return false;
        }

        try {
            FileOutputStream fos = new FileOutputStream(to);
            return copy(openAssetsInput(context, from), fos) > 0;
        } catch (IOException e) {
            Logger.e(e, "Fail copy from assets:" + from);
        }
        return false;
    }

    private static byte[] createBuffer() {
        return new byte[8192];
    }

    public static long copy(InputStream from, OutputStream to) throws IOException {
        byte[] buf = createBuffer();
        long total = 0;
        while (true) {
            int r = from.read(buf);
            if (r == -1) {
                break;
            }
            to.write(buf, 0, r);
            total += r;
        }
        return total;
    }

    private InputStream openAssetsInput(Context context, String fileName) throws IOException {
        return openAsset(context).open(fileName);
    }

    private AssetManager openAsset(Context context) {
        return context.getAssets();
    }
}
