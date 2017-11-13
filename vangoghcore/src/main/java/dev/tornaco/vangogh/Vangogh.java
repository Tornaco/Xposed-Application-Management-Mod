package dev.tornaco.vangogh;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;

import junit.framework.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import dev.tornaco.vangogh.display.ImageApplier;
import dev.tornaco.vangogh.display.ImageDisplayer;
import dev.tornaco.vangogh.display.ImageEffect;
import dev.tornaco.vangogh.display.ImageViewDisplayer;
import dev.tornaco.vangogh.loader.Loader;
import dev.tornaco.vangogh.loader.LoaderObserver;
import dev.tornaco.vangogh.media.Image;
import dev.tornaco.vangogh.media.ImageSource;
import dev.tornaco.vangogh.request.ImageRequest;
import dev.tornaco.vangogh.request.LoadRequestDispatcherTornaco;
import dev.tornaco.vangogh.request.RequestLooper;
import dev.tornaco.vangogh.widget.AbsListViewScrollDetector;
import lombok.Getter;

/**
 * Created by guohao4 on 2017/8/24.
 * Email: Tornaco@163.com
 */

@SuppressWarnings("WeakerAccess")
public class Vangogh {

    private static final Vangogh sMe = new Vangogh();

    @Nullable
    private RequestLooper mLooper;

    private static final Map<View, Object> ABS_LIST_DETECTORS = new HashMap<>();

    public static Vangogh unLinkScrollState(@NonNull View view) {
        Assert.assertNotNull(view);
        ABS_LIST_DETECTORS.remove(view);
        return sMe;
    }

