package dev.tornaco.vangogh.loader.cache;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.newstand.logger.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import dev.tornaco.vangogh.loader.BitmapUtil;
import dev.tornaco.vangogh.media.BitmapImage;
import dev.tornaco.vangogh.media.Image;
import dev.tornaco.vangogh.media.ImageSource;

/**
 * Created by guohao4 on 2017/8/28.
 * Email: Tornaco@163.com
 */

class DiskCache implements Cache<ImageSource, Image> {

    private File cacheDir;

    private final Object lock = new Object();

    DiskCache(File cacheDir) {
        this.cacheDir = cacheDir;
    }

    private String createFileNameFromSource(ImageSource source) {
        return String.valueOf(source.hashCode());
    }

    @Nullable
    @Override
    public Image get(@NonNull ImageSource source) {
        String fileName = createFileNameFromSource(source);
        String filePath = cacheDir.getPath() + File.separator + fileName;
        File cacheFile = new File(filePath);
        if (!cacheFile.exists()) return null;
        try {
            return new BitmapImage(BitmapUtil.decodeFile(source.getContext(), filePath),
                    "dick-cache");
        } catch (IOException e) {
            Logger.e(e, "Error when decode file");
        }
        return null;
    }

    @Override
    public boolean put(@NonNull ImageSource source, @NonNull Image image) {
        return putLocked(source, image);
    }

    private boolean putLocked(@NonNull ImageSource source, @NonNull Image image) {
        if (image.isRecycled() || image.asBitmap(source.getContext()) == null) return false;

        String fileName = createFileNameFromSource(source);
        String filePath = cacheDir.getPath() + File.separator + fileName;
        File cacheFile = new File(filePath);

        if (cacheFile.exists()) return false;

        if (!cacheFile.getParentFile().exists() && !cacheFile.getParentFile().mkdirs()) {
            Logger.e(new IOException("Fail create parent dir"), filePath);
            return false;
        }
        synchronized (this.lock) {
            try {
                AtomicFileCompat atomicFileCompat = new AtomicFileCompat(cacheFile);
                FileOutputStream fos = atomicFileCompat.startWrite();
                if (image.isRecycled()) return false;
                Bitmap bitmap = image.asBitmap(source.getContext());
                if (bitmap == null) return false;
                if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)) {
                    atomicFileCompat.failWrite(fos);
                } else {
                    atomicFileCompat.finishWrite(fos);
                }
            } catch (FileNotFoundException e) {
                Logger.e(e, "Fail create file output stream");
                return false;
            } catch (IOException e) {
                Logger.e(e, "Fail create file output stream");
                return false;
            }
        }
        return true;
    }

    @Override
    public void clear() {
        throw new IllegalArgumentException("[Void] clear() of disk-cache is not implemented");
    }
}
