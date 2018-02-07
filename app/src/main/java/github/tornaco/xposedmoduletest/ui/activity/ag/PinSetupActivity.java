package github.tornaco.xposedmoduletest.ui.activity.ag;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;
import android.widget.TextView;

import com.andrognito.pinlockview.IndicatorDots;
import com.andrognito.pinlockview.PinLockListener;
import com.andrognito.pinlockview.PinLockView;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.provider.LockStorage;
import github.tornaco.xposedmoduletest.ui.activity.NeedLockActivity;

/**
 * Created by guohao4 on 2017/10/21.
 * Email: Tornaco@163.com
 */

public class PinSetupActivity extends NeedLockActivity implements PinLockListener {

    public static void start(Context context) {
        Intent starter = new Intent(context, PinSetupActivity.class);
        starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(starter);
    }

    private PinLockView pinLockView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.verify_displayer_pin);
        pinLockView = findViewById(R.id.pin_lock_view);
        IndicatorDots indicatorDots = findViewById(R.id.indicator_dots);
        pinLockView.attachIndicatorDots(indicatorDots);
        if (mUserTheme.isReverseTheme()) {
            ImageView imageView = findViewById(R.id.icon);
            imageView.setColorFilter(ContextCompat.getColor(getContext(),
                    mUserTheme.getThemeColor()), android.graphics.PorterDuff.Mode.MULTIPLY);
        }
        pinLockView.setPinLockListener(this);

        setupLabel(getNewLockLabel());
    }

    private String getNewLockLabel() {
        return getString(R.string.input_new_password);
    }

    private void setupLabel(String label) {
        TextView textView = findViewById(R.id.label);
        textView.setText(label);
    }

    @Override
    protected String getLockLabel() {
        return getString(R.string.input_previous_password);
    }

    @Override
    public void onComplete(String pin) {
        LockStorage.setPin(getApplicationContext(), pin);// FIXME Need encrypt.
        LockStorage.setLockMethod(getActivity(), LockStorage.LockMethod.Pin);
        finish();
    }

    @Override
    public void onEmpty() {

    }

    @Override
    public void onPinChange(int pinLength, String intermediatePin) {

    }
}
