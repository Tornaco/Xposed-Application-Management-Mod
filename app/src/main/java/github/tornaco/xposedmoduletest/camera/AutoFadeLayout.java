package github.tornaco.xposedmoduletest.camera;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class AutoFadeLayout extends LinearLayout {

    public AutoFadeLayout(Context context) {
        super(context);
    }

    public AutoFadeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoFadeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AutoFadeLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void startFading(long delay) {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                setAlpha(0.1f);
            }
        }, delay);
    }

    public void stopFading() {
        setAlpha(1.0f);
    }
}
