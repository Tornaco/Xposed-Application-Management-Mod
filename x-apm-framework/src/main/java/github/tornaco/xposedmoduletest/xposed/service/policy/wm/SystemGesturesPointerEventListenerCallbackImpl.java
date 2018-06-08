package github.tornaco.xposedmoduletest.xposed.service.policy.wm;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.KeyEvent;
import android.view.WindowManager;

import java.util.Objects;

import github.tornaco.xposedmoduletest.xposed.service.input.KeyEventSender;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;
import lombok.Getter;

/**
 * Created by guohao4 on 2018/1/16.
 * Email: Tornaco@163.com
 */
@Getter
public class SystemGesturesPointerEventListenerCallbackImpl
        implements SystemGesturesPointerEventListener
        .Callbacks {

    private int screenWidth, screenHeight;

    private Context context;

    public SystemGesturesPointerEventListenerCallbackImpl(Context context) {
        this.context = context;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = Objects.requireNonNull(wm).getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        screenWidth = point.x;
        screenHeight = point.y;
    }

    private SwipeAreaX getSwipeAreaX(int x) {
        if (x <= screenWidth / 4) return SwipeAreaX.LEFT;
        if (x >= screenWidth - screenWidth / 4) return SwipeAreaX.RIGHT;
        return SwipeAreaX.CENTER;
    }

    private SwipeAreaY getSwipeAreaY(int y) {
        if (y <= screenHeight / 4) return SwipeAreaY.TOP;
        if (screenHeight - y <= 100) return SwipeAreaY.BOTTOM;
        return SwipeAreaY.CENTER;
    }

    @Override
    public void onSwipeFromTop() {
        XposedLog.boot("onSwipeFromTop");
    }

    @Override
    public void onSwipeFromBottom(int x, int y) {
        SwipeAreaX sax = getSwipeAreaX(x);
        SwipeAreaY say = getSwipeAreaY(y);
        XposedLog.verbose("onSwipeFromBottom: %s-%s %s-%s", x, y, sax, say);

        if (say == SwipeAreaY.BOTTOM) {
            switch (sax) {
                case LEFT:
                    KeyEventSender.injectKey(KeyEvent.KEYCODE_BACK);
                    break;
                case CENTER:
                    KeyEventSender.injectKey(KeyEvent.KEYCODE_HOME);
                    break;
                case RIGHT:
                    KeyEventSender.injectKey(KeyEvent.KEYCODE_APP_SWITCH);
                    break;
            }
        }
    }

    @Override
    public void onSwipeFromRight() {
        XposedLog.boot("onSwipeFromRight");
    }

    @Override
    public void onSwipeFromLeft() {
        XposedLog.boot("onSwipeFromLeft");
    }

    @Override
    public void onFling(int durationMs) {
        XposedLog.boot("onFling");
    }

    @Override
    public void onDown() {
        XposedLog.boot("onDown");
    }

    @Override
    public void onUpOrCancel() {
        XposedLog.boot("onUpOrCancel");
    }

    @Override
    public void onMouseHoverAtTop() {

    }

    @Override
    public void onMouseHoverAtBottom() {

    }

    @Override
    public void onMouseLeaveFromEdge() {

    }

    @Override
    public void onDebug() {

    }
}
