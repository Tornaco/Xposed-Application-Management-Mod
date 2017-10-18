package dev.tornaco.vangogh;

import android.support.annotation.NonNull;
import android.support.test.runner.AndroidJUnit4;

import junit.framework.AssertionFailedError;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import dev.tornaco.vangogh.request.ImageRequest;
import dev.tornaco.vangogh.request.RequestDispatcher;
import dev.tornaco.vangogh.request.RequestLooper;
import dev.tornaco.vangogh.request.Seq;

/**
 * Created by guohao4 on 2017/8/24.
 * Email: Tornaco@163.com
 */

@RunWith(AndroidJUnit4.class)
public class ImageRequestLooperTest {

    private static final int REQUEST_COUNT = 100;

    @Test(expected = AssertionFailedError.class)
    public void testQuit() {
        RequestLooper requestLooper = RequestLooper.newInstance(new RequestDispatcher() {
            @Override
            public void dispatch(@NonNull ImageRequest request) {
                Assert.fail();
            }

            @Override
            public boolean cancel(@NonNull ImageRequest imageRequest, boolean interruptRunning) {
                return false;
            }

            @Override
            public void cancelAll(boolean interruptRunning) {

            }

            @Override
            public void quit() {

            }
        }, Seq.FIFO);
        requestLooper.quitSafely();
        requestLooper.onNewRequest(ImageRequest.builder().build());
    }

}
