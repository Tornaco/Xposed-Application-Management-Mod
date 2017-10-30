package github.tornaco.xposedmoduletest.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.SeekBar;

import com.andrognito.pinlockview.IndicatorDots;
import com.andrognito.pinlockview.PinLockView;
import com.andrognito.pinlockview.ResourceUtils;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.x.XSettings;

/**
 * Created by guohao4 on 2017/10/30.
 * Email: Tornaco@163.com
 */

public class VerifyDisplayerSettingsActivity extends BaseActivity {

    private PinLockView pinLockView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_noter_fullscreen_settings);
        setupPinLockView();
        applyFromSettings();
    }

    private void setupPinLockView() {
        pinLockView = (PinLockView) findViewById(R.id.pin_lock_view);
        IndicatorDots indicatorDots = (IndicatorDots) findViewById(R.id.indicator_dots);
        pinLockView.attachIndicatorDots(indicatorDots);
        pinLockView.setEnabled(false);
    }

    private void applyFromSettings() {
        float defBtnSize = ResourceUtils.getDimensionInPx(this, R.dimen.keypad_btn_size_full);
        float defTextSize = ResourceUtils.getDimensionInPx(this, R.dimen.keypad_text_size_full);
        int defW = getResources().getDimensionPixelSize(R.dimen.keypad_w);
        int defH = getResources().getDimensionPixelSize(R.dimen.keypad_h);

        int btnSize = XSettings.getPinLockBtnSize(this, (int) defBtnSize);
        int textSize = XSettings.getPinLockTextSize(this, (int) defTextSize);
        int w = XSettings.getPinLockTextSize(this, defW);
        int h = XSettings.getPinLockTextSize(this, defH);

        pinLockView.setButtonSize(btnSize);
        pinLockView.setTextSize(textSize);

        SeekBar btnBar = (SeekBar) findViewById(R.id.btn_size_seek_bar);
        btnBar.setProgress(btnSize);
        btnBar.setOnSeekBarChangeListener(new OnSeekBarChangeListenerAdapter() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                XSettings.setPinLockBtnSize(getContext(), progress);
                pinLockView.setButtonSize(progress);
            }

        });

        SeekBar textBar = (SeekBar) findViewById(R.id.text_size_seek_bar);
        textBar.setProgress(textSize);
        textBar.setOnSeekBarChangeListener(new OnSeekBarChangeListenerAdapter() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                XSettings.setPinLockTextSize(getContext(), progress);
                pinLockView.setTextSize(progress);
            }
        });
    }

    private class OnSeekBarChangeListenerAdapter implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }
}
