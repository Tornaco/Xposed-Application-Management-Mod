package dev.tornaco.vangogh.request;

import android.os.Build;
import android.support.annotation.NonNull;

import junit.framework.Assert;

import org.newstand.logger.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import dev.tornaco.vangogh.common.Error;
import dev.tornaco.vangogh.display.ImageEffect;
import dev.tornaco.vangogh.loader.LoaderObserver;
import dev.tornaco.vangogh.loader.LoaderObserverAdapter;
import dev.tornaco.vangogh.loader.LoaderProxy;
import dev.tornaco.vangogh.media.Image;
import dev.tornaco.vangogh.media.ImageSource;
import lombok.Synchronized;

/**
 * Created by guohao4 on 2017/8/24.
 * Email: Tornaco@163.com
 */
public class LoadRequestDispatcherTornaco implements RequestDispatcher {

    private LoaderProxy proxy;
    private ExecutorService executorService;

    private final DisplayRequestDispatcher displayRequestDispatcher;

    private final Map<ImageRequest, RequestFuture> REQUESTS = new HashMap<>();

    public LoadRequestDispatcherTornaco(int poolSize) {
        this.proxy = new LoaderProxy();
        this.executorService = Executors.newScheduledThreadPool(poolSize);
        this.displayRequestDispatcher = DisplayRequestDispatcherTornaco.getInstance();
    }

    @Override
    @Synchronized
    public void dispatch(@NonNull final ImageRequest imageRequest) {
        Logger.v("LoadRequestDispatcherTornaco, dispatch: %s", imageRequest);
        Assert.assertNotNull("ImageRequest is null", imageRequest);
        Assert.assertNotNull("Image source is null", imageRequest.getImageSource());

        cancel(imageRequest, true);

        final LoaderObserver observer = imageRequest.getObserver();

        this.executorService.execute(new RequestFuture(imageRequest,
                new LoaderObserverAdapter() {
                    @Override
                    public void onImageFailure(@NonNull Error error) {
                        super.onImageFailure(error);
                        if (observer != null) observer.onImageFailure(error);
                        Logger.v("LoadRequestDispatcherTornaco.LoaderObserverAdapter, onImageFailure: %s", error);
                    }

                    @Override
                    public void onImageLoading(@NonNull ImageSource source) {
                        super.onImageLoading(source);
                        if (observer != null) observer.onImageLoading(source);
                        Logger.v("LoadRequestDispatcherTornaco.LoaderObserverAdapter, onImageLoading: %s", source);
                    }

                    @Override
                    public void onImageReady(@NonNull Image image) {
                        if (observer != null) observer.onImageReady(image);
                        Logger.v("LoadRequestDispatcherTornaco.LoaderObserverAdapter, onImageReadyToDisplay: %s", image);

                        LoadRequestDispatcherTornaco.this.onImageReady(imageRequest, image);
                    }
                }));
    }

    @Override
    public boolean cancel(@NonNull ImageRequest imageRequest, boolean interruptRunning) {
        imageRequest.setDirty(true);

        final RequestFuture future = REQUESTS.remove(imageRequest);
        Logger.i("LoadRequestDispatcherTornaco, cancel future: %s", future);

        if (future == null) return false;

        // FIXME. Too ugly.
        // Hook ID.
        DisplayRequest proxyRequest = new DisplayRequest(null, ImageRequest.builder().id(future.id).build(), null);
        displayRequestDispatcher.cancel(proxyRequest, interruptRunning);

        return future.cancel(interruptRunning);
    }

    @Override
    public void cancelAll(boolean interruptRunning) {
        Logger.i("cancelAll requests");
        synchronized (REQUESTS) {
            for (RequestFuture rf : REQUESTS.values()) {
                rf.cancel(interruptRunning);
            }
            REQUESTS.clear();
        }
    }

    @Override
    public void quit() {
        executorService.shutdownNow();
        synchronized (REQUESTS) {
            REQUESTS.clear();
        }
        displayRequestDispatcher.quit();
    }

    private void onImageReady(ImageRequest request, @NonNull Image image) {
        DisplayRequest displayRequest = new DisplayRequest(image, request, null);
        ImageEffect[] effects = displayRequest.getImageSource().getEffect();
        Image effectedImage = displayRequest.getImage();
        if (effects != null) {
            for (ImageEffect e : effects) {
                effectedImage = e.process(displayRequest.getContext(), effectedImage);
            }
            displayRequest.setImage(effectedImage);
        }
        displayRequestDispatcher.dispatch(displayRequest);
        // Publish used image.
        ImageManager.getInstance().onImageReadyToDisplay(displayRequest.getImageSource(), effectedImage);
    }

    private class RequestFuture extends FutureTask<Image> {

        private int id;

        RequestFuture(final ImageRequest imageRequest, final LoaderObserver observer) {

            super(new Callable<Image>() {
                @Override
                public Image call() throws Exception {

                    Image image = proxy.load(imageRequest, observer);

                    Logger.v("RequestFuture, call exit with no-null? " + String.valueOf(image != null));

                    synchronized (REQUESTS) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            //noinspection Since15
                            REQUESTS.remove(imageRequest, this);
                        } else {
                            REQUESTS.remove(imageRequest);
                        }
                    }
                    return image;
                }
            });

            this.id = imageRequest.getId();

            synchronized (REQUESTS) {
                REQUESTS.put(imageRequest, this);
            }
        }
    }
}
