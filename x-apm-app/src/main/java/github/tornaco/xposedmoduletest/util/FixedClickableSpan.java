package github.tornaco.xposedmoduletest.util;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import org.newstand.logger.Logger;

/**
 * Created by guohao4 on 2017/9/18.
 * Email: Tornaco@163.com
 */

class FixedClickableSpan extends ClickableSpan {

    private static final String TAG = "FixedClickableSpan:";

    private int normalColor = Integer.MAX_VALUE;
    private int pressedColor = Integer.MAX_VALUE;

    private boolean pressed;

    private OnClickListener<FixedClickableSpan> onClickListener;

    public void setPressed(boolean pressed) {
        this.pressed = pressed;
    }


    public FixedClickableSpan(int normalColor, int pressedColor,
                              OnClickListener<FixedClickableSpan> onClickListener) {
        this.normalColor = normalColor;
        this.pressedColor = pressedColor;
        this.onClickListener = onClickListener;
    }

    /**
     * Makes the text underlined and in the link color.
     */
    @Override
    public void updateDrawState(@NonNull TextPaint ds) {
        super.updateDrawState(ds);
        ds.clearShadowLayer();
        ds.setUnderlineText(false);
        if (normalColor < Integer.MAX_VALUE)
            ds.linkColor = normalColor;
        ds.bgColor = pressed ? (pressedColor < Integer.MAX_VALUE ? pressedColor : ds.bgColor)
                : Color.TRANSPARENT;
    }

    @Override
    public void onClick(View widget) {
        if (this.onClickListener != null) {
            this.onClickListener.onClick(widget, this);
        } else {
            Logger.w(TAG + "listener was null");
        }
    }

    public interface OnClickListener<T extends FixedClickableSpan> {
        void onClick(View widget, T span);
    }

}
