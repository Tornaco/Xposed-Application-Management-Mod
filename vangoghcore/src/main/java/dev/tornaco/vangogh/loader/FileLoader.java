package dev.tornaco.vangogh.loader;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.newstand.logger.Logger;

import java.io.File;
import java.io.IOException;

import dev.tornaco.vangogh.common.Error;
import dev.tornaco.vangogh.media.BitmapImage;
import dev.tornaco.vangogh.media.Image;
import dev.tornaco.vangogh.media.ImageSource;
import dev.tornaco.vangogh.media.MediaFile;

/**
 * Created by guohao4 on 2017/8/25.
 * Email: Tornaco@163.com
 */

class FileLoader extends BaseImageLoader {

    @Override
    boolean canHandleType(@Nullable ImageSource.SourceType type) {
        return type == ImageSource.SourceType.File;
    }

    @Nullable
    @Override
    Image doLoad(@NonNull ImageSource source, @Nullable LoaderObserver observer) {
        Logger.v("FileLoader, doLoad: %s", source);
        if (observer != null) observer.onImageLoading(source);

        String filePath = getSplitPath(source, ImageSource.SourceType.File);
        Logger.v("FileLoader, getSplitPath: %s", filePath);

        if (!new File(filePath).exists()) {
            Error error = Error.fileNotFound(filePath);
            if (observer != null) {
                observer.onImageFailure(error);
            }
            return null;
        }

        if (!new File(filePath).canRead()) {
            Error error = Error.fileNotReadable(filePath);
            if (observer != null) {
                observer.onImageFailure(error);
            }
            return null;
        }

        // Get mime type of the file.
        MediaFile.MediaFileType mediaFileType = MediaFile.getFileType(filePath);
        Logger.v("FileLoader, mediaFileType: %s", mediaFileType);

        if (mediaFileType == null || MediaFile.isImageFileType(mediaFileType.fileType)) {
            Bitmap bitmap;
            try {
                bitmap = BitmapUtil.decodeFile(source.getContext(), filePath);
            } catch (IOException e) {
                Error error = Error.io(e);
                if (observer != null) {
                    observer.onImageFailure(error);
                }
                return null;
            }

            Image image = new BitmapImage(bitmap, "file");
            if (observer != null) {
                observer.onImageReady(image);
            }
            return image;
        }

        if (MediaFile.isVideoFileType(mediaFileType.fileType)) {
            Bitmap videoThumb = ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
            Logger.v("FileLoader, videoThumb: %s", videoThumb);
            Image image = new BitmapImage(videoThumb);
            if (observer != null) {
                observer.onImageReady(image);
            }
            return image;
        }

        return null;
    }

    @Override
    public int priority() {
        return 1;
    }
}
