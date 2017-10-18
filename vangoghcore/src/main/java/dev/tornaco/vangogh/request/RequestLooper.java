package dev.tornaco.vangogh.request;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;

import junit.framework.Assert;

import org.newstand.logger.Logger;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import dev.tornaco.vangogh.media.DrawableImage;
import dev.tornaco.vangogh.media.ImageSource;
import lombok.Getter;

/**
 * Created by guohao4 on 2017/8/24.
 * Email: Tornaco@163.com
 */
public class RequestLooper {

    private static final AtomicInteger LOOP_ID = new AtomicInteger(0);

    private static final int MSG_HANDLE_NEW_REQUEST = 0x1;

    private HandlerThread hr;

    private final Handler handler;
    private final Seq seq;
    private final RequestDispatcher dispatcher;

    private AtomicBoolean hasQuit = new AtomicBoolean(false);

    private LinkedList<ImageRequest> pendingQueue;

    private DisplayRequestDispatcher directDirectDisplayDispatcher;

    @Getter
    private LooperState looperState = LooperState.IDLE;

    private RequestLooper(RequestDispatcher dispatcher, Seq seq) {
        this.hr = new HandlerThread("RequestLooper#" + LOOP_ID.getAndIncrement());
        this.hr.start();
        this.looperState = LooperState.LOOP_REQUESTED;
        Looper looper = hr.getLooper();
        this.handler = new Handler(looper) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                RequestLooper.this.handleMessage(msg);
            }
        };
        this.looperState = LooperState.LOOPING;
        this.seq = seq;
        this.dispatcher = dispatcher;
        this.directDirectDisplayDispatcher = DisplayRequestDispatcherTornaco.getInstance();
    }

    public static RequestLooper newInstance(RequestDispatcher dispatcher) {
        return newInstance(dispatcher, Seq.FIFO);
    }

    public static RequestLooper newInstance(RequestDispatcher dispatcher, Seq seq) {
        Assert.assertNotNull("RequestDispatcher is null", dispatcher);
        Assert.assertNotNull("Seq is null", seq);
        return new RequestLooper(dispatcher, seq);
    }

    public void onNewRequest(@NonNull ImageRequest imageRequest) {
        if (hasQuit.get()) return;

        Logger.v("\n\n------NEW REQUEST COMING------\n\n");

        // Apply placeholder.
        final ImageSource source = imageRequest.getImageSource();
        if (source.getPlaceHolder() >= 0) {
            Drawable placeHolderDrawable =
                    source.getPlaceHolder() > 0 ?
                            imageRequest.getContext().getResources()
                                    .getDrawable(source.getPlaceHolder())
                            : null;
            directDirectDisplayDispatcher.dispatch(new DisplayRequest(
                    new DrawableImage(placeHolderDrawable),
                    imageRequest, "no-applier"));
        }

        if (looperState == LooperState.PAUSED) {

            switch (this.seq) {
                case FIFO:
                    this.pendingQueue.addFirst(imageRequest);
                    break;
                case FILO:
                    this.pendingQueue.addLast(imageRequest);
                    break;
            }

            return;
        }

        switch (this.seq) {
            case FIFO:
                this.handler.sendMessage(this.handler.obtainMessage(MSG_HANDLE_NEW_REQUEST, imageRequest));
                break;
            case FILO:
                this.handler.sendMessageAtFrontOfQueue(this.handler.obtainMessage(MSG_HANDLE_NEW_REQUEST, imageRequest));
                break;
        }
    }

    public void pause() {
        if (looperState == LooperState.PAUSE_REQUESTED || looperState == LooperState.PAUSED) {
            return;
        }
        looperState = LooperState.PAUSE_REQUESTED;
        onRequestPause();
    }

    private synchronized void onRequestPause() {
        Logger.v("RequestLooper, onRequestPause");
        if (pendingQueue == null) pendingQueue = new LinkedList<>();
        looperState = LooperState.PAUSED;
    }

    public ImageRequest[] clearPendingRequests() {
        if (pendingQueue == null) return new ImageRequest[0];
        Object[] requests = pendingQueue.toArray();
        pendingQueue.clear();
        ImageRequest[] imageRequests = new ImageRequest[requests.length];
        for (int i = 0; i < requests.length; i++) {
            imageRequests[i] = (ImageRequest) requests[i];
        }
        return imageRequests;
    }

    public void cancelAll(boolean interruptRunning) {
        dispatcher.cancelAll(interruptRunning);
    }

    public void resume() {
        if (looperState == LooperState.PAUSE_REQUESTED || looperState == LooperState.PAUSED) {
            looperState = LooperState.LOOP_REQUESTED;

            Logger.v("RequestLooper, resume");

            ImageRequest[] requests = clearPendingRequests();
            looperState = LooperState.LOOPING;

            for (ImageRequest r : requests) {
                onNewRequest(r);
            }
        }
    }

    public void quit() {
        hasQuit.set(true);
        hr.quit();
        looperState = LooperState.QUIT;
        dispatcher.quit();
    }

    public void quitSafely() {
        hasQuit.set(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            hr.quitSafely();
            looperState = LooperState.QUIT;
        } else {
            quit();
        }
    }

    private void handleMessage(Message message) {
        this.dispatcher.dispatch((ImageRequest) message.obj);
    }

    private enum LooperState {
        IDLE, LOOP_REQUESTED, LOOPING, PAUSE_REQUESTED, PAUSED, QUIT
    }
}
