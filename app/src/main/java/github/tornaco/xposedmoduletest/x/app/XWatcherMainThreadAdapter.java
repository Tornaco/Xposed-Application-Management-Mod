package github.tornaco.xposedmoduletest.x.app;

import android.os.Handler;
import android.os.RemoteException;

/**
 * Created by guohao4 on 2017/10/27.
 * Email: Tornaco@163.com
 */

public class XWatcherMainThreadAdapter extends XWatcherAdapter {
    private Handler handler;

    public XWatcherMainThreadAdapter() {
        handler = new Handler();
    }

    public boolean postDelayed(Runnable r, long delayMillis) {
        return handler.postDelayed(r, delayMillis);
    }

    @Override
    public final void onServiceException(final String trace) throws RemoteException {
        super.onServiceException(trace);
        handler.post(new Runnable() {
            @Override
            public void run() {
                onServiceExceptionMainThread(trace);
            }
        });
    }

    @Override
    public final void onUserLeaving(final String reason) throws RemoteException {
        super.onUserLeaving(reason);
        handler.post(new Runnable() {
            @Override
            public void run() {
                onUserLeavingMainThread(reason);
            }
        });
    }

    protected void onUserLeavingMainThread(String reason) {

    }

    protected void onServiceExceptionMainThread(String trace) {

    }

    @Override
    public String toString() {
        return "XWatcherMainThreadAdapter{" +
                "handler=" + handler +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        XWatcherMainThreadAdapter that = (XWatcherMainThreadAdapter) o;

        return handler.equals(that.handler);
    }

    @Override
    public int hashCode() {
        return handler.hashCode();
    }
}