    public static Vangogh linkScrollState(@NonNull RecyclerView recyclerView) {
        Assert.assertNotNull(recyclerView);
        RecyclerView.OnScrollListener listener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE:
                        resume();
                        break;
                    default:
                        pause();
                        break;
                }
            }
        };
        ABS_LIST_DETECTORS.put(recyclerView, listener);
        recyclerView.addOnScrollListener(listener);
        return sMe;
    }

    public static Vangogh linkScrollState(@NonNull AbsListView absListView) {
        Assert.assertNotNull(absListView);

        if (ABS_LIST_DETECTORS.containsKey(absListView)) return sMe;

        AbsListViewScrollDetector absListViewScrollDetector = new AbsListViewScrollDetector();
        absListViewScrollDetector.setListView(absListView);
        absListViewScrollDetector.setScrollThreshold(30);
        absListViewScrollDetector.setCallback(new AbsListViewScrollDetector.Callback() {
            @Override
            public void onScrollUp() {
                pause();
            }

            @Override
            public void onScrollDown() {
                pause();
            }

            @Override
            public void onIdle() {
                resume();
            }
        });
        ABS_LIST_DETECTORS.put(absListView, absListViewScrollDetector);
        return sMe;
    }

    /**
     * @param fragment Instance of your fragment.
     * @return Vangogh single instance.
     */
    public static VangoghRequest with(Fragment fragment) {
        return with((fragment.getActivity().getApplicationContext()));
    }

    /**
     * @param c The application context.
     * @return Vangogh single instance.
     */
    public static VangoghRequest with(Context c) {
        return with(c.getApplicationContext(), VangoghConfig.defaultConfig(c));
    }

    /**
     * @param c      The application context.
     * @param config The {@link VangoghConfig} you want to use for.
     * @return Vangogh single instance.
     */
    public static VangoghRequest with(Context c, VangoghConfig config) {
        Assert.assertNotNull("Context can not be null", c);
        Assert.assertNotNull("VangoghConfig can not be null", config);
        return sMe.createRequest(c, config);
    }

    private synchronized VangoghRequest createRequest(Context c, VangoghConfig config) {
        VangoghConfigManager.getInstance().updateConfig(config);

        if (mLooper == null) {
            mLooper = RequestLooper.newInstance(new LoadRequestDispatcherTornaco(config.getRequestPoolSize()));
        }

        VangoghRequest request = new VangoghRequest();
        request.looper = mLooper;
        request.context = c;
        return request;
    }

    /**
     * Pause the {@link Vangogh}, all new request will be saved as pending.
     */
    public static void pause() {
        if (sMe.mLooper == null) return;
        sMe.mLooper.pause();
    }

    /**
     * Resume the {@link Vangogh}, pending request will be execute.
     */
    public static void resume() {
        if (sMe.mLooper == null) return;
        sMe.mLooper.resume();
    }

    /**
     * Clear all pending requests if exists.
     *
     * @return {@link ImageRequest} array that has been cleared,
     * it will return an empty array if no pending requests found.
     */
    public static ImageRequest[] clearPendingRequests() {
        if (sMe.mLooper == null) return new ImageRequest[0];
        return sMe.mLooper.clearPendingRequests();
    }

    /**
     * Cancel all requests.
     *
     * @param interruptRunning {@code true} if you want to interrupt running tasks.
     */
    public static void cancelAllRequest(boolean interruptRunning) {
        if (sMe.mLooper == null) return;
        sMe.mLooper.cancelAll(interruptRunning);
    }

    /**
     * Quit and clean up.
     */
    public static void quit() {
        if (sMe.mLooper == null) return;
        sMe.mLooper.quit();
    }

    @SuppressWarnings("WeakerAccess")
    @Getter
    public static class VangoghRequest {

        private RequestLooper looper;

        private Context context;

        private ImageSource source;
        private ImageDisplayer imageDisplayer;
        private ImageApplier applier;

        private LoaderObserver observer;

        private Loader<Image> loader;

        private boolean asBitmap, asDrawable;

        public VangoghRequest load(String url) {
            ImageSource source = new ImageSource();
            source.setContext(context);
            source.setUrl(url);
            this.source = source;
            return this;
        }

        public VangoghRequest load(Uri uri) {
            return load(uri.toString());
        }

        public VangoghRequest placeHolder(@DrawableRes int drawableRes) {
            source.setPlaceHolder(drawableRes);
            return this;
        }

        public VangoghRequest fallback(@DrawableRes int fallbackRes) {
            source.setFallback(fallbackRes);
            return this;
        }

        public VangoghRequest applier(ImageApplier animator) {
            this.applier = animator;
            return this;
        }

        public VangoghRequest effect(ImageEffect... effect) {
            Assert.assertNotNull("Set image source first", source);
            this.source.setEffect(effect);
            return this;
        }

        public VangoghRequest skipMemoryCache(boolean skip) {
            source.setSkipMemoryCache(skip);
            return this;
        }

        public VangoghRequest skipDiskCache(boolean skip) {
            source.setSkipDiskCache(skip);
            return this;
        }

        public VangoghRequest observer(LoaderObserver observer) {
            this.observer = observer;
            return this;
        }

        public VangoghRequest usingLoader(@NonNull Loader<Image> loader) {
            Assert.assertNotNull("Loader is null", loader);
            this.loader = loader;
            return this;
        }

        public VangoghRequest asBitmap() {
            this.asBitmap = true;
            this.asDrawable = false;
            return this;
        }

        public VangoghRequest asDrawable() {
            this.asBitmap = false;
            this.asDrawable = true;
            return this;
        }

        public ImageRequest into(@NonNull ImageView imageView) {
            Assert.assertNotNull("imageView is null", imageView);
            return into(new ImageViewDisplayer(imageView, asBitmap));
        }

        public ImageRequest into(@NonNull ImageDisplayer imageDisplayer) {
            this.imageDisplayer = imageDisplayer;

            // Check if we got valid params.
            Assert.assertNotNull(this.source);
            Assert.assertNotNull(this.imageDisplayer);
            Assert.assertNotNull(this.context);

            ImageRequest imageRequest =
                    ImageRequest.builder()
                            .context(context)
                            .requestTimeMills(System.currentTimeMillis())
                            .alias("ir-vango")
                            .displayer(this.imageDisplayer)
                            .imageSource(this.source)
                            .loader(this.loader)
                            .applier(applier)
                            .id(RequestIdFactory.next())
                            .observer(observer)
                            .build();
            looper.onNewRequest(imageRequest);
            return imageRequest;
        }

    }

    private static class RequestIdFactory {
        private static final AtomicInteger S_ID = new AtomicInteger(0);

        static int next() {
            return S_ID.getAndIncrement();
        }
    }
}
