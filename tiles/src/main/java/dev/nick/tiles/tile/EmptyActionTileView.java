package dev.nick.tiles.tile;

import android.content.Context;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class EmptyActionTileView extends TileView {
    public EmptyActionTileView(Context context) {
        super(context);
    }

    public EmptyActionTileView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onBindActionView(RelativeLayout container) {
        super.onBindActionView(container);
        getTitleTextView().setTextColor(getActionTextColor());
    }

    protected @ColorInt int getActionTextColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return getResources().getColor(android.R.color.holo_blue_light, getContext().getTheme());
        }
        return getResources().getColor(android.R.color.holo_blue_light);
    }
}
