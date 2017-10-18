package dev.tornaco.vangogh.loader;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.newstand.logger.Logger;

import dev.tornaco.vangogh.common.Error;
import dev.tornaco.vangogh.common.ErrorListener;
import dev.tornaco.vangogh.common.ProgressListener;
import dev.tornaco.vangogh.loader.network.HttpImageDownloader;
import dev.tornaco.vangogh.loader.network.ImageDownloader;
import dev.tornaco.vangogh.media.Image;
import dev.tornaco.vangogh.media.ImageSource;

/**
 * Created by guohao4 on 2017/8/28.
 * Email: Tornaco@163.com
 */

public class NetworkImageLoader extends BaseImageLoader {

    private ImageDownloader<String> imageDownloader;
    private FileLoader fileLoader;

    public NetworkImageLoader() {
        this.fileLoader = new FileLoader();
    }

    @Override
    public int priority() {
        return 10;
    }

    @Override
    boolean canHandleType(@Nullable ImageSource.SourceType type) {
        return type == ImageSource.SourceType.Http || type == ImageSource.SourceType.Https;
    }

    @Nullable
    @Override
    Image doLoad(@NonNull ImageSource source, @Nullable final LoaderObserver observer) {

        String tmpImageFile = new HttpImageDownloader(source.getContext().getCacheDir(), null)
                .download(source.getUrl(),
                        new ProgressListener() {
                            @Override
                            public void onProgressUpdate(float progress) {
                                if (observer != null) {
                                    observer.onProgressUpdate(progress);
                                }
                            }
                        }, new ErrorListener() {
                            @Override
                            public void onError(@NonNull Error cause) {
                                if (observer != null) {
                                    observer.onImageFailure(cause);
                                }
                            }
                        });

        Logger.v("NetworkImageLoader, tmpImageFile: %s", tmpImageFile);

        if (tmpImageFile == null) return null;

        try {
            ImageSource newSource = source.duplicate();
            newSource.setUrl(tmpImageFile);
            Logger.v("NetworkImageLoader, new source: %s", newSource);
            return fileLoader.doLoad(newSource, observer);
        } catch (CloneNotSupportedException e) {
            Logger.e(e, "WTF, ImageSource clone fail");
        }

        return null;
    }
}
