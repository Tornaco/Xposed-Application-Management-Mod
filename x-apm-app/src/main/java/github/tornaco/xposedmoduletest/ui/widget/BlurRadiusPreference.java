package github.tornaco.xposedmoduletest.ui.widget;

import android.content.Context;
import android.preference.SeekBarDialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;

import org.newstand.logger.Logger;

import github.tornaco.xposedmoduletest.xposed.util.XBitmapUtil;
import lombok.Setter;

/**
 * Created by guohao4 on 2017/12/2.
 * Email: Tornaco@163.com
 */

public class BlurRadiusPreference extends SeekBarDialogPreference {

    @Setter
    private int currentRadius = XBitmapUtil.BLUR_RADIUS;

    private int seekProgress;

    @Setter
    private OnSeekCompleteListener onSeekCompleteListener;

    public BlurRadiusPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public BlurRadiusPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public BlurRadiusPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BlurRadiusPreference(Context context) {
        super(context);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        SeekBar seekBar = getSeekBar(view);

        seekBar.setMax(XBitmapUtil.BLUR_RADIUS_MAX);
        seekBar.setProgress(currentRadius);
        seekProgress = currentRadius;

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Logger.d("onProgressChanged: " + progress);
                seekProgress = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        onSeekCompleteListener.onSeekComplete(seekProgress);
        currentRadius = seekProgress;
    }

    public interface OnSeekCompleteListener {
        void onSeekComplete(int progress);
    }
}
