package github.tornaco.xposedmoduletest.ui.activity.ag;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;
import android.widget.TextView;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;

import java.util.List;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.provider.LockStorage;
import github.tornaco.xposedmoduletest.ui.activity.NeedLockActivity;

/**
 * Created by guohao4 on 2017/10/21.
 * Email: Tornaco@163.com
 */

public class PatternSetupActivity extends NeedLockActivity implements PatternLockViewListener {

    public static void start(Context context) {
        Intent starter = new Intent(context, PatternSetupActivity.class);
        starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(starter);
    }

    private PatternLockView patternLockView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.verify_displayer_pattern);
        patternLockView = findViewById(R.id.pattern_lock_view);
        if (mUserTheme.isReverseTheme()) {
            ImageView imageView = findViewById(R.id.icon);
            imageView.setColorFilter(ContextCompat.getColor(getContext(),
                    mUserTheme.getThemeColor()), android.graphics.PorterDuff.Mode.MULTIPLY);
        }
        patternLockView.addPatternLockListener(this);
        patternLockView.setEnableHapticFeedback(true);

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
    public void onStarted() {

    }

    @Override
    public void onProgress(List<PatternLockView.Dot> progressPattern) {

    }

    @Override
    public void onComplete(List<PatternLockView.Dot> pattern) {
        LockStorage.setPattern(getApplicationContext(), PatternLockUtils.patternToString(patternLockView, pattern));
        LockStorage.setLockMethod(getActivity(), LockStorage.LockMethod.Pattern);
        finish();
    }

    @Override
    public void onCleared() {

    }
}
